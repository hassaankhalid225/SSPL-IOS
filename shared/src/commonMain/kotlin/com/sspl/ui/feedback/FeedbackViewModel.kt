package com.sspl.ui.feedback

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sspl.core.ApiStates
import com.sspl.core.models.CommonResponse
import com.sspl.core.models.FeedbackForm
import com.sspl.core.models.QuestionType
import com.sspl.core.usecases.GetFeedbackFormUseCase
import com.sspl.core.usecases.PostFeedbackFormUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class FeedbackViewModel(
    private val getFeedbackFormUseCase: GetFeedbackFormUseCase,
    private val postFeedbackFormUseCase: PostFeedbackFormUseCase,
) : ViewModel() {
    private val _conferenceId = MutableStateFlow<Long?>(null)
    private val _sessionId = MutableStateFlow<Long?>(null)

    private val _retry = MutableStateFlow(0)

    @OptIn(ExperimentalCoroutinesApi::class)
    val feedbackFormResponse =
        kotlinx.coroutines.flow.combine(_conferenceId.filterNotNull(), _retry) { id, _ -> id }.flatMapLatest { confID ->
            getFeedbackFormUseCase(confID)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = ApiStates.Loading
        )

    private val _uiState = MutableStateFlow(FeedbackUiState())
    val uiState: StateFlow<FeedbackUiState> = _uiState.asStateFlow()

    private val _isFeedbackSubmitted: MutableSharedFlow<ApiStates<CommonResponse>> = MutableSharedFlow()
    val isFeedbackSubmitted = _isFeedbackSubmitted.asSharedFlow()


    fun retry() { _retry.update { it + 1 } }

    fun setConferenceId(conferenceId: Long) {
        viewModelScope.launch {
            _conferenceId.update { conferenceId }
        }
    }
fun setSessionId(sessionId: Long?) {
    viewModelScope.launch {
        _sessionId.update { sessionId }
    }
}

    fun onEvent(event: FeedbackUiEvents) {
        when (event) {
            is FeedbackUiEvents.OnAnswer -> _uiState.update { st ->
                st.copy(answers = st.answers + (event.id to event.value))
            }

            is FeedbackUiEvents.OnSubmitClicked -> {
                val id = _conferenceId.value ?: return
                if (_uiState.value.isLoading) return
                // Web sends every answered entry; Submit is only enabled when all are answered.
                val answers = _uiState.value.answers.filterValues { it.isNotBlank() }
                viewModelScope.launch {
                    postFeedbackFormUseCase(
                        sessionId = _sessionId.value, conferenceId = id, answers = answers
                    ).collect { state ->
                        _uiState.update { it.copy(isLoading = state is ApiStates.Loading) }
                        _isFeedbackSubmitted.emit(state)
                    }
                }
            }
        }
    }
}

/**
 * Web EventFeedback.collectRequiredQuestionIds: EVALUATION with statements -> each statement id,
 * every other question -> its own id. All are required.
 */
fun FeedbackForm.requiredAnswerIds(): List<Int> = questionGroups.orEmpty().flatMap { g ->
    g.questions.orEmpty().flatMap { q ->
        val st = q.statements.orEmpty()
        if (q.type == QuestionType.EVALUATION && st.isNotEmpty()) st.map { it.id } else listOf(q.id)
    }
}

fun FeedbackForm.hasQuestions(): Boolean = questionGroups.orEmpty().any { it.questions.orEmpty().isNotEmpty() }

@Stable
@Immutable
data class FeedbackUiState(
    val isLoading: Boolean = false,
    /** question / statement id -> answer value (web `answers` map). */
    val answers: Map<Int, String> = emptyMap()
) {
    fun isComplete(form: FeedbackForm): Boolean =
        form.requiredAnswerIds().all { answers[it]?.trim()?.isNotEmpty() == true }
}

sealed interface FeedbackUiEvents {
    data class OnAnswer(val id: Int, val value: String) : FeedbackUiEvents
    data object OnSubmitClicked : FeedbackUiEvents
}
