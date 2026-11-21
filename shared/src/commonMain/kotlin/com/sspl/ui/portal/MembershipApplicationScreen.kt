package com.sspl.ui.portal

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.preat.peekaboo.image.picker.ResizeOptions
import com.preat.peekaboo.image.picker.SelectionMode
import com.preat.peekaboo.image.picker.rememberImagePickerLauncher
import com.sspl.Screen
import com.sspl.core.models.MembershipPlan
import com.sspl.designsystem.components.BlockingLoader
import com.sspl.designsystem.components.EmptyState
import com.sspl.designsystem.components.ErrorState
import com.sspl.designsystem.components.LoadState
import com.sspl.designsystem.components.LocalShellActions
import com.sspl.designsystem.components.PrimaryButton
import com.sspl.designsystem.components.SecondaryButton
import com.sspl.designsystem.components.SegmentedTabs
import com.sspl.designsystem.components.SkeletonDetail
import com.sspl.designsystem.components.SsplImage
import com.sspl.designsystem.components.SsplTextField
import com.sspl.designsystem.components.StickyCtaBar
import com.sspl.designsystem.icons.SsplIcons
import com.sspl.designsystem.theme.AppTheme
import com.sspl.ui.components.PaymentWebView
import kotlinx.coroutines.launch
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

/** Nav args (savedStateHandle on the calling entry), same pattern as "eventId" / "certSerial". */
const val ARG_MEMBERSHIP_PLAN_ID = "membershipPlanId"
const val ARG_IS_ALREADY_MEMBER = "isAlreadyMember"

/** Opens the membership application for [planId] (web `navigate('/membership-application', { state })`). */
fun NavController.openMembershipApplication(planId: Long, isAlreadyMember: Boolean) {
    currentBackStackEntry?.savedStateHandle?.set(ARG_MEMBERSHIP_PLAN_ID, planId)
    currentBackStackEntry?.savedStateHandle?.set(ARG_IS_ALREADY_MEMBER, isAlreadyMember)
    navigate(Screen.MembershipApplicationScreen.route)
}

/** Opens the plans list, optionally in "already a member" mode (web MembershipPage "Already a member?"). */
fun NavController.openMembershipPlans(isAlreadyMember: Boolean) {
    currentBackStackEntry?.savedStateHandle?.set(ARG_IS_ALREADY_MEMBER, isAlreadyMember)
    navigate(Screen.MembershipPlansScreen.route)
}

private enum class FormSection { Identity, Qualifications, Contact }

/**
 * D3b: Membership application (web `/membership-application`).
 * Plan summary → professional identity → qualifications → workplace & contact (photo) → sticky submit.
 */
@Composable
fun MembershipApplicationScreen(navController: NavController) {
    val args = remember {
        val h = navController.previousBackStackEntry?.savedStateHandle
        h?.get<Long>(ARG_MEMBERSHIP_PLAN_ID) to (h?.get<Boolean>(ARG_IS_ALREADY_MEMBER) ?: false)
    }
    val viewModel: MembershipApplicationViewModel = portalViewModel {
        MembershipApplicationViewModel(it.get(), args.first, args.second)
    }
    val state by viewModel.state.collectAsStateWithLifecycle()
    val shell = LocalShellActions.current

    if (shell.isGuest) {
        PortalGuestGate(onSignIn = shell.onAvatar, modifier = Modifier.fillMaxSize())
        return
    }
    if (state.missingPlan) {
        EmptyState(
            title = "Choose a plan first",
            message = "Select a membership plan to start your application.",
            icon = SsplIcons.CardMembership,
            actionText = "View membership plans",
            onActionClick = { navController.openMembershipPlans(state.isAlreadyMember) },
            modifier = Modifier.fillMaxSize()
        )
        return
    }

    state.paymentUrl?.let { url ->
        // Self-contained full-screen dialog — not wrapped.
        PaymentWebView(
            url = url,
            onPaymentSuccess = { viewModel.onPaymentFinished(true) },
            onPaymentFailed = { viewModel.onPaymentFinished(false) },
            onClose = { viewModel.onPaymentFinished(false) }
        )
    }
    if (state.isSubmitting) {
        BlockingLoader(if (state.isAlreadyMember) "Submitting your application…" else "Starting secure payment…")
    }

    when (val c = state.content) {
        is LoadState.Loading -> SkeletonDetail(Modifier.fillMaxSize())
        is LoadState.Error -> ErrorState(
            title = "Application didn't load",
            message = c.message,
            onRetry = viewModel::retry,
            modifier = Modifier.fillMaxSize()
        )
        is LoadState.Ready -> when {
            state.outcome != null -> OutcomeView(state, navController)
            state.alreadyActive -> EmptyState(
                title = "You're already a member",
                message = "Your membership is active. Manage it from your membership page.",
                icon = SsplIcons.Verified,
                actionText = "Go to membership",
                onActionClick = {
                    navController.navigate(Screen.MembershipScreen.route) {
                        popUpTo(Screen.MembershipScreen.route) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
            else -> ApplicationForm(state, c.data.plan, viewModel, navController)
        }
    }
}

@OptIn(ExperimentalEncodingApi::class)
@Composable
private fun ApplicationForm(
    state: MembershipApplicationUiState,
    plan: MembershipPlan,
    vm: MembershipApplicationViewModel,
    navController: NavController
) {
    val c = AppTheme.colors
    val focus = LocalFocusManager.current
    val scroll = rememberScrollState()
    val scope = rememberCoroutineScope()
    val sectionY = remember { mutableStateMapOf<FormSection, Int>() }
    val next = KeyboardActions(onNext = { focus.moveFocus(FocusDirection.Down) })
    val done = KeyboardActions(onDone = { focus.clearFocus() })

    val launcher = rememberImagePickerLauncher(
        selectionMode = SelectionMode.Single,
        scope = scope,
        resizeOptions = ResizeOptions(width = 1024, height = 1024, resizeThresholdBytes = 0L, compressionQuality = 0.85),
        onResult = { list -> list.firstOrNull()?.let(vm::onPhotoPicked) }
    )

    // Scroll to the first section with an error after a failed submit.
    LaunchedEffect(state.validationAttempt) {
        if (state.validationAttempt == 0 || state.errors.isEmpty()) return@LaunchedEffect
        val e = state.errors.keys
        val section = when {
            AppField.PMDC in e -> FormSection.Identity
            e.any { it.startsWith("basic_") || it.startsWith("adv_") } -> FormSection.Qualifications
            else -> FormSection.Contact
        }
        sectionY[section]?.let { scroll.animateScrollTo((it - 24).coerceAtLeast(0)) }
    }
    LaunchedEffect(state.formError) { if (state.formError != null) scroll.animateScrollTo(0) }

    val photoSource = remember(state.newPhoto, state.existingPhotoUrl) {
        state.newPhoto?.let { "data:image/jpeg;base64," + Base64.encode(it) } ?: state.existingPhotoUrl
    }
    val payable = !state.isAlreadyMember && plan.displayFee > 0

    Column(Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .imePadding()
                .verticalScroll(scroll)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            PortalHero(
                eyebrow = if (state.isAlreadyMember) "Existing member" else "Membership application",
                title = plan.name,
                summary = if (state.profilePrefilled)
                    "Your saved profile is loaded below. Review, update if needed, then submit."
                else "Complete your professional profile to apply for membership.",
                icon = if (state.isAlreadyMember) SsplIcons.HowToReg else SsplIcons.Shield,
                stats = buildList {
                    if (!state.isAlreadyMember) add(formatRupees(plan.displayFee) to (if (plan.type.equals("LIFE", true)) "Lifetime access" else "Annual billing"))
                    add("Lahore" to "Chapter")
                    add((if (plan.type.equals("LIFE", true)) "Unlimited" else "1 Year") to "Duration")
                }
            )

            state.formError?.let { PortalNotice(it, NoticeTone.Error, onDismiss = vm::dismissError) }
            if (state.profilePrefilled) {
                PortalNotice(
                    "Profile data and photo loaded from your account. You only need to change fields that are outdated.",
                    NoticeTone.Info
                )
            }
            if (state.isAlreadyMember) {
                PortalNotice(
                    "Profile update only — no payment required. Your membership will be activated after the office verifies it.",
                    NoticeTone.Info
                )
            }

            // --- Professional identity ---
            PortalSection(
                title = "Professional identity",
                icon = SsplIcons.Badge,
                modifier = Modifier.onGloballyPositioned { sectionY[FormSection.Identity] = it.positionInParent().y.toInt() }
            ) {
                FieldLabel("Title")
                SegmentedTabs(
                    options = TITLE_OPTIONS,
                    selectedIndex = TITLE_OPTIONS.indexOf(state.title).coerceAtLeast(0),
                    onSelect = { vm.setTitle(TITLE_OPTIONS[it]) }
                )
                Spacer(Modifier.height(12.dp))
                SsplTextField(
                    value = state.pmdcNumber,
                    onValueChange = vm::setPmdc,
                    label = "PMDC registration number *",
                    placeholder = "12345-P",
                    leadingIcon = SsplIcons.Description,
                    error = state.errors[AppField.PMDC],
                    imeAction = ImeAction.Next,
                    keyboardActions = next
                )
            }

            // --- Medical qualifications ---
            PortalSection(
                title = "Medical qualifications",
                icon = SsplIcons.School,
                modifier = Modifier.onGloballyPositioned { sectionY[FormSection.Qualifications] = it.positionInParent().y.toInt() }
            ) {
                SubHeading("Basic qualification *")
                SsplTextField(value = "MBBS", onValueChange = {}, label = "Degree", leadingIcon = SsplIcons.School, enabled = false)
                Spacer(Modifier.height(10.dp))
                SsplTextField(
                    value = state.basic.institute,
                    onValueChange = vm::setBasicInstitute,
                    label = "Institution / University",
                    placeholder = "e.g., King Edward Med Univ",
                    leadingIcon = SsplIcons.Apartment,
                    error = state.errors[AppField.BASIC_INST],
                    imeAction = ImeAction.Next,
                    keyboardActions = next
                )
                Spacer(Modifier.height(10.dp))
                SsplTextField(
                    value = state.basic.passingYear,
                    onValueChange = vm::setBasicYear,
                    label = "Passing year",
                    placeholder = "YYYY",
                    leadingIcon = SsplIcons.CalendarMonth,
                    keyboardType = KeyboardType.Number,
                    error = state.errors[AppField.BASIC_YEAR],
                    imeAction = ImeAction.Next,
                    keyboardActions = next
                )

                Divider()
                SubHeading("Advance qualification *")
                Text(
                    "$POSTGRAD_MEMBERSHIP_NOTICE MBBS is mandatory. Any one of FCPS, FRCS, or MS is required.",
                    style = AppTheme.typography.bodySmall
                )
                Spacer(Modifier.height(10.dp))
                FieldLabel("Degree")
                SegmentedTabs(
                    options = ADVANCE_DEGREES,
                    selectedIndex = ADVANCE_DEGREES.indexOf(state.advance.degree).coerceAtLeast(0),
                    onSelect = { vm.setAdvanceDegree(ADVANCE_DEGREES[it]) }
                )
                Spacer(Modifier.height(10.dp))
                SsplTextField(
                    value = state.advance.institute,
                    onValueChange = vm::setAdvanceInstitute,
                    label = "Institution / University",
                    placeholder = "${state.advance.degree} Institute",
                    leadingIcon = SsplIcons.Apartment,
                    error = state.errors[AppField.ADV_INST],
                    imeAction = ImeAction.Next,
                    keyboardActions = next
                )
                Spacer(Modifier.height(10.dp))
                SsplTextField(
                    value = state.advance.passingYear,
                    onValueChange = vm::setAdvanceYear,
                    label = "Passing year",
                    placeholder = "${state.advance.degree} Passing Year",
                    leadingIcon = SsplIcons.CalendarMonth,
                    keyboardType = KeyboardType.Number,
                    error = state.errors[AppField.ADV_YEAR],
                    imeAction = ImeAction.Next,
                    keyboardActions = next
                )

                Divider()
                SubHeading("Other education (optional)")
                state.others.forEachIndexed { index, q ->
                    if (index > 0) Spacer(Modifier.height(12.dp))
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .clip(AppTheme.shapes.small)
                            .background(c.surfaceTint)
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Qualification ${index + 1}", style = AppTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                            Box(
                                Modifier.size(48.dp).clip(CircleShape).clickable { vm.removeOther(q.id) },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(SsplIcons.DeleteOutline, contentDescription = "Remove qualification", tint = c.error, modifier = Modifier.size(20.dp))
                            }
                        }
                        SsplTextField(
                            value = q.degree, onValueChange = { vm.updateOther(q.id, degree = it) },
                            label = "Degree", leadingIcon = SsplIcons.School,
                            imeAction = ImeAction.Next, keyboardActions = next
                        )
                        SsplTextField(
                            value = q.institute, onValueChange = { vm.updateOther(q.id, institute = it) },
                            label = "Institution / University", placeholder = "Institution Name",
                            leadingIcon = SsplIcons.Apartment, imeAction = ImeAction.Next, keyboardActions = next
                        )
                        SsplTextField(
                            value = q.passingYear, onValueChange = { vm.updateOther(q.id, year = it) },
                            label = "Passing year", placeholder = "YYYY", leadingIcon = SsplIcons.CalendarMonth,
                            keyboardType = KeyboardType.Number, imeAction = ImeAction.Done, keyboardActions = done
                        )
                    }
                }
                Spacer(Modifier.height(10.dp))
                SecondaryButton(
                    text = "Add other education",
                    onClick = vm::addOther,
                    enabled = state.canAddOther,
                    icon = SsplIcons.Add,
                    modifier = Modifier.fillMaxWidth()
                )
                if (!state.canAddOther) {
                    Text(
                        "Please complete the current qualification first",
                        style = AppTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            // --- Workplace & contact ---
            PortalSection(
                title = "Workplace & contact information",
                icon = SsplIcons.Business,
                modifier = Modifier.onGloballyPositioned { sectionY[FormSection.Contact] = it.positionInParent().y.toInt() }
            ) {
                val photoError = state.errors[AppField.PHOTO]
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clip(AppTheme.shapes.small)
                        .background(c.surfaceTint)
                        .border(1.dp, if (photoError != null) c.error else c.border, AppTheme.shapes.small)
                        .clickable { launcher.launch() }
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(Modifier.size(72.dp).clip(CircleShape).background(c.surface), contentAlignment = Alignment.Center) {
                        if (photoSource != null) {
                            SsplImage(source = photoSource, contentDescription = "Profile photo preview", modifier = Modifier.fillMaxSize())
                        } else {
                            Icon(SsplIcons.Person, contentDescription = null, tint = c.muted, modifier = Modifier.size(32.dp))
                        }
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Profile picture *", style = AppTheme.typography.titleSmall)
                        Text(
                            if (state.existingPhotoUrl != null && state.newPhoto == null)
                                "Using your saved profile photo. Upload a new file only if you want to replace it."
                            else "Please upload a passport size picture with a white or blue background.",
                            style = AppTheme.typography.bodySmall
                        )
                        Spacer(Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(SsplIcons.CameraAlt, contentDescription = null, tint = c.primary, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(
                                if (photoSource != null) "Change photo" else "Choose photo",
                                style = AppTheme.typography.labelMedium.copy(color = c.primary, fontWeight = FontWeight.SemiBold)
                            )
                        }
                    }
                }
                AnimatedVisibility(photoError != null) {
                    Text(
                        photoError.orEmpty(),
                        style = AppTheme.typography.labelSmall.copy(color = c.error),
                        modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                    )
                }
                Spacer(Modifier.height(12.dp))
                SsplTextField(
                    value = state.hospital,
                    onValueChange = vm::setHospital,
                    label = "Hospital / Clinic name *",
                    placeholder = "e.g., Services Hospital Lahore",
                    leadingIcon = SsplIcons.Apartment,
                    error = state.errors[AppField.HOSPITAL],
                    imeAction = ImeAction.Next,
                    keyboardActions = next
                )
                Spacer(Modifier.height(10.dp))
                SsplTextField(
                    value = state.address,
                    onValueChange = vm::setAddress,
                    label = "Address (Hospital/Clinic or Residence) *",
                    placeholder = "Full address with city",
                    leadingIcon = SsplIcons.LocationOn,
                    error = state.errors[AppField.ADDRESS],
                    imeAction = ImeAction.Next,
                    keyboardActions = next
                )
                Spacer(Modifier.height(10.dp))
                SsplTextField(
                    value = state.accountPhone,
                    onValueChange = vm::setPhone,
                    label = "Mobile number (for payment) *",
                    placeholder = "03001234567",
                    leadingIcon = SsplIcons.PhoneAndroid,
                    keyboardType = KeyboardType.Phone,
                    error = state.errors[AppField.PHONE],
                    imeAction = ImeAction.Next,
                    keyboardActions = next
                )
                Spacer(Modifier.height(10.dp))
                SsplTextField(
                    value = state.whatsapp,
                    onValueChange = vm::setWhatsapp,
                    label = "WhatsApp number *",
                    placeholder = "03001234567",
                    leadingIcon = SsplIcons.Chat,
                    keyboardType = KeyboardType.Phone,
                    error = state.errors[AppField.WHATSAPP],
                    imeAction = ImeAction.Done,
                    keyboardActions = KeyboardActions(onDone = { focus.clearFocus(); vm.submit() })
                )
            }

            PortalNotice(
                title = "Important note",
                message = "Please ensure all medical registration details match your official PMDC documents for quick approval.",
                tone = NoticeTone.Warning
            )
            Row(
                Modifier.fillMaxWidth().clickable { navController.navigateUp() }.heightIn(min = 48.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(SsplIcons.ArrowBack, contentDescription = null, tint = c.muted, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Back to plans", style = AppTheme.typography.labelMedium)
            }
        }

        StickyCtaBar(
            primaryText = if (payable) "Pay & apply" else "Submit application",
            onPrimaryClick = { focus.clearFocus(); vm.submit() },
            priceText = if (state.isAlreadyMember) null else formatRupees(plan.displayFee),
            priceSubtext = if (state.isAlreadyMember) null else plan.name,
            loading = state.isSubmitting,
            enabled = !state.isSubmitting
        )
    }
}

@Composable
private fun OutcomeView(state: MembershipApplicationUiState, navController: NavController) {
    val paid = state.outcome == ApplicationOutcome.Paid
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        PortalHero(
            eyebrow = "Membership",
            title = if (paid) "Payment received" else "Application submitted",
            summary = "Your membership application is under review. You will receive your member role after approval.",
            icon = SsplIcons.CheckCircle
        )
        PortalNotice(
            title = "Membership application pending",
            message = if (paid) "Payment received. Your membership application is now under review."
            else "Your membership application is under review. You will receive your member role after approval.",
            tone = NoticeTone.Success
        )
        PrimaryButton(
            text = "Go to membership",
            onClick = {
                navController.navigate(Screen.MembershipScreen.route) {
                    popUpTo(Screen.MembershipScreen.route) { inclusive = true }
                    launchSingleTop = true
                }
            }
        )
        SecondaryButton(
            text = "Back to member desk",
            onClick = {
                navController.navigate(Screen.PortalOverviewScreen.route) {
                    popUpTo(Screen.PortalOverviewScreen.route) { inclusive = true }
                    launchSingleTop = true
                }
            },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun FieldLabel(text: String) {
    Text(
        text,
        style = AppTheme.typography.labelMedium.copy(color = AppTheme.colors.heading, fontWeight = FontWeight.Medium),
        modifier = Modifier.padding(bottom = 6.dp)
    )
}

@Composable
private fun SubHeading(text: String) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 8.dp)) {
        Icon(SsplIcons.School, contentDescription = null, tint = AppTheme.colors.primary, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(8.dp))
        Text(text, style = AppTheme.typography.titleSmall)
    }
}

@Composable
private fun Divider() {
    Spacer(Modifier.height(14.dp))
    Box(Modifier.fillMaxWidth().height(1.dp).background(AppTheme.colors.divider))
    Spacer(Modifier.height(14.dp))
}
