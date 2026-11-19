package com.sspl.ui.userdetails

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.preat.peekaboo.image.picker.SelectionMode
import com.preat.peekaboo.image.picker.rememberImagePickerLauncher
import com.sspl.Screen
import com.sspl.core.ApiStates
import com.sspl.core.auth.RoleUtils
import com.sspl.core.models.User
import com.sspl.designsystem.components.AvatarImage
import com.sspl.designsystem.components.ConfirmDialog
import com.sspl.designsystem.components.EmptyState
import com.sspl.designsystem.components.ErrorState
import com.sspl.designsystem.components.InfoRow
import com.sspl.designsystem.components.MembershipBadge
import com.sspl.designsystem.components.PrimaryButton
import com.sspl.designsystem.components.RoleChip
import com.sspl.designsystem.components.SecondaryButton
import com.sspl.designsystem.components.SectionHeader
import com.sspl.designsystem.components.SsplCard
import com.sspl.designsystem.components.TextButtonLink
import com.sspl.designsystem.theme.AppTheme
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.koin.compose.viewmodel.koinViewModel

/**
 * A7: Revamped Profile View Screen with guest gating, role chips, membership badge & photo edit.
 */
@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel = koinViewModel(),
    onParentGraph: () -> Unit = {}
) {
    val userDetails by viewModel.userDetails.collectAsStateWithLifecycle()
    val onSignedOut by viewModel.onSignedOut.collectAsStateWithLifecycle()
    val isGuestUser by viewModel.isGuestUser.collectAsStateWithLifecycle()
    val profileImageBase64 by viewModel.profileImage.collectAsStateWithLifecycle()

    var showSignOutDialog by remember { mutableStateOf(false) }
    val scaffoldState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val launcher = rememberImagePickerLauncher(
        selectionMode = SelectionMode.Single,
        scope = scope,
        onResult = { byteArrays ->
            byteArrays.firstOrNull()?.let {
                viewModel.updateProfileImage(it)
            }
        }
    )

    LaunchedEffect(Unit) {
        viewModel.getUserDetails()
    }

    LaunchedEffect(onSignedOut) {
        if (onSignedOut) {
            onParentGraph()
        }
    }

    if (showSignOutDialog) {
        ConfirmDialog(
            title = "Sign Out",
            message = "Are you sure you want to sign out of your SSPL account?",
            confirmText = "Sign Out",
            dismissText = "Cancel",
            onConfirm = {
                showSignOutDialog = false
                viewModel.onSignOut()
            },
            onDismiss = { showSignOutDialog = false }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(scaffoldState) },
        containerColor = AppTheme.colors.background
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(AppTheme.colors.background)
        ) {
            // Guest User View
            if (isGuestUser == true) {
                GuestProfileContent(
                    onSignIn = onParentGraph,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                when (val state = userDetails) {
                    is ApiStates.Loading -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = AppTheme.colors.secondary)
                        }
                    }
                    is ApiStates.Failure -> {
                        ErrorState(
                            title = "Failed to load profile",
                            message = state.error.message,
                            onRetry = { viewModel.getUserDetails() },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    is ApiStates.Success -> {
                        val user = state.data
                        if (user != null) {
                            AuthenticatedProfileContent(
                                user = user,
                                profileImageBase64 = profileImageBase64,
                                onEditPersonal = {
                                    navController.currentBackStackEntry?.savedStateHandle?.set(
                                        "isPersonalDetails", true
                                    )
                                    navController.currentBackStackEntry?.savedStateHandle?.set(
                                        "currentUser", Json.encodeToString(user)
                                    )
                                    navController.navigate(Screen.EditProfile.route)
                                },
                                onEditProfessional = {
                                    navController.currentBackStackEntry?.savedStateHandle?.set(
                                        "isPersonalDetails", false
                                    )
                                    navController.currentBackStackEntry?.savedStateHandle?.set(
                                        "currentUser", Json.encodeToString(user)
                                    )
                                    navController.navigate(Screen.EditProfile.route)
                                },
                                onChangePhoto = { launcher.launch() },
                                onSignOutClick = { showSignOutDialog = true }
                            )
                        } else {
                            EmptyState(
                                title = "No profile details found",
                                message = "Unable to retrieve user data."
                            )
                        }
                    }
                    else -> Unit
                }
            }
        }
    }
}

@Composable
private fun GuestProfileContent(
    onSignIn: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        AvatarImage(
            name = "Guest User",
            size = 90.dp
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Guest Account",
            style = AppTheme.typography.headlineMedium.copy(
                color = AppTheme.colors.heading,
                fontWeight = FontWeight.Bold
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "You are currently browsing SSPL as a guest. Sign in to view and manage your membership, event registrations, CME certificates, and abstract submissions.",
            style = AppTheme.typography.bodyMedium.copy(
                color = AppTheme.colors.muted,
                textAlign = TextAlign.Center
            )
        )

        Spacer(modifier = Modifier.height(28.dp))

        PrimaryButton(
            text = "Sign In / Register",
            onClick = onSignIn,
            icon = Icons.AutoMirrored.Filled.ExitToApp,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun AuthenticatedProfileContent(
    user: User,
    profileImageBase64: String?,
    onEditPersonal: () -> Unit,
    onEditProfessional: () -> Unit,
    onChangePhoto: () -> Unit,
    onSignOutClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Avatar + Change Photo
        Box(
            modifier = Modifier.size(110.dp)
        ) {
            AvatarImage(
                name = user.fullName().ifBlank { user.account?.email.orEmpty() },
                base64Image = profileImageBase64,
                imageUrl = user.profilePicture,
                size = 110.dp
            )

            // Edit photo badge
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .align(Alignment.BottomEnd)
                    .clip(CircleShape)
                    .background(AppTheme.colors.secondary)
                    .clickable(onClick = onChangePhoto),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit photo",
                    tint = AppTheme.colors.background,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Name
        Text(
            text = user.fullName().ifBlank { "SSPL Member" },
            style = AppTheme.typography.titleLarge.copy(
                color = AppTheme.colors.heading,
                fontWeight = FontWeight.Bold
            ),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Email
        Text(
            text = user.account?.email.orEmpty(),
            style = AppTheme.typography.bodyMedium.copy(
                color = AppTheme.colors.muted
            ),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Roles & Membership Badges
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val roles = RoleUtils.getUserRoles(user)
            roles.forEach { role ->
                RoleChip(role = role)
            }
            MembershipBadge(status = user.membershipStatus)
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Card 1: Account Information
        SsplCard(modifier = Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SectionHeader(
                    title = "Account Details",
                    action = "Edit",
                    onAction = onEditPersonal
                )

                InfoRow(
                    label = "Full Name",
                    value = user.fullName().ifBlank { "Not provided" },
                    icon = Icons.Default.Person
                )

                InfoRow(
                    label = "Email Address",
                    value = user.account?.email ?: "Not provided",
                    icon = Icons.Default.Email
                )

                InfoRow(
                    label = "Phone Number",
                    value = user.account?.phone ?: "Not provided",
                    icon = Icons.Default.Phone
                )

                InfoRow(
                    label = "Gender",
                    value = user.gender ?: "Not specified"
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Card 2: Professional Information
        SsplCard(modifier = Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SectionHeader(
                    title = "Professional Profile",
                    action = "Edit",
                    onAction = onEditProfessional
                )

                InfoRow(
                    label = "PMDC / Council #",
                    value = user.pmdcNo ?: "Not provided",
                    icon = Icons.Default.MedicalServices
                )

                InfoRow(
                    label = "Designation",
                    value = user.designation ?: "Not provided",
                    icon = Icons.Default.Work
                )

                InfoRow(
                    label = "Institution",
                    value = user.instituteName ?: "Not provided",
                    icon = Icons.Default.Business
                )

                InfoRow(
                    label = "City",
                    value = user.city ?: "Not provided",
                    icon = Icons.Default.LocationCity
                )

                InfoRow(
                    label = "Country",
                    value = user.country ?: "Not provided",
                    icon = Icons.Default.LocationOn
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Card 3: Membership Status
        SsplCard(modifier = Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SectionHeader(title = "Membership")

                InfoRow(
                    label = "Plan Status",
                    value = RoleUtils.getMembershipLabel(user),
                    icon = Icons.Default.VerifiedUser
                )

                InfoRow(
                    label = "Active Access",
                    value = if (RoleUtils.hasActiveMembershipPlan(user)) "Active" else "Inactive / Expired"
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Sign Out Button
        SecondaryButton(
            text = "Sign Out",
            onClick = onSignOutClick,
            icon = Icons.AutoMirrored.Filled.ExitToApp,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}
