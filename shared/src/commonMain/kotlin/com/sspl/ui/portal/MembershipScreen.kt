package com.sspl.ui.portal

import com.sspl.designsystem.icons.SsplIcons

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CardMembership
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.HowToReg
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.School
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.sspl.Screen
import com.sspl.core.auth.RoleUtils
import com.sspl.core.repositories.MemberSnapshot
import com.sspl.designsystem.components.BlockingLoader
import com.sspl.designsystem.components.ErrorState
import com.sspl.designsystem.components.GlassPill
import com.sspl.designsystem.components.GradientHeroCard
import com.sspl.designsystem.components.IconTile
import com.sspl.designsystem.components.LoadState
import com.sspl.designsystem.components.LocalShellActions
import com.sspl.designsystem.components.NavTile
import com.sspl.designsystem.components.PrimaryButton
import com.sspl.designsystem.components.SkeletonBox
import com.sspl.designsystem.components.SkeletonList
import com.sspl.designsystem.components.SsplRefreshBox
import com.sspl.designsystem.components.SsplTextField
import com.sspl.designsystem.theme.AppTheme
import com.sspl.ui.components.PaymentWebView
import com.sspl.utils.DisplayDates

private val MEMBER_BENEFITS = listOf(
    "Access to all professional events and conferences",
    "Member directory listing and networking opportunities",
    "Exclusive newsletters and professional resources",
    "Voting rights and participation in governance"
)

/**
 * D2: Membership (web `/dashboard/membership`): status, application state, life upgrade with
 * profile verification → payment, benefits.
 */
@Composable
fun MembershipScreen(
    navController: NavController,
    viewModel: MembershipViewModel = portalViewModel { MembershipViewModel(it.get(), it.get()) }
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val shell = LocalShellActions.current

    if (shell.isGuest) {
        PortalGuestGate(onSignIn = shell.onAvatar, modifier = Modifier.fillMaxSize())
        return
    }

    state.paymentUrl?.let { url ->
        PaymentWebView(
            url = url,
            onPaymentSuccess = { viewModel.onPaymentFinished(true) },
            onPaymentFailed = { viewModel.onPaymentFinished(false) },
            onClose = { viewModel.onPaymentFinished(false) }
        )
    }
    if (state.isUpgrading) BlockingLoader("Starting your life membership upgrade…")

    SsplRefreshBox(
        isRefreshing = state.isRefreshing,
        onRefresh = viewModel::refresh,
        modifier = Modifier.fillMaxSize()
    ) {
        when (val m = state.member) {
            is LoadState.Loading -> Column(Modifier.fillMaxSize().padding(16.dp)) {
                SkeletonBox(Modifier.fillMaxWidth().height(170.dp), AppTheme.shapes.card)
                SkeletonList(count = 3, contentPadding = 0.dp)
            }
            is LoadState.Error -> ErrorState(
                title = "Membership didn't load",
                message = m.message,
                onRetry = viewModel::retry,
                modifier = Modifier.fillMaxSize()
            )
            is LoadState.Ready -> MembershipContent(navController, state, m.data, viewModel)
        }
    }
}

@Composable
private fun MembershipContent(
    navController: NavController,
    state: MembershipUiState,
    snap: MemberSnapshot,
    vm: MembershipViewModel
) {
    val go: (String) -> Unit = { navController.openPortal(it) }
    LazyColumn(
        modifier = Modifier.fillMaxSize().imePadding(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item(key = "hero") { StatusHero(state, snap) }

        state.successMessage?.let { msg ->
            item(key = "ok") { PortalNotice(msg, NoticeTone.Success, onDismiss = vm::dismissMessage) }
        }
        if (state.hasUnpaidUpgradeCheckout && !state.hasPendingUpgrade) {
            item(key = "unpaid") {
                PortalNotice(
                    title = "Payment not completed",
                    message = "Your life membership upgrade payment was not completed. You are still a regular member and can start the upgrade again below.",
                    tone = NoticeTone.Error
                )
            }
        }
        if (state.hasPendingUpgrade) {
            item(key = "pendingUp") {
                PortalNotice(
                    title = "Life upgrade pending approval",
                    message = "Your payment has been received. Your upgrade to ${snap.pendingPlanName ?: "Life Membership"} is awaiting admin approval. You remain a regular member until approved.",
                    tone = NoticeTone.Warning
                )
            }
        }
        if (state.isPendingApplication) {
            item(key = "pendingApp") {
                PortalNotice(
                    title = "Membership application pending",
                    message = "Your membership application is under review. You will receive your member role after approval.",
                    tone = NoticeTone.Info
                )
            }
        }
        if (state.isLifeMember) {
            item(key = "life") {
                PortalNotice(title = "Life member", message = "You have an active life membership.", tone = NoticeTone.Success)
            }
        }

        if (!state.isActiveMember && !state.isPendingApplication) {
            item(key = "apply") {
                PortalSection(
                    title = "Apply for membership",
                    subtitle = "Join the society for events, networking and resources",
                    icon = SsplIcons.CardMembership
                ) {
                    Text("Life, Ordinary, Associate or Student plans.", style = AppTheme.typography.bodySmall)
                    Spacer(Modifier.height(12.dp))
                    PrimaryButton("View membership plans", onClick = { go(Screen.MembershipPlansScreen.route) })
                }
            }
            item(key = "already") {
                NavTile(
                    icon = SsplIcons.HowToReg,
                    title = "Already a member?",
                    subtitle = "Contact the SSPL office to link your existing membership",
                    onClick = { go(Screen.ContactUsScreen.route) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        if (state.isActiveMember && !state.isLifeMember && !state.hasPendingUpgrade) {
            item(key = "upgrade") { UpgradeCard(state, snap, vm) }
        }

        item(key = "benefits") {
            PortalSection(title = "Membership benefits", icon = SsplIcons.WorkspacePremium) {
                MEMBER_BENEFITS.forEach { b -> BenefitRow(b) }
            }
        }

        item(key = "help") {
            NavTile(
                icon = SsplIcons.SupportAgent,
                title = "Questions about membership?",
                subtitle = "Verification, payment proofs or fees",
                onClick = { go(Screen.ContactUsScreen.route) },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun StatusHero(state: MembershipUiState, snap: MemberSnapshot) {
    val user = snap.user
    val tier = when {
        state.isLifeMember -> "Life member"
        state.isActiveMember -> RoleUtils.getMembershipLabel(user).takeIf { it != "Registered user" } ?: "Regular member"
        state.isPendingApplication -> "Application pending"
        else -> "Not a member yet"
    }
    val details = state.details
    GradientHeroCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            GlassPill("MEMBERSHIP", icon = SsplIcons.Verified)
            Spacer(Modifier.weight(1f))
            snap.membershipStatus?.let { StatusOnGradient(it.replace('_', ' ')) }
        }
        Spacer(Modifier.height(12.dp))
        Text("CURRENT TIER", style = AppTheme.typography.labelSmall.copy(color = Color.White.copy(alpha = 0.6f)))
        Text(tier, style = AppTheme.typography.headlineSmall.copy(color = Color.White), maxLines = 2, overflow = TextOverflow.Ellipsis)
        Spacer(Modifier.height(14.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            HeroFact("Member no.", details?.membershipNumber ?: user?.profile?.orgNumber ?: "—", Modifier.weight(1f))
            HeroFact(
                "Plan",
                details?.plan?.name ?: user?.membershipPlan?.name ?: user?.membershipPlan?.title ?: "—",
                Modifier.weight(1f)
            )
            HeroFact(
                if (state.isLifeMember) "Validity" else "Valid until",
                when {
                    state.isLifeMember -> "Lifetime"
                    !details?.expiresAt.isNullOrBlank() -> DisplayDates.medium(details?.expiresAt).ifBlank { details?.expiresAt.orEmpty() }
                    else -> "—"
                },
                Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun HeroFact(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier) {
        Text(label.uppercase(), style = AppTheme.typography.labelSmall.copy(color = Color.White.copy(alpha = 0.6f)))
        Text(value, style = AppTheme.typography.titleSmall.copy(color = Color.White), maxLines = 2, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun BenefitRow(text: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.Top) {
        Icon(SsplIcons.CheckCircle, contentDescription = null, tint = AppTheme.colors.success, modifier = Modifier.size(16.dp).padding(top = 2.dp))
        Spacer(Modifier.width(10.dp))
        Text(text, style = AppTheme.typography.bodyMedium)
    }
}

@Composable
private fun UpgradeCard(state: MembershipUiState, snap: MemberSnapshot, vm: MembershipViewModel) {
    val c = AppTheme.colors
    val f = state.form
    val err = state.fieldErrors
    PortalSection(
        title = "Upgrade to life membership",
        subtitle = "You are an active regular member. Apply for life membership and pay the upgrade fee.",
        icon = SsplIcons.WorkspacePremium
    ) {
        when {
            state.lifePlans.isNotEmpty() -> Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                state.lifePlans.forEach { plan ->
                    val sel = plan.id == state.selectedLifePlanId
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(AppTheme.shapes.small)
                            .background(if (sel) c.surfaceTint else c.surface)
                            .border(if (sel) 1.5.dp else 1.dp, if (sel) c.primary else c.border, AppTheme.shapes.small)
                            .clickable { vm.selectLifePlan(plan.id) }
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            if (sel) SsplIcons.CheckCircle else SsplIcons.RadioButtonUnchecked,
                            contentDescription = if (sel) "Selected" else null,
                            tint = if (sel) c.primary else c.muted,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(plan.name, style = AppTheme.typography.titleSmall)
                            Text("One-time · lifetime validity", style = AppTheme.typography.bodySmall)
                        }
                        Text(formatRupees(plan.displayFee), style = AppTheme.typography.number.copy(fontSize = AppTheme.typography.titleLarge.fontSize, color = c.primary))
                    }
                }
            }
            state.plansError != null -> PortalNotice("Life plans didn't load: ${state.plansError}. Pull down to retry.", NoticeTone.Error)
            else -> PortalNotice("No life membership plan is open right now.", NoticeTone.Info)
        }

        Spacer(Modifier.height(16.dp))
        Text("Verify your details before payment", style = AppTheme.typography.titleSmall)
        Text("Payment requires a valid mobile number and email. Update any incorrect fields below.", style = AppTheme.typography.bodySmall)
        Spacer(Modifier.height(10.dp))
        Row(
            Modifier.fillMaxWidth().clip(AppTheme.shapes.small).background(c.boxColor).padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconTile(SsplIcons.Email, size = 32.dp, iconSize = 16.dp, corner = 10.dp, background = c.surface)
            Spacer(Modifier.width(10.dp))
            Column {
                Text("Email", style = AppTheme.typography.labelSmall)
                Text(snap.email ?: "Not set", style = AppTheme.typography.bodyMedium.copy(color = c.heading))
            }
        }
        Spacer(Modifier.height(12.dp))
        SsplTextField(
            value = f[UpgradeField.PHONE].orEmpty(),
            onValueChange = { vm.onFieldChange(UpgradeField.PHONE, it) },
            label = "Mobile number (for payment) *",
            placeholder = "03001234567",
            leadingIcon = SsplIcons.Phone,
            keyboardType = KeyboardType.Phone,
            error = err[UpgradeField.PHONE]
        )
        Spacer(Modifier.height(10.dp))
        SsplTextField(
            value = f[UpgradeField.WHATSAPP].orEmpty(),
            onValueChange = { vm.onFieldChange(UpgradeField.WHATSAPP, it) },
            label = "WhatsApp number",
            placeholder = "03001234567",
            leadingIcon = SsplIcons.Phone,
            keyboardType = KeyboardType.Phone,
            error = err[UpgradeField.WHATSAPP]
        )
        Spacer(Modifier.height(14.dp))
        Row(
            Modifier.fillMaxWidth().clip(AppTheme.shapes.small).background(c.surfaceTint).padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(SsplIcons.School, contentDescription = null, tint = c.primary, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(10.dp))
            Text(
                "$POSTGRAD_MEMBERSHIP_NOTICE MBBS is mandatory. Any one of FCPS, FRCS, or MS is required.",
                style = AppTheme.typography.bodySmall.copy(color = c.body)
            )
        }
        Spacer(Modifier.height(12.dp))
        listOf(
            Triple("MBBS", UpgradeField.MBBS_INST, UpgradeField.MBBS_YEAR),
            Triple("FCPS", UpgradeField.FCPS_INST, UpgradeField.FCPS_YEAR),
            Triple("FRCS", UpgradeField.FRCS_INST, UpgradeField.FRCS_YEAR),
            Triple("MS", UpgradeField.MS_INST, UpgradeField.MS_YEAR)
        ).forEach { (q, inst, year) ->
            val req = q == "MBBS"
            Row(Modifier.fillMaxWidth().padding(bottom = 10.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                SsplTextField(
                    value = f[inst].orEmpty(),
                    onValueChange = { vm.onFieldChange(inst, it) },
                    label = "$q institute${if (req) " *" else ""}",
                    error = err[inst],
                    modifier = Modifier.weight(1.6f)
                )
                SsplTextField(
                    value = f[year].orEmpty(),
                    onValueChange = { vm.onFieldChange(year, it) },
                    label = "Year${if (req) " *" else ""}",
                    placeholder = "YYYY",
                    keyboardType = KeyboardType.Number,
                    error = err[year],
                    modifier = Modifier.weight(1f)
                )
            }
        }
        state.formError?.let {
            PortalNotice(it, NoticeTone.Error)
            Spacer(Modifier.height(12.dp))
        }
        PrimaryButton(
            text = "Pay and apply for life membership",
            onClick = vm::upgrade,
            loading = state.isUpgrading,
            enabled = !state.isUpgrading && state.selectedLifePlan != null,
            icon = SsplIcons.WorkspacePremium
        )
        state.selectedLifePlan?.let {
            Spacer(Modifier.height(6.dp))
            Text(
                "You'll pay ${formatRupees(it.displayFee)} securely through Swich.",
                style = AppTheme.typography.bodySmall,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
