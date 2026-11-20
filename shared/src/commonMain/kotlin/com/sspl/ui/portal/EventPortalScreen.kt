package com.sspl.ui.portal

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material.icons.filled.HowToReg
import androidx.compose.material.icons.filled.RateReview
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.sspl.Screen
import com.sspl.core.repositories.PortalRegistration
import com.sspl.designsystem.components.EmptyState
import com.sspl.designsystem.components.ErrorState
import com.sspl.designsystem.components.LoadState
import com.sspl.designsystem.components.LocalShellActions
import com.sspl.designsystem.components.NavTile
import com.sspl.designsystem.components.OutlineButton
import com.sspl.designsystem.components.PrimaryButton
import com.sspl.designsystem.components.SegmentedTabs
import com.sspl.designsystem.components.SkeletonBox
import com.sspl.designsystem.components.SkeletonList
import com.sspl.designsystem.components.SsplRefreshBox
import com.sspl.designsystem.theme.AppTheme

/**
 * D5: Event portal (web `/dashboard/event-portal`). Confirmed registrations; each opens the
 * event's sessions where attendance (GPS / selfie), session feedback and certificates live.
 */
@Composable
fun EventPortalScreen(
    navController: NavController,
    viewModel: EventPortalViewModel = portalViewModel { EventPortalViewModel(it.get()) }
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val shell = LocalShellActions.current
    if (shell.isGuest) {
        PortalGuestGate(onSignIn = shell.onAvatar, modifier = Modifier.fillMaxSize())
        return
    }
    val go: (String) -> Unit = { navController.openPortal(it) }

    SsplRefreshBox(
        isRefreshing = state.isRefreshing,
        onRefresh = viewModel::refresh,
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val data = (state.data as? LoadState.Ready)?.data
            item(key = "hero") {
                PortalHero(
                    eyebrow = "Event portal",
                    title = "Attendance, feedback, certificate",
                    summary = "For every event you're confirmed in: mark attendance at the venue, share session feedback, then download your certificate.",
                    icon = Icons.Default.EventAvailable,
                    stats = if (data != null) listOf(
                        "${data.upcoming.size}" to "Upcoming",
                        "${data.past.size}" to "Attended / past"
                    ) else emptyList(),
                    extra = {
                        Spacer(Modifier.height(14.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                            StepChip(1, "Attendance", Icons.Default.HowToReg, Modifier.weight(1f))
                            StepChip(2, "Feedback", Icons.Default.RateReview, Modifier.weight(1f))
                            StepChip(3, "Certificate", Icons.Default.WorkspacePremium, Modifier.weight(1f))
                        }
                    }
                )
            }

            when (val s = state.data) {
                is LoadState.Loading -> {
                    item(key = "tabsSk") { SkeletonBox(Modifier.fillMaxWidth().height(44.dp), AppTheme.shapes.chip) }
                    item(key = "listSk") { SkeletonList(count = 3, contentPadding = 0.dp) }
                }
                is LoadState.Error -> item(key = "err") {
                    ErrorState(title = "Events didn't load", message = s.message, onRetry = viewModel::retry)
                }
                is LoadState.Ready -> {
                    val d = s.data
                    if (d.pendingCount > 0) {
                        item(key = "pending") {
                            PortalNotice(
                                message = "${d.pendingCount} registration${if (d.pendingCount > 1) "s are" else " is"} awaiting payment or approval. Complete it in My registrations to unlock attendance.",
                                tone = NoticeTone.Warning
                            )
                        }
                    }
                    if (d.isEmpty) {
                        item(key = "empty") {
                            EmptyState(
                                title = "No confirmed registrations",
                                message = "If your registration is pending, open My registrations to complete payment or check approval status.",
                                icon = Icons.Default.EventBusy,
                                actionText = "Open my registrations",
                                onActionClick = { go(Screen.MyRegistrationsScreen.route) }
                            )
                        }
                    } else {
                        item(key = "tabs") {
                            SegmentedTabs(
                                options = listOf("Upcoming", "Past"),
                                selectedIndex = state.tab,
                                onSelect = viewModel::selectTab,
                                counts = listOf(d.upcoming.size, d.past.size)
                            )
                        }
                        val list = if (state.tab == 0) d.upcoming else d.past
                        if (list.isEmpty()) {
                            item(key = "tabEmpty") {
                                EmptyState(
                                    title = if (state.tab == 0) "Nothing upcoming" else "No past events",
                                    message = if (state.tab == 0) "Browse events and register for the next programme." else "Events you attended will appear here with their certificates.",
                                    icon = Icons.Default.EventAvailable,
                                    actionText = if (state.tab == 0) "Browse events" else null,
                                    onActionClick = if (state.tab == 0) ({ go(Screen.EventsListScreen.route) }) else null
                                )
                            }
                        } else {
                            items(list, key = { it.id }) { reg -> PortalEventCard(reg, navController) }
                        }
                    }
                }
            }

            item(key = "regs") {
                NavTile(
                    icon = Icons.AutoMirrored.Filled.Assignment,
                    title = "My registrations",
                    subtitle = "Payments, approvals and entry passes",
                    onClick = { go(Screen.MyRegistrationsScreen.route) },
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun StepChip(n: Int, label: String, icon: ImageVector, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(AppTheme.shapes.small)
            .background(Color.White.copy(alpha = 0.12f))
            .padding(vertical = 8.dp, horizontal = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            Modifier.size(26.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.18f)),
            contentAlignment = Alignment.Center
        ) { Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp)) }
        Spacer(Modifier.height(4.dp))
        Text(
            "$n. $label",
            style = AppTheme.typography.labelSmall.copy(color = Color.White),
            textAlign = TextAlign.Center, maxLines = 1
        )
    }
}

@Composable
private fun PortalEventCard(reg: PortalRegistration, navController: NavController) {
    UpcomingRow(
        reg = reg,
        onClick = { openEventSessions(navController, reg) },
        footer = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    reg.registrationNumber?.let { MetaChip("Reg. $it") }
                    MetaChip(if (reg.approvalStatus.equals("APPROVED", true)) "Approved" else "Confirmed")
                }
                if (reg.needsPayment) {
                    PortalNotice(
                        "Payment is pending for this registration. Complete payment from My registrations before marking attendance.",
                        NoticeTone.Warning
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    PrimaryButton(
                        text = "Sessions & attendance",
                        onClick = { openEventSessions(navController, reg) },
                        enabled = reg.conferenceId != null,
                        modifier = Modifier.weight(1.4f)
                    )
                    OutlineButton(
                        text = "Certificates",
                        onClick = { navController.openPortal(Screen.MyCertificatesScreen.route) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    )
}
