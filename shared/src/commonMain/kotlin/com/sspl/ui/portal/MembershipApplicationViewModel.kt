package com.sspl.ui.portal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sspl.core.models.MembershipApplicant
import com.sspl.core.models.MembershipApplicationForm
import com.sspl.core.models.MembershipPlan
import com.sspl.core.repositories.PortalRepository
import com.sspl.core.repositories.PortalUpload
import com.sspl.designsystem.components.LoadState
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Field keys = the web form's error keys. */
object AppField {
    const val PMDC = "pmdc_number"
    const val HOSPITAL = "hospital"
    const val ADDRESS = "address"
    const val PHONE = "account_phone"
    const val WHATSAPP = "whatsapp"
    const val BASIC_INST = "basic_institute"
    const val BASIC_YEAR = "basic_passing_year"
    const val ADV_INST = "adv_institute"
    const val ADV_YEAR = "adv_passing_year"
    const val PHOTO = "profile_image"
}

/** One qualification row (web `basicQual` / `advQual` / `otherQuals[]`). */
data class Qualification(
    val id: Long,
    val degree: String,
    val institute: String = "",
    val passingYear: String = ""
) {
    /** Web `isQualificationComplete`. */
    val isComplete: Boolean get() = degree.isNotBlank() && institute.isNotBlank() && passingYear.isNotBlank()
}

/** Postgraduate degrees accepted for the "Advance qualification" select (web order). */
val ADVANCE_DEGREES = listOf("FCPS", "MS", "FRCS")
val TITLE_OPTIONS = listOf("Dr", "Prof")

/** The data a loaded form needs: the selected plan and the signed-in applicant. */
data class ApplicationContext(val plan: MembershipPlan, val applicant: MembershipApplicant)

sealed interface ApplicationOutcome {
    /** Free plan or already-member: submitted, awaiting approval. */
    data object Submitted : ApplicationOutcome
    /** Payment completed in the in-app checkout. */
    data object Paid : ApplicationOutcome
}

data class MembershipApplicationUiState(
    val content: LoadState<ApplicationContext> = LoadState.Loading,
    /** No plan id was passed — the user must pick a plan first (web redirects to /membership-plans). */
    val missingPlan: Boolean = false,
    /** Web: ACTIVE member without a pending upgrade is sent back to the membership page. */
    val alreadyActive: Boolean = false,
    val isAlreadyMember: Boolean = false,
    val profilePrefilled: Boolean = false,
    val title: String = "Dr",
    val pmdcNumber: String = "",
    val hospital: String = "",
    val address: String = "",
    val accountPhone: String = "",
    val whatsapp: String = "",
    val basic: Qualification = Qualification(0, "MBBS"),
    val advance: Qualification = Qualification(1, "FCPS"),
    val others: List<Qualification> = emptyList(),
    val existingPhotoUrl: String? = null,
    val newPhoto: ByteArray? = null,
    val errors: Map<String, String> = emptyMap(),
    /** Bumped on every failed validation so the screen can scroll to the first error once. */
    val validationAttempt: Int = 0,
    val formError: String? = null,
    val isSubmitting: Boolean = false,
    val paymentUrl: String? = null,
    val outcome: ApplicationOutcome? = null
) {
    /** Web `canAddMore`: only when the last "other" row is complete. */
    val canAddOther: Boolean get() = others.isEmpty() || others.last().isComplete
    val hasPhoto: Boolean get() = newPhoto != null || !existingPhotoUrl.isNullOrBlank()
}

/** Port of web `sanitizeYearInput`. */
fun sanitizeYear(value: String): String = value.filter { it.isDigit() }.take(4)

/** Port of web `buildEducationProfile` (MBBS + FCPS/FRCS/MS keys). */
fun buildEducationProfile(basic: Qualification, advance: Qualification, others: List<Qualification>): Map<String, String> {
    val profile = linkedMapOf(
        "mbbs_institute" to basic.institute.trim(),
        "mbbs_passing_year" to basic.passingYear.trim()
    )
    fun apply(q: Qualification) {
        val key = when (q.degree.trim().uppercase()) {
            "FCPS" -> "fcps"
            "FRCS" -> "frcs"
            "MS" -> "ms"
            else -> return
        }
        if (q.institute.isBlank() || q.passingYear.isBlank()) return
        profile["${key}_institute"] = q.institute.trim()
        profile["${key}_passing_year"] = q.passingYear.trim()
    }
    apply(advance)
    others.filter { it.isComplete }.forEach(::apply)
    return profile
}

/** Port of web `hasRequiredPostgraduate`. */
fun hasRequiredPostgraduate(profile: Map<String, String>): Boolean =
    listOf("fcps", "frcs", "ms").any { !profile["${it}_institute"].isNullOrBlank() && !profile["${it}_passing_year"].isNullOrBlank() }

/**
 * Membership application (web `/membership-application`): selected plan → professional profile form →
 * PUT /external/register/{userId} → in-app payment, or straight to "pending approval" for existing members.
 */
class MembershipApplicationViewModel(
    private val repository: PortalRepository,
    private val planId: Long?,
    isAlreadyMember: Boolean
) : ViewModel() {

    private val _state = MutableStateFlow(
        MembershipApplicationUiState(isAlreadyMember = isAlreadyMember, missingPlan = planId == null)
    )
    val state: StateFlow<MembershipApplicationUiState> = _state.asStateFlow()
    private var nextId = 10L

    init { load() }

    fun retry() = load()

    private fun load() {
        val id = planId ?: return
        _state.update { it.copy(content = LoadState.Loading) }
        viewModelScope.launch {
            val plansCall = async { repository.getMembershipPlans() }
            val meCall = async { repository.getMembershipApplicant() }
            val plans = plansCall.await()
            val me = meCall.await()
            val failure = plans.exceptionOrNull() ?: me.exceptionOrNull()
            if (failure != null) {
                _state.update { it.copy(content = LoadState.Error(failure.message ?: "We couldn't load your application. Please try again.")) }
                return@launch
            }
            val plan = plans.getOrNull().orEmpty().firstOrNull { it.id == id }
            if (plan == null) {
                _state.update { it.copy(content = LoadState.Error("This membership plan is no longer available. Please choose another plan.")) }
                return@launch
            }
            val applicant = me.getOrThrow()
            hydrate(applicant)
            _state.update {
                it.copy(
                    content = LoadState.Ready(ApplicationContext(plan, applicant)),
                    alreadyActive = applicant.membershipStatus.equals("ACTIVE", true) && !applicant.pendingUpgrade
                )
            }
        }
    }

    /** Web `applyUserToMainForm` + `hydrateEducationFromProfile`. */
    private fun hydrate(a: MembershipApplicant) {
        val edu = a.education
        val postgrad = listOf("FCPS" to "fcps", "FRCS" to "frcs", "MS" to "ms")
            .filter { (_, k) -> edu["${k}_institute"].orEmpty().isNotBlank() && edu["${k}_passing_year"].orEmpty().isNotBlank() }
            .map { (deg, k) -> Qualification(nextId++, deg, edu["${k}_institute"].orEmpty(), edu["${k}_passing_year"].orEmpty()) }
        val basic = Qualification(0, "MBBS", edu["mbbs_institute"].orEmpty(), edu["mbbs_passing_year"].orEmpty())
        val photo = a.profilePicture?.takeIf { it.isNotBlank() }
        _state.update {
            it.copy(
                title = a.title.takeIf { t -> t in TITLE_OPTIONS } ?: "Dr",
                pmdcNumber = a.pmdcNumber,
                hospital = a.hospital,
                address = a.address,
                accountPhone = a.accountPhone,
                whatsapp = a.whatsapp,
                basic = basic,
                advance = postgrad.firstOrNull()?.copy(id = 1) ?: Qualification(1, "FCPS"),
                others = postgrad.drop(1),
                existingPhotoUrl = photo,
                profilePrefilled = photo != null || a.pmdcNumber.isNotBlank() || a.hospital.isNotBlank() ||
                        a.address.isNotBlank() || basic.institute.isNotBlank()
            )
        }
    }

    private fun edit(clear: List<String>, change: (MembershipApplicationUiState) -> MembershipApplicationUiState) =
        _state.update { s -> change(s).copy(errors = s.errors - clear.toSet(), formError = null) }

    fun setTitle(v: String) = edit(emptyList()) { it.copy(title = v) }
    fun setPmdc(v: String) = edit(listOf(AppField.PMDC)) { it.copy(pmdcNumber = v) }
    fun setHospital(v: String) = edit(listOf(AppField.HOSPITAL)) { it.copy(hospital = v) }
    fun setAddress(v: String) = edit(listOf(AppField.ADDRESS)) { it.copy(address = v) }
    fun setPhone(v: String) = edit(listOf(AppField.PHONE)) { it.copy(accountPhone = v) }
    fun setWhatsapp(v: String) = edit(listOf(AppField.WHATSAPP)) { it.copy(whatsapp = v) }
    fun setBasicInstitute(v: String) = edit(listOf(AppField.BASIC_INST)) { it.copy(basic = it.basic.copy(institute = v)) }
    fun setBasicYear(v: String) = edit(listOf(AppField.BASIC_YEAR)) { it.copy(basic = it.basic.copy(passingYear = sanitizeYear(v))) }
    fun setAdvanceDegree(v: String) = edit(listOf(AppField.ADV_INST, AppField.ADV_YEAR)) { it.copy(advance = it.advance.copy(degree = v)) }
    fun setAdvanceInstitute(v: String) = edit(listOf(AppField.ADV_INST)) { it.copy(advance = it.advance.copy(institute = v)) }
    fun setAdvanceYear(v: String) = edit(listOf(AppField.ADV_YEAR)) { it.copy(advance = it.advance.copy(passingYear = sanitizeYear(v))) }

    fun addOther() {
        if (!_state.value.canAddOther) return
        _state.update { it.copy(others = it.others + Qualification(nextId++, "MD")) }
    }

    fun removeOther(id: Long) = _state.update { it.copy(others = it.others.filterNot { q -> q.id == id }) }

    fun updateOther(id: Long, degree: String? = null, institute: String? = null, year: String? = null) =
        _state.update { s ->
            s.copy(others = s.others.map { q ->
                if (q.id != id) q else q.copy(
                    degree = degree ?: q.degree,
                    institute = institute ?: q.institute,
                    passingYear = year?.let(::sanitizeYear) ?: q.passingYear
                )
            })
        }

    fun onPhotoPicked(bytes: ByteArray) = edit(listOf(AppField.PHOTO)) { it.copy(newPhoto = bytes) }

    /** Port of web `validateForm` (same rules, same messages). */
    private fun validate(s: MembershipApplicationUiState): Map<String, String> {
        val e = linkedMapOf<String, String>()
        if (s.pmdcNumber.isBlank()) e[AppField.PMDC] = "PMDC number is required"
        if (s.hospital.isBlank()) e[AppField.HOSPITAL] = "Hospital / Clinic name is required"
        if (s.address.isBlank()) e[AppField.ADDRESS] = "Address is required"
        if (s.accountPhone.isBlank()) e[AppField.PHONE] = "Mobile number is required for payment"
        else if (!isValidPhone(s.accountPhone)) e[AppField.PHONE] = "Invalid mobile number (e.g. 03001234567)"
        if (s.whatsapp.isBlank()) e[AppField.WHATSAPP] = "WhatsApp number is required"
        else if (!isValidPhone(s.whatsapp)) e[AppField.WHATSAPP] = "Invalid WhatsApp number"
        if (s.basic.institute.isBlank()) e[AppField.BASIC_INST] = "Institution is required"
        validateYear(s.basic.passingYear)?.let { e[AppField.BASIC_YEAR] = it }
        // web validateAdvanceQualification
        if (s.advance.degree in ADVANCE_DEGREES) {
            if (s.advance.institute.isBlank()) e[AppField.ADV_INST] = "Institution is required"
            validateYear(s.advance.passingYear)?.let { e[AppField.ADV_YEAR] = it }
        }
        val edu = buildEducationProfile(s.basic, s.advance, s.others)
        if (!hasRequiredPostgraduate(edu) && AppField.ADV_INST !in e && AppField.ADV_YEAR !in e) {
            e[AppField.ADV_INST] = POSTGRAD_REQUIRED_MESSAGE
        }
        if (!s.hasPhoto) e[AppField.PHOTO] = "Profile picture is required"
        return e
    }

    fun submit() {
        val s = _state.value
        if (s.isSubmitting) return
        val ctx = (s.content as? LoadState.Ready)?.data ?: return
        val errors = validate(s)
        if (errors.isNotEmpty()) {
            _state.update { it.copy(errors = errors, formError = null, validationAttempt = it.validationAttempt + 1) }
            return
        }
        _state.update { it.copy(isSubmitting = true, errors = emptyMap(), formError = null) }
        val form = MembershipApplicationForm(
            planId = ctx.plan.id,
            title = s.title,
            pmdcNumber = s.pmdcNumber,
            hospital = s.hospital,
            address = s.address,
            accountPhone = s.accountPhone,
            whatsapp = s.whatsapp,
            education = buildEducationProfile(s.basic, s.advance, s.others),
            isAlreadyMember = s.isAlreadyMember
        )
        val photo = s.newPhoto?.let { PortalUpload(it, "profile_picture.jpg", "image/jpeg") }
        viewModelScope.launch {
            repository.applyForMembership(ctx.applicant, form, photo)
                .onSuccess { r ->
                    _state.update { st ->
                        when {
                            !r.success -> st.copy(isSubmitting = false, formError = "Failed to process membership application. Please try again.")
                            r.paymentUrl != null && r.transactionId != null && !s.isAlreadyMember ->
                                st.copy(isSubmitting = false, paymentUrl = r.paymentUrl)
                            s.isAlreadyMember || !r.paymentRequired ->
                                st.copy(isSubmitting = false, outcome = ApplicationOutcome.Submitted)
                            else -> st.copy(isSubmitting = false, formError = "Payment could not be started. Please try again.")
                        }
                    }
                }
                .onFailure { e ->
                    val msg = e.message ?: "Failed to process membership application. Please try again."
                    val fieldKey = when {
                        Regex("whatsapp", RegexOption.IGNORE_CASE).containsMatchIn(msg) -> AppField.WHATSAPP
                        Regex("mobile|phone|msisdn", RegexOption.IGNORE_CASE).containsMatchIn(msg) -> AppField.PHONE
                        Regex("pmdc", RegexOption.IGNORE_CASE).containsMatchIn(msg) -> AppField.PMDC
                        else -> null
                    }
                    _state.update {
                        it.copy(
                            isSubmitting = false,
                            formError = msg,
                            errors = if (fieldKey != null) mapOf(fieldKey to msg) else it.errors
                        )
                    }
                }
        }
    }

    fun onPaymentFinished(success: Boolean) = _state.update {
        if (success) it.copy(paymentUrl = null, outcome = ApplicationOutcome.Paid)
        else it.copy(paymentUrl = null, formError = "Payment was not completed. You can try again any time.")
    }

    fun dismissError() = _state.update { it.copy(formError = null) }
}
