package com.sspl.ui.userdetails

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sspl.core.usecases.ValidateUserCityUseCase
import com.sspl.core.usecases.ValidateUserCountryUseCase
import com.sspl.core.usecases.ValidateUserDesignationUseCase
import com.sspl.core.usecases.ValidateUserInstitutionUseCase
import com.sspl.core.usecases.ValidateUserPmdcNoUseCase
import com.sspl.ui.kyc.ValidationResult
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class UserDetailViewModel(
    private val validateUserCityUseCase: ValidateUserCityUseCase,
    private val validateUserCountryUseCase: ValidateUserCountryUseCase,
    private val validateUserDesignationUseCase: ValidateUserDesignationUseCase,
    private val validateUserInstitutionUseCase: ValidateUserInstitutionUseCase,
    private val validateUserPmDcNumberUseCase: ValidateUserPmdcNoUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(UserDetailsUiState())
    val uiState = _uiState.asStateFlow()

    fun onEvent(event: UserDetailsUiEvents) {
        viewModelScope.launch {
            when (event) {
                is UserDetailsUiEvents.OnCityTextChange -> {
                    val result = validateUserCityUseCase(event.cityText)
                    _uiState.update {
                        it.copy(
                            city = event.cityText,
                            cityError = if (result is ValidationResult.Error) "City can't be empty" else null
                        ).apply { updateSaveEnabledState() }
                    }
                }

                is UserDetailsUiEvents.OnCountryTextChange -> {
                    val result = validateUserCountryUseCase(event.countryText)
                    _uiState.update {
                        it.copy(
                            country = event.countryText,
                            countryError = if (result is ValidationResult.Error) "Country can't be empty" else null
                        ).apply { updateSaveEnabledState() }
                    }
                }

                is UserDetailsUiEvents.OnDesignationTextChange -> {
                    val result = validateUserDesignationUseCase(event.designationText)
                    _uiState.update {
                        it.copy(
                            designation = event.designationText,
                            designationError = if (result is ValidationResult.Error) "Designation can't be empty" else null
                        ).apply { updateSaveEnabledState() }
                    }
                }

                is UserDetailsUiEvents.OnInstitutionTextChange -> {
                    val result = validateUserInstitutionUseCase(event.institutionText)
                    _uiState.update {
                        it.copy(
                            institution = event.institutionText,
                            institutionError = if (result is ValidationResult.Error) "Institution can't be empty" else null
                        ).apply { updateSaveEnabledState() }
                    }
                }

                is UserDetailsUiEvents.OnPmDcNumberTextChange -> {
                    val result = validateUserPmDcNumberUseCase(event.pmDcNumberText)
                    _uiState.update {
                        it.copy(
                            pmDcNumber = event.pmDcNumberText,
                            pmDcNumberError = if (result is ValidationResult.Error) "PMDC Number can't be empty" else null
                        ).apply { updateSaveEnabledState() }
                    }
                }

                is UserDetailsUiEvents.OnRegistrationNumberTextChange -> {
                    _uiState.update {
                        it.copy(
                            registrationNumber = event.registrationNumberText
                        ).apply { updateSaveEnabledState() }
                    }
                }

                UserDetailsUiEvents.OnSave -> {
                    // Simulate a network call or save logic
                    _uiState.update {
                        it.copy(
                            isLoading = true
                        )
                    }
                    delay(2000) // Simulate network delay
                    _uiState.update {
                        it.copy(
                            isLoading = false
                        )
                    }
                }
            }
        }
    }

    // Helper function to update the save button enabled state based on field validation
    private fun UserDetailsUiState.updateSaveEnabledState() {
        _uiState.update {
            it.copy(
                isSaveEnabled = listOf(
                    cityError,
                    countryError,
                    designationError,
                    institutionError,
                    pmDcNumberError
                ).all { error -> error == null }
            )
        }
    }
}


data class UserDetailsUiState(
    val institution: String = "",
    val city: String = "",
    val country: String = "",
    val pmDcNumber: String = "",
    val registrationNumber: String? = "",
    val designation: String = "",
    val institutionError: String? = null,
    val cityError: String? = null,
    val countryError: String? = null,
    val pmDcNumberError: String? = null,
    val registrationNumberError: String? = null,
    val designationError: String? = null,
    val isSaveEnabled: Boolean = false,
    val isLoading: Boolean = false
)

sealed interface UserDetailsUiEvents {
    data object OnSave : UserDetailsUiEvents
    data class OnInstitutionTextChange(val institutionText: String) : UserDetailsUiEvents
    data class OnCityTextChange(val cityText: String) : UserDetailsUiEvents
    data class OnCountryTextChange(val countryText: String) : UserDetailsUiEvents
    data class OnPmDcNumberTextChange(val pmDcNumberText: String) : UserDetailsUiEvents
    data class OnRegistrationNumberTextChange(val registrationNumberText: String) :
        UserDetailsUiEvents

    data class OnDesignationTextChange(val designationText: String) : UserDetailsUiEvents
}