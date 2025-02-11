package com.sspl.ui.feedback

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.sspl.resources.Res
import com.sspl.resources.app_logo
import com.sspl.resources.feedback
import com.sspl.resources.feedback_sub_title
import com.sspl.ui.components.AppTextField
import com.sspl.ui.components.AppTextLabel
import com.sspl.ui.components.AppTextTitle
import com.sspl.ui.components.ButtonWithProgress
import com.sspl.ui.kyc.HeaderSection
import com.sspl.ui.postcreation.DescriptionTextEditor
import com.sspl.utils.IconResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel


@Composable
fun FeedbackScreen(navController: NavController, viewModel: FeedbackViewModel = koinViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    FeedbackScreenContent(
        facilitatorName = uiState.facilitatorName,
        facilitatorNameError = uiState.facilitatorNameError,
        bestAspectsComment = uiState.bestAspectsComment,
        areaToImproveComment = uiState.areaToImproveComment,
        isLoading = uiState.isLoading,
        isEnabled = uiState.isEnabled,
        onEvent = viewModel::onEvent
    )
}


@Composable
fun FeedbackScreenContent(
    facilitatorName: String = "",
    facilitatorNameError: String? = null,
    bestAspectsComment: String = "",
    areaToImproveComment: String = "",
    isLoading: Boolean = false,
    isEnabled: Boolean = false,
    onEvent: (FeedbackUiEvents) -> Unit = {}
) {

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxSize().background(Color.White).verticalScroll(
            rememberScrollState()
        ).padding(16.dp)
    ) {
        Spacer(Modifier.height(4.dp))

        HeaderSection(
            iconResource = IconResource.fromDrawableResource(drawable = Res.drawable.app_logo),
            title = stringResource(Res.string.feedback),
            subTitle = stringResource(Res.string.feedback_sub_title)
        )
        Spacer(Modifier.height(8.dp))
        ActivityCard(modifier = Modifier.fillMaxWidth(), title = "Activity Evaluation", content = {
            QuestionSelector(questionText = "Objectives were achieved?", onAnswerSelected = {})
            QuestionSelector(questionText = "Activity was well organized?", onAnswerSelected = {})
            QuestionSelector(questionText = "Environment promoted learning ", onAnswerSelected = {})
            QuestionSelector(questionText = "Updated my knowledge/skills ", onAnswerSelected = {})
            QuestionSelector(questionText = "Filled gaps in my professional practice ",
                onAnswerSelected = {})
            QuestionSelector(questionText = "Would recommend to colleagues", onAnswerSelected = {})
        })

        Spacer(Modifier.height(8.dp))

        ActivityCard(modifier = Modifier.fillMaxWidth(),
            title = "Facilitator Evaluation",
            content = {
                AppTextField(placeholder = "Facilitators name",
                    text = facilitatorName,
                    errorMessage = facilitatorNameError,
                    onTextChange = {
                        onEvent(FeedbackUiEvents.OnFacilitatorNameChanged(it))
                    })
                QuestionSelector(questionText = "knowledge ", onAnswerSelected = {})
                QuestionSelector(questionText = "Clear Presentations ", onAnswerSelected = {})
                QuestionSelector(questionText = "interaction & discussion ", onAnswerSelected = {})
                QuestionSelector(questionText = "Explained key concepts ", onAnswerSelected = {})
            })

        Spacer(Modifier.height(8.dp))

        ActivityCard(modifier = Modifier.fillMaxWidth(), title = "Impact of activity", content = {
            YesNoQuestion(questionText = "Competence", onAnswerSelected = { boolean ->

            })
            YesNoQuestion(questionText = "Performance", onAnswerSelected = { boolean ->

            })
            YesNoQuestion(questionText = "Patient Outcome", onAnswerSelected = { boolean ->

            })
        })
        Spacer(Modifier.height(8.dp))

        ActivityCard(modifier = Modifier.fillMaxWidth(), title = "Comments", content = {
            DescriptionTextEditor(text = bestAspectsComment,
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier.fillMaxWidth().heightIn(min = 100.dp, max = 300.dp),
                placeholderText = "Best aspects of workshop:",
                onTextChanged = {
                    onEvent(FeedbackUiEvents.OnBestAspectsCommentChanged(it))
                })
            DescriptionTextEditor(text = areaToImproveComment,
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier.fillMaxWidth().heightIn(min = 100.dp, max = 300.dp),
                placeholderText = "Areas for improvement:",
                onTextChanged = {
                    onEvent(FeedbackUiEvents.OnAreaToImproveCommentChanged(it))
                })
        })

        Spacer(Modifier.height(8.dp))
        ButtonWithProgress(isEnabled = isEnabled,
            isLoading = isLoading,
            modifier = Modifier.imePadding(),
            buttonText = "Submit",
            onClick = {
                onEvent(FeedbackUiEvents.OnSubmitClicked)
            })

    }

}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuestionSelector(
    modifier: Modifier = Modifier, questionText: String, onAnswerSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var selectedOption by remember { mutableStateOf("") }

    val options = listOf(
        "Strongly Agree", "Agree", "Neutral", "Disagree", "Strongly Disagree"
    )

    Column(
        modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        AppTextLabel(
            text = questionText
        )

        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
            OutlinedTextField(value = selectedOption,
                onValueChange = {},
                readOnly = true,
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    errorBorderColor = MaterialTheme.colorScheme.error,
                    focusedLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unfocusedLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    focusedTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unfocusedTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable, true)
                    .fillMaxWidth(),
                placeholder = {
                    Text("Select an option", style = MaterialTheme.typography.bodySmall)

                })

            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                options.forEach { option ->
                    DropdownMenuItem(text = { Text(option) }, onClick = {
                        selectedOption = option
                        expanded = false
                        onAnswerSelected(option)
                    })
                }
            }
        }
    }
}


@Composable
fun ActivityCard(modifier: Modifier = Modifier, title: String, content: @Composable () -> Unit) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.4f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(22.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AppTextTitle(text = title, modifier.align(Alignment.Start))
            Spacer(Modifier.height(6.dp))
            content()
        }
    }
}

@Composable
fun YesNoQuestion(
    modifier: Modifier = Modifier, questionText: String, onAnswerSelected: (Boolean) -> Unit
) {
    var selectedAnswer by remember { mutableStateOf<Boolean?>(null) }

    Column(
        modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        AppTextLabel(
            text = questionText
        )

        Row(
            modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            RadioButton(text = "Yes", selected = selectedAnswer == true, onClick = {
                selectedAnswer = true
                onAnswerSelected(true)
            })

            RadioButton(text = "No", selected = selectedAnswer == false, onClick = {
                selectedAnswer = false
                onAnswerSelected(false)
            })
        }
    }
}

@Composable
private fun RadioButton(
    text: String, selected: Boolean, onClick: () -> Unit
) {
    Row(
        Modifier.selectable(
            selected = selected, onClick = onClick, role = Role.RadioButton
        ).padding(8.dp), verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = null // null because we're handling the click on the parent
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = text)
    }
}




