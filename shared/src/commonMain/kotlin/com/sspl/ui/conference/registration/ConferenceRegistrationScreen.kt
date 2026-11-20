package com.sspl.ui.conference.registration

import com.sspl.designsystem.icons.SsplIcons

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.sspl.Screen
import com.sspl.core.models.Conference
import com.sspl.designsystem.components.EmptyState
import com.sspl.designsystem.components.ErrorState
import com.sspl.designsystem.components.GlassPill
import com.sspl.designsystem.components.GradientHeroCard
import com.sspl.designsystem.components.IconTile
import com.sspl.designsystem.components.ListRowCard
import com.sspl.designsystem.components.SkeletonDetail
import com.sspl.designsystem.components.StatusBadge
import com.sspl.designsystem.components.StickyCtaBar
import com.sspl.designsystem.theme.AppTheme
import com.sspl.ui.conference.formatPkr
import kotlinx.serialization.json.Json
import org.koin.compose.viewmodel.koinViewModel

private val lenientJson = Json { ignoreUnknownKeys = true; coerceInputValues = true }

/**
 * C5: Legacy conference registration (POST /conferences/{id}/register → Swich initiate-payment
 * inside PaymentWebView). Flow unchanged; UI rebuilt.
 */
@Composable
fun ConferenceRegistrationScreen(
    navController: NavController,
    viewModel: ConferenceRegistrationViewModel = koinViewModel()
) {
    val conferenceArg = remember {
        navController.previousBackStackEntry?.savedStateHandle?.get<String>("data")
            ?.let { runCatching { lenientJson.decodeFromString<Conference>(it) }.getOrNull() }
    }
    LaunchedEffect(conferenceArg) { conferenceArg?.let { viewModel.loadData(it.id) } }

    if (conferenceArg == null) {
        EmptyState(
            title = "Conference unavailable",
            message = "Open registration again from the conference page.",
            icon = SsplIcons.EventBusy,
            modifier = Modifier.fillMaxSize()
        )
        return
    }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    when (val state = uiState) {
        is RegistrationUiState.Loading -> SkeletonDetail(Modifier.fillMaxSize(), showHero = false)
        is RegistrationUiState.Error -> ErrorState(
            message = state.message,
            onRetry = { viewModel.loadData(conferenceArg.id) },
            modifier = Modifier.fillMaxSize()
        )
        is RegistrationUiState.Content -> ConferenceRegistrationContent(
            state = state,
            viewModel = viewModel,
            navController = navController,
            initialTitle = conferenceArg.title
        )
    }
}

@Composable
fun ConferenceRegistrationContent(
    state: RegistrationUiState.Content,
    viewModel: ConferenceRegistrationViewModel,
    navController: NavController,
    initialTitle: String?
) {
    val colors = AppTheme.colors
    val roles = state.roles
    var selected by remember(state.isRegistered, state.registration) {
        val rid = state.registration?.roleId
        mutableIntStateOf(if (rid != null) roles.indexOfFirst { it.id == rid }.coerceAtLeast(0) else 0)
    }

    // Payment WebView (unchanged behaviour)
    if (state.paymentUrl != null) {
        com.sspl.ui.components.PaymentWebView(
            url = state.paymentUrl,
            onPaymentSuccess = {
                viewModel.onPaymentUrlOpened()
                viewModel.loadData(state.details.id)
            },
            onPaymentFailed = { viewModel.onPaymentUrlOpened() },
            onClose = { viewModel.onPaymentUrlOpened() }
        )
    }

    LaunchedEffect(state.registration?.paymentStatus) {
        if (state.registration?.paymentStatus == "SUCCESS") {
            navController.currentBackStackEntry?.savedStateHandle?.set("status", "success")
            navController.currentBackStackEntry?.savedStateHandle?.set("registrationId", state.registration.id)
            navController.navigate(Screen.PaymentStatusScreen.route) { launchSingleTop = true }
        }
    }

    Box(Modifier.fillMaxSize()) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 110.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            GradientHeroCard {
                GlassPill("CONFERENCE REGISTRATION")
                Spacer(Modifier.height(10.dp))
                Text(
                    initialTitle ?: state.details.title ?: "Annual conference",
                    style = AppTheme.typography.headlineSmall.copy(color = Color.White)
                )
                state.details.summary?.takeIf { it.isNotBlank() }?.let {
                    Text(it, style = AppTheme.typography.bodySmall.copy(color = Color.White.copy(alpha = 0.78f)), maxLines = 2, overflow = TextOverflow.Ellipsis)
                }
            }

            if (state.isRegistered && state.registration != null) {
                val paid = state.registration.paymentStatus == "SUCCESS"
                ListRowCard(
                    title = if (paid) "Registration confirmed" else "Registration submitted",
                    subtitle = if (paid) "Your seat is confirmed." else "Complete payment to confirm your seat.",
                    leading = {
                        IconTile(
                            if (paid) SsplIcons.CheckCircle else SsplIcons.HourglassTop,
                            tint = if (paid) colors.success else colors.warning,
                            background = if (paid) colors.successContainer else colors.warningContainer
                        )
                    },
                    trailing = { StatusBadge(state.registration.paymentStatus) }
                )
            }

            if (roles.isNotEmpty()) {
                Text("Select your category", style = AppTheme.typography.titleMedium, modifier = Modifier.padding(top = 6.dp))
                roles.forEachIndexed { i, role ->
                    val sel = i == selected
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clip(AppTheme.shapes.card)
                            .background(if (sel) colors.surfaceTint else colors.surface)
                            .border(if (sel) 1.5.dp else 1.dp, if (sel) colors.primary else colors.border, AppTheme.shapes.card)
                            .selectable(selected = sel, enabled = !state.isRegistered, role = Role.RadioButton) { selected = i }
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            Modifier.size(22.dp).clip(CircleShape)
                                .background(if (sel) colors.buttonStart else colors.surface)
                                .border(2.dp, if (sel) colors.primary else colors.border, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            if (sel) Icon(SsplIcons.Check, null, tint = colors.onPrimary, modifier = Modifier.size(14.dp))
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(role.name, style = AppTheme.typography.titleSmall)
                            role.description?.takeIf { it.isNotBlank() }?.let { Text(it, style = AppTheme.typography.bodySmall) }
                        }
                        Text(formatPkr(role.fee), style = AppTheme.typography.titleSmall.copy(color = colors.primary, fontWeight = FontWeight.Bold))
                    }
                }
            }

            state.error?.let { msg ->
                Row(
                    Modifier.fillMaxWidth().clip(AppTheme.shapes.small).background(colors.errorContainer).padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(SsplIcons.ErrorOutline, null, tint = colors.error, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(msg, style = AppTheme.typography.bodySmall.copy(color = colors.error))
                }
            }
        }

        val paymentStatus = state.registration?.paymentStatus
        val fee = state.registration?.feeAmount ?: roles.getOrNull(selected)?.fee ?: 0.0
        val cta: Triple<String, Boolean, () -> Unit>? = when {
            !state.isRegistered -> Triple("Register", roles.isNotEmpty(), { roles.getOrNull(selected)?.let { viewModel.register(it.id) }; Unit })
            paymentStatus == "PENDING" || paymentStatus == "FAILED" -> Triple("Pay online", true, { viewModel.initiatePayment() })
            paymentStatus == "SUCCESS" -> Triple("View my registrations", true, {
                navController.navigate(Screen.MyRegistrationsScreen.route) { launchSingleTop = true }
            })
            else -> null
        }
        if (cta != null) {
            StickyCtaBar(
                primaryText = cta.first,
                onPrimaryClick = cta.third,
                enabled = cta.second && !state.isLoading,
                loading = state.isLoading,
                priceText = if (paymentStatus == "SUCCESS") null else formatPkr(fee),
                priceSubtext = if (paymentStatus == "SUCCESS") null else "Total",
                modifier = Modifier.align(Alignment.BottomCenter).consumeWindowInsets(WindowInsets.navigationBars)
            )
        }
    }
}
