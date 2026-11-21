package com.sspl.ui.userdetails

import com.sspl.designsystem.icons.SsplIcons

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CardMembership
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.preat.peekaboo.image.picker.ResizeOptions
import com.preat.peekaboo.image.picker.SelectionMode
import com.preat.peekaboo.image.picker.rememberImagePickerLauncher
import com.sspl.Screen
import com.sspl.core.ApiStates
import com.sspl.core.auth.RoleUtils
import com.sspl.core.models.User
import com.sspl.designsystem.components.AvatarImage
import com.sspl.designsystem.components.BlockingLoader
import com.sspl.designsystem.components.ConfirmDialog
import com.sspl.designsystem.components.ErrorState
import com.sspl.designsystem.components.FeedbackBanner
import com.sspl.designsystem.components.GlassPill
import com.sspl.designsystem.components.GradientHeroCard
import com.sspl.designsystem.components.IconTile
import com.sspl.designsystem.components.InfoGrid
import com.sspl.designsystem.components.ListRowCard
import com.sspl.designsystem.components.NavTile
import com.sspl.designsystem.components.PrimaryButton
import com.sspl.designsystem.components.SectionHeader
import com.sspl.designsystem.components.SkeletonBox
import com.sspl.designsystem.components.SkeletonCircle
import com.sspl.designsystem.components.SkeletonLine
import com.sspl.designsystem.components.SkeletonList
import com.sspl.designsystem.components.SkeletonVariant
import com.sspl.designsystem.components.SsplCard
import com.sspl.designsystem.components.SsplRefreshBox
import com.sspl.designsystem.components.StatusBadge
import com.sspl.designsystem.components.BadgeVariant
import com.sspl.designsystem.theme.AppTheme
import kotlinx.serialization.json.Json
import org.koin.compose.viewmodel.koinViewModel

/**
 * My profile (inside the main shell — the shell top bar owns the status-bar inset).
 * Shows the cached session user immediately and refreshes from GET /users/me in the background.
 */
@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel = koinViewModel(),
    onParentGraph: () -> Unit = {}
) {
    val userDetails by viewModel.userDetails.collectAsStateWithLifecycle()
    val cachedUser by viewModel.user.collectAsStateWithLifecycle()
    val onSignedOut by viewModel.onSignedOut.collectAsStateWithLifecycle()
    val isGuestUser by viewModel.isGuestUser.collectAsStateWithLifecycle()
    val profileImageBase64 by viewModel.profileImage.collectAsStateWithLifecycle()

    var showSignOutDialog by remember { mutableStateOf(false) }
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val isUploadingPhoto by viewModel.isUploadingPhoto.collectAsStateWithLifecycle()
    val isSigningOut by viewModel.isSigningOut.collectAsStateWithLifecycle()

    // Always re-encode as JPEG and downscale so the longest side is <= 1024 px before upload.
    val launcher = rememberImagePickerLauncher(
        selectionMode = SelectionMode.Single,
        scope = scope,
        resizeOptions = ResizeOptions(
            width = 1024,
            height = 1024,
            resizeThresholdBytes = 0L,
            compressionQuality = 0.85
        ),
        onResult = { byteArrays -> byteArrays.firstOrNull()?.let { viewModel.updateProfileImage(it) } }
    )

    LaunchedEffect(isGuestUser) {
        if (isGuestUser == false) viewModel.getUserDetails()
    }
    LaunchedEffect(Unit) {
        viewModel.messages.collect { snackbar.showSnackbar(it) }
    }
    LaunchedEffect(onSignedOut) {
        if (onSignedOut) onParentGraph()
    }

    if (showSignOutDialog) {
        ConfirmDialog(
            title = "Sign out?",
            message = "You'll need to sign in again to access your membership, registrations and certificates.",
            confirmText = "Sign out",
            dismissText = "Cancel",
            onConfirm = {
                showSignOutDialog = false
                viewModel.onSignOut()
            },
            onDismiss = { showSignOutDialog = false }
        )
    }

    Box(Modifier.fillMaxSize().background(AppTheme.colors.background)) {
        when {
            isGuestUser == null -> ProfileSkeleton()
            isGuestUser == true -> GuestProfileContent(onSignIn = onParentGraph)
            else -> {
                val state = userDetails
                val fresh = (state as? ApiStates.Success)?.data
                // Session user first: GET /auth/me and the edit screen both write to the session,
                // so it is always the most recent copy.
                val user = cachedUser ?: fresh
                val isLoading = state is ApiStates.Loading
                val failure = state as? ApiStates.Failure

                when {
                    user == null && isLoading -> ProfileSkeleton()
                    user == null && failure != null -> ErrorState(
                        title = "Couldn't load your profile",
                        message = failure.error.message.ifBlank { "Check your connection and try again." },
                        onRetry = { viewModel.getUserDetails() },
                        modifier = Modifier.fillMaxSize()
                    )
                    user == null -> ErrorState(
                        title = "Profile unavailable",
                        message = "We couldn't find your profile details. Pull to refresh or sign in again.",
                        onRetry = { viewModel.getUserDetails() },
                        modifier = Modifier.fillMaxSize()
                    )
                    else -> SsplRefreshBox(
                        isRefreshing = isLoading,
                        onRefresh = { viewModel.getUserDetails() },
                        modifier = Modifier.fillMaxSize()
                    ) {
                        AuthenticatedProfileContent(
                            user = user,
                            profileImageBase64 = profileImageBase64,
                            refreshError = failure?.error?.message,
                            onRetry = { viewModel.getUserDetails() },
                            onEditPersonal = { navigateToEdit(navController, user, personal = true) },
                            onEditProfessional = { navigateToEdit(navController, user, personal = false) },
                            onChangePhoto = { launcher.launch() },
                            onOpen = { route -> navController.navigate(route) },
                            onSignOutClick = { showSignOutDialog = true }
                        )
                    }
                }
            }
        }
        SnackbarHost(snackbar, Modifier.align(Alignment.BottomCenter).padding(12.dp))
    }

    if (isUploadingPhoto) BlockingLoader("Uploading photo…")
    if (isSigningOut) BlockingLoader("Signing out…")
}

private fun navigateToEdit(navController: NavController, user: User, personal: Boolean) {
    navController.currentBackStackEntry?.savedStateHandle?.set("isPersonalDetails", personal)
    navController.currentBackStackEntry?.savedStateHandle?.set("currentUser", Json.encodeToString(User.serializer(), user))
    navController.navigate(Screen.EditProfile.route)
}

// ---------------------------------------------------------------------------------------------
// Guest
// ---------------------------------------------------------------------------------------------

@Composable
private fun GuestProfileContent(onSignIn: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        GradientHeroCard(contentPadding = 20.dp) {
            GlassPill("GUEST ACCESS")
            Spacer(Modifier.height(14.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                AvatarImage(
                    name = "Guest",
                    initials = "G",
                    size = 56.dp,
                    modifier = Modifier.border(2.dp, Color.White.copy(alpha = 0.55f), CircleShape)
                )
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text("You're browsing as a guest", style = AppTheme.typography.titleMedium.copy(color = Color.White))
                    Text(
                        "Sign in to unlock your member portal",
                        style = AppTheme.typography.bodySmall.copy(color = Color.White.copy(alpha = 0.75f))
                    )
                }
            }
        }

        SectionHeader(title = "With an SSPL account you can")
        GuestBenefit(SsplIcons.CardMembership, "Manage your membership", "Apply, renew and track your plan status")
        GuestBenefit(SsplIcons.EventAvailable, "Register for events", "Conferences, workshops and CME sessions")
        GuestBenefit(SsplIcons.WorkspacePremium, "Download certificates", "All your CME and attendance certificates")

        Spacer(Modifier.height(4.dp))
        PrimaryButton(text = "Sign in or create account", onClick = onSignIn, icon = SsplIcons.Login)
    }
}

@Composable
private fun GuestBenefit(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, subtitle: String) {
    ListRowCard(
        title = title,
        subtitle = subtitle,
        leading = { IconTile(icon) }
    )
}

// ---------------------------------------------------------------------------------------------
// Member
// ---------------------------------------------------------------------------------------------

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AuthenticatedProfileContent(
    user: User,
    profileImageBase64: String?,
    refreshError: String?,
    onRetry: () -> Unit,
    onEditPersonal: () -> Unit,
    onEditProfessional: () -> Unit,
    onChangePhoto: () -> Unit,
    onOpen: (String) -> Unit,
    onSignOutClick: () -> Unit
) {
    val roles = remember(user) { RoleUtils.getUserRoles(user).filter { it != "USER" } }
    val membershipLabel = remember(user) { RoleUtils.getMembershipLabel(user) }
    val activePlan = remember(user) { RoleUtils.hasActiveMembershipPlan(user) }
    val notProvided = "Not provided"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (refreshError != null) {
            Column(Modifier.clickable(onClick = onRetry)) {
                FeedbackBanner(
                    message = "Couldn't refresh your profile. Showing saved details — tap to retry.",
                    isError = true
                )
            }
        }

        // Header card
        GradientHeroCard(contentPadding = 18.dp) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(80.dp)) {
                    AvatarImage(
                        name = user.fullName().ifBlank { user.account?.email.orEmpty() },
                        base64Image = profileImageBase64,
                        imageUrl = user.profilePicture?.let { RoleUtils.getProfileImageUrl(it) }?.takeIf { it.isNotBlank() },
                        initials = RoleUtils.getUserInitials(user),
                        size = 76.dp,
                        modifier = Modifier.border(2.dp, Color.White.copy(alpha = 0.6f), CircleShape)
                    )
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .clickable(onClick = onChangePhoto),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            SsplIcons.CameraAlt,
                            contentDescription = "Change profile photo",
                            tint = AppTheme.colors.gradientMid,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        text = RoleUtils.getUserDisplayName(user),
                        style = AppTheme.typography.titleLarge.copy(color = Color.White),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = user.account?.email.orEmpty(),
                        style = AppTheme.typography.bodySmall.copy(color = Color.White.copy(alpha = 0.75f)),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Spacer(Modifier.height(14.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val badge = profileRoleBadge(user)
                GlassPill(badge.first, icon = badge.second)
                roles.take(4).forEach { GlassPill(it.replace('_', ' ')) }
            }
        }

        // Personal info
        Column {
            SectionHeader(title = "Personal info", actionText = "Edit", onActionClick = onEditPersonal)
            SsplCard {
                InfoGrid(
                    items = listOf(
                        "Full name" to user.fullName().ifBlank { notProvided },
                        "Gender" to (user.gender?.takeIf { it.isNotBlank() }?.lowercase()
                            ?.replaceFirstChar { it.uppercase() } ?: notProvided),
                        "Phone" to (user.account?.phone?.takeIf { it.isNotBlank() } ?: notProvided),
                        "WhatsApp" to (user.whatsapp?.takeIf { it.isNotBlank() } ?: notProvided),
                        "Email" to (user.account?.email ?: notProvided),
                        "City" to (user.city?.takeIf { it.isNotBlank() } ?: notProvided),
                        "Country" to (user.country?.takeIf { it.isNotBlank() } ?: notProvided),
                        "Address" to (user.address?.takeIf { it.isNotBlank() } ?: notProvided)
                    )
                )
            }
        }

        // Professional
        Column {
            SectionHeader(title = "Professional", actionText = "Edit", onActionClick = onEditProfessional)
            SsplCard {
                InfoGrid(
                    items = listOf(
                        "PMDC number" to (user.pmdcNo?.takeIf { it.isNotBlank() } ?: notProvided),
                        "Designation" to (user.designation?.takeIf { it.isNotBlank() } ?: notProvided),
                        "Hospital / clinic" to (user.hospital?.takeIf { it.isNotBlank() } ?: notProvided),
                        "Institute" to (user.instituteName?.takeIf { it.isNotBlank() } ?: notProvided),
                        "Society reg. no." to (user.profile?.orgNumber?.takeIf { it.isNotBlank() } ?: notProvided)
                    )
                )
            }
        }

        // Education (web ProfilePage "Education")
        Column {
            SectionHeader(title = "Education", actionText = "Edit", onActionClick = onEditProfessional)
            SsplCard {
                val p = user.profile
                InfoGrid(
                    items = buildList {
                        add("MBBS" to educationLine(p?.mbbsInstitute, p?.mbbsPassingYear, notProvided))
                        add("FCPS" to educationLine(p?.fcpsInstitute, p?.fcpsPassingYear, notProvided))
                        if (!p?.frcsInstitute.isNullOrBlank() || !p?.frcsPassingYear.isNullOrBlank()) {
                            add("FRCS" to educationLine(p?.frcsInstitute, p?.frcsPassingYear, notProvided))
                        }
                        add("MS" to educationLine(p?.msInstitute, p?.msPassingYear, notProvided))
                    }
                )
            }
        }

        // Membership
        Column {
            SectionHeader(title = "Membership")
            ListRowCard(
                title = membershipLabel,
                subtitle = if (activePlan) "Your membership is active" else "No active membership plan",
                meta = if (activePlan) null else "View plans and apply",
                leading = { IconTile(SsplIcons.CardMembership) },
                trailing = {
                    StatusBadge(
                        status = if (activePlan) "Active" else (user.membershipStatus?.replace('_', ' ')?.lowercase()
                            ?.replaceFirstChar { it.uppercase() } ?: "Inactive"),
                        variant = if (activePlan) BadgeVariant.SUCCESS
                        else if (user.membershipStatus.equals("PENDING", true)) BadgeVariant.WARNING
                        else BadgeVariant.NEUTRAL
                    )
                },
                onClick = { onOpen(Screen.MembershipScreen.route) }
            )
        }

        // Shortcuts
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            SectionHeader(title = "Shortcuts")
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                NavTile(
                    icon = SsplIcons.WorkspacePremium,
                    title = "Certificates",
                    subtitle = "CME & attendance",
                    onClick = { onOpen(Screen.MyCertificatesScreen.route) },
                    modifier = Modifier.weight(1f)
                )
                NavTile(
                    icon = SsplIcons.EventAvailable,
                    title = "Registrations",
                    subtitle = "Events you joined",
                    onClick = { onOpen(Screen.MyRegistrationsScreen.route) },
                    modifier = Modifier.weight(1f)
                )
            }
            NavTile(
                icon = SsplIcons.Settings,
                title = "Settings",
                subtitle = "Appearance, account and legal",
                onClick = { onOpen(Screen.SettingsScreen.route) },
                modifier = Modifier.fillMaxWidth()
            )
            NavTile(
                icon = SsplIcons.Logout,
                title = "Sign out",
                subtitle = "Sign out of this device",
                onClick = onSignOutClick,
                tint = AppTheme.colors.error,
                tileBackground = AppTheme.colors.errorContainer,
                modifier = Modifier.fillMaxWidth()
            )
        }
        Spacer(Modifier.height(8.dp))
    }
}

private fun educationLine(institute: String?, year: String?, fallback: String): String {
    val parts = listOfNotNull(institute?.takeIf { it.isNotBlank() }, year?.takeIf { it.isNotBlank() })
    return if (parts.isEmpty()) fallback else parts.joinToString(" · ")
}

/**
 * Web ProfilePage.jsx `RoleBadge`: pending review → registered user (role USER) → plan name /
 * role with a life-member or member icon.
 */
private fun profileRoleBadge(user: User): Pair<String, androidx.compose.ui.graphics.vector.ImageVector> {
    if (user.membershipStatus.equals("PENDING", ignoreCase = true)) return "Pending review" to SsplIcons.Schedule
    val role = user.role?.uppercase()?.takeIf { it.isNotBlank() } ?: "USER"
    if (role == "USER") return "Registered user" to SsplIcons.Person
    val planName = user.membershipPlan?.name?.takeIf { it.isNotBlank() }
        ?: user.membershipPlan?.title?.takeIf { it.isNotBlank() }
    val displayRole = (planName ?: user.role.orEmpty()).replace('_', ' ')
    val r = displayRole.uppercase()
    return when {
        r.contains("LIFE") -> displayRole to SsplIcons.WorkspacePremium
        r.contains("ORDINARY") || r.contains("MEMBER") || r.contains("ASSOCIATE") -> displayRole to SsplIcons.CardMembership
        else -> displayRole.ifBlank { "User" } to SsplIcons.Person
    }
}

@Composable
private fun ProfileSkeleton() {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SkeletonBox(Modifier.fillMaxWidth().height(150.dp), AppTheme.shapes.card)
        Row(verticalAlignment = Alignment.CenterVertically) {
            SkeletonCircle(40.dp)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                SkeletonLine(widthFraction = 0.5f, height = 14.dp)
                SkeletonLine(widthFraction = 0.3f, height = 11.dp)
            }
        }
        SkeletonBox(Modifier.fillMaxWidth().height(110.dp), AppTheme.shapes.card)
        SkeletonList(count = 2, variant = SkeletonVariant.COMPACT, contentPadding = 0.dp)
    }
}
