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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
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
                    sessionID?.let { sid ->
                        navController.previousBackStackEntry?.savedStateHandle?.set("feedbackSubmittedSessionId", sid)
                    }
                    navController.previousBackStackEntry?.savedStateHandle?.set("feedbackSubmitted", true)
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
    val answers = uiState.answers
    val complete = uiState.isComplete(feedbackForm)
    LazyColumn(
        modifier = Modifier.fillMaxSize().imePadding(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item(key = "hero") {
            GradientHeroCard {
                GlassPill("FEEDBACK", icon = SsplIcons.RateReview)
                Spacer(Modifier.height(10.dp))
                Text(
                    feedbackForm.title?.takeIf { it.isNotBlank() } ?: "Session feedback",
                    style = AppTheme.typography.headlineSmall.copy(color = Color.White)
                )
                Text(
                    feedbackForm.description?.takeIf { it.isNotBlank() }
                        ?: "All questions are required. Your answers help us plan better CME sessions.",
                    style = AppTheme.typography.bodySmall.copy(color = Color.White.copy(alpha = 0.78f))
                )
            }
        }
        if (!feedbackForm.hasQuestions()) {
            item(key = "none") {
                EmptyState(
                    title = "No feedback form yet",
                    message = "No feedback form available for this event yet.",
                    icon = SsplIcons.RateReview
                )
            }
            return@LazyColumn
        }
        itemsIndexed(feedbackForm.questionGroups.orEmpty(), key = { i, g -> "g_${g.id ?: i}_$i" }) { _, group ->
            SsplCard {
                group.title?.takeIf { it.isNotBlank() }?.let { Text(it, style = AppTheme.typography.titleSmall) }
                group.description?.takeIf { it.isNotBlank() }?.let { Text(it, style = AppTheme.typography.bodySmall) }
                group.questions.orEmpty().forEach { q ->
                    Spacer(Modifier.height(12.dp))
                    val statements = q.statements.orEmpty()
                    val options = q.options.orEmpty()
                    when {
                        // EVALUATION with statements: one answer per statement id (web parity).
                        q.type == QuestionType.EVALUATION && statements.isNotEmpty() -> {
                            q.statement?.takeIf { it.isNotBlank() }?.let {
                                Text(it, style = AppTheme.typography.bodyMedium.copy(color = AppTheme.colors.heading, fontWeight = FontWeight.Medium))
                            }
                            q.description?.takeIf { it.isNotBlank() }?.let { Text(it, style = AppTheme.typography.bodySmall) }
                            statements.forEach { st ->
                                QuestionSelector(
                                    label = st.statement.orEmpty(),
                                    options = options,
                                    selected = answers[st.id],
                                    onSelect = { onEvent(FeedbackUiEvents.OnAnswer(st.id, it.value)) },
                                    modifier = Modifier.padding(top = 10.dp)
                                )
                            }
                        }
                        // CHOICE / YESNO / EVALUATION without statements: answered by question id.
                        q.type == QuestionType.CHOICE || q.type == QuestionType.YESNO || q.type == QuestionType.EVALUATION -> {
                            QuestionSelector(
                                label = q.statement.orEmpty(),
                                description = q.description,
                                options = options,
                                selected = answers[q.id],
                                onSelect = { onEvent(FeedbackUiEvents.OnAnswer(q.id, it.value)) }
                            )
                        }
                        else -> {
                            RequiredLabel(q.statement.orEmpty())
                            q.description?.takeIf { it.isNotBlank() }?.let { Text(it, style = AppTheme.typography.bodySmall) }
                            Spacer(Modifier.height(6.dp))
                            CommentTextEditor(
                                value = answers[q.id].orEmpty(),
                                placeholderText = "Your answer...",
                                onTextChanged = { onEvent(FeedbackUiEvents.OnAnswer(q.id, it)) }
                            )
                        }
                    }
                }
            }
        }
        item(key = "submit") {
            Column {
                if (!complete) {
                    Text(
                        "Please answer all questions before submitting.",
                        style = AppTheme.typography.bodySmall,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                PrimaryButton(
                    text = "Submit feedback",
                    onClick = { onEvent(FeedbackUiEvents.OnSubmitClicked) },
                    enabled = complete && !uiState.isLoading,
                    loading = uiState.isLoading,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun RequiredLabel(text: String) {
    val colors = AppTheme.colors
    Text(
        buildAnnotatedString {
            append(text)
            withStyle(SpanStyle(color = colors.error)) { append(" *") }
        },
        style = AppTheme.typography.bodyMedium.copy(color = colors.heading, fontWeight = FontWeight.Medium)
    )
}

/** Single-choice pills (evaluation scale, choice or yes/no). */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun QuestionSelector(
    label: String,
    options: List<Option>,
    selected: String?,
    onSelect: (Option) -> Unit,
    modifier: Modifier = Modifier,
    description: String? = null
) {
    val colors = AppTheme.colors
    Column(modifier.fillMaxWidth()) {
        RequiredLabel(label)
        description?.takeIf { it.isNotBlank() }?.let { Text(it, style = AppTheme.typography.bodySmall) }
        Spacer(Modifier.height(6.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            options.forEach { o ->
                val sel = selected == o.value
                Box(
                    Modifier
                        .clip(AppTheme.shapes.chip)
                        .background(if (sel) colors.buttonStart else colors.surface)
                        .border(1.dp, if (sel) colors.primary else colors.border, AppTheme.shapes.chip)
                        .selectable(selected = sel, role = Role.RadioButton) { onSelect(o) }
                        .heightIn(min = 48.dp)
                        .padding(horizontal = 14.dp, vertical = 9.dp),
                    contentAlignment = Alignment.Center
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
    value: String,
    modifier: Modifier = Modifier,
    placeholderText: String = "Share your thoughts",
    onTextChanged: (String) -> Unit
) {
    val colors = AppTheme.colors
    OutlinedTextField(
        value = value,
        onValueChange = onTextChanged,
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
