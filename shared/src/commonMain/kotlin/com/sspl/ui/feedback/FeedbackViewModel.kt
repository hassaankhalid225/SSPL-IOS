package com.sspl.ui.feedback

import androidx.lifecycle.ViewModel
import com.sspl.core.usecases.ValidateUserNameUseCase
import com.sspl.ui.kyc.ValidationResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class FeedbackViewModel(
    private val validateUserNameUseCase: ValidateUserNameUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(FeedbackUiState())
    val uiState: StateFlow<FeedbackUiState> = _uiState.asStateFlow()

    fun onEvent(event: FeedbackUiEvents) {
        when (event) {

            is FeedbackUiEvents.OnBestAspectsCommentChanged -> {
                _uiState.value = _uiState.value.copy(bestAspectsComment = event.bestAspectsComment)
            }

            is FeedbackUiEvents.OnFacilitatorNameChanged -> {
                val isUserNameValid = validateUserNameUseCase(event.facilitatorName)

                _uiState.value = _uiState.value.copy(
                    facilitatorName = event.facilitatorName,
                    facilitatorNameError = if (isUserNameValid is ValidationResult.Error) "Invalid Name" else null
                )

            }

            is FeedbackUiEvents.OnAreaToImproveCommentChanged -> {
                _uiState.value =
                    _uiState.value.copy(areaToImproveComment = event.areaToImproveComment)
            }

            is FeedbackUiEvents.OnSubmitClicked -> {

            }

        }

    }

}

data class FeedbackUiState(
    val isLoading: Boolean = false,
    val isEnabled: Boolean = false,
    val facilitatorName: String = "",
    val facilitatorNameError: String? = null,
    val bestAspectsComment: String = "",
    val areaToImproveComment: String = "",
)

sealed interface FeedbackUiEvents {
    data class OnFacilitatorNameChanged(val facilitatorName: String) : FeedbackUiEvents
    data class OnBestAspectsCommentChanged(val bestAspectsComment: String) : FeedbackUiEvents
    data class OnAreaToImproveCommentChanged(val areaToImproveComment: String) : FeedbackUiEvents
    data object OnSubmitClicked : FeedbackUiEvents

}