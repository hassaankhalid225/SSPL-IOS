package com.sspl.ui.feedback

import com.sspl.designsystem.icons.SsplIcons

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.sspl.core.ApiStates
import com.sspl.core.models.FeedbackForm
import com.sspl.core.models.Option
import com.sspl.core.models.QuestionType
import com.sspl.core.models.Statement
import com.sspl.designsystem.components.EmptyState
import com.sspl.designsystem.components.ErrorState
import com.sspl.designsystem.components.GlassPill
import com.sspl.designsystem.components.GradientHeroCard
import com.sspl.designsystem.components.PrimaryButton
import com.sspl.designsystem.components.SkeletonList
import com.sspl.designsystem.components.SsplCard
import com.sspl.designsystem.theme.AppTheme
import org.koin.compose.viewmodel.koinViewModel

/**
 * Session / event feedback form (GET feedback form → POST answers). Logic unchanged;
 * dropdowns replaced by tappable rating pills, comments are proper multi-line fields.
 */
@Composable
fun FeedbackScreen(navController: NavController, viewModel: FeedbackViewModel = koinViewModel()) {
    val handle = remember { navController.previousBackStackEntry?.savedStateHandle }
    val conferenceID = remember { handle?.get<Long>("conferenceID") }
    // Session ID is null when feedback is for the whole conference.
    val sessionID = remember { handle?.get<Long>("sessionID") }

    if (conferenceID == null) {
        EmptyState(
            title = "Choose what to review",
            message = "Open feedback from a session in your event programme or from the Event Portal.",
            icon = SsplIcons.RateReview,
            modifier = Modifier.fillMaxSize()
        )
        return
    }

    val snackbar = remember { SnackbarHostState() }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(conferenceID, sessionID) {
        viewModel.setConferenceId(conferenceID)
        viewModel.setSessionId(sessionID)
    }
    LaunchedEffect(Unit) {
        viewModel.isFeedbackSubmitted.collect { status ->
            when (status) {
                is ApiStates.Failure -> snackbar.showSnackbar(status.error.message ?: "Feedback couldn't be sent. Try again.")
                is ApiStates.Success -> {
                    snackbar.showSnackbar(status.data?.message?.takeIf { it.isNotBlank() } ?: "Thanks for your feedback!")
                    navController.popBackStack()
                }
                else -> Unit
            }
        }
    }

    val form = viewModel.feedbackFormResponse.collectAsStateWithLifecycle().value
    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(snackbar) },
        containerColor = Color.Transparent
    ) { _ ->
        when (form) {
            is ApiStates.Failure -> ErrorState(
                message = form.error.message ?: "The feedback form couldn't be loaded.",
                onRetry = viewModel::retry,
                modifier = Modifier.fillMaxSize()
            )
            is ApiStates.Success -> form.data?.let {
                FeedbackScreenContent(uiState = uiState, feedbackForm = it, onEvent = viewModel::onEvent)
            } ?: EmptyState(title = "No feedback form", message = "Feedback isn't open for this event yet.", modifier = Modifier.fillMaxSize())
            else -> SkeletonList(count = 4)
        }
    }
}

@Composable
fun FeedbackScreenContent(
    uiState: FeedbackUiState,
    feedbackForm: FeedbackForm,
    onEvent: (FeedbackUiEvents) -> Unit = {}
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().imePadding(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item(key = "hero") {
            GradientHeroCard {
                GlassPill("FEEDBACK", icon = SsplIcons.RateReview)
                Spacer(Modifier.height(10.dp))
                Text(feedbackForm.title, style = AppTheme.typography.headlineSmall.copy(color = Color.White))
                Text(
                    feedbackForm.description?.takeIf { it.isNotBlank() } ?: "Your answers help us plan better CME sessions.",
                    style = AppTheme.typography.bodySmall.copy(color = Color.White.copy(alpha = 0.78f))
                )
            }
        }
        itemsIndexed(feedbackForm.questionGroups, key = { i, _ -> "g_$i" }) { _, group ->
            SsplCard {
                Text(group.title, style = AppTheme.typography.titleSmall)
                if (group.description.isNotBlank()) {
                    Text(group.description, style = AppTheme.typography.bodySmall)
                }
                group.questions.forEach { question ->
                    Spacer(Modifier.height(12.dp))
                    when (question.type) {
                        QuestionType.EVALUATION, QuestionType.YESNO -> question.statements.forEach { st ->
                            QuestionSelector(
                                statement = st,
                                options = question.options,
                                onAnswerSelected = { s, o -> onEvent(FeedbackUiEvents.OnAnswerSelected(s, o)) },
                                modifier = Modifier.padding(bottom = 10.dp)
                            )
                        }
                        QuestionType.COMMENT -> CommentTextEditor(
                            placeholderText = question.statement,
                            onTextChanged = {
                                onEvent(
                                    FeedbackUiEvents.OnAnswerSelected(
                                        statement = Statement(id = question.id, statement = question.statement),
                                        comment = it
                                    )
                                )
                            }
                        )
                    }
                }
            }
        }
        item(key = "submit") {
            PrimaryButton(
                text = "Submit feedback",
                onClick = { onEvent(FeedbackUiEvents.OnSubmitClicked) },
                enabled = uiState.isEnabled && !uiState.isLoading,
                loading = uiState.isLoading,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

/** Single-choice pills for a statement (evaluation scale or yes/no). */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun QuestionSelector(
    modifier: Modifier = Modifier,
    statement: Statement,
    options: List<Option>,
    onAnswerSelected: (Statement, Option) -> Unit
) {
    var selected by rememberSaveable(statement.id) { mutableStateOf<String?>(null) }
    val colors = AppTheme.colors
    Column(modifier.fillMaxWidth()) {
        Text(statement.statement, style = AppTheme.typography.bodyMedium.copy(color = colors.heading))
        Spacer(Modifier.height(6.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            options.forEach { o ->
                val sel = selected == o.value
                Box(
                    Modifier
                        .clip(AppTheme.shapes.chip)
                        .background(if (sel) colors.buttonStart else colors.surface)
                        .border(1.dp, if (sel) colors.primary else colors.border, AppTheme.shapes.chip)
                        .selectable(selected = sel, role = Role.RadioButton) {
                            selected = o.value
                            onAnswerSelected(statement, o)
                        }
                        .heightIn(min = 40.dp)
                        .padding(horizontal = 14.dp, vertical = 9.dp)
                ) {
                    Text(
                        o.label,
                        style = AppTheme.typography.labelMedium.copy(
                            color = if (sel) colors.onPrimary else colors.body,
                            fontWeight = if (sel) FontWeight.SemiBold else FontWeight.Medium
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun CommentTextEditor(
    modifier: Modifier = Modifier,
    placeholderText: String = "Share your thoughts",
    onTextChanged: (String) -> Unit
) {
    var text by rememberSaveable { mutableStateOf("") }
    val colors = AppTheme.colors
    OutlinedTextField(
        value = text,
        onValueChange = { text = it; onTextChanged(it) },
        modifier = modifier.fillMaxWidth().heightIn(min = 110.dp),
        placeholder = { Text(placeholderText, style = AppTheme.typography.bodyMedium.copy(color = colors.muted)) },
        textStyle = AppTheme.typography.bodyLarge.copy(color = colors.heading),
        shape = AppTheme.shapes.button,
        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Default),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = colors.primary,
            unfocusedBorderColor = colors.border,
            focusedContainerColor = colors.surface,
            unfocusedContainerColor = colors.boxColor
        )
    )
}
