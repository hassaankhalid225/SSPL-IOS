package com.sspl.ui.portal

import com.sspl.designsystem.icons.SsplIcons

import androidx.compose.animation.animateColorAsState
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CardMembership
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.sspl.Screen
import com.sspl.core.models.MembershipPlan
import com.sspl.designsystem.components.BlockingLoader
import com.sspl.designsystem.components.EmptyState
import com.sspl.designsystem.components.LoadState
import com.sspl.designsystem.components.LoadStateSwitch
import com.sspl.designsystem.components.LocalShellActions
import com.sspl.designsystem.components.NavTile
import com.sspl.designsystem.components.SkeletonList
import com.sspl.designsystem.components.SkeletonVariant
import com.sspl.designsystem.components.SsplRefreshBox
import com.sspl.designsystem.components.StickyCtaBar
import com.sspl.designsystem.theme.AppTheme
import com.sspl.ui.components.PaymentWebView

/**
 * D3: Membership plans (web `/membership-plans`). Comparable plan cards, single selection,
 * sticky CTA → POST /membership/register → payment (flow unchanged).
 */
@Composable
fun MembershipPlansScreen(
    navController: NavController,
    viewModel: MembershipPlansViewModel = portalViewModel { MembershipPlansViewModel(it.get()) }
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val shell = LocalShellActions.current

    state.paymentUrl?.let { url ->
        PaymentWebView(
            url = url,
            onPaymentSuccess = { viewModel.onPaymentFinished(true) },
            onPaymentFailed = { viewModel.onPaymentFinished(false) },
            onClose = { viewModel.onPaymentFinished(false) }
        )
    }
    if (state.isApplying) BlockingLoader("Preparing your application…")

    Column(Modifier.fillMaxSize()) {
        SsplRefreshBox(
            isRefreshing = state.isRefreshing,
            onRefresh = viewModel::refresh,
            modifier = Modifier.weight(1f)
        ) {
            LoadStateSwitch(
                state = state.plans,
                modifier = Modifier.fillMaxSize(),
                skeleton = { SkeletonList(count = 3, variant = SkeletonVariant.MEDIA) },
                empty = {
                    EmptyState(
                        title = "No plans open right now",
                        message = "No membership plans are available at the moment. Please check back or contact the office.",
                        icon = SsplIcons.CardMembership,
                        actionText = "Contact us",
                        onActionClick = { navController.openPortal(Screen.ContactUsScreen.route) },
                        modifier = Modifier.fillMaxSize()
                    )
                },
                onRetry = viewModel::retry
            ) { plans ->
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item(key = "hero") {
                        PortalHero(
                            eyebrow = "Membership",
                            title = "Join the professional community",
                            summary = "Choose the membership that fits your practice and gain access to events, directory listing and society resources.",
                            icon = SsplIcons.CardMembership,
                            stats = listOf("${plans.size}" to "Plans available")
                        )
                    }
                    state.message?.let { msg ->
                        item(key = "msg") {
                            PortalNotice(msg, if (state.isError) NoticeTone.Error else NoticeTone.Success, onDismiss = viewModel::dismissMessage)
                        }
                    }
                    items(plans, key = { it.id }) { plan ->
                        PlanCard(plan, selected = plan.id == state.selectedPlanId, onSelect = { viewModel.select(plan.id) })
                    }
                    item(key = "secure") {
                        Row(
                            Modifier.fillMaxWidth().padding(top = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(SsplIcons.Lock, contentDescription = null, tint = AppTheme.colors.muted, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Payments are processed securely through Swich.", style = AppTheme.typography.bodySmall)
                        }
                    }
                    item(key = "help") {
                        NavTile(
                            icon = SsplIcons.SupportAgent,
                            title = "Questions before you join?",
                            subtitle = "Talk to the membership office",
                            onClick = { navController.openPortal(Screen.ContactUsScreen.route) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        val selected = state.selectedPlan
        if (state.plans is LoadState.Ready && selected != null) {
            StickyCtaBar(
                primaryText = if (shell.isGuest) "Sign in to apply" else if (selected.displayFee > 0) "Continue to pay" else "Apply now",
                onPrimaryClick = { if (shell.isGuest) shell.onAvatar() else viewModel.apply() },
                priceText = formatRupees(selected.displayFee),
                priceSubtext = selected.name,
                loading = state.isApplying,
                enabled = !state.isApplying
            )
        }
    }
}

@Composable
private fun PlanCard(plan: MembershipPlan, selected: Boolean, onSelect: () -> Unit) {
    val c = AppTheme.colors
    val borderColor by animateColorAsState(if (selected) c.primary else c.border.copy(alpha = 0.7f), label = "planBorder")
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(AppTheme.shapes.card)
            .background(if (selected) c.surfaceTint else c.surface)
            .border(if (selected) 1.5.dp else 1.dp, borderColor, AppTheme.shapes.card)
            .clickable(onClick = onSelect)
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(plan.name, style = AppTheme.typography.titleMedium)
                    if (plan.isPopular()) {
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Most chosen",
                            style = AppTheme.typography.labelSmall.copy(color = Color.White, fontWeight = FontWeight.SemiBold),
                            modifier = Modifier.clip(RoundedCornerShape(50)).background(c.primaryGradient)
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
                Text(plan.durationText(), style = AppTheme.typography.bodySmall)
            }
            Icon(
                if (selected) SsplIcons.CheckCircle else SsplIcons.RadioButtonUnchecked,
                contentDescription = if (selected) "Selected plan" else "Select plan",
                tint = if (selected) c.primary else c.muted,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(Modifier.height(10.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            Text(formatRupees(plan.displayFee), style = AppTheme.typography.number.copy(color = c.primary))
            if (plan.displayFee > 0 && !plan.type.equals("LIFE", true) && plan.type != null) {
                Spacer(Modifier.width(4.dp))
                Text("/${plan.type.lowercase()}", style = AppTheme.typography.bodySmall, modifier = Modifier.padding(bottom = 3.dp))
            }
        }
        plan.description?.takeIf { it.isNotBlank() }?.let {
            Spacer(Modifier.height(6.dp))
            Text(it, style = AppTheme.typography.bodySmall.copy(color = c.body))
        }
        Spacer(Modifier.height(12.dp))
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            plan.features().forEach { feat ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier.size(18.dp).clip(CircleShape).background(c.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(SsplIcons.Check, contentDescription = null, tint = c.primary, modifier = Modifier.size(12.dp))
                    }
                    Spacer(Modifier.width(10.dp))
                    Text(feat, style = AppTheme.typography.bodyMedium)
                }
            }
        }
    }
}
