package com.sspl.ui.fellowship

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SupervisorAccount
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.sspl.core.models.FellowCase
import com.sspl.core.models.TrainerDashboardResponse
import com.sspl.core.repositories.FellowshipRepository
import com.sspl.designsystem.components.SsplBadge
import com.sspl.designsystem.components.SsplBadgeVariant
import com.sspl.designsystem.components.SsplButton
import com.sspl.designsystem.components.SsplButtonVariant
import com.sspl.designsystem.components.SsplCard
import com.sspl.designsystem.theme.AppTheme
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

/**
 * E7: Trainer / Supervisor Dashboard
 */
@Composable
fun TrainerDashboardScreen(
    navController: NavController,
    fellowshipRepository: FellowshipRepository = koinInject()
) {
    val scope = rememberCoroutineScope()
    var dashboardData by remember { mutableStateOf<TrainerDashboardResponse?>(null) }
    var casesList by remember { mutableStateOf<List<FellowCase>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var actionMessage by remember { mutableStateOf<String?>(null) }

    val loadTrainerData = {
        scope.launch {
            isLoading = true
            fellowshipRepository.getTrainerDashboard().collect { data ->
                dashboardData = data
            }
            fellowshipRepository.getCases().collect { cases ->
                casesList = cases
                isLoading = false
            }
        }
    }

    LaunchedEffect(Unit) {
        loadTrainerData()
    }

    val pendingCases = casesList.filter { it.status == "PENDING" }

    val handleCaseAction = { caseItem: FellowCase, status: String ->
        scope.launch {
            fellowshipRepository.reviewCase(
                caseId = caseItem.id,
                status = status,
                remarks = if (status == "APPROVED") "Verified operative technique and records" else "Incomplete operative notes"
            )
            actionMessage = "Case #${caseItem.id} marked as $status."
            loadTrainerData()
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "SUPERVISOR DESK",
                        style = AppTheme.typography.labelSmall.copy(
                            color = AppTheme.colors.secondary,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Master Trainer Desk",
                        style = AppTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = AppTheme.colors.heading
                        )
                    )
                }

                IconButton(onClick = { loadTrainerData() }) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh",
                        tint = AppTheme.colors.secondary
                    )
                }
            }
            Text(
                text = "Review clinical logbook submissions and evaluate candidate performance.",
                style = AppTheme.typography.bodySmall.copy(color = AppTheme.colors.muted)
            )
        }

        if (actionMessage != null) {
            item {
                Text(
                    text = actionMessage!!,
                    style = AppTheme.typography.bodySmall.copy(
                        color = AppTheme.colors.secondary,
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }
        }

        // Supervisor Stats
        item {
            SsplCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Pending Logbook Reviews",
                            style = AppTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = AppTheme.colors.heading
                            )
                        )
                        SsplBadge(
                            text = "${pendingCases.size} PENDING",
                            variant = SsplBadgeVariant.Warning
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Procedures logged by assigned trainees requiring faculty verification.",
                        style = AppTheme.typography.bodySmall.copy(color = AppTheme.colors.muted)
                    )
                }
            }
        }

        if (isLoading) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = AppTheme.colors.secondary)
                }
            }
        } else if (pendingCases.isEmpty()) {
            item {
                SsplCard(modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "All trainee surgical cases have been audited and approved.",
                            style = AppTheme.typography.bodyMedium.copy(color = AppTheme.colors.muted)
                        )
                    }
                }
            }
        } else {
            items(pendingCases) { caseItem ->
                SsplCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = caseItem.procedure,
                                style = AppTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = AppTheme.colors.heading
                                ),
                                modifier = Modifier.weight(1f)
                            )
                            SsplBadge(
                                text = "AWAITING AUDIT",
                                variant = SsplBadgeVariant.Warning
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Diagnosis: ${caseItem.diagnosis}",
                            style = AppTheme.typography.bodySmall.copy(color = AppTheme.colors.body)
                        )

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Patient MRN: ${caseItem.mrnNumber ?: "Not recorded"} • Role: ${caseItem.role ?: "Primary"}",
                            style = AppTheme.typography.bodySmall.copy(
                                color = AppTheme.colors.secondary,
                                fontWeight = FontWeight.SemiBold
                            )
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            SsplButton(
                                text = "Approve Case",
                                onClick = { handleCaseAction(caseItem, "APPROVED") },
                                variant = SsplButtonVariant.Primary,
                                modifier = Modifier.weight(1f)
                            )
                            SsplButton(
                                text = "Reject",
                                onClick = { handleCaseAction(caseItem, "REJECTED") },
                                variant = SsplButtonVariant.Outline,
                                modifier = Modifier.weight(1f)
                            )
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

