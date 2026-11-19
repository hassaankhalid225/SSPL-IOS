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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Feedback
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.WorkspacePremium
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
import com.sspl.core.models.Conference
import com.sspl.core.models.MyRegistrationsData
import com.sspl.core.models.Registration
import com.sspl.core.repositories.ConferenceRepository
import com.sspl.core.repositories.EventRepository
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
 * D5: Event Portal (Active Conference Attendance, Sessions & Feedback)
 */
@Composable
fun EventPortalScreen(
    navController: NavController,
    eventRepository: EventRepository = koinInject(),
    conferenceRepository: ConferenceRepository = koinInject()
) {
    val scope = rememberCoroutineScope()
    var registrationsData by remember { mutableStateOf<MyRegistrationsData?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var joinSessionCode by remember { mutableStateOf("") }
    var joinMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        eventRepository.getMyRegistrations().collect { data ->
            registrationsData = data
            isLoading = false
        }
    }

    val confirmedRegistrations = registrationsData?.all?.filter {
        it.paymentStatus == "SUCCESS"
    } ?: emptyList()

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
                text = "EVENT PORTAL",
                style = AppTheme.typography.labelSmall.copy(
                    color = AppTheme.colors.secondary,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Active Event Portal",
                style = AppTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = AppTheme.colors.heading
                )
            )
            Text(
                text = "Join active sessions, mark verified attendance, and unlock certificates.",
                style = AppTheme.typography.bodySmall.copy(color = AppTheme.colors.muted)
            )
        }

        // Quick Session Join Code Box
        item {
            SsplCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.QrCodeScanner,
                            contentDescription = null,
                            tint = AppTheme.colors.secondary,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Join Session by Code",
                            style = AppTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = AppTheme.colors.heading
                            )
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Enter the 6-character room code announced on the hall display.",
                        style = AppTheme.typography.bodySmall.copy(color = AppTheme.colors.muted)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SsplTextField(
                            value = joinSessionCode,
                            onValueChange = { joinSessionCode = it.uppercase().take(10) },
                            placeholder = "e.g. SSPL01",
                            modifier = Modifier.weight(1f)
                        )
                        SsplButton(
                            text = "Join",
                            onClick = {
                                if (joinSessionCode.isNotBlank()) {
                                    navController.navigate(Screen.SessionScreen.route)
                                }
                            },
                            enabled = joinSessionCode.isNotBlank()
                        )
                    }
                }
            }
        }

        // Active Registered Events List
        item {
            Text(
                text = "Your Registered Events",
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
        } else if (confirmedRegistrations.isEmpty()) {
            item {
                SsplCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No confirmed event registrations found.",
                            style = AppTheme.typography.bodyMedium.copy(color = AppTheme.colors.muted)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        SsplButton(
                            text = "Browse Events",
                            onClick = { navController.navigate(Screen.ScientificProgramsScreen.route) }
                        )
                    }
                }
            }
        } else {
            items(confirmedRegistrations) { reg ->
                SsplCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = reg.conference?.title ?: "Conference Event",
                                style = AppTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = AppTheme.colors.heading
                                ),
                                modifier = Modifier.weight(1f)
                            )
                            SsplBadge(
                                text = "CONFIRMED",
                                variant = SsplBadgeVariant.Success
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Role: ${reg.role?.name ?: "Delegate"} • Reg #${reg.id}",
                            style = AppTheme.typography.bodySmall.copy(
                                color = AppTheme.colors.secondary,
                                fontWeight = FontWeight.SemiBold
                            )
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            SsplButton(
                                text = "Live Hall Session",
                                onClick = { navController.navigate(Screen.SessionScreen.route) },
                                variant = SsplButtonVariant.Primary,
                                modifier = Modifier.weight(1f)
                            )
                            SsplButton(
                                text = "Feedback",
                                onClick = { navController.navigate(Screen.FeedbackScreen.route) },
                                variant = SsplButtonVariant.Outline,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

