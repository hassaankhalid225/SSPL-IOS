package com.sspl.ui.portal

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.sspl.Screen
import com.sspl.core.models.EventItem
import com.sspl.core.repositories.EventRepository
import com.sspl.core.repositories.PortalRepository
import com.sspl.designsystem.components.SsplButton
import com.sspl.designsystem.components.SsplButtonVariant
import com.sspl.designsystem.components.SsplCard
import com.sspl.designsystem.components.SsplTextField
import com.sspl.designsystem.theme.AppTheme
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

/**
 * D6: Abstract Submission Form
 */
@Composable
fun AbstractSubmissionScreen(
    navController: NavController,
    portalRepository: PortalRepository = koinInject(),
    eventRepository: EventRepository = koinInject()
) {
    val scope = rememberCoroutineScope()
    var events by remember { mutableStateOf<List<EventItem>>(emptyList()) }
    var selectedEvent by remember { mutableStateOf<EventItem?>(null) }
    var isEventDropdownOpen by remember { mutableStateOf(false) }

    var title by remember { mutableStateOf("") }
    var authors by remember { mutableStateOf("") }
    var affiliations by remember { mutableStateOf("") }

    var isSubmitting by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    var isError by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        eventRepository.getEvents(active = true).collect { eventList ->
            events = eventList
            selectedEvent = eventList.firstOrNull()
        }
    }

    val handleSubmit = {
        if (title.isBlank()) {
            statusMessage = "Please enter the abstract title."
            isError = true
        } else if (authors.isBlank()) {
            statusMessage = "Please provide authors list."
            isError = true
        } else if (selectedEvent == null) {
            statusMessage = "Please select an eligible conference or meeting."
            isError = true
        } else {
            scope.launch {
                isSubmitting = true
                statusMessage = null
                val result = portalRepository.submitAbstract(
                    title = title,
                    authors = authors,
                    affiliations = affiliations,
                    conferenceId = selectedEvent?.id,
                    meetingId = null
                )
                isSubmitting = false
                if (result.isSuccess) {
                    statusMessage = "Abstract submitted successfully for scientific review!"
                    isError = false
                    title = ""
                    authors = ""
                    affiliations = ""
                } else {
                    statusMessage = result.exceptionOrNull()?.message ?: "Submission failed. Please try again."
                    isError = true
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
                text = "SCIENTIFIC SCHOLARSHIP",
                style = AppTheme.typography.labelSmall.copy(
                    color = AppTheme.colors.secondary,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Submit Abstract",
                style = AppTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = AppTheme.colors.heading
                )
            )
            Text(
                text = "Submit scientific paper or clinical study for review by the scientific committee.",
                style = AppTheme.typography.bodySmall.copy(color = AppTheme.colors.muted)
            )
        }

        if (statusMessage != null) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (isError) AppTheme.colors.error.copy(alpha = 0.1f)
                            else AppTheme.colors.accent.copy(alpha = 0.15f)
                        )
                        .padding(12.dp)
                ) {
                    Text(
                        text = statusMessage!!,
                        style = AppTheme.typography.bodySmall.copy(
                            color = if (isError) AppTheme.colors.error else AppTheme.colors.secondary,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
            }
        }

        item {
            SsplCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Event Selector
                    Text(
                        text = "Target Event / Conference *",
                        style = AppTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = AppTheme.colors.heading
                        )
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(AppTheme.colors.surface)
                            .clickable { isEventDropdownOpen = true }
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = selectedEvent?.title ?: "Select Event",
                                style = AppTheme.typography.bodyMedium.copy(
                                    color = if (selectedEvent != null) AppTheme.colors.heading else AppTheme.colors.muted
                                )
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                tint = AppTheme.colors.muted
                            )
                        }

                        DropdownMenu(
                            expanded = isEventDropdownOpen,
                            onDismissRequest = { isEventDropdownOpen = false }
                        ) {
                            events.forEach { event ->
                                DropdownMenuItem(
                                    text = { Text(event.title) },
                                    onClick = {
                                        selectedEvent = event
                                        isEventDropdownOpen = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Title
                    SsplTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = "Abstract Title *",
                        placeholder = "e.g. Outcome of Microvascular Free Flaps in Lower Limb Trauma",
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Authors
                    SsplTextField(
                        value = authors,
                        onValueChange = { authors = it },
                        label = "Author(s) *",
                        placeholder = "e.g. Dr. A. Khan, Prof. M. Tariq",
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Affiliations
                    SsplTextField(
                        value = affiliations,
                        onValueChange = { affiliations = it },
                        label = "Institution / Hospital Affiliation",
                        placeholder = "e.g. King Edward Medical University / Mayo Hospital, Lahore",
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Guidelines notice
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(AppTheme.colors.background)
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "Guidelines: Abstracts should include Background, Methods, Results, and Conclusion (maximum 350 words). Once accepted, presentation decks can be uploaded in Presentations.",
                            style = AppTheme.typography.bodySmall.copy(
                                color = AppTheme.colors.muted,
                                fontSize = 11.5.sp
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    SsplButton(
                        text = if (isSubmitting) "Submitting..." else "Submit Abstract",
                        onClick = { handleSubmit() },
                        enabled = !isSubmitting,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        item {
            SsplButton(
                text = "View My Submissions",
                onClick = { navController.navigate(Screen.MySubmissionsScreen.route) },
                variant = SsplButtonVariant.Outline,
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

