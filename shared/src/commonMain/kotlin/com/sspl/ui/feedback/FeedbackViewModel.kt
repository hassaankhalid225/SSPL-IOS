package com.sspl.ui.feedback

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sspl.core.ApiStates
import com.sspl.core.models.FeedbackForm
import com.sspl.core.usecases.GetFeedbackFormUseCase
import com.sspl.core.usecases.ValidateUserNameUseCase
import com.sspl.ui.kyc.ValidationResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class FeedbackViewModel(
    private val validateUserNameUseCase: ValidateUserNameUseCase,
    private val getFeedbackFormUseCase: GetFeedbackFormUseCase
) : ViewModel() {
    private val _conferenceId = MutableStateFlow<Long?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val feedbackFormResponse =
        _conferenceId.filterNotNull().filterNotNull().flatMapLatest { confID ->
            getFeedbackFormUseCase(confID)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = ApiStates.Loading
        )

    private val _uiState = MutableStateFlow(FeedbackUiState())
    val uiState: StateFlow<FeedbackUiState> = _uiState.asStateFlow()

    fun setConferenceId(conferenceId: Long) {
        viewModelScope.launch {
            _conferenceId.update { conferenceId }
        }
    }

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

    private fun submitFeedback() {

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