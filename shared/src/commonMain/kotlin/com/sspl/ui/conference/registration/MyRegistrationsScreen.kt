package com.sspl.ui.conference.registration

import com.sspl.designsystem.icons.SsplIcons

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.sspl.Screen
import com.sspl.core.models.Registration
import com.sspl.designsystem.components.BadgeVariant
import com.sspl.designsystem.components.ChipRow
import com.sspl.designsystem.components.DecorativeRings
import com.sspl.designsystem.components.EmptyState
import com.sspl.designsystem.components.ErrorState
import com.sspl.designsystem.components.GlassPill
import com.sspl.designsystem.components.IconTile
import com.sspl.designsystem.components.ListRowCard
import com.sspl.designsystem.components.PrimaryButton
import com.sspl.designsystem.components.SecondaryButton
import com.sspl.designsystem.components.SkeletonList
import com.sspl.designsystem.components.SsplRefreshBox
import com.sspl.designsystem.components.StatusBadge
import com.sspl.designsystem.theme.AppTheme
import com.sspl.platform.shared.UrlOpener
import com.sspl.session.UserSession
import com.sspl.utils.DisplayDates
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

private data class RegStatus(val label: String, val variant: BadgeVariant, val note: String?)

private fun Registration.uiStatus(): RegStatus = when {
    isLocked -> RegStatus("Locked", BadgeVariant.NEUTRAL, null)
    isConfirmed -> RegStatus("Approved", BadgeVariant.SUCCESS, "Approved — Event Portal features are available.")
    approvalStatus.equals("REJECTED", ignoreCase = true) || paymentStatus.equals("FAILED", ignoreCase = true) && !needsPayment ->
        RegStatus("Rejected", BadgeVariant.ERROR, null)
    needsPayment -> RegStatus(if (paymentStatus.equals("FAILED", true)) "Payment failed" else "Pending payment", BadgeVariant.WARNING, "Complete payment to confirm your seat.")
    feeAmount == 0.0 || isPendingApproval -> RegStatus("Pending approval", BadgeVariant.WARNING, "Submitted. Awaiting administrator review and approval.")
    else -> RegStatus(paymentStatus.lowercase().replaceFirstChar { it.uppercase() }, BadgeVariant.NEUTRAL, null)
}

/** D4: My registrations — filter, entry badge, retry payment. */
@Composable
fun MyRegistrationsScreen(
    conferenceId: Long = 0L,
    navController: NavController,
    viewModel: MyRegistrationsViewModel = koinViewModel(),
    userSession: UserSession = koinInject(),
    urlOpener: UrlOpener = koinInject()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val user by userSession.userFlow.collectAsStateWithLifecycle()
    var badgeFor by remember { mutableStateOf<Registration?>(null) }

    LaunchedEffect(Unit) {
        viewModel.events.collect { ev ->
            when (ev) {
                is MyRegistrationsEvent.OpenPayment -> {
                    urlOpener.openUrl(ev.url)
                    navController.currentBackStackEntry?.savedStateHandle?.apply {
                        set("awaitingTransactionId", ev.transactionId)
                        set("registrationId", ev.registrationId)
                    }
                    navController.navigate(Screen.PaymentStatusScreen.route) { launchSingleTop = true }
                }
            }
        }
    }

    SsplRefreshBox(
        isRefreshing = state.isRefreshing,
        onRefresh = viewModel::refresh,
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 28.dp)) {
            item(key = "head") {
                Column(Modifier.padding(start = 16.dp, end = 16.dp, top = 14.dp)) {
                    Text("PARTICIPATION", style = AppTheme.typography.overline)
                    Text("My registrations", style = AppTheme.typography.headlineMedium)
                    Text("Payment status, entry badge and approvals in one place.", style = AppTheme.typography.bodySmall)
                }
            }
            val data = state.data
            if (data != null && !data.canRegisterConference && data.activeConferenceRegistration != null) {
                item(key = "limit") {
                    ListRowCard(
                        title = "One conference at a time",
                        subtitle = "You're registered for ${data.activeConferenceRegistration?.conference?.title ?: "a conference"}. Complete or cancel it to register for another.",
                        leading = { IconTile(SsplIcons.Warning, tint = AppTheme.colors.warning, background = AppTheme.colors.warningContainer) },
                        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 12.dp)
                    )
                }
            }
            if (data != null) {
                item(key = "filters") {
                    ChipRow(
                        options = RegistrationFilter.entries.mapIndexed { i, f -> "${f.label} · ${state.counts.getOrElse(i) { 0 }}" },
                        selected = "${state.filter.label} · ${state.counts.getOrElse(state.filter.ordinal) { 0 }}",
                        onSelect = { label -> RegistrationFilter.entries.firstOrNull { label.startsWith(it.label + " ·") }?.let(viewModel::setFilter) },
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                }
            }
            state.actionError?.let { err ->
                item(key = "actionError") {
                    ListRowCard(
                        title = "Payment couldn't start",
                        subtitle = err,
                        leading = { IconTile(SsplIcons.ErrorOutline, tint = AppTheme.colors.error, background = AppTheme.colors.errorContainer) },
                        trailing = {
                            IconButton(onClick = viewModel::clearActionError) {
                                Icon(SsplIcons.Close, contentDescription = "Dismiss", tint = AppTheme.colors.muted)
                            }
                        },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                }
            }
            when {
                state.isLoading -> item(key = "sk") { SkeletonList(count = 4) }
                state.error != null && data == null -> item(key = "err") {
                    ErrorState(message = state.error ?: "", onRetry = { viewModel.load() }, modifier = Modifier.padding(top = 24.dp))
                }
                state.visible.isEmpty() -> item(key = "empty") {
                    EmptyState(
                        title = if (state.filter == RegistrationFilter.ALL) "No registrations yet" else "Nothing in ${state.filter.label.lowercase()}",
                        message = "When you register for an event it shows up here with its payment status and entry badge.",
                        icon = SsplIcons.Assignment,
                        actionText = "Browse events",
                        onActionClick = { navController.navigate(Screen.ScientificProgramsScreen.route) { launchSingleTop = true } },
                        modifier = Modifier.padding(top = 24.dp)
                    )
                }
                else -> items(state.visible, key = { "reg_${it.id}" }) { reg ->
                    RegistrationCard(
                        reg = reg,
                        retrying = state.retryingId == reg.id,
                        busy = state.retryingId != null,
                        onBadge = { badgeFor = reg },
                        onPay = { viewModel.retryPayment(reg) },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                    )
                }
            }
            item(key = "help") {
                ListRowCard(
                    title = "Need help with a registration?",
                    subtitle = "Payment, badge or approval questions — the secretariat can help.",
                    leading = { IconTile(SsplIcons.SupportAgent) },
                    onClick = { navController.navigate(Screen.ContactUsScreen.route) { launchSingleTop = true } },
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 18.dp)
                )
            }
        }
    }

    badgeFor?.let { reg ->
        AttendeeBadgeDialog(
            registration = reg,
            userName = listOfNotNull(user?.prefix, user?.firstName, user?.lastName).joinToString(" ").ifBlank { "Delegate" },
            institution = user?.institution ?: "SSP member",
            onDismiss = { badgeFor = null }
        )
    }
}

@Composable
private fun RegistrationCard(
    reg: Registration,
    retrying: Boolean,
    busy: Boolean,
    onBadge: () -> Unit,
    onPay: () -> Unit,
    modifier: Modifier = Modifier
) {
    val st = reg.uiStatus()
    val isWorkshop = reg.conference?.type?.name == "WORKSHOP"
    ListRowCard(
        title = reg.conference?.title ?: "Society event",
        subtitle = listOfNotNull(
            reg.role?.name?.let { "Registered as $it" },
            if (reg.feeAmount == 0.0) "Free" else com.sspl.ui.conference.formatPkr(reg.feeAmount)
        ).joinToString(" · "),
        meta = listOfNotNull(
            reg.registrationNumber?.let { "Reg. $it" } ?: "Reg. #${reg.id}",
            DisplayDates.medium(reg.conference?.startDate).takeIf { it.isNotBlank() }
        ).joinToString(" · "),
        leading = { IconTile(if (isWorkshop) SsplIcons.School else SsplIcons.Event) },
        trailing = { StatusBadge(st.label, variant = st.variant) },
        modifier = modifier,
        footer = if (st.note != null || reg.isConfirmed || reg.needsPayment) {
            {
                st.note?.let {
                    Text(it, style = AppTheme.typography.bodySmall.copy(color = AppTheme.colors.body))
                }
                if (reg.isConfirmed && !reg.isLocked) {
                    Spacer(Modifier.height(10.dp))
                    SecondaryButton("View entry badge", onBadge, icon = Icons.Default.QrCode2)
                } else if (reg.needsPayment && !reg.isLocked) {
                    Spacer(Modifier.height(10.dp))
                    PrimaryButton(
                        text = "Pay ${com.sspl.ui.conference.formatPkr(reg.feeAmount)}",
                        onClick = onPay,
                        loading = retrying,
                        enabled = !busy
                    )
                }
            }
        } else null
    )
}

/** Entry pass. Navy gradient header on a surface card (white text only on the gradient). */
@Composable
private fun AttendeeBadgeDialog(
    registration: Registration,
    userName: String,
    institution: String,
    onDismiss: () -> Unit
) {
    val colors = AppTheme.colors
    Dialog(onDismissRequest = onDismiss) {
        Column(
            Modifier
                .fillMaxWidth()
                .clip(AppTheme.shapes.card)
                .background(colors.surfaceElevated)
        ) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .background(colors.headerGradient)
            ) {
                DecorativeRings(Modifier.matchParentSize())
                Column(Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        GlassPill("ENTRY PASS", icon = SsplIcons.Badge)
                        Spacer(Modifier.weight(1f))
                        IconButton(onClick = onDismiss) {
                            Icon(SsplIcons.Close, contentDescription = "Close", tint = Color.White)
                        }
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(
                        registration.conference?.title ?: "SSPL event",
                        style = AppTheme.typography.titleMedium.copy(color = Color.White),
                        maxLines = 2
                    )
                    Text(
                        (registration.role?.name ?: "Delegate").uppercase(),
                        style = AppTheme.typography.overline.copy(color = Color.White.copy(alpha = 0.8f))
                    )
                }
            }
            Column(Modifier.fillMaxWidth().padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(userName, style = AppTheme.typography.titleLarge, textAlign = TextAlign.Center)
                Text(institution, style = AppTheme.typography.bodySmall, textAlign = TextAlign.Center)
                Spacer(Modifier.height(14.dp))
                // White QR plate: scanners need a light background in both themes.
                Column(
                    Modifier
                        .clip(AppTheme.shapes.small)
                        .background(Color.White)
                        .border(1.dp, colors.border, AppTheme.shapes.small)
                        .padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.QrCode2, contentDescription = "Registration code", tint = colors.primaryDark, modifier = Modifier.size(140.dp))
                    Text(
                        "SSPL-REG-${registration.id}",
                        style = AppTheme.typography.labelMedium.copy(color = colors.primaryDark, fontWeight = FontWeight.Bold)
                    )
                }
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatusBadge(registration.registrationNumber ?: "REG #${registration.id}", variant = BadgeVariant.INFO)
                    StatusBadge("Approved", variant = BadgeVariant.SUCCESS)
                }
                Spacer(Modifier.height(12.dp))
                Text(
                    "Show this pass at the registration desk for badge printing and venue check-in.",
                    style = AppTheme.typography.bodySmall,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(16.dp))
                PrimaryButton("Done", onDismiss)
                Spacer(Modifier.width(1.dp))
            }
        }
    }
}
