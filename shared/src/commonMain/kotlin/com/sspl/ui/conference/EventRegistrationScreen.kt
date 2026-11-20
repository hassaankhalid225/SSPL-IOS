package com.sspl.ui.conference

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.sspl.Screen
import com.sspl.core.models.EventRole
import com.sspl.designsystem.components.BadgeVariant
import com.sspl.designsystem.components.BlockingLoader
import com.sspl.designsystem.components.EmptyState
import com.sspl.designsystem.components.ErrorState
import com.sspl.designsystem.components.FactRow
import com.sspl.designsystem.components.IconTile
import com.sspl.designsystem.components.LocalShellActions
import com.sspl.designsystem.components.PrimaryButton
import com.sspl.designsystem.components.SecondaryButton
import com.sspl.designsystem.components.SkeletonDetail
import com.sspl.designsystem.components.SsplCard
import com.sspl.designsystem.components.SsplImage
import com.sspl.designsystem.components.StatusBadge
import com.sspl.designsystem.components.StepIndicator
import com.sspl.designsystem.components.StickyCtaBar
import com.sspl.designsystem.components.TextButtonLink
import com.sspl.designsystem.theme.AppTheme
import com.sspl.platform.shared.UrlOpener
import com.sspl.session.UserSession
import com.sspl.ui.conference.components.AddOnSetsList
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

/**
 * C3: Event registration. Step 1 choose a fee category (role), step 2 review & confirm.
 * POST /external/events/{id}/register { role_id }. If the backend returns a Swich payment
 * URL it is opened exactly as before (system browser), then Payment status takes over.
 */
@Composable
fun EventRegistrationScreen(
    navController: NavController,
    viewModel: EventDetailViewModel = koinViewModel(),
    userSession: UserSession = koinInject(),
    urlOpener: UrlOpener = koinInject()
) {
    val eventIdArg = remember { navController.previousBackStackEntry?.savedStateHandle?.get<Long>("eventId") ?: 0L }
    LaunchedEffect(eventIdArg) { viewModel.loadEvent(eventIdArg) }

    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val user by userSession.userFlow.collectAsStateWithLifecycle()
    val shell = LocalShellActions.current

    // Payment hand-off: open the Swich URL once, then show the payment status screen.
    LaunchedEffect(state.paymentUrl) {
        val url = state.paymentUrl ?: return@LaunchedEffect
        viewModel.onPaymentUrlConsumed()
        urlOpener.openUrl(url)
        navController.currentBackStackEntry?.savedStateHandle?.apply {
            set("awaitingTransactionId", state.transactionId)
            set("registrationId", state.registrationId ?: 0L)
        }
        navController.navigate(Screen.PaymentStatusScreen.route) { launchSingleTop = true }
    }

    val event = state.event
    when {
        shell.isGuest -> {
            EmptyState(
                title = "Sign in to register",
                message = "Event registration needs your SSPL member account.",
                icon = Icons.Default.Lock,
                actionText = "Sign in",
                onActionClick = shell.onAvatar,
                modifier = Modifier.fillMaxSize()
            )
            return
        }
        event == null && state.loadError != null -> {
            ErrorState(message = state.loadError ?: "", onRetry = viewModel::retry, modifier = Modifier.fillMaxSize())
            return
        }
        event == null -> {
            SkeletonDetail(Modifier.fillMaxSize(), showHero = false)
            return
        }
    }
    val ev = event ?: return
    val card = state.card ?: return

    if (state.registrationSuccess && state.paymentUrl == null) {
        val awaitingPayment = state.transactionId != null
        RegistrationDone(
            pending = state.registrationPending || awaitingPayment,
            message = if (awaitingPayment) "Your seat is reserved. It's confirmed once the payment goes through — check My registrations for the latest status."
            else state.registrationMessage ?: "Registration complete.",
            eventTitle = ev.title,
            onMyRegistrations = {
                navController.navigate(Screen.MyRegistrationsScreen.route) {
                    popUpTo(Screen.EventDetailScreen.route)
                    launchSingleTop = true
                }
            },
            onBack = { navController.popBackStack() }
        )
        return
    }

    val roles = remember(ev) { ev.roles.filter { it.isActive }.ifEmpty { ev.roles } }
    var step by rememberSaveable { mutableIntStateOf(if (roles.size <= 1) 2 else 1) }
    var selectedRoleId by rememberSaveable { mutableStateOf(roles.singleOrNull()?.id ?: -1L) }
    val selectedRole: EventRole? = roles.firstOrNull { it.id == selectedRoleId }
    val alreadyRegistered = state.registrationData?.isRegistered == true
    val fee = selectedRole?.currentFee ?: 0.0

    if (state.isRegistering) BlockingLoader(if (fee > 0) "Registering and preparing payment…" else "Submitting registration…")

    Box(Modifier.fillMaxSize()) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 110.dp)
        ) {
            StepIndicator(
                currentStep = step,
                totalSteps = 2,
                stepLabels = listOf("Choose category", "Review & confirm"),
                modifier = Modifier.padding(top = 8.dp)
            )
            EventSummaryStrip(card, Modifier.padding(horizontal = 16.dp, vertical = 8.dp))

            if (alreadyRegistered) {
                SsplCard(Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconTile(Icons.Default.CheckCircle, tint = AppTheme.colors.success, background = AppTheme.colors.successContainer)
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text("You're already registered", style = AppTheme.typography.titleSmall)
                            Text("Manage payment or view your entry badge in My registrations.", style = AppTheme.typography.bodySmall)
                        }
                    }
                }
            }

            AnimatedContent(
                targetState = step,
                transitionSpec = { fadeIn(tween(220)) togetherWith fadeOut(tween(160)) },
                label = "regStep"
            ) { s ->
                Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                    if (s == 1) {
                        Text("Select your registration category", style = AppTheme.typography.titleMedium, modifier = Modifier.padding(top = 10.dp, bottom = 8.dp))
                        roles.forEach { role ->
                            RoleChoiceCard(
                                role = role,
                                selected = role.id == selectedRoleId,
                                enabled = !alreadyRegistered,
                                onSelect = { selectedRoleId = role.id },
                                modifier = Modifier.padding(vertical = 5.dp)
                            )
                        }
                        if (ev.addOnSets.isNotEmpty()) {
                            Text("Included with registration", style = AppTheme.typography.titleMedium, modifier = Modifier.padding(top = 16.dp, bottom = 8.dp))
                            AddOnSetsList(ev.addOnSets)
                        }
                    } else {
                        ReviewStep(
                            userName = listOfNotNull(user?.prefix, user?.firstName, user?.lastName).joinToString(" ").ifBlank { "Member" },
                            email = user?.email,
                            pmdc = user?.pmdcNo,
                            institution = user?.institution,
                            role = selectedRole,
                            canChangeRole = roles.size > 1,
                            onChangeRole = { step = 1 },
                            addOnCount = ev.addOnSets.sumOf { it.addOns.orEmpty().size }
                        )
                    }
                }
            }

            state.errorMessage?.let { msg ->
                Row(
                    Modifier
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                        .fillMaxWidth()
                        .clip(AppTheme.shapes.small)
                        .background(AppTheme.colors.errorContainer)
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = AppTheme.colors.error, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(msg, style = AppTheme.typography.bodySmall.copy(color = AppTheme.colors.error))
                }
            }
        }

        val primary: Triple<String, Boolean, () -> Unit> = when {
            alreadyRegistered -> Triple("Go to My registrations", true, {
                navController.navigate(Screen.MyRegistrationsScreen.route) { launchSingleTop = true }
            })
            !card.registrationOpen -> Triple("Registration closed", false, {})
            state.registrationData?.canRegister == false -> Triple("Registration unavailable", false, {})
            step == 1 -> Triple("Continue", selectedRole != null, { step = 2 })
            fee > 0 -> Triple("Register & pay", roles.isEmpty() || selectedRole != null, { viewModel.register(selectedRole?.id) })
            else -> Triple("Confirm registration", roles.isEmpty() || selectedRole != null, { viewModel.register(selectedRole?.id) })
        }
        StickyCtaBar(
            primaryText = primary.first,
            onPrimaryClick = primary.third,
            enabled = primary.second && !state.isRegistering,
            loading = state.isRegistering,
            priceText = if (selectedRole != null || roles.isEmpty()) formatPkr(fee) else null,
            priceSubtext = if (selectedRole != null || roles.isEmpty()) "Total" else null,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .consumeWindowInsets(WindowInsets.navigationBars)
        )
    }
}

@Composable
private fun EventSummaryStrip(card: EventCardUi, modifier: Modifier = Modifier) {
    val colors = AppTheme.colors
    Row(
        modifier
            .fillMaxWidth()
            .clip(AppTheme.shapes.card)
            .background(colors.surface)
            .border(1.dp, colors.border.copy(alpha = 0.7f), AppTheme.shapes.card)
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SsplImage(
            card.image, card.title,
            Modifier.size(64.dp).clip(AppTheme.shapes.small),
            fallbackIcon = Icons.Default.Event
        )
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(card.typeLabel.uppercase(), style = AppTheme.typography.overline)
            Text(card.title, style = AppTheme.typography.titleSmall, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Text(listOf(card.dateRange, card.venue).joinToString(" · "), style = AppTheme.typography.labelSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun RoleChoiceCard(
    role: EventRole,
    selected: Boolean,
    enabled: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = AppTheme.colors
    Row(
        modifier
            .fillMaxWidth()
            .clip(AppTheme.shapes.card)
            .background(if (selected) colors.surfaceTint else colors.surface)
            .border(if (selected) 1.5.dp else 1.dp, if (selected) colors.primary else colors.border, AppTheme.shapes.card)
            .selectable(selected = selected, enabled = enabled, role = Role.RadioButton, onClick = onSelect)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(22.dp)
                .clip(CircleShape)
                .background(if (selected) colors.primary else colors.surface)
                .border(2.dp, if (selected) colors.primary else colors.border, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (selected) Icon(Icons.Default.Check, contentDescription = null, tint = colors.onPrimary, modifier = Modifier.size(14.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(role.name, style = AppTheme.typography.titleSmall)
            role.description?.takeIf { it.isNotBlank() }?.let {
                Text(it, style = AppTheme.typography.bodySmall, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
            if (role.isEarlyBirdActive) {
                Spacer(Modifier.height(4.dp))
                StatusBadge("Early bird", variant = BadgeVariant.WARNING)
            }
        }
        Spacer(Modifier.width(8.dp))
        Column(horizontalAlignment = Alignment.End) {
            Text(formatPkr(role.currentFee), style = AppTheme.typography.titleSmall.copy(color = colors.primary, fontWeight = FontWeight.Bold))
            if (role.isEarlyBirdActive && role.fee > role.currentFee) {
                Text(
                    formatPkr(role.fee),
                    style = AppTheme.typography.labelSmall.copy(textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough)
                )
            }
        }
    }
}

@Composable
private fun ReviewStep(
    userName: String,
    email: String?,
    pmdc: String?,
    institution: String?,
    role: EventRole?,
    canChangeRole: Boolean,
    onChangeRole: () -> Unit,
    addOnCount: Int
) {
    val colors = AppTheme.colors
    Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.padding(top = 10.dp)) {
        SsplCard {
            Text("REGISTRANT", style = AppTheme.typography.overline)
            Spacer(Modifier.height(8.dp))
            FactRow(Icons.Default.Person, "Name", userName)
            listOfNotNull(
                email?.takeIf { it.isNotBlank() }?.let { "Email" to it },
                pmdc?.takeIf { it.isNotBlank() }?.let { "PMDC no." to it },
                institution?.takeIf { it.isNotBlank() }?.let { "Institution" to it }
            ).forEach { (k, v) ->
                Spacer(Modifier.height(6.dp))
                Row {
                    Text(k, style = AppTheme.typography.labelMedium, modifier = Modifier.width(96.dp))
                    Text(v, style = AppTheme.typography.bodyMedium.copy(color = colors.heading), maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
        }
        SsplCard {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("ORDER SUMMARY", style = AppTheme.typography.overline, modifier = Modifier.weight(1f))
                if (canChangeRole) TextButtonLink("Change", onChangeRole)
            }
            Spacer(Modifier.height(8.dp))
            SummaryLine(role?.name ?: "Attendee registration", formatPkr(role?.currentFee ?: 0.0))
            if (role != null && role.isEarlyBirdActive && role.fee > role.currentFee) {
                SummaryLine("Early-bird saving", "− " + formatPkr(role.fee - role.currentFee), muted = true)
            }
            if (addOnCount > 0) SummaryLine("Included add-ons", "$addOnCount", muted = true)
            HorizontalDivider(Modifier.padding(vertical = 10.dp), color = colors.divider)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Total payable", style = AppTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                Text(formatPkr(role?.currentFee ?: 0.0), style = AppTheme.typography.number.copy(color = colors.primary, fontSize = AppTheme.typography.titleLarge.fontSize))
            }
            if ((role?.currentFee ?: 0.0) > 0.0) {
                Spacer(Modifier.height(8.dp))
                Text(
                    "You'll be taken to the secure Swich payment page to complete payment.",
                    style = AppTheme.typography.bodySmall
                )
            } else {
                Spacer(Modifier.height(8.dp))
                Text("Free registrations may need admin approval before they're confirmed.", style = AppTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun SummaryLine(label: String, value: String, muted: Boolean = false) {
    Row(Modifier.fillMaxWidth().padding(vertical = 3.dp)) {
        Text(label, style = AppTheme.typography.bodyMedium.copy(color = if (muted) AppTheme.colors.muted else AppTheme.colors.body), modifier = Modifier.weight(1f))
        Text(value, style = AppTheme.typography.bodyMedium.copy(color = AppTheme.colors.heading, fontWeight = FontWeight.SemiBold))
    }
}

@Composable
private fun RegistrationDone(
    pending: Boolean,
    message: String,
    eventTitle: String,
    onMyRegistrations: () -> Unit,
    onBack: () -> Unit
) {
    val colors = AppTheme.colors
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        IconTile(
            if (pending) Icons.Default.HourglassTop else Icons.Default.CheckCircle,
            tint = if (pending) colors.warning else colors.success,
            background = if (pending) colors.warningContainer else colors.successContainer,
            size = 88.dp, iconSize = 44.dp, corner = 44.dp
        )
        Spacer(Modifier.height(18.dp))
        Text(if (pending) "Registration submitted" else "You're registered!", style = AppTheme.typography.headlineMedium, textAlign = TextAlign.Center)
        Spacer(Modifier.height(6.dp))
        Text(eventTitle, style = AppTheme.typography.titleSmall.copy(color = colors.primary), textAlign = TextAlign.Center)
        Spacer(Modifier.height(10.dp))
        Text(message, style = AppTheme.typography.bodyMedium, textAlign = TextAlign.Center)
        Spacer(Modifier.height(24.dp))
        PrimaryButton("View my registrations", onMyRegistrations)
        Spacer(Modifier.height(10.dp))
        SecondaryButton("Back to event", onBack)
    }
}
