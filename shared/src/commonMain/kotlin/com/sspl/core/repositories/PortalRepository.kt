package com.sspl.core.repositories

import com.sspl.core.models.AbstractSubmission
import com.sspl.core.models.GenerateCertificateRequest
import com.sspl.core.models.GenerateCertificateResponse
import com.sspl.core.models.MembershipApplicant
import com.sspl.core.models.MembershipApplicationForm
import com.sspl.core.models.MembershipApplicationResult
import com.sspl.core.models.MembershipPlan
import com.sspl.core.models.Presentation
import com.sspl.core.models.User
import com.sspl.core.models.UserCertificate
import com.sspl.core.models.UserMembershipDetails
import com.sspl.utils.BASE_URL
import io.ktor.client.HttpClient
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.longOrNull
import kotlinx.serialization.json.put

/** Thrown when the backend answers with a non-2xx status. [message] is the server's message when it has one. */
class PortalApiException(message: String, val status: Int = 0) : Exception(message)

/** A file the user picked (abstract attachment / presentation deck). */
class PortalUpload(val bytes: ByteArray, val fileName: String, val mimeType: String)

/** An abstract plus its human submission code (`submission_id`, shown on the web as "ID: #…"). */
data class PortalSubmission(val item: AbstractSubmission, val code: String?) {
    val displayId: String get() = code ?: item.id.toString()
}

/** An event accepting abstracts (dropdown option). */
data class PortalEventOption(val id: Long, val title: String, val type: String?)

/** A presentation plus fields the shared model lacks. */
data class PortalPresentation(val item: Presentation, val adminFeedback: String?, val conferenceId: Long?)

/** One page of a paginated list (`{ records, meta }`). */
data class PortalPage<T>(
    val records: List<T>,
    val totalRecords: Int,
    val totalPages: Int,
    val currentPage: Int
)

/**
 * Membership-relevant user flags that the shared [User] model doesn't carry
 * (`pending_upgrade`, `upgrade_checkout_pending`, `pending_plan`, education fields…). Read from GET /auth/me.
 */
data class MemberSnapshot(
    val user: User?,
    val membershipStatus: String?,
    val role: String?,
    val pendingUpgrade: Boolean,
    val upgradeCheckoutPending: Boolean,
    val pendingPlanName: String?,
    val hasPendingPlan: Boolean,
    val accountPhone: String,
    val email: String?,
    val whatsapp: String,
    val education: Map<String, String>
)

/**
 * A registration from GET /external/events/my-registrations, parsed leniently
 * (the shared Conference model rejects unknown event types such as CONFERENCE / MONTHLY_MEETING).
 */
data class PortalRegistration(
    val id: Long,
    val conferenceId: Long?,
    val title: String,
    val summary: String?,
    val type: String?,
    val startDate: String?,
    val endDate: String?,
    val venue: String?,
    val roleName: String?,
    val feeAmount: Double,
    val paymentStatus: String?,
    val approvalStatus: String?,
    val status: String?,
    val registrationNumber: String?
) {
    /** Same rule as the web event portal list. */
    val isConfirmed: Boolean
        get() = approvalStatus.equals("APPROVED", true) ||
                status.equals("CONFIRMED", true) ||
                ((paymentStatus.equals("SUCCESS", true) || paymentStatus.equals("COMPLETED", true)) &&
                        !approvalStatus.equals("REJECTED", true))

    val needsPayment: Boolean
        get() = feeAmount > 0.0 && !paymentStatus.equals("SUCCESS", true) && !paymentStatus.equals("COMPLETED", true)
}

class PortalRepository(private val httpClient: HttpClient) {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    // ------------------------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------------------------

    private suspend fun HttpResponse.textOrThrow(fallback: String): String {
        val text = bodyAsText()
        if (!status.isSuccess()) throw PortalApiException(extractMessage(text) ?: fallback, status.value)
        return text
    }

    /** Pulls `message` / `error` / first of `errors` out of an error body. */
    private fun extractMessage(text: String): String? = runCatching {
        val o = json.parseToJsonElement(text) as? JsonObject ?: return null
        o.str("message") ?: o.str("error") ?: (o["error"] as? JsonObject)?.str("message")
            ?: (o["errors"] as? JsonArray)?.firstOrNull()?.let { e ->
                (e as? JsonPrimitive)?.contentOrNull ?: (e as? JsonObject)?.let { it.str("message") ?: it.str("msg") }
            }
    }.getOrNull()?.takeIf { it.isNotBlank() }

    private fun JsonObject.str(key: String): String? =
        (this[key] as? JsonPrimitive)?.contentOrNull?.takeIf { it.isNotBlank() && it != "null" }

    private fun JsonObject.long(key: String): Long? =
        (this[key] as? JsonPrimitive)?.let { it.longOrNull ?: it.contentOrNull?.toLongOrNull() }

    private fun JsonObject.dbl(key: String): Double? =
        (this[key] as? JsonPrimitive)?.let { it.doubleOrNull ?: it.contentOrNull?.toDoubleOrNull() }

    private fun JsonObject.bool(key: String): Boolean =
        (this[key] as? JsonPrimitive)?.let { it.booleanOrNull ?: (it.contentOrNull == "1") } ?: false

    private fun JsonObject.obj(key: String): JsonObject? = this[key] as? JsonObject

    /** Finds the list in `[...]`, `{records}`, `{data}`, `{data:{records}}`, or a named key. */
    private fun arrayIn(parsed: JsonElement, vararg extraKeys: String): JsonArray {
        if (parsed is JsonArray) return parsed
        val o = parsed as? JsonObject ?: return JsonArray(emptyList())
        for (k in extraKeys) (o[k] as? JsonArray)?.let { return it }
        (o["records"] as? JsonArray)?.let { return it }
        (o["data"] as? JsonArray)?.let { return it }
        o.obj("data")?.let { d ->
            for (k in extraKeys) (d[k] as? JsonArray)?.let { return it }
            (d["records"] as? JsonArray)?.let { return it }
        }
        return JsonArray(emptyList())
    }

    private fun metaOf(parsed: JsonElement): JsonObject? {
        val o = parsed as? JsonObject ?: return null
        return o.obj("meta") ?: o.obj("_meta") ?: o.obj("data")?.let { it.obj("meta") ?: it.obj("_meta") }
    }

    // ------------------------------------------------------------------------------------
    // Membership
    // ------------------------------------------------------------------------------------

    /** GET /membership/plan?page=1&limit=10 */
    suspend fun getMembershipPlans(): Result<List<MembershipPlan>> = runCatching {
        val text = httpClient.get("$BASE_URL/membership/plan") {
            parameter("page", 1)
            parameter("limit", 10)
        }.textOrThrow("Failed to load membership plans")
        withContext(Dispatchers.Default) {
            arrayIn(json.parseToJsonElement(text)).mapNotNull { el ->
                runCatching { json.decodeFromJsonElement<MembershipPlan>(el) }.getOrNull()
            }
        }
    }

    /** GET /membership/my */
    suspend fun getMyMembership(): Result<UserMembershipDetails?> = runCatching {
        val text = httpClient.get("$BASE_URL/membership/my").textOrThrow("Failed to load membership")
        val parsed = json.parseToJsonElement(text) as? JsonObject ?: return@runCatching null
        val node = parsed.obj("membership") ?: parsed.obj("data") ?: parsed
        runCatching { json.decodeFromJsonElement<UserMembershipDetails>(node) }.getOrNull()
    }

    /** GET /auth/me: fresh user + membership flags the shared model drops. */
    suspend fun getMemberSnapshot(): Result<MemberSnapshot> = runCatching {
        val text = httpClient.get("$BASE_URL/auth/me").textOrThrow("Failed to load your profile")
        withContext(Dispatchers.Default) {
            val root = json.parseToJsonElement(text) as? JsonObject ?: JsonObject(emptyMap())
            val u = root.obj("user") ?: root.obj("data")?.let { it.obj("user") ?: it } ?: root
            val user = runCatching { json.decodeFromJsonElement<User>(u) }.getOrNull()
            val profile = u.obj("profile")
            val account = u.obj("account")
            val pendingPlan = u.obj("pending_plan")
            val eduKeys = listOf(
                "mbbs_institute", "mbbs_passing_year", "fcps_institute", "fcps_passing_year",
                "frcs_institute", "frcs_passing_year", "ms_institute", "ms_passing_year"
            )
            MemberSnapshot(
                user = user,
                membershipStatus = u.str("membership_status"),
                role = u.str("role"),
                pendingUpgrade = u.bool("pending_upgrade") || u.obj("pending_upgrade") != null,
                upgradeCheckoutPending = u.bool("upgrade_checkout_pending") || u.obj("upgrade_checkout_pending") != null,
                pendingPlanName = pendingPlan?.str("name"),
                hasPendingPlan = pendingPlan != null || (u.long("pending_plan") ?: 0L) > 0L,
                accountPhone = account?.str("phone") ?: u.str("phone") ?: "",
                email = account?.str("email") ?: u.str("email"),
                whatsapp = profile?.obj("contact")?.str("whatsapp") ?: "",
                education = eduKeys.associateWith { k -> profile?.str(k) ?: "" }
            )
        }
    }

    /**
     * POST /membership/register { plan_id, org_id } → payment details.
     * Returns the payment URL when the backend sends one (null otherwise).
     */
    suspend fun registerMembership(planId: Long): Result<String?> = runCatching {
        val text = httpClient.post("$BASE_URL/membership/register") {
            contentType(ContentType.Application.Json)
            // Built by hand: the DTO's org_id default is dropped by kotlinx (encodeDefaults = false),
            // which the backend rejects with "plan_id and org_id are required".
            setBody(buildJsonObject {
                put("plan_id", planId)
                put("org_id", 1)
            })
        }.textOrThrow("Membership registration failed")
        paymentUrlOf(text)
    }

    /**
     * POST /membership/upgrade-life — same JSON body as the web:
     * `{ plan_id, org_id, account_phone?, profile?: { mbbs_institute…, contact?: { whatsapp } } }`.
     * Returns the payment URL when present.
     */
    suspend fun upgradeToLifeMembership(
        planId: Long,
        accountPhone: String,
        profileFields: Map<String, String>,
        whatsapp: String?
    ): Result<String?> = runCatching {
        val body = buildJsonObject {
            put("plan_id", planId)
            put("org_id", 1)
            if (accountPhone.isNotBlank()) put("account_phone", accountPhone)
            val clean = profileFields.filterValues { it.isNotBlank() }
            if (clean.isNotEmpty() || !whatsapp.isNullOrBlank()) {
                put("profile", buildJsonObject {
                    clean.forEach { (k, v) -> put(k, v) }
                    if (!whatsapp.isNullOrBlank()) put("contact", buildJsonObject { put("whatsapp", whatsapp) })
                })
            }
        }
        val text = httpClient.post("$BASE_URL/membership/upgrade-life") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }.textOrThrow("Life membership upgrade failed")
        paymentUrlOf(text)
    }

    private fun paymentUrlOf(text: String): String? = runCatching {
        val o = json.parseToJsonElement(text) as? JsonObject ?: return null
        val candidates = listOfNotNull(o, o.obj("payment"), o.obj("data"), o.obj("data")?.obj("payment"))
        candidates.firstNotNullOfOrNull { it.str("paymentUrl") ?: it.str("payment_url") }
    }.getOrNull()

    /**
     * GET /auth/me → everything the membership application needs (web `getUserProfile` +
     * `hydrateMembershipFormFromUser` / `hydrateEducationFromProfile`).
     */
    suspend fun getMembershipApplicant(): Result<MembershipApplicant> = runCatching {
        val text = httpClient.get("$BASE_URL/auth/me").textOrThrow("Failed to load your profile")
        withContext(Dispatchers.Default) {
            val root = json.parseToJsonElement(text) as? JsonObject ?: JsonObject(emptyMap())
            val u = root.obj("user") ?: root.obj("data")?.let { it.obj("user") ?: it } ?: root
            val id = u.long("id") ?: throw PortalApiException("User ID not found. Please login again.")
            val profile = u.obj("profile") ?: JsonObject(emptyMap())
            val account = u.obj("account")
            val eduKeys = listOf(
                "mbbs_institute", "mbbs_passing_year", "fcps_institute", "fcps_passing_year",
                "frcs_institute", "frcs_passing_year", "ms_institute", "ms_passing_year"
            )
            MembershipApplicant(
                id = id,
                firstName = u.str("first_name").orEmpty(),
                lastName = u.str("last_name").orEmpty(),
                email = account?.str("email") ?: u.str("email").orEmpty(),
                accountPhone = account?.str("phone") ?: u.str("phone").orEmpty(),
                orgId = u.long("org_id")?.takeIf { it > 0 } ?: 1L,
                prefix = u.str("prefix"),
                gender = u.str("gender"),
                cnic = u.str("cnic"),
                membershipStatus = u.str("membership_status"),
                pendingUpgrade = u.bool("pending_upgrade") || u.obj("pending_upgrade") != null,
                profilePicture = u.str("profile_picture"),
                rawProfile = profile,
                rawAccount = account,
                title = profile.str("title") ?: "Dr",
                pmdcNumber = profile.str("pmdc_number").orEmpty(),
                hospital = profile.str("hospital") ?: profile.str("institute").orEmpty(),
                address = profile.str("address").orEmpty(),
                whatsapp = profile.obj("contact")?.str("whatsapp").orEmpty(),
                education = eduKeys.associateWith { k -> profile.str(k).orEmpty() }
            )
        }
    }

    /**
     * PUT /external/register/{userId} — web `applyForMembership`.
     * JSON body normally; multipart (`profile[...]`, `account[...]`, `profile_picture`) when a new
     * photo is attached. Built by hand because the shared Json has encodeDefaults = false.
     */
    suspend fun applyForMembership(
        applicant: MembershipApplicant,
        form: MembershipApplicationForm,
        photo: PortalUpload?
    ): Result<MembershipApplicationResult> = runCatching {
        // web cleanObject: drop null / undefined / "" values from the saved profile.
        val existingProfile = applicant.rawProfile.filterValues { v ->
            v !is JsonNull && !(v is JsonPrimitive && v.isString && v.content.isEmpty())
        }
        val newFields = buildMap {
            put("title", form.title)
            put("pmdc_number", form.pmdcNumber.trim())
            put("hospital", form.hospital.trim())
            put("address", form.address.trim())
            putAll(form.education)
        }.filterValues { it.isNotBlank() }
        val whatsapp = form.whatsapp.trim().takeIf { it.isNotEmpty() }
        val phone = form.accountPhone.trim().ifBlank { applicant.accountPhone }
        val url = "$BASE_URL/external/register/${applicant.id}"

        val response = if (photo != null) {
            httpClient.put(url) {
                setBody(MultiPartFormDataContent(formData {
                    append("first_name", applicant.firstName)
                    append("last_name", applicant.lastName)
                    append("account[email]", applicant.email)
                    append("account[phone]", phone)
                    append("plan_id", form.planId.toString())
                    append("org_id", applicant.orgId.toString())
                    form.expiresAt?.takeIf { it.isNotBlank() }?.let { append("expires_at", it) }
                    applicant.prefix?.let { append("prefix", it) }
                    applicant.gender?.let { append("gender", it) }
                    applicant.cnic?.let { append("cnic", it) }
                    if (form.isAlreadyMember) append("is_already_member", "true")

                    val existingContact = existingProfile["contact"] as? JsonObject
                    if (whatsapp != null || existingContact != null) {
                        val contact = linkedMapOf<String, String>()
                        if (applicant.email.isNotBlank()) contact["email"] = applicant.email
                        if (applicant.accountPhone.isNotBlank()) contact["phone"] = applicant.accountPhone
                        existingContact?.forEach { (k, v) ->
                            (v as? JsonPrimitive)?.contentOrNull?.takeIf { it.isNotBlank() }?.let { contact[k] = it }
                        }
                        if (whatsapp != null) contact["whatsapp"] = whatsapp
                        contact.forEach { (k, v) -> append("profile[contact][$k]", v) }
                    }
                    // Merged profile scalars (nested objects such as `contact` are handled above).
                    val merged = linkedMapOf<String, String>()
                    existingProfile.forEach { (k, v) ->
                        if (k != "contact" && v is JsonPrimitive) merged[k] = v.content
                    }
                    merged.putAll(newFields)
                    merged.forEach { (k, v) -> append("profile[$k]", v) }

                    append("profile_picture", photo.bytes, fileHeaders(photo))
                }))
            }
        } else {
            val body = buildJsonObject {
                put("first_name", applicant.firstName)
                put("last_name", applicant.lastName)
                put("account", buildJsonObject {
                    val acc = applicant.rawAccount
                    if (acc != null) {
                        acc.forEach { (k, v) -> if (k != "password" && k != "phone") put(k, v) }
                    } else {
                        put("email", applicant.email)
                    }
                    put("phone", phone)
                })
                put("plan_id", form.planId)
                put("org_id", applicant.orgId)
                form.expiresAt?.takeIf { it.isNotBlank() }?.let { put("expires_at", it) }
                applicant.prefix?.let { put("prefix", it) }
                applicant.gender?.let { put("gender", it) }
                applicant.cnic?.let { put("cnic", it) }
                put("is_already_member", form.isAlreadyMember)
                put("profile", buildJsonObject {
                    existingProfile.forEach { (k, v) -> put(k, v) }
                    newFields.forEach { (k, v) -> put(k, v) }
                    if (whatsapp != null) {
                        put("contact", buildJsonObject {
                            if (applicant.email.isNotBlank()) put("email", applicant.email)
                            if (applicant.accountPhone.isNotBlank()) put("phone", applicant.accountPhone)
                            put("whatsapp", whatsapp)
                        })
                    }
                })
            }
            httpClient.put(url) {
                contentType(ContentType.Application.Json)
                setBody(body)
            }
        }
        val text = response.textOrThrow("Membership application failed")
        val o = runCatching { json.parseToJsonElement(text) as? JsonObject }.getOrNull() ?: JsonObject(emptyMap())
        val payment = o.obj("payment") ?: o.obj("data")?.obj("payment")
        MembershipApplicationResult(
            success = (o["success"] as? JsonPrimitive)?.booleanOrNull ?: true,
            paymentRequired = payment?.bool("required") ?: false,
            paymentUrl = payment?.let { it.str("paymentUrl") ?: it.str("payment_url") },
            transactionId = payment?.let { it.str("transactionId") ?: it.str("transaction_id") },
            message = o.str("message")
        )
    }

    // ------------------------------------------------------------------------------------
    // Abstracts
    // ------------------------------------------------------------------------------------

    /** GET /abstractSubmission/?page&limit&status */
    suspend fun getMySubmissions(
        status: String? = null,
        page: Int = 1,
        limit: Int = 20
    ): Result<PortalPage<PortalSubmission>> = runCatching {
        val text = httpClient.get("$BASE_URL/abstractSubmission/") {
            parameter("page", page)
            parameter("limit", limit)
            if (!status.isNullOrBlank() && status != "ALL") parameter("status", status)
        }.textOrThrow("Failed to load submissions")
        withContext(Dispatchers.Default) {
            val parsed = json.parseToJsonElement(text)
            val records = arrayIn(parsed).mapNotNull { el -> (el as? JsonObject)?.let(::toSubmission) }
            val meta = metaOf(parsed)
            PortalPage(
                records = records,
                totalRecords = meta?.long("total_records")?.toInt() ?: meta?.long("total")?.toInt() ?: records.size,
                totalPages = meta?.long("total_pages")?.toInt()?.coerceAtLeast(1) ?: 1,
                currentPage = meta?.long("current_page")?.toInt() ?: page
            )
        }
    }

    /** Normalises snake/camel keys (`reviewer_feedback`, `fileUrl`, `submission_id`…). */
    private fun toSubmission(o: JsonObject): PortalSubmission? {
        val base = runCatching { json.decodeFromJsonElement<AbstractSubmission>(o) }.getOrNull() ?: return null
        val event = o.obj("conference") ?: o.obj("meeting") ?: o.obj("event")
        val norm = base.copy(
            fileUrl = base.fileUrl ?: o.str("fileUrl"),
            createdAt = base.createdAt ?: o.str("createdAt"),
            reviewerComments = base.reviewerComments ?: o.str("reviewer_feedback") ?: o.str("reviewerFeedback"),
            eventTitle = base.eventTitle ?: event?.str("title"),
            status = base.status.ifBlank { "PENDING" }
        )
        return PortalSubmission(item = norm, code = o.str("submission_id"))
    }

    /**
     * POST /abstractSubmission/create (multipart: title, authors, affiliations, attachment, conferenceId).
     * Returns the server message.
     */
    suspend fun submitAbstract(
        title: String,
        authors: String,
        affiliations: String,
        conferenceId: Long?,
        meetingId: Long?,
        attachment: PortalUpload
    ): Result<String> = runCatching {
        val text = httpClient.post("$BASE_URL/abstractSubmission/create") {
            setBody(MultiPartFormDataContent(formData {
                append("title", title)
                append("authors", authors)
                append("affiliations", affiliations)
                if (conferenceId != null) append("conferenceId", conferenceId.toString())
                if (meetingId != null) append("meetingId", meetingId.toString())
                append("attachment", attachment.bytes, fileHeaders(attachment))
            }))
        }.textOrThrow("Failed to submit abstract")
        messageOf(text) ?: "Abstract submitted successfully!"
    }

    private fun fileHeaders(f: PortalUpload) = Headers.build {
        append(HttpHeaders.ContentType, f.mimeType.ifBlank { "application/octet-stream" })
        append(HttpHeaders.ContentDisposition, "filename=\"${f.fileName.replace("\"", "")}\"")
    }

    private fun messageOf(text: String): String? = runCatching {
        (json.parseToJsonElement(text) as? JsonObject)?.str("message")
    }.getOrNull()

    /**
     * GET /external/events?abstract_eligible=true&active=true — only id/title/type are kept
     * (the payload embeds multi-MB base64 images we never show here).
     */
    suspend fun getAbstractEligibleEvents(): Result<List<PortalEventOption>> = runCatching {
        val text = httpClient.get("$BASE_URL/external/events") {
            parameter("abstract_eligible", "true")
            parameter("active", "true")
        }.textOrThrow("Failed to load events")
        withContext(Dispatchers.Default) {
            arrayIn(json.parseToJsonElement(text), "events").mapNotNull { el ->
                val o = el as? JsonObject ?: return@mapNotNull null
                val id = o.long("id") ?: return@mapNotNull null
                PortalEventOption(id = id, title = o.str("title") ?: "Event #$id", type = o.str("type"))
            }
        }
    }

    // ------------------------------------------------------------------------------------
    // Presentations
    // ------------------------------------------------------------------------------------

    /** GET /presentations/my */
    suspend fun getMyPresentations(): Result<List<PortalPresentation>> = runCatching {
        val text = httpClient.get("$BASE_URL/presentations/my").textOrThrow("Failed to load presentations")
        withContext(Dispatchers.Default) {
            arrayIn(json.parseToJsonElement(text)).mapNotNull { el ->
                val o = el as? JsonObject ?: return@mapNotNull null
                val p = runCatching { json.decodeFromJsonElement<Presentation>(o) }.getOrNull() ?: return@mapNotNull null
                PortalPresentation(
                    item = p.copy(
                        fileUrl = p.fileUrl ?: o.str("fileUrl") ?: o.str("file_path"),
                        createdAt = p.createdAt ?: o.str("createdAt"),
                        abstractSubmissionId = p.abstractSubmissionId ?: o.long("abstractSubmissionId")
                    ),
                    adminFeedback = o.str("admin_feedback") ?: o.str("adminFeedback"),
                    conferenceId = o.long("conference_id") ?: o.long("conferenceId")
                )
            }
        }
    }

    /** POST /presentations/upload (multipart: abstract_submission_id, title, file). */
    suspend fun uploadPresentation(abstractSubmissionId: Long, title: String, file: PortalUpload): Result<String> = runCatching {
        val text = httpClient.post("$BASE_URL/presentations/upload") {
            setBody(MultiPartFormDataContent(formData {
                append("abstract_submission_id", abstractSubmissionId.toString())
                append("title", title)
                append("file", file.bytes, fileHeaders(file))
            }))
        }.textOrThrow("Failed to upload presentation")
        messageOf(text) ?: "Presentation uploaded successfully!"
    }

    // ------------------------------------------------------------------------------------
    // Certificates
    // ------------------------------------------------------------------------------------

    /** GET /certificates/my[?conferenceId] — normalises camelCase (`serialNumber`, `fileUrl`, `template.name`). */
    suspend fun getMyCertificates(conferenceId: Long? = null): Result<List<UserCertificate>> = runCatching {
        val text = httpClient.get("$BASE_URL/certificates/my") {
            if (conferenceId != null) parameter("conferenceId", conferenceId)
        }.textOrThrow("Failed to load certificates")
        withContext(Dispatchers.Default) {
            arrayIn(json.parseToJsonElement(text), "certificates").mapNotNull { el ->
                val o = el as? JsonObject ?: return@mapNotNull null
                val c = runCatching { json.decodeFromJsonElement<UserCertificate>(o) }.getOrNull() ?: UserCertificate()
                val conf = o.obj("conference") ?: o.obj("event")
                val session = o.obj("session")
                c.copy(
                    id = c.id ?: o.long("id"),
                    title = c.title ?: o.obj("template")?.str("name") ?: o.str("name"),
                    serialNumber = c.serialNumber ?: o.str("serialNumber") ?: o.str("serial"),
                    fileUrl = (c.fileUrl ?: o.str("fileUrl"))?.replace('\\', '/'),
                    downloadUrl = c.downloadUrl ?: o.str("downloadUrl"),
                    issueDate = c.issueDate ?: o.str("issuedAt") ?: o.str("issued_at") ?: o.str("createdAt") ?: o.str("created_at"),
                    eventTitle = c.eventTitle ?: conf?.str("title") ?: session?.str("title"),
                    conferenceId = c.conferenceId ?: o.long("conferenceId") ?: conf?.long("id"),
                    sessionId = c.sessionId ?: o.long("sessionId") ?: session?.long("id"),
                    type = o.str("type") ?: conf?.str("type") ?: c.type
                )
            }
        }
    }

    /** POST /conferences/certificate/generate */
    suspend fun generateCertificate(sessionId: Long?, personId: Long?, conferenceId: Long?): Result<GenerateCertificateResponse> =
        runCatching {
            val text = httpClient.post("$BASE_URL/conferences/certificate/generate") {
                contentType(ContentType.Application.Json)
                setBody(GenerateCertificateRequest(sessionId, personId, conferenceId))
            }.textOrThrow("Failed to generate certificate")
            // Web CertificateDownload reads `certificate.fileUrl || url || downloadUrl` (camelCase);
            // read both spellings so the file URL is never lost.
            val o = json.parseToJsonElement(text) as? JsonObject ?: JsonObject(emptyMap())
            val cert = o.obj("certificate")
            GenerateCertificateResponse(
                success = (o["success"] as? JsonPrimitive)?.booleanOrNull ?: true,
                message = o.str("message"),
                certificate = cert?.let {
                    UserCertificate(
                        id = it.long("id"),
                        serialNumber = it.str("serialNumber") ?: it.str("serial_number"),
                        fileUrl = it.str("fileUrl") ?: it.str("file_url"),
                        downloadUrl = it.str("downloadUrl") ?: it.str("download_url")
                    )
                },
                url = o.str("url"),
                downloadUrl = o.str("downloadUrl")
            )
        }

    // ------------------------------------------------------------------------------------
    // Event registrations (portal view)
    // ------------------------------------------------------------------------------------

    /** GET /external/events/my-registrations — merged like the web (`all` or the four buckets). */
    suspend fun getMyRegistrations(): Result<List<PortalRegistration>> = runCatching {
        val text = httpClient.get("$BASE_URL/external/events/my-registrations")
            .textOrThrow("Failed to load your registered events")
        withContext(Dispatchers.Default) {
            val root = json.parseToJsonElement(text) as? JsonObject ?: JsonObject(emptyMap())
            val data = root.obj("data") ?: root
            val all = (data["all"] as? JsonArray).orEmpty()
            val merged = all.ifEmpty {
                listOf("conferences", "workshops", "monthly_meetings", "conference_events")
                    .flatMap { (data[it] as? JsonArray).orEmpty() }
            }
            merged.mapNotNull { (it as? JsonObject)?.let(::toRegistration) }.distinctBy { it.id }
        }
    }

    private fun toRegistration(o: JsonObject): PortalRegistration? {
        val id = o.long("id") ?: return null
        val c = o.obj("conference") ?: o.obj("event")
        val venue = c?.obj("venue")
        return PortalRegistration(
            id = id,
            conferenceId = o.long("conference_id") ?: c?.long("id"),
            title = c?.str("title") ?: "Event",
            summary = c?.str("summary"),
            type = c?.str("type"),
            startDate = c?.str("start_date") ?: c?.str("eventStart"),
            endDate = c?.str("end_date") ?: c?.str("eventEnd"),
            venue = venue?.let { it.str("title") ?: it.str("name") },
            roleName = o.obj("role")?.str("name"),
            feeAmount = o.dbl("fee_amount") ?: 0.0,
            paymentStatus = o.str("payment_status"),
            approvalStatus = o.str("approval_status"),
            status = o.str("status"),
            registrationNumber = o.str("registration_number")
        )
    }
}
