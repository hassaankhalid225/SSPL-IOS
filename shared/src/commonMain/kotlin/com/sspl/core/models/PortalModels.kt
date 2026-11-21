@file:OptIn(kotlinx.serialization.ExperimentalSerializationApi::class)

package com.sspl.core.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Models for Phase 5: My Portal (D1–D9)
 */

@Serializable
data class MembershipPlan(
    val id: Long,
    val name: String,
    val description: String? = null,
    val fees: Double? = null,
    val fee: Double? = null,
    val type: String? = null, // "REGULAR", "LIFE", "STUDENT", etc.
    val duration: String? = null,
    val benefits: List<String>? = null,
    @SerialName("is_active") val isActive: Boolean = true
) {
    val displayFee: Double
        get() = fees ?: fee ?: 0.0
}

@Serializable
data class MembershipPlanListResponse(
    val records: List<MembershipPlan> = emptyList(),
    @SerialName("_meta") val meta: PaginationMeta? = null
)

@Serializable
data class MyMembershipResponse(
    val success: Boolean = true,
    val membership: UserMembershipDetails? = null,
    val message: String? = null
)

@Serializable
data class UserMembershipDetails(
    val id: Long? = null,
    @SerialName("plan_id") val planId: Long? = null,
    val status: String? = null, // "ACTIVE", "PENDING", "EXPIRED", "REJECTED"
    val role: String? = null,
    @SerialName("membership_number") val membershipNumber: String? = null,
    @SerialName("expires_at") val expiresAt: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    val plan: MembershipPlan? = null
)

@Serializable
data class MembershipRegisterRequest(
    @SerialName("plan_id") val planId: Long,
    @kotlinx.serialization.EncodeDefault @SerialName("org_id") val orgId: Long = 1
)

@Serializable
data class UpgradeLifeMembershipRequest(
    @SerialName("plan_id") val planId: Long,
    @kotlinx.serialization.EncodeDefault @SerialName("org_id") val orgId: Long = 1,
    @SerialName("account_phone") val accountPhone: String? = null,
    val profile: Map<String, String>? = null
)

@Serializable
data class AbstractSubmission(
    val id: Long,
    val title: String,
    val authors: String? = null,
    val affiliations: String? = null,
    val status: String = "PENDING", // PENDING, ACCEPTED, REJECTED
    @SerialName("file_url") val fileUrl: String? = null,
    @SerialName("attachment_url") val attachmentUrl: String? = null,
    @SerialName("conference_id") val conferenceId: Long? = null,
    @SerialName("meeting_id") val meetingId: Long? = null,
    @SerialName("event_title") val eventTitle: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("reviewer_comments") val reviewerComments: String? = null
) {
    val bestFileUrl: String?
        get() = fileUrl ?: attachmentUrl
}

@Serializable
data class AbstractSubmissionListResponse(
    val records: List<AbstractSubmission> = emptyList(),
    val meta: PaginationMeta? = null
)

@Serializable
data class Presentation(
    val id: Long,
    @SerialName("abstract_submission_id") val abstractSubmissionId: Long? = null,
    val title: String,
    @SerialName("file_url") val fileUrl: String? = null,
    val status: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    val abstract: AbstractSubmission? = null
)

@Serializable
data class UserCertificate(
    val id: Long? = null,
    val title: String? = null,
    @SerialName("serial_number") val serialNumber: String? = null,
    @SerialName("file_url") val fileUrl: String? = null,
    @SerialName("download_url") val downloadUrl: String? = null,
    @SerialName("certificate_url") val certificateUrl: String? = null,
    @SerialName("issue_date") val issueDate: String? = null,
    @SerialName("event_title") val eventTitle: String? = null,
    @SerialName("conference_id") val conferenceId: Long? = null,
    @SerialName("session_id") val sessionId: Long? = null,
    val type: String? = "CONFERENCE" // CONFERENCE, WORKSHOP, FELLOWSHIP
) {
    val bestUrl: String?
        get() = fileUrl ?: downloadUrl ?: certificateUrl
}

@Serializable
data class CertificatesResponse(
    val success: Boolean = true,
    val certificates: List<UserCertificate> = emptyList(),
    val records: List<UserCertificate> = emptyList()
) {
    val allCertificates: List<UserCertificate>
        get() = if (certificates.isNotEmpty()) certificates else records
}

@Serializable
data class GenerateCertificateRequest(
    @SerialName("sessionId") val sessionId: Long? = null,
    @SerialName("personId") val personId: Long? = null,
    @SerialName("conferenceId") val conferenceId: Long? = null
)

@Serializable
data class GenerateCertificateResponse(
    val success: Boolean = true,
    val message: String? = null,
    val certificate: UserCertificate? = null,
    val url: String? = null,
    val downloadUrl: String? = null
)


/**
 * The signed-in user as needed by the membership application (web `MembershipApplicationPage`).
 * Read from GET /auth/me; [rawProfile] / [rawAccount] keep every saved field so the PUT can merge
 * them exactly like the web (`{ ...existingProfile, ...formFields }`).
 */
data class MembershipApplicant(
    val id: Long,
    val firstName: String,
    val lastName: String,
    val email: String,
    val accountPhone: String,
    val orgId: Long,
    val prefix: String?,
    val gender: String?,
    val cnic: String?,
    val membershipStatus: String?,
    val pendingUpgrade: Boolean,
    val profilePicture: String?,
    val rawProfile: kotlinx.serialization.json.JsonObject,
    val rawAccount: kotlinx.serialization.json.JsonObject?,
    // Pre-fill (web `hydrateMembershipFormFromUser` / `hydrateEducationFromProfile`)
    val title: String,
    val pmdcNumber: String,
    val hospital: String,
    val address: String,
    val whatsapp: String,
    val education: Map<String, String>
)

/** Form values sent by PUT /external/register/{userId}. Profile keys are the web's keys. */
data class MembershipApplicationForm(
    val planId: Long,
    val title: String,
    val pmdcNumber: String,
    val hospital: String,
    val address: String,
    val accountPhone: String,
    val whatsapp: String,
    /** `mbbs_institute`, `mbbs_passing_year`, `fcps_*`, `frcs_*`, `ms_*` (web `buildEducationProfile`). */
    val education: Map<String, String>,
    val isAlreadyMember: Boolean,
    val expiresAt: String? = null
)

/** Parsed response of PUT /external/register/{userId}. */
data class MembershipApplicationResult(
    val success: Boolean,
    val paymentRequired: Boolean,
    val paymentUrl: String?,
    val transactionId: String?,
    val message: String?
)
