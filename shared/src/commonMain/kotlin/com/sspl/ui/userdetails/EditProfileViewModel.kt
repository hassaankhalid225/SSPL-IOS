package com.sspl.ui.userdetails

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sspl.core.ApiStates
import com.sspl.core.models.User
import com.sspl.core.usecases.GetUserDetailsUseCase
import com.sspl.core.usecases.PhoneNumberValidationUseCase
import com.sspl.core.usecases.PostUserDetailsUseCase
import com.sspl.core.usecases.ProfileUpdateForm
import com.sspl.core.usecases.ProfileUpdateState
import com.sspl.session.UserSession
import com.sspl.ui.components.countrypicker.Country
import com.sspl.ui.components.countrypicker.Pakistan
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Edit profile — same fields, required rules, body and error mapping as the web
 * "My profile" page (sspl-frotend/src/pages/userDashboard/ProfilePage.jsx), plus the app's
 * mobile number, designation and society registration number.
 */
class EditProfileViewModel(
    private val userSession: UserSession,
    private val phoneValidationUseCase: PhoneNumberValidationUseCase,
    private val postUserDetailsUseCase: PostUserDetailsUseCase,
    private val getUserDetailsUseCase: GetUserDetailsUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditProfileUiState())
    val uiState = _uiState.asStateFlow()

    private var prefilled = false

    /** Prefill once from the user handed over by the profile screen (falls back to the session). */
    fun prefill(fromProfile: User?) {
        if (prefilled) return
        prefilled = true
        viewModelScope.launch {
            val user = fromProfile ?: userSession.getUser() ?: return@launch
            val p = user.profile
            _uiState.update {
                it.copy(
                    fields = mapOf(
                        F_FIRST_NAME to user.firstName.orEmpty(),
                        F_LAST_NAME to user.lastName.orEmpty(),
                        F_PHONE to user.account?.phone.orEmpty(),
                        F_PMDC to p?.pmdcNumber.orEmpty(),
                        F_HOSPITAL to p?.hospital.orEmpty(),
                        F_ADDRESS to p?.address.orEmpty(),
                        F_CITY to p?.city.orEmpty(),
                        F_COUNTRY to p?.country.orEmpty(),
                        F_INSTITUTE to p?.institute.orEmpty(),
                        F_TITLE to p?.title.orEmpty(),
                        F_ORG_NUMBER to p?.orgNumber.orEmpty(),
                        F_MBBS_INSTITUTE to p?.mbbsInstitute.orEmpty(),
                        F_MBBS_YEAR to p?.mbbsPassingYear.orEmpty(),
                        F_FCPS_INSTITUTE to p?.fcpsInstitute.orEmpty(),
                        F_FCPS_YEAR to p?.fcpsPassingYear.orEmpty(),
                        F_MS_INSTITUTE to p?.msInstitute.orEmpty(),
                        F_MS_YEAR to p?.msPassingYear.orEmpty(),
                        F_WHATSAPP to p?.contact?.whatsapp.orEmpty()
                    ),
                    gender = user.gender?.uppercase().orEmpty(),
                    email = user.account?.email.orEmpty()
                )
            }
        }
    }

    fun onFieldChange(key: String, value: String) {
        _uiState.update {
            it.copy(
                fields = it.fields + (key to value),
                fieldErrors = it.fieldErrors - errorKeyFor(key),
                formError = null,
                saveSuccess = false
            )
        }
    }

    fun onGenderChange(gender: String) {
        _uiState.update {
            it.copy(gender = gender, fieldErrors = it.fieldErrors - F_GENDER, formError = null, saveSuccess = false)
        }
    }

    fun onPhoneCountryChange(country: Country) =
        _uiState.update { it.copy(phoneCountry = country, showPhoneCountryChooser = false) }

    fun togglePhoneCountryChooser() =
        _uiState.update { it.copy(showPhoneCountryChooser = !it.showPhoneCountryChooser) }

    fun dismissSuccess() = _uiState.update { it.copy(saveSuccess = false) }

    /** Client-side rules = the web form's `required` fields (+ the app's phone format check). */
    private fun validate(s: EditProfileUiState): Map<String, String> {
        val errors = linkedMapOf<String, String>()
        if (s.value(F_FIRST_NAME).isBlank()) errors[F_FIRST_NAME] = "First name is required"
        if (s.value(F_LAST_NAME).isBlank()) errors[F_LAST_NAME] = "Last name is required"
        if (s.value(F_MBBS_INSTITUTE).isBlank()) errors[F_MBBS_INSTITUTE] = "MBBS institute is required"
        if (s.value(F_MBBS_YEAR).isBlank()) errors[F_MBBS_YEAR] = "MBBS passing year is required"
        val phone = s.value(F_PHONE).trim()
        if (phone.isNotEmpty() && !phoneValidationUseCase(phone)) errors[ERR_PHONE] = "Invalid phone number"
        return errors
    }

    fun save() {
        val s = _uiState.value
        if (s.isSaving) return
        val errors = validate(s)
        if (errors.isNotEmpty()) {
            _uiState.update {
                it.copy(fieldErrors = errors, formError = "Please correct the highlighted fields.", saveSuccess = false)
            }
            return
        }
        val form = ProfileUpdateForm(
            firstName = s.value(F_FIRST_NAME),
            lastName = s.value(F_LAST_NAME),
            gender = s.gender,
            phone = s.value(F_PHONE).ifBlank { null },
            pmdcNumber = s.value(F_PMDC),
            hospital = s.value(F_HOSPITAL),
            address = s.value(F_ADDRESS),
            city = s.value(F_CITY),
            country = s.value(F_COUNTRY),
            institute = s.value(F_INSTITUTE),
            title = s.value(F_TITLE),
            orgNumber = s.value(F_ORG_NUMBER),
            mbbsInstitute = s.value(F_MBBS_INSTITUTE),
            mbbsPassingYear = s.value(F_MBBS_YEAR),
            fcpsInstitute = s.value(F_FCPS_INSTITUTE),
            fcpsPassingYear = s.value(F_FCPS_YEAR),
            msInstitute = s.value(F_MS_INSTITUTE),
            msPassingYear = s.value(F_MS_YEAR),
            whatsapp = s.value(F_WHATSAPP)
        )
        viewModelScope.launch {
            postUserDetailsUseCase.updateProfile(form).collect { state ->
                when (state) {
                    ProfileUpdateState.Loading -> _uiState.update {
                        it.copy(isSaving = true, fieldErrors = emptyMap(), formError = null, saveSuccess = false)
                    }
                    is ProfileUpdateState.Failure -> _uiState.update {
                        it.copy(
                            isSaving = false,
                            fieldErrors = state.fieldErrors,
                            formError = state.message.ifBlank {
                                if (state.fieldErrors.isNotEmpty()) "Please correct the highlighted fields." else null
                            }
                        )
                    }
                    is ProfileUpdateState.Success -> {
                        refreshSessionUser(form, state.user)
                        _uiState.update { it.copy(isSaving = false, saveSuccess = true) }
                    }
                }
            }
        }
    }

    /**
     * Web calls `refreshUser(updateResult)`. We re-read GET /auth/me; if that fails, merge the
     * saved values into the cached user so every screen shows the new details immediately.
     */
    private suspend fun refreshSessionUser(form: ProfileUpdateForm, echoed: User?) {
        val current = userSession.userFlow.value ?: userSession.getUser()
        val fresh = runCatching {
            (getUserDetailsUseCase().first { it !is ApiStates.Loading } as? ApiStates.Success)?.data
        }.getOrNull()
        val merged: User? = when {
            fresh != null -> fresh.copy(
                account = fresh.account ?: current?.account,
                roles = fresh.roles.ifEmpty { current?.roles.orEmpty() },
                role = fresh.role ?: current?.role
            )
            current != null -> {
                val p = current.profile ?: com.sspl.core.models.Profile()
                current.copy(
                    firstName = form.firstName.trim(),
                    lastName = form.lastName.trim(),
                    gender = form.gender.ifBlank { current.gender },
                    account = current.account?.copy(phone = form.phone ?: current.account.phone),
                    profilePicture = echoed?.profilePicture ?: current.profilePicture,
                    profile = p.copy(
                        pmdcNumber = form.pmdcNumber.trim(),
                        hospital = form.hospital.trim(),
                        address = form.address.trim(),
                        city = form.city.trim(),
                        country = form.country.trim(),
                        institute = form.institute.trim(),
                        title = form.title.trim(),
                        orgNumber = form.orgNumber.trim(),
                        mbbsInstitute = form.mbbsInstitute.trim(),
                        mbbsPassingYear = form.mbbsPassingYear.trim(),
                        fcpsInstitute = form.fcpsInstitute.trim().ifBlank { p.fcpsInstitute },
                        fcpsPassingYear = form.fcpsPassingYear.trim().ifBlank { p.fcpsPassingYear },
                        msInstitute = form.msInstitute.trim().ifBlank { p.msInstitute },
                        msPassingYear = form.msPassingYear.trim().ifBlank { p.msPassingYear },
                        contact = if (form.whatsapp.isNotBlank())
                            (p.contact ?: com.sspl.core.models.Contact()).copy(whatsapp = form.whatsapp.trim())
                        else p.contact
                    )
                )
            }
            else -> null
        }
        merged?.let { userSession.setUser(it) }
    }

    companion object {
        const val F_FIRST_NAME = "first_name"
        const val F_LAST_NAME = "last_name"
        const val F_GENDER = "gender"
        const val F_PHONE = "phone"
        const val F_PMDC = "pmdc_number"
        const val F_HOSPITAL = "hospital"
        const val F_ADDRESS = "address"
        const val F_CITY = "city"
        const val F_COUNTRY = "country"
        const val F_INSTITUTE = "institute"
        const val F_TITLE = "title"
        const val F_ORG_NUMBER = "org_number"
        const val F_MBBS_INSTITUTE = "mbbs_institute"
        const val F_MBBS_YEAR = "mbbs_passing_year"
        const val F_FCPS_INSTITUTE = "fcps_institute"
        const val F_FCPS_YEAR = "fcps_passing_year"
        const val F_MS_INSTITUTE = "ms_institute"
        const val F_MS_YEAR = "ms_passing_year"
        const val F_WHATSAPP = "whatsapp"

        /** Server maps account.phone → "account_phone" (web PROFILE_FIELD_MAP). */
        const val ERR_PHONE = "account_phone"

        fun errorKeyFor(fieldKey: String): String = if (fieldKey == F_PHONE) ERR_PHONE else fieldKey
    }
}

data class EditProfileUiState(
    val fields: Map<String, String> = emptyMap(),
    /** "" (not set), MALE, FEMALE or OTHER. */
    val gender: String = "",
    val email: String = "",
    val phoneCountry: Country = Pakistan,
    val showPhoneCountryChooser: Boolean = false,
    /** Keyed like the web form (see [EditProfileViewModel.errorKeyFor]). */
    val fieldErrors: Map<String, String> = emptyMap(),
    val formError: String? = null,
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false
) {
    fun value(key: String): String = fields[key].orEmpty()
    fun error(key: String): String? = fieldErrors[EditProfileViewModel.errorKeyFor(key)]
}
