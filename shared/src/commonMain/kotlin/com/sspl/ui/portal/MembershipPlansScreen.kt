package com.sspl.ui.portal

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.sspl.core.models.MembershipPlan
import com.sspl.core.repositories.PortalRepository
import com.sspl.designsystem.components.SsplBadge
import com.sspl.designsystem.components.SsplBadgeVariant
import com.sspl.designsystem.components.SsplButton
import com.sspl.designsystem.components.SsplButtonVariant
import com.sspl.designsystem.components.SsplCard
import com.sspl.designsystem.theme.AppTheme
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

/**
 * D3: Public Membership Plans
 */
@Composable
fun MembershipPlansScreen(
    navController: NavController,
    portalRepository: PortalRepository = koinInject()
) {
    val scope = rememberCoroutineScope()
    var plans by remember { mutableStateOf<List<MembershipPlan>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var actionMessage by remember { mutableStateOf<String?>(null) }
    var processingPlanId by remember { mutableStateOf<Long?>(null) }

    LaunchedEffect(Unit) {
        portalRepository.getMembershipPlans().collect { planList ->
            plans = planList
            isLoading = false
        }
    }

    val handleApplyPlan = { plan: MembershipPlan ->
        scope.launch {
            processingPlanId = plan.id
            actionMessage = null
            val res = portalRepository.registerMembership(plan.id)
            processingPlanId = null
            if (res.isSuccess) {
                actionMessage = "Application for ${plan.name} registered successfully!"
            } else {
                actionMessage = res.exceptionOrNull()?.message ?: "Application failed. Please retry."
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
                text = "MEMBERSHIP PLANS",
                style = AppTheme.typography.labelSmall.copy(
                    color = AppTheme.colors.secondary,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Choose Your Membership Tier",
                style = AppTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = AppTheme.colors.heading
                )
            )
            Text(
                text = "Select a plan that suits your practice and professional standing.",
                style = AppTheme.typography.bodySmall.copy(color = AppTheme.colors.muted)
            )
        }

        if (actionMessage != null) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(AppTheme.colors.accent.copy(alpha = 0.15f))
                        .padding(12.dp)
                ) {
                    Text(
                        text = actionMessage!!,
                        style = AppTheme.typography.bodySmall.copy(
                            color = AppTheme.colors.secondary,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
            }
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
        } else if (plans.isEmpty()) {
            item {
                SsplCard(modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No membership plans available at this time.",
                            style = AppTheme.typography.bodyMedium.copy(color = AppTheme.colors.muted)
                        )
                    }
                }
            }
        } else {
            items(plans) { plan ->
                val isLife = plan.type == "LIFE" || plan.name.contains("Life", ignoreCase = true)
                SsplCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = plan.name,
                                    style = AppTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = AppTheme.colors.heading
                                    )
                                )
                                Text(
                                    text = plan.duration ?: if (isLife) "Lifetime" else "Annual",
                                    style = AppTheme.typography.bodySmall.copy(color = AppTheme.colors.muted)
                                )
                            }

                            if (isLife) {
                                SsplBadge(
                                    text = "POPULAR",
                                    variant = SsplBadgeVariant.Success
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = if (plan.displayFee > 0) "PKR ${plan.displayFee.toLong()}" else "Free",
                            style = AppTheme.typography.displaySmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = AppTheme.colors.secondary
                            )
                        )

                        if (!plan.description.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = plan.description,
                                style = AppTheme.typography.bodySmall.copy(color = AppTheme.colors.body)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        val planBenefits = plan.benefits ?: listOf(
                            "Directory listing with verification mark",
                            "Conferences & workshops discounts",
                            "Voting rights in society governance",
                            "Society certificate and credentials"
                        )

                        planBenefits.forEach { benefit ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = AppTheme.colors.secondary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = benefit,
                                    style = AppTheme.typography.bodySmall.copy(color = AppTheme.colors.body)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        SsplButton(
                            text = if (processingPlanId == plan.id) "Processing..." else "Apply for ${plan.name}",
                            onClick = { handleApplyPlan(plan) },
                            enabled = processingPlanId == null,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

