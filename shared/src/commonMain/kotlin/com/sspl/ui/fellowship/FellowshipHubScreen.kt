package com.sspl.ui.fellowship

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Feedback
import androidx.compose.material.icons.filled.HowToReg
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.SupervisorAccount
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.sspl.Screen
import com.sspl.core.models.FellowshipProgram
import com.sspl.core.models.FellowshipStats
import com.sspl.core.repositories.FellowshipRepository
import com.sspl.designsystem.components.ModernSupportCard
import com.sspl.designsystem.components.SsplBadge
import com.sspl.designsystem.components.SsplBadgeVariant
import com.sspl.designsystem.components.SsplButton
import com.sspl.designsystem.components.SsplCard
import com.sspl.designsystem.theme.AppTheme
import org.koin.compose.koinInject

/**
 * E1: Fellowship Hub (Public Overview, Stats, Programs & Links)
 */
@Composable
fun FellowshipHubScreen(
    navController: NavController,
    fellowshipRepository: FellowshipRepository = koinInject()
) {
    var programs by remember { mutableStateOf<List<FellowshipProgram>>(emptyList()) }
    var stats by remember { mutableStateOf(FellowshipStats()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        fellowshipRepository.getPublicStats().collect { s ->
            stats = s
        }
        fellowshipRepository.getOpenPrograms().collect { list ->
            programs = list
            isLoading = false
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
                text = "SPECIALIZED TRAINING",
                style = AppTheme.typography.labelSmall.copy(
                    color = AppTheme.colors.secondary,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Fellowship Hub",
                style = AppTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = AppTheme.colors.heading
                )
            )
            Text(
                text = "Structured postgraduate clinical fellowships certified by Society of Surgeons of Pakistan (Lahore).",
                style = AppTheme.typography.bodySmall.copy(color = AppTheme.colors.muted)
            )
        }

        // Stats Strip
        item {
            SsplCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    StatTile(label = "Active Fellows", value = "${stats.activeFellows}")
                    StatTile(label = "Centres", value = "${stats.trainingCenters}")
                    StatTile(label = "Graduates", value = "${stats.graduatedFellows}")
                    StatTile(label = "Trainers", value = "${stats.masterTrainers}")
                }
            }
        }

        // Quick Navigation Tiles
        item {
            Text(
                text = "Fellowship Services",
                style = AppTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = AppTheme.colors.heading
                )
            )
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                FellowshipActionTile(
                    title = "Accredited Training Centres",
                    subtitle = "Directory of approved clinical hospitals and supervisors",
                    icon = Icons.Default.Business,
                    iconBg = Color(0xFFE8F1F5),
                    iconTint = Color(0xFF07538E),
                    onClick = { navController.navigate(Screen.TrainingCentresScreen.route) }
                )

                FellowshipActionTile(
                    title = "Fellowship Guidelines",
                    subtitle = "Eligibility criteria, logbook rules, and accreditation curriculum",
                    icon = Icons.Default.Description,
                    iconBg = Color(0xFFF3E8FF),
                    iconTint = Color(0xFF7E22CE),
                    onClick = { navController.navigate(Screen.FellowshipGuidelinesScreen.route) }
                )

                FellowshipActionTile(
                    title = "Apply as a Fellow",
                    subtitle = "Candidate application with qualifications & preferred centers",
                    icon = Icons.Default.School,
                    iconBg = Color(0xFFE8F8F0),
                    iconTint = Color(0xFF1B8755),
                    onClick = { navController.navigate(Screen.ApplyAsFellowScreen.route) }
                )

                FellowshipActionTile(
                    title = "Apply as Training Centre",
                    subtitle = "Institutional accreditation for departments and hospitals",
                    icon = Icons.Default.HowToReg,
                    iconBg = Color(0xFFFFF7E6),
                    iconTint = Color(0xFFD97706),
                    onClick = { navController.navigate(Screen.ApplyAsTrainingCentreScreen.route) }
                )

                FellowshipActionTile(
                    title = "Fellow Clinical Logbook",
                    subtitle = "Manage cases, attendance verification, and research",
                    icon = Icons.Default.People,
                    iconBg = Color(0xFFEDE9FE),
                    iconTint = Color(0xFF6D28D9),
                    onClick = { navController.navigate(Screen.FellowDashboardScreen.route) }
                )

                FellowshipActionTile(
                    title = "Trainer Supervisor Desk",
                    subtitle = "Review cases, verify logs, and assess fellows",
                    icon = Icons.Default.SupervisorAccount,
                    iconBg = Color(0xFFFEF3C7),
                    iconTint = Color(0xFFB45309),
                    onClick = { navController.navigate(Screen.TrainerDashboardScreen.route) }
                )

                FellowshipActionTile(
                    title = "Fellowship Feedback",
                    subtitle = "Structured evaluation questionnaire (Part 1 & 2)",
                    icon = Icons.Default.Feedback,
                    iconBg = Color(0xFFFCE7F3),
                    iconTint = Color(0xFFBE185D),
                    onClick = { navController.navigate(Screen.FellowshipFeedbackScreen.route) }
                )
            }
        }

        // Open Programs
        item {
            Text(
                text = "Open Fellowship Programmes",
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
                        .height(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = AppTheme.colors.secondary)
                }
            }
        } else if (programs.isEmpty()) {
            item {
                SsplCard(modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No open fellowship cycles right now. Inquiries can be sent via Contact Us.",
                            style = AppTheme.typography.bodySmall.copy(color = AppTheme.colors.muted),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(programs) { prog ->
                SsplCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = prog.title,
                                style = AppTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = AppTheme.colors.heading
                                ),
                                modifier = Modifier.weight(1f)
                            )
                            SsplBadge(
                                text = "OPEN",
                                variant = SsplBadgeVariant.Success
                            )
                        }

                        prog.specialty?.let { spec ->
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Subspecialty: $spec",
                                style = AppTheme.typography.bodySmall.copy(
                                    color = AppTheme.colors.secondary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                        }

                        prog.duration?.let { dur ->
                            Text(
                                text = "Duration: $dur",
                                style = AppTheme.typography.bodySmall.copy(color = AppTheme.colors.muted)
                            )
                        }

                        prog.applicationDeadline?.let { dl ->
                            Text(
                                text = "Deadline: $dl",
                                style = AppTheme.typography.bodySmall.copy(
                                    color = AppTheme.colors.accent,
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        SsplButton(
                            text = "Apply for this Programme",
                            onClick = { navController.navigate(Screen.ApplyAsFellowScreen.route) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        item {
            ModernSupportCard(
                title = "Fellowship Committee Desk",
                subtitle = "Have questions regarding program eligibility, logbook quotas, or supervisor approval? Connect with the fellowship secretariat.",
                buttonText = "Contact Secretariat",
                onContactClick = { navController.navigate(Screen.ContactUsScreen.route) }
            )
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun StatTile(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = AppTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                color = AppTheme.colors.secondary
            )
        )
        Text(
            text = label,
            style = AppTheme.typography.labelSmall.copy(color = AppTheme.colors.muted)
        )
    }
}

@Composable
private fun FellowshipActionTile(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color,
    onClick: () -> Unit
) {
    SsplCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = AppTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = AppTheme.colors.heading
                    )
                )
                Text(
                    text = subtitle,
                    style = AppTheme.typography.bodySmall.copy(color = AppTheme.colors.muted)
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = AppTheme.colors.muted.copy(alpha = 0.5f),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

