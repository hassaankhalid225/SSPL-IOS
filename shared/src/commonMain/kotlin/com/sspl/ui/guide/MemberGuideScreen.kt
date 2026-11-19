package com.sspl.ui.guide

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.sspl.Screen
import com.sspl.designsystem.components.BadgeVariant
import com.sspl.designsystem.components.SecondaryButton
import com.sspl.designsystem.components.SsplBadge
import com.sspl.designsystem.theme.AppTheme

data class GuideStepDetail(
    val stepTitle: String,
    val stepBody: String
)

data class GuideTopic(
    val number: String,
    val title: String,
    val summary: String,
    val steps: List<GuideStepDetail>,
    val ctaLabel: String? = null,
    val ctaTarget: String? = null
)

private val MEMBER_GUIDE_TOPICS = listOf(
    GuideTopic(
        number = "01",
        title = "How to become a member",
        summary = "Create an account, select an eligible membership plan (regular or life), and submit your professional credentials.",
        steps = listOf(
            GuideStepDetail("Select a Plan", "Open Membership Plans from the drawer or home screen. Choose between Regular and Life Membership."),
            GuideStepDetail("Complete Profile Form", "Fill in personal details, PMDC registration number, designation, institutional affiliation, and city."),
            GuideStepDetail("Payment & Approval", "Submit required membership fees or verification documents. Track application status under Profile.")
        ),
        ctaLabel = "Explore Membership",
        ctaTarget = Screen.ProfileScreen.route
    ),
    GuideTopic(
        number = "02",
        title = "Login and member desk",
        summary = "Access your registrations, mark attendance at events, submit feedback, and download accredited certificates.",
        steps = listOf(
            GuideStepDetail("Member Login", "Use the login button on the top bar or drawer with your registered email and password."),
            GuideStepDetail("Forgot Password", "Use the reset password flow if credentials are misplaced; a verification OTP will be sent to your email."),
            GuideStepDetail("Member Desk", "Manage active conference registrations, case logbooks, and surgical guidelines.")
        ),
        ctaLabel = "Member Login",
        ctaTarget = Screen.SignInScreen.route
    ),
    GuideTopic(
        number = "03",
        title = "How to register for conferences & workshops",
        summary = "Monthly clinical meetings, annual conferences, and hands-on surgical workshops are open for registration.",
        steps = listOf(
            GuideStepDetail("Browse Events", "Open Scientific Programs from the home or events tab to view scheduled sessions."),
            GuideStepDetail("View Details & Fee", "Review the faculty, venue, CME credit hours, and designated role registration fees."),
            GuideStepDetail("Register Now", "Select your role (Consultant, Trainee, Delegate) and complete the secure payment.")
        ),
        ctaLabel = "Browse Conferences",
        ctaTarget = Screen.ScientificProgramsScreen.route
    ),
    GuideTopic(
        number = "04",
        title = "Conference and pre-conference workshops",
        summary = "Workshops require separate registrations and limited seating compared to main conference registrations.",
        steps = listOf(
            GuideStepDetail("Separate Workshop Registration", "Ensure you register individually for each workshop you plan to attend."),
            GuideStepDetail("Seat Confirmation", "Workshops have strict seat limits; early registration guarantees your workstation.")
        ),
        ctaLabel = "View Workshops",
        ctaTarget = Screen.WorkshopsScreen.route
    ),
    GuideTopic(
        number = "05",
        title = "How to mark attendance",
        summary = "Attendance validation is available during the active event window for confirmed delegates.",
        steps = listOf(
            GuideStepDetail("Active Window", "Open the session in your app when the live session starts."),
            GuideStepDetail("Location / Code Verification", "Verify your presence via GPS location or the session QR code displayed in the hall."),
            GuideStepDetail("Recorded Confirmation", "A green confirmation badge verifies your attendance for CME credits.")
        )
    ),
    GuideTopic(
        number = "06",
        title = "Submitting session & event feedback",
        summary = "Feedback unlocks after attendance is recorded and is mandatory for certificate generation.",
        steps = listOf(
            GuideStepDetail("Access Feedback Form", "Click the feedback button on the attended session card."),
            GuideStepDetail("Rate & Comment", "Rate scientific content, speaker delivery, and venue organization."),
            GuideStepDetail("Certificate Unlock", "Completing the feedback form instantly unlocks your digital CME certificate.")
        )
    ),
    GuideTopic(
        number = "07",
        title = "Certificates — generation & verification",
        summary = "Digital certificates feature secure serial numbers verifiable by institutions and regulatory bodies.",
        steps = listOf(
            GuideStepDetail("Download Certificate", "Access your certificate PDF anytime from your member portal."),
            GuideStepDetail("Serial Verification", "Employers and councils can verify authenticity instantly on the public Verify Certificate page.")
        ),
        ctaLabel = "Verify Certificate",
        ctaTarget = Screen.VerifyCertificateScreen.route
    )
)

@Composable
fun MemberGuideScreen(navController: NavController) {
    val expandedStates = remember { mutableStateMapOf<String, Boolean>() }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(AppTheme.colors.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Hero Header
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = AppTheme.colors.primary)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "HOW-TO GUIDE",
                        style = AppTheme.typography.labelSmall,
                        color = AppTheme.colors.secondary,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.3.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Member & Events Guide",
                        style = AppTheme.typography.headlineMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Step-by-step instructions for membership, event registration, marking attendance, and earning CME certificates.",
                        style = AppTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.88f)
                    )
                }
            }
        }

        // Guide Topics
        items(MEMBER_GUIDE_TOPICS) { topic ->
            val isExpanded = expandedStates[topic.number] ?: false
            GuideTopicCard(
                topic = topic,
                isExpanded = isExpanded,
                onToggle = { expandedStates[topic.number] = !isExpanded },
                onCtaClick = {
                    topic.ctaTarget?.let { route ->
                        navController.navigate(route)
                    }
                }
            )
        }
    }
}

@Composable
private fun GuideTopicCard(
    topic: GuideTopic,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    onCtaClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable { onToggle() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = AppTheme.colors.surface),
        border = BorderStroke(
            1.dp,
            if (isExpanded) AppTheme.colors.primary.copy(alpha = 0.4f) else AppTheme.colors.border
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(
                            color = if (isExpanded) AppTheme.colors.primary else AppTheme.colors.primary.copy(alpha = 0.1f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = topic.number,
                        style = AppTheme.typography.labelMedium,
                        color = if (isExpanded) Color.White else AppTheme.colors.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = topic.title,
                        style = AppTheme.typography.titleMedium,
                        color = AppTheme.colors.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                }
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = if (isExpanded) AppTheme.colors.primary else AppTheme.colors.textMuted
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = topic.summary,
                style = AppTheme.typography.bodyMedium,
                color = AppTheme.colors.onSurface.copy(alpha = 0.82f),
                lineHeight = 20.sp
            )

            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column {
                    Spacer(modifier = Modifier.height(14.dp))
                    topic.steps.forEachIndexed { index, step ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .background(AppTheme.colors.accent.copy(alpha = 0.15f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${index + 1}",
                                    style = AppTheme.typography.labelSmall,
                                    color = AppTheme.colors.accent,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = step.stepTitle,
                                    style = AppTheme.typography.titleSmall,
                                    color = AppTheme.colors.onSurface,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = step.stepBody,
                                    style = AppTheme.typography.bodySmall,
                                    color = AppTheme.colors.textMuted,
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }

                    if (topic.ctaLabel != null) {
                        Spacer(modifier = Modifier.height(14.dp))
                        SecondaryButton(
                            text = topic.ctaLabel,
                            onClick = onCtaClick,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

