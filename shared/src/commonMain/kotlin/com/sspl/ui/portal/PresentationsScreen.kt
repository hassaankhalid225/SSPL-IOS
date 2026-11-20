package com.sspl.ui.portal

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Slideshow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.sspl.Screen
import com.sspl.core.models.AbstractSubmission
import com.sspl.core.models.Presentation
import com.sspl.core.repositories.PortalRepository
import com.sspl.designsystem.components.ModernSupportCard
import com.sspl.designsystem.components.SsplBadge
import com.sspl.designsystem.components.SsplBadgeVariant
import com.sspl.designsystem.components.SsplButton
import com.sspl.designsystem.components.SsplButtonVariant
import com.sspl.designsystem.components.SsplCard
import com.sspl.designsystem.components.SsplTextField
import com.sspl.designsystem.theme.AppTheme
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

/**
 * D8: Presentation Deck Upload & List Screen
 */
@Composable
fun PresentationsScreen(
    navController: NavController,
    portalRepository: PortalRepository = koinInject()
) {
    val scope = rememberCoroutineScope()
    var presentations by remember { mutableStateOf<List<Presentation>>(emptyList()) }
    var acceptedAbstracts by remember { mutableStateOf<List<AbstractSubmission>>(emptyList()) }
    var selectedAbstract by remember { mutableStateOf<AbstractSubmission?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var newTitle by remember { mutableStateOf("") }
    var actionMessage by remember { mutableStateOf<String?>(null) }
    var isSubmitting by remember { mutableStateOf(false) }

    val loadData = {
        scope.launch {
            isLoading = true
            portalRepository.getMyPresentations().collect { list ->
                presentations = list
            }
            portalRepository.getMySubmissions().collect { subs ->
                val accepted = subs.filter {
                    it.status.equals("ACCEPTED", ignoreCase = true) || it.status.equals("APPROVED", ignoreCase = true)
                }
                acceptedAbstracts = accepted
                if (selectedAbstract == null && accepted.isNotEmpty()) {
                    selectedAbstract = accepted.first()
                    newTitle = "Presentation: ${accepted.first().title}"
                }
                isLoading = false
            }
        }
    }

    LaunchedEffect(Unit) {
        loadData()
    }

    val handleUpload = {
        val targetAbstract = selectedAbstract
        if (targetAbstract == null) {
            actionMessage = "Please select an accepted abstract to queue your deck."
        } else if (newTitle.isBlank()) {
            actionMessage = "Please enter a presentation title."
        } else {
            scope.launch {
                isSubmitting = true
                actionMessage = null
                val res = portalRepository.uploadPresentation(
                    abstractSubmissionId = targetAbstract.id,
                    title = newTitle
                )
                isSubmitting = false
                if (res.isSuccess) {
                    actionMessage = "Presentation deck queued successfully for AV Hall projection!"
                    newTitle = ""
                    loadData()
                } else {
                    actionMessage = res.exceptionOrNull()?.message ?: "Failed to queue presentation."
                }
            }
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
                text = "SCIENTIFIC PRESENTATIONS",
                style = AppTheme.typography.labelSmall.copy(
                    color = AppTheme.colors.secondary,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Presentation Uploads",
                style = AppTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = AppTheme.colors.heading
                )
            )
            Text(
                text = "Upload and queue slide decks for accepted abstracts to be shown in the AV hall.",
                style = AppTheme.typography.bodySmall.copy(color = AppTheme.colors.muted)
            )
        }

        // Upload Presentation Notice Card
        item {
            SsplCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CloudUpload,
                            contentDescription = null,
                            tint = AppTheme.colors.secondary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "AV Hall Deck Guidelines",
                            style = AppTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = AppTheme.colors.heading
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "• Slides must be in 16:9 widescreen format.\n• Supported formats: PowerPoint (.pptx, .ppt) or PDF.\n• Please ensure videos/audio are embedded.\n• Decks should be uploaded at least 4 hours before the session.",
                        style = AppTheme.typography.bodySmall.copy(
                            color = AppTheme.colors.body,
                            lineHeight = 20.sp
                        )
                    )
                }
            }
        }

        // Interactive Presentation Queue Form (if accepted abstracts exist)
        if (acceptedAbstracts.isNotEmpty()) {
            item {
                SsplCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Slideshow,
                                contentDescription = null,
                                tint = AppTheme.colors.secondary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Queue Deck for AV Hall",
                                style = AppTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = AppTheme.colors.heading
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Select Accepted Abstract:",
                            style = AppTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = AppTheme.colors.body
                            )
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(acceptedAbstracts) { abs ->
                                val isSelected = selectedAbstract?.id == abs.id
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(50.dp))
                                        .background(
                                            if (isSelected) AppTheme.colors.primaryDark else AppTheme.colors.boxColor
                                        )
                                        .border(
                                            width = 1.dp,
                                            color = if (isSelected) Color.Transparent else AppTheme.colors.border,
                                            shape = RoundedCornerShape(50.dp)
                                        )
                                        .clickable {
                                            selectedAbstract = abs
                                            newTitle = "Presentation: ${abs.title}"
                                        }
                                        .padding(horizontal = 14.dp, vertical = 7.dp)
                                ) {
                                    Text(
                                        text = "#${abs.id} · ${abs.title.take(24)}...",
                                        style = AppTheme.typography.labelSmall.copy(
                                            color = if (isSelected) Color.White else AppTheme.colors.heading,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                        )
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        SsplTextField(
                            value = newTitle,
                            onValueChange = { newTitle = it },
                            label = "Presentation Title"
                        )

                        actionMessage?.let { msg ->
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = msg,
                                style = AppTheme.typography.bodySmall.copy(
                                    color = if (msg.contains("success", ignoreCase = true)) Color(0xFF047857) else Color(0xFFDC2626),
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        SsplButton(
                            text = if (isSubmitting) "Queueing Deck..." else "Submit Deck to AV Queue",
                            onClick = { handleUpload() },
                            enabled = !isSubmitting,
                            variant = SsplButtonVariant.Primary,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        item {
            Text(
                text = "Uploaded Presentations",
                style = AppTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = AppTheme.colors.heading
                )
            )
        }

        if (isLoading) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = AppTheme.colors.secondary)
                }
            }
        } else if (presentations.isEmpty()) {
            item {
                SsplCard(modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No presentation decks uploaded yet.",
                            style = AppTheme.typography.bodyMedium.copy(color = AppTheme.colors.muted)
                        )
                    }
                }
            }
        } else {
            items(presentations) { item ->
                SsplCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = item.title,
                                style = AppTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = AppTheme.colors.heading
                                ),
                                modifier = Modifier.weight(1f)
                            )
                            SsplBadge(
                                text = item.status ?: "UPLOADED",
                                variant = SsplBadgeVariant.Success
                            )
                        }

                        item.createdAt?.let { dt ->
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Uploaded: ${dt.split("T").firstOrNull() ?: dt}",
                                style = AppTheme.typography.bodySmall.copy(color = AppTheme.colors.muted)
                            )
                        }
                    }
                }
            }
        }

        item {
            ModernSupportCard(
                title = "Need AV Assistance?",
                subtitle = "For technical requirements, audio-visual podium checks, or deck modifications on event day, speak with the technical desk.",
                buttonText = "Contact Support",
                onContactClick = { navController.navigate(Screen.ContactUsScreen.route) }
            )
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

