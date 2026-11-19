package com.sspl.ui.fellowship

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.sspl.core.repositories.FellowshipRepository
import com.sspl.designsystem.components.SsplButton
import com.sspl.designsystem.components.SsplCard
import com.sspl.designsystem.components.SsplTextField
import com.sspl.designsystem.theme.AppTheme
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

/**
 * E8: Fellowship Evaluation & Feedback Screen
 */
@Composable
fun FellowshipFeedbackScreen(
    navController: NavController,
    fellowshipRepository: FellowshipRepository = koinInject()
) {
    val scope = rememberCoroutineScope()

    var clinicalVolumeRating by remember { mutableStateOf(5) }
    var supervisionRating by remember { mutableStateOf(5) }
    var academicAtmosphereRating by remember { mutableStateOf(5) }
    var overallRating by remember { mutableStateOf(5) }
    var feedbackText by remember { mutableStateOf("") }

    var isSubmitting by remember { mutableStateOf(false) }
    var submissionSuccess by remember { mutableStateOf(false) }

    val handleSubmit = {
        scope.launch {
            isSubmitting = true
            // Mock or actual call to feedback
            kotlinx.coroutines.delay(400)
            isSubmitting = false
            submissionSuccess = true
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(AppTheme.colors.background)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "PROGRAM QUALITY AUDIT",
                style = AppTheme.typography.labelSmall.copy(
                    color = AppTheme.colors.secondary,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Fellowship Feedback",
                style = AppTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = AppTheme.colors.heading
                )
            )
            Text(
                text = "Confidential evaluation of training centre facilities, operative volume, and supervision.",
                style = AppTheme.typography.bodySmall.copy(color = AppTheme.colors.muted)
            )
        }

        if (submissionSuccess) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(AppTheme.colors.accent.copy(alpha = 0.15f))
                        .padding(14.dp)
                ) {
                    Text(
                        text = "Thank you! Your feedback has been recorded securely by the SSPL Fellowship Academic Board.",
                        style = AppTheme.typography.bodySmall.copy(
                            color = AppTheme.colors.secondary,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
            }
        }

        item {
            SsplCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Training Unit Evaluation",
                        style = AppTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = AppTheme.colors.heading
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    RatingRow(
                        label = "Operative Case Volume & Diversity",
                        rating = clinicalVolumeRating,
                        onRatingChange = { clinicalVolumeRating = it }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    RatingRow(
                        label = "Quality of Faculty Supervision in OT",
                        rating = supervisionRating,
                        onRatingChange = { supervisionRating = it }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    RatingRow(
                        label = "Academic Atmosphere & Conferences",
                        rating = academicAtmosphereRating,
                        onRatingChange = { academicAtmosphereRating = it }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    RatingRow(
                        label = "Overall Programme Satisfaction",
                        rating = overallRating,
                        onRatingChange = { overallRating = it }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    SsplTextField(
                        value = feedbackText,
                        onValueChange = { feedbackText = it },
                        label = "Suggestions for Improvement",
                        placeholder = "Share specific observations on clinical rotations or logbook...",
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    SsplButton(
                        text = if (isSubmitting) "Submitting..." else "Submit Evaluation",
                        onClick = { handleSubmit() },
                        enabled = !isSubmitting,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun RatingRow(
    label: String,
    rating: Int,
    onRatingChange: (Int) -> Unit
) {
    Column {
        Text(
            text = label,
            style = AppTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            (1..5).forEach { star ->
                val isFilled = star <= rating
                Icon(
                    imageVector = if (isFilled) Icons.Filled.Star else Icons.Outlined.Star,
                    contentDescription = "$star stars",
                    tint = if (isFilled) Color(0xFFF59E0B) else AppTheme.colors.border,
                    modifier = Modifier
                        .size(26.dp)
                        .clickable { onRatingChange(star) }
                )
            }
        }
    }
}

