package com.sspl.ui.portal

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CardMembership
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.HowToReg
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.sspl.Screen
import com.sspl.core.auth.isFellowshipTrainer
import com.sspl.core.auth.isLifeMember
import com.sspl.core.auth.isRegularMember
import com.sspl.designsystem.components.SsplBadge
import com.sspl.designsystem.components.SsplBadgeVariant
import com.sspl.designsystem.components.SsplButton
import com.sspl.designsystem.components.SsplButtonVariant
import com.sspl.designsystem.components.SsplCard
import com.sspl.designsystem.theme.AppTheme
import com.sspl.session.UserSession
import org.koin.compose.koinInject

/**
 * D1: Member Workspace / My Portal Overview
 */
@Composable
fun PortalOverviewScreen(
    navController: NavController,
    userSession: UserSession = koinInject()
) {
    val user by userSession.userFlow.collectAsState()

    val name = listOfNotNull(user?.prefix, user?.firstName, user?.lastName)
        .joinToString(" ")
        .ifBlank { "Member" }

    val membershipLabel = when {
        user?.isLifeMember() == true -> "Life Member"
        user?.isRegularMember() == true -> "Regular Member"
        user?.membershipStatus == "ACTIVE" -> "Active Member"
        user?.membershipStatus == "PENDING" -> "Application Pending"
        else -> "Registered User"
    }

    val isTrainer = user?.isFellowshipTrainer() == true

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(AppTheme.colors.background)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(16.dp))

            // Header Eyebrow & Greeting
            Text(
                text = "MEMBER DESK",
                style = AppTheme.typography.labelSmall.copy(
                    color = AppTheme.colors.secondary,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Welcome back, ${user?.firstName ?: "Doctor"}",
                style = AppTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = AppTheme.colors.heading
                )
            )
            Text(
                text = "Manage your society profile, events, abstracts, and certifications.",
                style = AppTheme.typography.bodySmall.copy(
                    color = AppTheme.colors.muted
                )
            )
        }

        // Trainer Dual Access Banner if applicable
        if (isTrainer) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = AppTheme.colors.primary
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.School,
                                contentDescription = null,
                                tint = AppTheme.colors.accent,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "TRAINER ACCESS",
                                style = AppTheme.typography.labelSmall.copy(
                                    color = AppTheme.colors.accent,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Fellowship Supervisor Desk",
                            style = AppTheme.typography.titleMedium.copy(
                                color = AppTheme.colors.onPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = "Review submitted cases, log attendance, and conduct assessments.",
                            style = AppTheme.typography.bodySmall.copy(
                                color = AppTheme.colors.onPrimary.copy(alpha = 0.8f)
                            )
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        SsplButton(
                            text = "Open Trainer Dashboard",
                            onClick = { navController.navigate(Screen.TrainerDashboardScreen.route) },
                            variant = SsplButtonVariant.Outline,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        // Member Status Card
        item {
            SsplCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "SIGNED IN AS",
                                style = AppTheme.typography.labelSmall.copy(
                                    color = AppTheme.colors.muted,
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                            Text(
                                text = name,
                                style = AppTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = AppTheme.colors.heading
                                )
                            )
                        }

                        val badgeVariant = when {
                            user?.isLifeMember() == true -> SsplBadgeVariant.Success
                            user?.isRegularMember() == true -> SsplBadgeVariant.Info
                            user?.membershipStatus == "ACTIVE" -> SsplBadgeVariant.Success
                            user?.membershipStatus == "PENDING" -> SsplBadgeVariant.Warning
                            else -> SsplBadgeVariant.Neutral
                        }

                        SsplBadge(
                            text = membershipLabel,
                            variant = badgeVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(AppTheme.colors.border)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Email: ${user?.account?.email ?: user?.email ?: "Not set"}",
                                style = AppTheme.typography.bodySmall.copy(color = AppTheme.colors.muted)
                            )
                            if (!user?.phone.isNullOrBlank()) {
                                Text(
                                    text = "Phone: ${user?.phone}",
                                    style = AppTheme.typography.bodySmall.copy(color = AppTheme.colors.muted)
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.clickable {
                                navController.navigate(Screen.ProfileScreen.route)
                            },
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "Edit Profile",
                                style = AppTheme.typography.labelMedium.copy(
                                    color = AppTheme.colors.secondary,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = AppTheme.colors.secondary,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }
        }

        // Quick Actions Section Title
        item {
            Text(
                text = "Workspace Services",
                style = AppTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = AppTheme.colors.heading
                )
            )
        }

        // Quick Action Items
        item {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                PortalQuickActionTile(
                    title = "Browse Events & Conferences",
                    subtitle = "Register for conferences, workshops, and monthly meetings",
                    icon = Icons.Default.DateRange,
                    iconBg = Color(0xFFE8F1F5),
                    iconTint = Color(0xFF07538E),
                    onClick = { navController.navigate(Screen.ScientificProgramsScreen.route) }
                )

                PortalQuickActionTile(
                    title = "My Registrations",
                    subtitle = "Entry passes, QR badges, and payment statuses",
                    icon = Icons.Default.HowToReg,
                    iconBg = Color(0xFFE8F8F0),
                    iconTint = Color(0xFF1B8755),
                    onClick = { navController.navigate(Screen.MyRegistrationsScreen.route) }
                )

                PortalQuickActionTile(
                    title = "Submit an Abstract",
                    subtitle = "Send scientific work for upcoming programmes",
                    icon = Icons.Default.Send,
                    iconBg = Color(0xFFFFF7E6),
                    iconTint = Color(0xFFD97706),
                    onClick = { navController.navigate(Screen.AbstractSubmissionScreen.route) }
                )

                PortalQuickActionTile(
                    title = "My Abstract Submissions",
                    subtitle = "Track review status of your scientific papers",
                    icon = Icons.Default.Description,
                    iconBg = Color(0xFFF3E8FF),
                    iconTint = Color(0xFF7E22CE),
                    onClick = { navController.navigate(Screen.MySubmissionsScreen.route) }
                )

                PortalQuickActionTile(
                    title = "Presentation Uploads",
                    subtitle = "Submit slide decks for accepted abstracts",
                    icon = Icons.Default.Star,
                    iconBg = Color(0xFFFEF3C7),
                    iconTint = Color(0xFFB45309),
                    onClick = { navController.navigate(Screen.PresentationsScreen.route) }
                )

                PortalQuickActionTile(
                    title = "Membership & Renewal",
                    subtitle = "View benefits, renew, or upgrade to Life Member",
                    icon = Icons.Default.CardMembership,
                    iconBg = Color(0xFFFCE7F3),
                    iconTint = Color(0xFFBE185D),
                    onClick = { navController.navigate(Screen.MembershipScreen.route) }
                )

                PortalQuickActionTile(
                    title = "My Certificates",
                    subtitle = "Download, print, and verify earned credentials",
                    icon = Icons.Default.WorkspacePremium,
                    iconBg = Color(0xFFEDE9FE),
                    iconTint = Color(0xFF6D28D9),
                    onClick = { navController.navigate(Screen.MyCertificatesScreen.route) }
                )

                PortalQuickActionTile(
                    title = "Fellowship Desk",
                    subtitle = "Logbook, cases, attendance, and program guidelines",
                    icon = Icons.Default.School,
                    iconBg = Color(0xFFE0F2FE),
                    iconTint = Color(0xFF0369A1),
                    onClick = { navController.navigate(Screen.FellowshipHubScreen.route) }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun PortalQuickActionTile(
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
                    .size(44.dp)
                    .clip(RoundedCornerShape(10.dp))
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

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = AppTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = AppTheme.colors.heading
                    )
                )
                Text(
                    text = subtitle,
                    style = AppTheme.typography.bodySmall.copy(
                        color = AppTheme.colors.muted
                    )
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

