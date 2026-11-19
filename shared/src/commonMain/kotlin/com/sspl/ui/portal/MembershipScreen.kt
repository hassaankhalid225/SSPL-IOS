package com.sspl.ui.portal

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.sspl.Screen
import com.sspl.core.auth.isLifeMember
import com.sspl.core.auth.isRegularMember
import com.sspl.core.models.MembershipPlan
import com.sspl.core.models.UserMembershipDetails
import com.sspl.core.repositories.PortalRepository
import com.sspl.designsystem.components.SsplBadge
import com.sspl.designsystem.components.SsplBadgeVariant
import com.sspl.designsystem.components.SsplButton
import com.sspl.designsystem.components.SsplButtonVariant
import com.sspl.designsystem.components.SsplCard
import com.sspl.designsystem.components.SsplTextField
import com.sspl.designsystem.theme.AppTheme
import com.sspl.session.UserSession
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

/**
 * D2: Membership Status & Life Upgrade
 */
@Composable
fun MembershipScreen(
    navController: NavController,
    portalRepository: PortalRepository = koinInject(),
    userSession: UserSession = koinInject()
) {
    val user by userSession.userFlow.collectAsState()
    val scope = rememberCoroutineScope()

    var membershipDetails by remember { mutableStateOf<UserMembershipDetails?>(null) }
    var plans by remember { mutableStateOf<List<MembershipPlan>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var selectedLifePlan by remember { mutableStateOf<MembershipPlan?>(null) }
    var upgradePhone by remember { mutableStateOf(user?.phone ?: "") }
    var isUpgrading by remember { mutableStateOf(false) }
    var upgradeMessage by remember { mutableStateOf<String?>(null) }
    var isError by remember { mutableStateOf(false) }

    val isActiveMember = user?.membershipStatus == "ACTIVE"
    val isLife = user?.isLifeMember() == true

    LaunchedEffect(Unit) {
        isLoading = true
        portalRepository.getMyMembership().collect { details ->
            membershipDetails = details
        }
        portalRepository.getMembershipPlans().collect { planList ->
            plans = planList
            selectedLifePlan = planList.find { it.type == "LIFE" || it.name.contains("Life", ignoreCase = true) }
            isLoading = false
        }
    }

    val handleUpgrade = {
        selectedLifePlan?.let { plan ->
            scope.launch {
                isUpgrading = true
                upgradeMessage = null
                val result = portalRepository.upgradeToLifeMembership(
                    planId = plan.id,
                    phone = upgradePhone,
                    profileFields = mapOf(
                        "institution" to (user?.institution ?: ""),
                        "pmdc" to (user?.pmdc ?: "")
                    )
                )
                isUpgrading = false
                if (result.isSuccess) {
                    upgradeMessage = "Life membership upgrade request submitted successfully!"
                    isError = false
                } else {
                    upgradeMessage = result.exceptionOrNull()?.message ?: "Upgrade request failed. Please try again."
                    isError = true
                }
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(AppTheme.colors.background)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "MEMBERSHIP",
                style = AppTheme.typography.labelSmall.copy(
                    color = AppTheme.colors.secondary,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Society Membership",
                style = AppTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = AppTheme.colors.heading
                )
            )
            Text(
                text = "View your current membership tier, benefits, and upgrade options.",
                style = AppTheme.typography.bodySmall.copy(color = AppTheme.colors.muted)
            )
        }

        if (isLoading) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = AppTheme.colors.secondary)
                }
            }
        } else {
            // Status Card
            item {
                SsplCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "CURRENT TIER",
                                    style = AppTheme.typography.labelSmall.copy(
                                        color = AppTheme.colors.muted,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                )
                                Text(
                                    text = when {
                                        isLife -> "Life Member"
                                        isActiveMember -> "Regular Member"
                                        user?.membershipStatus == "PENDING" -> "Application Pending"
                                        else -> "Non-Member / Guest"
                                    },
                                    style = AppTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = AppTheme.colors.heading
                                    )
                                )
                            }

                            val badgeVariant = when {
                                isLife -> SsplBadgeVariant.Success
                                isActiveMember -> SsplBadgeVariant.Success
                                user?.membershipStatus == "PENDING" -> SsplBadgeVariant.Warning
                                else -> SsplBadgeVariant.Neutral
                            }

                            SsplBadge(
                                text = user?.membershipStatus ?: "INACTIVE",
                                variant = badgeVariant
                            )
                        }

                        if (membershipDetails != null) {
                            Spacer(modifier = Modifier.height(12.dp))
                            membershipDetails?.membershipNumber?.let { num ->
                                Text(
                                    text = "Membership No: $num",
                                    style = AppTheme.typography.bodySmall.copy(
                                        fontWeight = FontWeight.Medium,
                                        color = AppTheme.colors.secondary
                                    )
                                )
                            }
                            membershipDetails?.expiresAt?.let { exp ->
                                Text(
                                    text = "Valid Until: $exp",
                                    style = AppTheme.typography.bodySmall.copy(color = AppTheme.colors.muted)
                                )
                            }
                        }
                    }
                }
            }

            // If not active, show Apply button
            if (!isActiveMember) {
                item {
                    SsplCard(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.WorkspacePremium,
                                contentDescription = null,
                                tint = AppTheme.colors.secondary,
                                modifier = Modifier.size(40.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Become a Society Member",
                                style = AppTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Gain voting rights, discounted conference fees, journal access, and fellowship eligibility.",
                                style = AppTheme.typography.bodySmall.copy(color = AppTheme.colors.muted),
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            SsplButton(
                                text = "View Membership Plans",
                                onClick = { navController.navigate(Screen.MembershipPlansScreen.route) },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }

            // If active regular member and not life member: Show Life Upgrade Form
            if (isActiveMember && !isLife) {
                item {
                    SsplCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = Color(0xFFD97706),
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Upgrade to Life Membership",
                                    style = AppTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = AppTheme.colors.heading
                                    )
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Permanent membership with no annual renewals required.",
                                style = AppTheme.typography.bodySmall.copy(color = AppTheme.colors.muted)
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            selectedLifePlan?.let { plan ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(AppTheme.colors.accent.copy(alpha = 0.1f))
                                        .padding(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = plan.name,
                                                style = AppTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                            )
                                            Text(
                                                text = "Lifetime duration",
                                                style = AppTheme.typography.bodySmall.copy(color = AppTheme.colors.muted)
                                            )
                                        }
                                        Text(
                                            text = "Rs. ${plan.displayFee.toLong()}",
                                            style = AppTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = AppTheme.colors.secondary
                                            )
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            SsplTextField(
                                value = upgradePhone,
                                onValueChange = { upgradePhone = it },
                                label = "Confirm Mobile Number",
                                placeholder = "03001234567",
                                modifier = Modifier.fillMaxWidth()
                            )

                            if (upgradeMessage != null) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = upgradeMessage!!,
                                    style = AppTheme.typography.bodySmall.copy(
                                        color = if (isError) AppTheme.colors.error else AppTheme.colors.secondary,
                                        fontWeight = FontWeight.Medium
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            SsplButton(
                                text = if (isUpgrading) "Submitting..." else "Apply for Life Upgrade",
                                onClick = { handleUpgrade() },
                                enabled = !isUpgrading && upgradePhone.isNotBlank(),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }

            // Benefits list
            item {
                SsplCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Membership Benefits",
                            style = AppTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = AppTheme.colors.heading
                            )
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        val benefits = listOf(
                            "Full access to scientific conferences, workshops, and symposiums",
                            "Official voting rights in biennial society elections",
                            "Free listing in the Verified Plastic Surgeons Directory",
                            "Certificate of Practice and society badge",
                            "Eligibility for Society Fellowship & Master Trainer positions"
                        )

                        benefits.forEach { b ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = AppTheme.colors.secondary,
                                    modifier = Modifier
                                        .size(16.dp)
                                        .padding(top = 2.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = b,
                                    style = AppTheme.typography.bodySmall.copy(color = AppTheme.colors.body)
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

