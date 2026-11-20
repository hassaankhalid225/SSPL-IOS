package com.sspl.ui.portal

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CardMembership
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Slideshow
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.sspl.Screen
import com.sspl.core.auth.RoleUtils
import com.sspl.core.models.User
import com.sspl.core.models.UserMembershipDetails
import com.sspl.core.repositories.PortalRegistration
import com.sspl.designsystem.components.DecorativeRings
import com.sspl.designsystem.components.GlassPill
import com.sspl.designsystem.components.IconTile
import com.sspl.designsystem.components.ListRowCard
import com.sspl.designsystem.components.LocalShellActions
import com.sspl.designsystem.components.NavTile
import com.sspl.designsystem.components.QuickActionTile
import com.sspl.designsystem.components.SectionHeader
import com.sspl.designsystem.components.SkeletonBox
import com.sspl.designsystem.components.SkeletonListItem
import com.sspl.designsystem.components.SsplRefreshBox
import com.sspl.designsystem.components.StatTile
import com.sspl.designsystem.components.StatusBadge
import com.sspl.designsystem.theme.AppTheme
import com.sspl.utils.DisplayDates

private data class PortalAction(val icon: ImageVector, val label: String, val route: String)

/**
 * D1: Member Desk (web `/dashboard` overview).
 * Digital membership card → activity stats → quick actions → role desks → upcoming events → help.
 */
@Composable
fun PortalOverviewScreen(
    navController: NavController,
    viewModel: PortalOverviewViewModel = portalViewModel { PortalOverviewViewModel(it.get(), it.get()) }
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val shell = LocalShellActions.current
    val user = state.user

    if (shell.isGuest && user?.isGuest() != false) {
        PortalGuestGate(onSignIn = shell.onAvatar, modifier = Modifier.fillMaxSize())
        return
    }

    val go: (String) -> Unit = { navController.openPortal(it) }
    val isTrainer = RoleUtils.hasUserRole(user, "FELLOWSHIP_TRAINER")
    val isFellow = RoleUtils.hasUserRole(user, "FELLOWSHIP_FELLOW")

    val actions = listOf(
        PortalAction(Icons.Default.Event, "Browse events", Screen.EventsListScreen.route),
        PortalAction(Icons.AutoMirrored.Filled.Assignment, "My registrations", Screen.MyRegistrationsScreen.route),
        PortalAction(Icons.Default.EventAvailable, "Event portal", Screen.EventPortalScreen.route),
        PortalAction(Icons.Default.Send, "Submit abstract", Screen.AbstractSubmissionScreen.route),
        PortalAction(Icons.Default.Description, "My submissions", Screen.MySubmissionsScreen.route),
        PortalAction(Icons.Default.Slideshow, "Presentations", Screen.PresentationsScreen.route),
        PortalAction(Icons.Default.WorkspacePremium, "Certificates", Screen.MyCertificatesScreen.route),
        PortalAction(Icons.Default.CardMembership, "Membership", Screen.MembershipScreen.route)
    )

    SsplRefreshBox(
        isRefreshing = state.isRefreshing,
        onRefresh = viewModel::refresh,
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item(key = "greeting") {
                Column {
                    Text("MEMBER DESK", style = AppTheme.typography.overline)
                    Spacer(Modifier.height(2.dp))
                    Text(
                        "Welcome back, ${user?.firstName?.takeIf { it.isNotBlank() } ?: "there"}",
                        style = AppTheme.typography.headlineMedium,
                        maxLines = 1, overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        "Manage your society profile, events, abstracts and certificates from one place.",
                        style = AppTheme.typography.bodySmall
                    )
                }
            }

            item(key = "card") {
                MembershipCard(
                    user = user,
                    membership = state.membership,
                    onOpen = { go(Screen.MembershipScreen.route) },
                    onProfile = { go(Screen.ProfileScreen.route) }
                )
            }

            state.partialError?.let { msg ->
                item(key = "partial") { PortalNotice(msg, NoticeTone.Warning) }
            }

            item(key = "stats") {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    val c = AppTheme.colors
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        CountTile(state, PortalOverviewViewModel.KEY_REGS, state.registrations, "Registrations",
                            Icons.AutoMirrored.Filled.Assignment, c.primary, Modifier.weight(1f)) { go(Screen.MyRegistrationsScreen.route) }
                        CountTile(state, PortalOverviewViewModel.KEY_CERTS, state.certificates, "Certificates",
                            Icons.Default.WorkspacePremium, c.success, Modifier.weight(1f)) { go(Screen.MyCertificatesScreen.route) }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        CountTile(state, PortalOverviewViewModel.KEY_SUBS, state.submissions, "Abstracts submitted",
                            Icons.Default.Description, c.accent, Modifier.weight(1f)) { go(Screen.MySubmissionsScreen.route) }
                        CountTile(state, PortalOverviewViewModel.KEY_PRES, state.presentations, "Presentations",
                            Icons.Default.Slideshow, c.warning, Modifier.weight(1f)) { go(Screen.PresentationsScreen.route) }
                    }
                }
            }

            item(key = "actions") {
                Column {
                    SectionHeader(title = "Continue where you left off")
                    Spacer(Modifier.height(6.dp))
                    actions.chunked(4).forEach { row ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            row.forEach { a ->
                                QuickActionTile(
                                    icon = a.icon, label = a.label, onClick = { go(a.route) },
                                    modifier = Modifier.weight(1f).heightIn(min = 96.dp)
                                )
                            }
                        }
                    }
                }
            }

            if (isTrainer) {
                item(key = "trainer") {
                    RoleDeskCard(
                        eyebrow = "Dual access · Fellowship trainer & member",
                        title = "Training centre / master trainer desk",
                        body = "Manage assigned trainees, case approvals, attendance verification, assessments and research.",
                        cta = "Open trainer desk",
                        icon = Icons.Default.School,
                        onClick = { go(Screen.TrainerDashboardScreen.route) }
                    )
                }
            }

            item(key = "fellowship") {
                NavTile(
                    icon = Icons.Default.School,
                    title = if (isFellow) "My fellowship workspace" else "Fellowship",
                    subtitle = if (isFellow) "Logbook, cases, attendance and feedback" else "Open the mini-fellowship workspace if you are enrolled",
                    onClick = { go(if (isFellow) Screen.FellowDashboardScreen.route else Screen.FellowshipHubScreen.route) },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item(key = "upcomingHeader") {
                SectionHeader(
                    title = "Upcoming for you",
                    actionText = "Event portal",
                    onActionClick = { go(Screen.EventPortalScreen.route) },
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
            when {
                PortalOverviewViewModel.KEY_REGS in state.loading && state.registrations == null -> items(2, key = { "upSk$it" }) { SkeletonListItem() }
                state.upcoming.isEmpty() -> item(key = "upEmpty") {
                    ListRowCard(
                        title = "No upcoming events yet",
                        subtitle = "Register for a conference, workshop or monthly meeting to see it here.",
                        leading = { IconTile(Icons.Default.CalendarMonth) },
                        trailing = {
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = AppTheme.colors.primary)
                        },
                        onClick = { go(Screen.EventsListScreen.route) }
                    )
                }
                else -> items(state.upcoming, key = { "up${it.id}" }) { reg ->
                    UpcomingRow(reg, onClick = { openEventSessions(navController, reg) })
                }
            }

            item(key = "help") {
                NavTile(
                    icon = Icons.Default.SupportAgent,
                    title = "Need help with your portal?",
                    subtitle = "Membership verification, payment proofs or abstract queries",
                    onClick = { go(Screen.ContactUsScreen.route) },
                    modifier = Modifier.fillMaxWidth().padding(top = 6.dp)
                )
            }
        }
    }
}

private fun Int?.display(): String = this?.toString() ?: "—"

@Composable
private fun CountTile(
    state: PortalOverviewUiState,
    key: String,
    value: Int?,
    label: String,
    icon: ImageVector,
    tint: Color,
    modifier: Modifier,
    onClick: () -> Unit
) {
    if (key in state.loading && value == null) {
        SkeletonBox(modifier.height(104.dp), RoundedCornerShape(18.dp))
    } else {
        StatTile(value = value.display(), label = label, icon = icon, tint = tint, onClick = onClick, modifier = modifier)
    }
}

/** The "digital membership card". */
@Composable
private fun MembershipCard(
    user: User?,
    membership: UserMembershipDetails?,
    onOpen: () -> Unit,
    onProfile: () -> Unit
) {
    val name = listOfNotNull(user?.prefix?.takeIf { it.isNotBlank() }, RoleUtils.getUserDisplayName(user)).joinToString(" ")
    val label = RoleUtils.getMembershipLabel(user)
    val status = user?.membershipStatus?.replace('_', ' ')
    val isLife = RoleUtils.isLifeMember(user)
    val memberNo = membership?.membershipNumber ?: user?.profile?.orgNumber
    val validity = when {
        isLife -> "Lifetime"
        !membership?.expiresAt.isNullOrBlank() -> DisplayDates.medium(membership?.expiresAt).ifBlank { membership?.expiresAt.orEmpty() }
        else -> "—"
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(AppTheme.shapes.card)
            .background(AppTheme.colors.headerGradient)
            .clickable(onClick = onOpen)
    ) {
        DecorativeRings(Modifier.matchParentSize())
        Column(Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                GlassPill("SSPL MEMBERSHIP", icon = Icons.Default.Verified)
                Spacer(Modifier.weight(1f))
                if (status != null) StatusOnGradient(status)
            }
            Spacer(Modifier.height(16.dp))
            Text(
                name, style = AppTheme.typography.titleLarge.copy(color = Color.White),
                maxLines = 1, overflow = TextOverflow.Ellipsis
            )
            Text(
                label, style = AppTheme.typography.bodyMedium.copy(color = Color.White.copy(alpha = 0.78f)),
                maxLines = 2, overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                CardFact("Member no.", memberNo ?: "—", Modifier.weight(1f))
                CardFact("PMDC", user?.pmdc?.takeIf { it.isNotBlank() } ?: "—", Modifier.weight(1f))
                CardFact("Valid until", validity, Modifier.weight(1f))
            }
            Spacer(Modifier.height(14.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(Color.White.copy(alpha = 0.16f))
                        .clickable(onClick = onProfile)
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Update profile", style = AppTheme.typography.labelMedium.copy(color = Color.White, fontWeight = FontWeight.SemiBold))
                }
                Spacer(Modifier.weight(1f))
                Text("Membership details", style = AppTheme.typography.labelMedium.copy(color = Color.White.copy(alpha = 0.85f)))
                Spacer(Modifier.width(4.dp))
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
            }
        }
    }
}

@Composable
private fun CardFact(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier) {
        Text(label.uppercase(), style = AppTheme.typography.labelSmall.copy(color = Color.White.copy(alpha = 0.6f)))
        Text(
            value, style = AppTheme.typography.titleSmall.copy(color = Color.White),
            maxLines = 1, overflow = TextOverflow.Ellipsis
        )
    }
}

/** Status pill readable on the navy gradient. */
@Composable
fun StatusOnGradient(status: String) {
    val c = AppTheme.colors
    val dot = when (status.uppercase()) {
        "ACTIVE", "APPROVED" -> c.success
        "PENDING" -> c.warning
        else -> c.error
    }
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(Color.White)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(7.dp).clip(RoundedCornerShape(50)).background(dot))
        Spacer(Modifier.width(5.dp))
        Text(
            status.uppercase(),
            style = AppTheme.typography.labelSmall.copy(color = c.gradientMid, fontWeight = FontWeight.Bold)
        )
    }
}

@Composable
private fun RoleDeskCard(
    eyebrow: String,
    title: String,
    body: String,
    cta: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    val c = AppTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(AppTheme.shapes.card)
            .background(c.surfaceTint)
            .border(1.dp, c.primary.copy(alpha = 0.25f), AppTheme.shapes.card)
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconTile(icon, size = 46.dp, iconSize = 22.dp, background = c.surface)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(eyebrow.uppercase(), style = AppTheme.typography.overline, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(title, style = AppTheme.typography.titleSmall)
            Text(body, style = AppTheme.typography.bodySmall, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(6.dp))
            Text(cta, style = AppTheme.typography.labelMedium.copy(color = c.primary, fontWeight = FontWeight.SemiBold))
        }
        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = c.primary, modifier = Modifier.size(18.dp))
    }
}

/** Date tile + title row for registrations. */
@Composable
fun UpcomingRow(reg: PortalRegistration, onClick: () -> Unit, footer: (@Composable () -> Unit)? = null) {
    ListRowCard(
        title = reg.title,
        subtitle = listOfNotNull(
            DisplayDates.range(reg.startDate, reg.endDate).takeIf { it.isNotBlank() },
            reg.venue
        ).joinToString(" · ").ifBlank { null },
        meta = listOfNotNull(eventTypeLabel(reg.type), reg.roleName?.let { "Role: $it" }).joinToString(" · "),
        leading = { DateTile(reg.startDate) },
        trailing = {
            val relative = DisplayDates.relative(reg.startDate)
            if (relative.isNotBlank()) StatusBadge(relative, variant = com.sspl.designsystem.components.BadgeVariant.INFO)
        },
        onClick = onClick,
        footer = footer?.let { f -> { f() } }
    )
}

@Composable
fun DateTile(iso: String?) {
    val c = AppTheme.colors
    Column(
        modifier = Modifier
            .size(48.dp)
            .clip(RoundedCornerShape(13.dp))
            .background(c.primaryContainer),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(DisplayDates.day(iso), style = AppTheme.typography.titleMedium.copy(color = c.onPrimaryContainer, fontWeight = FontWeight.Bold))
        Text(DisplayDates.monthShort(iso).uppercase(), style = AppTheme.typography.labelSmall.copy(color = c.onPrimaryContainer))
    }
}
