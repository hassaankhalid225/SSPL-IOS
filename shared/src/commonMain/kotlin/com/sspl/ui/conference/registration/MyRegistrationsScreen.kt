package com.sspl.ui.conference.registration

import com.sspl.designsystem.icons.SsplIcons

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.imePadding
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import com.sspl.core.models.User
import com.sspl.designsystem.components.ConfirmDialog
import com.sspl.designsystem.components.OutlineButton
import com.sspl.designsystem.components.SsplTextField
import io.github.alexzhirkevich.qrose.options.QrErrorCorrectionLevel
import io.github.alexzhirkevich.qrose.rememberQrCodePainter
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
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
    userSession: UserSession = koinInject()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val user by userSession.userFlow.collectAsStateWithLifecycle()
    var badgeFor by remember { mutableStateOf<Registration?>(null) }
    // Swich page runs in-app (PaymentWebView); when it closes, Payment status verifies it.
    var pendingPayment by remember { mutableStateOf<MyRegistrationsEvent.OpenPayment?>(null) }

    LaunchedEffect(Unit) {
        viewModel.events.collect { ev ->
            when (ev) {
                is MyRegistrationsEvent.OpenPayment -> pendingPayment = ev
            }
        }
    }
    pendingPayment?.let { ev ->
        val afterPayment: () -> Unit = {
            pendingPayment = null
            navController.currentBackStackEntry?.savedStateHandle?.apply {
                set("awaitingTransactionId", ev.transactionId)
                set("registrationId", ev.registrationId)
            }
            navController.navigate(Screen.PaymentStatusScreen.route) { launchSingleTop = true }
        }
        com.sspl.ui.components.PaymentWebView(
            url = ev.url,
            onPaymentSuccess = afterPayment,
            onPaymentFailed = afterPayment,
            onClose = afterPayment
        )
    }
    state.alertMessage?.let { msg ->
        ConfirmDialog(
            title = "Email needed for payment",
            message = msg,
            confirmText = "OK",
            dismissText = "Close",
            onConfirm = viewModel::dismissAlert,
            onDismiss = viewModel::dismissAlert
        )
    }
    state.phonePromptFor?.let {
        PaymentPhoneDialog(
            initial = state.phonePromptInitial,
            onSubmit = viewModel::submitPhone,
            onDismiss = viewModel::dismissPhonePrompt
        )
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
            user = user,
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
                    SecondaryButton("View entry pass", onBadge, icon = SsplIcons.QrCode)
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

/** Web BadgeModal role badge titles. */
private fun roleBadgeTitle(role: String?): String {
    val upper = (role ?: "").uppercase()
    return when {
        "FACULTY" in upper || "SPEAKER" in upper -> "FACULTY / SPEAKER"
        "ORGANIZ" in upper || "COMMITTEE" in upper -> "ORGANIZING COMMITTEE"
        "VIP" in upper || "CHAIR" in upper || "GUEST" in upper -> "DISTINGUISHED GUEST"
        "RESIDENT" in upper || "TRAINEE" in upper || "STUDENT" in upper -> "POSTGRADUATE TRAINEE"
        else -> upper.ifBlank { "CONFERENCE DELEGATE" }
    }
}

/** JS truthiness for the raw `registration_id` (null, "", 0 and false are falsy). */
private fun JsonElement?.truthy(): Boolean {
    if (this == null || this is JsonNull) return false
    val p = this as? JsonPrimitive ?: return true
    if (p.isString) return p.content.isNotEmpty()
    return p.content != "0" && p.content != "false" && p.content != "0.0"
}

/**
 * Exact web BadgeModal payload:
 * JSON.stringify({ id, uid: registration_id || id, pid: person.id || user.id, registration_id }).
 */
internal fun entryPassPayload(reg: Registration, userId: Long?): String {
    val raw = reg.registrationIdRaw
    val pid = reg.person?.id?.takeIf { it != 0L } ?: userId?.takeIf { it != 0L }
    return buildJsonObject {
        put("id", reg.id)
        if (raw != null && raw.truthy()) put("uid", raw) else put("uid", reg.id)
        pid?.let { put("pid", it) }
        put("registration_id", raw ?: JsonNull)
    }.toString()
}

/** Entry pass (web BadgeModal): scannable QR of the registration, attendee + event facts. */
@Composable
private fun AttendeeBadgeDialog(
    registration: Registration,
    user: User?,
    onDismiss: () -> Unit
) {
    val colors = AppTheme.colors
    val person = registration.person
    val raw = registration.registrationIdRaw
    val regNumber = if (raw.truthy()) (raw as? JsonPrimitive)?.content ?: raw.toString() else "REG-${registration.id}"
    val fullName = listOf(
        person?.prefix ?: user?.prefix ?: person?.salutation,
        person?.firstName ?: user?.firstName,
        person?.lastName ?: user?.lastName
    ).filterNot { it.isNullOrBlank() }.joinToString(" ").trim().ifBlank { "CONFERENCE ATTENDEE" }
    val affiliation = listOf(person?.hospital, person?.institution, person?.city)
        .firstOrNull { !it.isNullOrBlank() }
    val conf = registration.conference
    val dateRange = DisplayDates.range(conf?.startDate, conf?.endDate)
    val venue = conf?.venue?.let { it.title ?: it.name }?.takeIf { it.isNotBlank() } ?: "Lahore, Pakistan"
    val payload = remember(registration, user?.id) { entryPassPayload(registration, user?.id) }
    val qrPainter = rememberQrCodePainter(payload) {
        errorCorrectionLevel = QrErrorCorrectionLevel.Medium
    }

    Dialog(onDismissRequest = onDismiss) {
        Column(
            Modifier
                .fillMaxWidth()
                .clip(AppTheme.shapes.card)
                .background(colors.surfaceElevated)
                .verticalScroll(rememberScrollState())
        ) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .background(colors.headerGradient)
            ) {
                DecorativeRings(Modifier.matchParentSize())
                Column(Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        GlassPill("ENTRY PASS", icon = SsplIcons.QrCode)
                        Spacer(Modifier.weight(1f))
                        IconButton(onClick = onDismiss) {
                            Icon(SsplIcons.Close, contentDescription = "Close", tint = Color.White)
                        }
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(
                        conf?.title?.takeIf { it.isNotBlank() } ?: "ANNUAL SURGICAL CONFERENCE",
                        style = AppTheme.typography.titleMedium.copy(color = Color.White),
                        maxLines = 2
                    )
                    Text(
                        listOf(dateRange, venue).filter { it.isNotBlank() }.joinToString(" · "),
                        style = AppTheme.typography.bodySmall.copy(color = Color.White.copy(alpha = 0.8f)),
                        maxLines = 2
                    )
                }
            }
            Column(Modifier.fillMaxWidth().padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatusBadge(roleBadgeTitle(registration.role?.name), variant = BadgeVariant.INFO)
                    StatusBadge(regNumber, variant = BadgeVariant.NEUTRAL)
                }
                Spacer(Modifier.height(14.dp))
                // White quiet-zone plate: scanners need dark modules on a light background in both themes.
                Box(
                    Modifier
                        .clip(AppTheme.shapes.small)
                        .background(Color.White)
                        .border(1.dp, colors.border, AppTheme.shapes.small)
                        .padding(16.dp)
                ) {
                    Image(
                        painter = qrPainter,
                        contentDescription = "Entry pass QR code for registration $regNumber",
                        modifier = Modifier.size(200.dp)
                    )
                }
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(SsplIcons.VerifiedUser, contentDescription = null, tint = colors.primary, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(
                        "Verified attendee QR code",
                        style = AppTheme.typography.labelMedium.copy(color = colors.primary, fontWeight = FontWeight.SemiBold)
                    )
                }
                Spacer(Modifier.height(14.dp))
                Text("ATTENDEE NAME", style = AppTheme.typography.overline)
                Text(fullName, style = AppTheme.typography.titleLarge, textAlign = TextAlign.Center)
                affiliation?.let { Text(it, style = AppTheme.typography.bodySmall, textAlign = TextAlign.Center) }
                Spacer(Modifier.height(12.dp))
                Text(
                    "Show this QR code at the conference check-in desk on arrival. Organisers will scan it to issue your printed badge.",
                    style = AppTheme.typography.bodySmall,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(16.dp))
                PrimaryButton("Done", onDismiss)
            }
        }
    }
}

/** Web PhoneModal: ask for a mobile number before starting Swich payment. */
@Composable
private fun PaymentPhoneDialog(
    initial: String,
    onSubmit: (String) -> String?,
    onDismiss: () -> Unit
) {
    var phone by remember { mutableStateOf(initial) }
    var error by remember { mutableStateOf<String?>(null) }
    val submit: () -> Unit = { error = onSubmit(phone) }
    Dialog(onDismissRequest = onDismiss) {
        Column(
            Modifier
                .fillMaxWidth()
                .clip(AppTheme.shapes.card)
                .background(AppTheme.colors.surfaceElevated)
                .imePadding()
                .padding(20.dp)
        ) {
            Text("Mobile number required", style = AppTheme.typography.titleMedium)
            Spacer(Modifier.height(4.dp))
            Text("Swich needs a mobile number to process your payment.", style = AppTheme.typography.bodySmall)
            Spacer(Modifier.height(14.dp))
            SsplTextField(
                value = phone,
                onValueChange = { phone = it; error = null },
                label = "Mobile number",
                placeholder = "03XXXXXXXXX",
                leadingIcon = SsplIcons.Phone,
                error = error,
                keyboardType = KeyboardType.Phone,
                imeAction = ImeAction.Done,
                keyboardActions = KeyboardActions(onDone = { submit() })
            )
            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlineButton("Cancel", onDismiss, modifier = Modifier.weight(1f))
                PrimaryButton("Continue", submit, modifier = Modifier.weight(1f), enabled = phone.isNotBlank())
            }
        }
    }
}
