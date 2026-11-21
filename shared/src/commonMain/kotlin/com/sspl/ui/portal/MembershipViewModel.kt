package com.sspl.ui.portal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sspl.core.auth.RoleUtils
import com.sspl.core.models.MembershipPlan
import com.sspl.core.models.UserMembershipDetails
import com.sspl.core.repositories.MemberSnapshot
import com.sspl.core.repositories.PortalRepository
import com.sspl.designsystem.components.LoadState
import com.sspl.session.UserSession
import com.sspl.utils.DisplayDates
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Upgrade form keys = the web's `upgradeForm` keys (sent as-is in `profile`). */
object UpgradeField {
    const val PHONE = "account_phone"
    const val WHATSAPP = "whatsapp"
    const val MBBS_INST = "mbbs_institute"
    const val MBBS_YEAR = "mbbs_passing_year"
    const val FCPS_INST = "fcps_institute"
    const val FCPS_YEAR = "fcps_passing_year"
    const val FRCS_INST = "frcs_institute"
    const val FRCS_YEAR = "frcs_passing_year"
    const val MS_INST = "ms_institute"
    const val MS_YEAR = "ms_passing_year"
    val education = listOf(MBBS_INST, MBBS_YEAR, FCPS_INST, FCPS_YEAR, FRCS_INST, FRCS_YEAR, MS_INST, MS_YEAR)
}

const val POSTGRAD_REQUIRED_MESSAGE = "FCPS, FRCS, or MS qualification is required"
const val POSTGRAD_MEMBERSHIP_NOTICE =
    "A recognized postgraduate qualification in Surgery, verifiable on the official PM&DC website through the applicant's PM&DC registration number, is mandatory for SSPL membership."

/** Port of web `isValidPhoneNumber` (≥10 local digits after the country code / trunk 0). */
fun isValidPhone(value: String, minLocalDigits: Int = 10): Boolean =
    phoneLocalDigits(value).length >= minLocalDigits

/** Web `COUNTRY_CODES` dial codes, longest first (as `parsePhoneNumber` sorts them). */
private val WEB_DIAL_CODES = listOf(
    "+92", "+1", "+44", "+91", "+971", "+966", "+974", "+965", "+968", "+973", "+20", "+90", "+86", "+81",
    "+82", "+49", "+33", "+39", "+34", "+61", "+64", "+55", "+27", "+234", "+254", "+880", "+94", "+977",
    "+98", "+964"
).sortedByDescending { it.length }

/** Port of web `parsePhoneNumber(...).local`. */
private fun phoneLocalDigits(value: String): String {
    val compact = value.trim().filterNot { it.isWhitespace() }
    if (compact.isEmpty()) return ""
    if (compact.startsWith("+")) {
        WEB_DIAL_CODES.firstOrNull { compact.startsWith(it) }?.let { code ->
            return compact.drop(code.length).filter { it.isDigit() }
        }
    }
    val digits = compact.filter { it.isDigit() }
    return when {
        digits.startsWith("92") && digits.length >= 12 -> digits.drop(2).trimStart('0')
        Regex("^03\\d{9}$").matches(digits) -> digits.drop(1)
        digits.startsWith("0") -> digits.drop(1)
        else -> digits
    }
}

/** Port of web `validateYearValue`. Returns an error or null. */
fun validateYear(value: String): String? {
    val v = value.trim()
    if (v.isEmpty()) return "Passing year is required"
    if (v.length != 4 || !v.all { it.isDigit() }) return "Must be a valid 4-digit year"
    val y = v.toInt()
    if (y < 1950 || y > DisplayDates.today().year + 1) return "Enter a valid year"
    return null
}

data class MembershipUiState(
    val member: LoadState<MemberSnapshot> = LoadState.Loading,
    val details: UserMembershipDetails? = null,
    val lifePlans: List<MembershipPlan> = emptyList(),
    val plansError: String? = null,
    val selectedLifePlanId: Long? = null,
    val form: Map<String, String> = emptyMap(),
    val fieldErrors: Map<String, String> = emptyMap(),
    val formError: String? = null,
    val isUpgrading: Boolean = false,
    val isRefreshing: Boolean = false,
    val paymentUrl: String? = null,
    val successMessage: String? = null
) {
    private val snap get() = (member as? LoadState.Ready)?.data
    val isActiveMember get() = snap?.membershipStatus.equals("ACTIVE", true)
    val isLifeMember get() = isActiveMember &&
            (snap?.role.equals("LIFE_MEMBER", true) || RoleUtils.isLifeMember(snap?.user))
    val hasPendingUpgrade get() = snap?.pendingUpgrade == true
    val hasUnpaidUpgradeCheckout get() = snap?.upgradeCheckoutPending == true
    val isPendingApplication get() = snap?.membershipStatus.equals("PENDING", true) && !hasPendingUpgrade &&
            (snap?.hasPendingPlan == true || (snap?.user?.planId ?: 0L) > 0L)
    val selectedLifePlan get() = lifePlans.firstOrNull { it.id == selectedLifePlanId }
}

class MembershipViewModel(
    private val repository: PortalRepository,
    private val userSession: UserSession
) : ViewModel() {

    private val _state = MutableStateFlow(MembershipUiState())
    val state: StateFlow<MembershipUiState> = _state.asStateFlow()

    init { load(initial = true) }

    fun retry() = load(initial = true)
    fun refresh() = load(initial = false)

    private fun load(initial: Boolean) {
        _state.update {
            if (initial) it.copy(member = LoadState.Loading) else it.copy(isRefreshing = true)
        }
        viewModelScope.launch {
            val snapD = async { repository.getMemberSnapshot() }
            val plansD = async { repository.getMembershipPlans() }
            val snap = snapD.await()
            val plans = plansD.await()

            val snapshot = snap.getOrNull()
            // Keep the cached session user in sync (web calls refreshUser()).
            snapshot?.user?.takeIf { it.id > 0 && !it.firstName.isNullOrBlank() }?.let { runCatching { userSession.setUser(it) } }

            val details = if (snapshot?.membershipStatus.equals("ACTIVE", true)) repository.getMyMembership().getOrNull() else null
            val life = plans.getOrNull().orEmpty().filter { p ->
                p.type.equals("LIFE", true) || p.name.uppercase().contains("LIFE")
            }

            _state.update { s ->
                val member = when {
                    snapshot != null -> LoadState.Ready(snapshot)
                    s.member is LoadState.Ready && !initial -> s.member
                    else -> LoadState.Error(snap.exceptionOrNull()?.message ?: "We couldn't load your membership.")
                }
                val form = if (snapshot != null && (s.form.isEmpty() || initial)) buildMap {
                    put(UpgradeField.PHONE, snapshot.accountPhone)
                    put(UpgradeField.WHATSAPP, snapshot.whatsapp)
                    UpgradeField.education.forEach { k -> put(k, snapshot.education[k].orEmpty()) }
                } else s.form
                s.copy(
                    member = member,
                    details = details ?: s.details,
                    lifePlans = life,
                    plansError = plans.exceptionOrNull()?.message,
                    selectedLifePlanId = s.selectedLifePlanId?.takeIf { id -> life.any { it.id == id } } ?: life.firstOrNull()?.id,
                    form = form,
                    isRefreshing = false
                )
            }
        }
    }

    fun selectLifePlan(id: Long) = _state.update { it.copy(selectedLifePlanId = id) }

    fun onFieldChange(key: String, value: String) {
        val v = if (key.endsWith("_year")) value.filter { it.isDigit() }.take(4) else value
        _state.update { it.copy(form = it.form + (key to v), fieldErrors = it.fieldErrors - key, formError = null) }
    }

    private fun validate(form: Map<String, String>): Map<String, String> {
        val e = mutableMapOf<String, String>()
        val phone = form[UpgradeField.PHONE].orEmpty()
        if (phone.isBlank()) e[UpgradeField.PHONE] = "Mobile number is required for payment"
        else if (!isValidPhone(phone)) e[UpgradeField.PHONE] = "Enter a valid mobile number (e.g. 03001234567)"
        val wa = form[UpgradeField.WHATSAPP].orEmpty()
        if (wa.isNotBlank() && !isValidPhone(wa)) e[UpgradeField.WHATSAPP] = "Enter a valid WhatsApp number"
        if (form[UpgradeField.MBBS_INST].isNullOrBlank()) e[UpgradeField.MBBS_INST] = "MBBS institute is required"
        validateYear(form[UpgradeField.MBBS_YEAR].orEmpty())?.let { e[UpgradeField.MBBS_YEAR] = it }
        fun has(i: String, y: String) = !form[i].isNullOrBlank() && !form[y].isNullOrBlank()
        val postgrad = has(UpgradeField.FCPS_INST, UpgradeField.FCPS_YEAR) ||
                has(UpgradeField.FRCS_INST, UpgradeField.FRCS_YEAR) ||
                has(UpgradeField.MS_INST, UpgradeField.MS_YEAR)
        if (!postgrad) e[UpgradeField.FCPS_INST] = POSTGRAD_REQUIRED_MESSAGE
        return e
    }

    fun upgrade() {
        val s = _state.value
        if (s.isUpgrading) return
        val plan = s.selectedLifePlan
        if (plan == null) {
            _state.update { it.copy(formError = "Please select a life membership plan.") }
            return
        }
        val errors = validate(s.form)
        if (errors.isNotEmpty()) {
            _state.update {
                it.copy(fieldErrors = errors, formError = "Please fix the highlighted fields before continuing to payment.")
            }
            return
        }
        _state.update { it.copy(isUpgrading = true, formError = null, fieldErrors = emptyMap(), successMessage = null) }
        viewModelScope.launch {
            val profile = UpgradeField.education.associateWith { s.form[it].orEmpty().trim() }
            val result = repository.upgradeToLifeMembership(
                planId = plan.id,
                accountPhone = s.form[UpgradeField.PHONE].orEmpty().trim(),
                profileFields = profile,
                whatsapp = s.form[UpgradeField.WHATSAPP]?.trim()
            )
            result.onSuccess { url ->
                _state.update { it.copy(isUpgrading = false, paymentUrl = url) }
                if (url == null) {
                    _state.update { it.copy(successMessage = "Your life membership upgrade request was submitted.") }
                    refresh()
                }
            }.onFailure { e ->
                val msg = e.message ?: "Life membership upgrade failed"
                val phoneMsg = Regex("mobile number|phone|whatsapp|msisdn", RegexOption.IGNORE_CASE).containsMatchIn(msg)
                _state.update {
                    it.copy(
                        isUpgrading = false,
                        formError = msg,
                        fieldErrors = if (phoneMsg) it.fieldErrors + (UpgradeField.PHONE to msg) else it.fieldErrors
                    )
                }
            }
        }
    }

    fun onPaymentFinished(success: Boolean) {
        _state.update {
            it.copy(
                paymentUrl = null,
                successMessage = if (success) "Payment received. Your upgrade is awaiting admin approval." else it.successMessage,
                formError = if (!success) "Payment was not completed. You can start the upgrade again." else it.formError
            )
        }
        refresh()
    }

    fun dismissMessage() = _state.update { it.copy(successMessage = null) }
}
