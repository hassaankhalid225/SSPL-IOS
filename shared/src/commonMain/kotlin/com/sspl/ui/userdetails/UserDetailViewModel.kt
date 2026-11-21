package com.sspl.ui.userdetails

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sspl.core.ApiStates
import com.sspl.core.models.User

import com.sspl.core.usecases.PostUserDetailsUseCase
import com.sspl.core.usecases.ValidateUserCityUseCase
import com.sspl.core.usecases.ValidateUserCountryUseCase
import com.sspl.core.usecases.ValidateUserDesignationUseCase
import com.sspl.core.usecases.ValidateUserInstitutionUseCase
import com.sspl.core.usecases.ValidateUserPmdcNoUseCase
import com.sspl.core.usecases.ValidateUserRegistrationNumberUseCase
import com.sspl.ui.kyc.ValidationResult
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class UserDetailViewModel(
    private val validateUserCityUseCase: ValidateUserCityUseCase,
    private val validateUserCountryUseCase: ValidateUserCountryUseCase,
    private val validateUserDesignationUseCase: ValidateUserDesignationUseCase,
    private val validateUserInstitutionUseCase: ValidateUserInstitutionUseCase,
    private val validateUserRegistrationNumberUseCase: ValidateUserRegistrationNumberUseCase,
    private val validateUserPmDcNumberUseCase: ValidateUserPmdcNoUseCase,
    private val postUserDetailsUseCase: PostUserDetailsUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(UserDetailsUiState())
    val uiState = _uiState.asStateFlow()

    private val _networkState = MutableSharedFlow<ApiStates<User>>()
    val networkState: SharedFlow<ApiStates<User>> = _networkState.asSharedFlow()

    fun onEvent(event: UserDetailsUiEvents) {
        viewModelScope.launch {
            when (event) {
                is UserDetailsUiEvents.OnCityTextChange -> {
                    // Optional on the web sign-up form.
                    _uiState.update { it.copy(city = event.cityText, cityError = null) }
                }

                is UserDetailsUiEvents.ToggleCountryPicker -> {
                    _uiState.update {
                        it.copy(
                            showCountryPicker = !it.showCountryPicker
                        )
                    }
                }

                is UserDetailsUiEvents.OnCountryChange -> {
                    val country = event.country.ifBlank {
                        _uiState.value.country
                    }
                    // Optional on the web sign-up form.
                    _uiState.update { it.copy(country = country, showCountryPicker = false, countryError = null) }
                }

                is UserDetailsUiEvents.OnDesignationTextChange -> {
                    _uiState.update { it.copy(designation = event.designationText, designationError = null) }
                }

                is UserDetailsUiEvents.OnInstitutionTextChange -> {
                    // Optional on the web sign-up form.
                    _uiState.update { it.copy(institution = event.institutionText, institutionError = null) }
                }

                is UserDetailsUiEvents.OnPmDcNumberTextChange -> {
                    _uiState.update { it.copy(pmDcNumber = event.pmDcNumberText, pmDcNumberError = null) }
                }

                is UserDetailsUiEvents.OnRegistrationNumberTextChange -> {
                    _uiState.update { it.copy(registrationNumber = event.registrationNumberText, registrationNumberError = null) }
                }

                UserDetailsUiEvents.OnSave -> {
                    updateSaveEnabledState()
                    if (_uiState.value.isSaveEnabled && !_uiState.value.isLoading) {
                        postUserDetailsUseCase(
                            pmdcNo = _uiState.value.pmDcNumber.orEmpty(),
                            instituteName = _uiState.value.institution,
                            country = _uiState.value.country,
                            city = _uiState.value.city,
                            designation = _uiState.value.designation,
                            registrationNo = _uiState.value.registrationNumber.takeIf { it.isNotBlank() }
                        ).collect { state ->
                            _uiState.update { it.copy(isLoading = state is ApiStates.Loading) }
                            _networkState.emit(state)
                        }
                    }
                }
            }
            updateSaveEnabledState()
        }
    }

    /**
     * Every field is optional (web Signup.jsx: institute / city / country are `required={false}`;
     * PMDC / designation are optional). Save is enabled once at least one field is filled — the
     * user can always "Skip for now".
     */
    private fun updateSaveEnabledState() {
        _uiState.update { s ->
            s.copy(
                isSaveEnabled = listOf(s.institution, s.city, s.country, s.pmDcNumber.orEmpty(), s.designation,
                    s.registrationNumber).any { it.isNotBlank() }
            )
        }
    }
}


data class UserDetailsUiState(
    val city: String = "",
    val country: String = "",
    val institution: String = "",
    val pmDcNumber: String? = "",
    val registrationNumber: String = "",
    val designation: String = "",
    val institutionError: String? = null,
    val cityError: String? = null,
    val showCountryPicker: Boolean = false,
    val countryError: String? = null,
    val pmDcNumberError: String? = null,
    val registrationNumberError: String? = null,
    val designationError: String? = null,
    val isSaveEnabled: Boolean = false,
    val isLoading: Boolean = false
)

sealed interface UserDetailsUiEvents {
    data object OnSave : UserDetailsUiEvents
    data object ToggleCountryPicker : UserDetailsUiEvents
    data class OnInstitutionTextChange(val institutionText: String) : UserDetailsUiEvents
    data class OnCityTextChange(val cityText: String) : UserDetailsUiEvents
    data class OnCountryChange(val country: String) : UserDetailsUiEvents
    data class OnPmDcNumberTextChange(val pmDcNumberText: String) : UserDetailsUiEvents
    data class OnRegistrationNumberTextChange(val registrationNumberText: String) :
        UserDetailsUiEvents

    data class OnDesignationTextChange(val designationText: String) : UserDetailsUiEvents
}