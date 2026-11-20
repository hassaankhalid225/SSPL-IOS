package com.sspl.ui.fellowship

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
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
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import com.sspl.Screen
import com.sspl.core.models.CreateCaseRequest
import com.sspl.core.models.FellowCase
import com.sspl.core.models.FellowDashboardResponse
import com.sspl.core.repositories.FellowshipRepository
import com.sspl.designsystem.components.ModernSupportCard
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
 * E6: Fellow Workspace & Clinical Logbook Dashboard
 */
@Composable
fun FellowDashboardScreen(
    navController: NavController,
    fellowshipRepository: FellowshipRepository = koinInject(),
    userSession: UserSession = koinInject()
) {
    val user by userSession.userFlow.collectAsState()
    val scope = rememberCoroutineScope()

    var dashboardData by remember { mutableStateOf<FellowDashboardResponse?>(null) }
    var casesList by remember { mutableStateOf<List<FellowCase>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var selectedTab by remember { mutableStateOf("CASES") }
    var showAddCaseDialog by remember { mutableStateOf(false) }

    val loadDashboard = {
        scope.launch {
            isLoading = true
            fellowshipRepository.getFellowDashboard().collect { data ->
                dashboardData = data
            }
            fellowshipRepository.getCases().collect { cases ->
                casesList = cases
                isLoading = false
            }
        }
    }

    LaunchedEffect(Unit) {
        loadDashboard()
    }

    val tabs = listOf(
        "CASES" to "Cases Log (${casesList.size})",
        "ATTENDANCE" to "Attendance",
        "ASSESSMENTS" to "Assessments",
        "RESEARCH" to "Research"
    )

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
                        text = "FELLOW LOGBOOK",
                        style = AppTheme.typography.labelSmall.copy(
                            color = AppTheme.colors.secondary,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Clinical Workspace",
                        style = AppTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = AppTheme.colors.heading
                        )
                    )
                }

                IconButton(onClick = { loadDashboard() }) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh",
                        tint = AppTheme.colors.secondary
                    )
                }
            }
            Text(
                text = "Log operative procedures, verify ward attendance, and monitor graduation progress.",
                style = AppTheme.typography.bodySmall.copy(color = AppTheme.colors.muted)
            )
        }

        // Certificate Announcement Card (1:1 with Web FellowshipDashboardView.jsx)
        val cert = dashboardData?.certificates?.firstOrNull()
        if (cert != null) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = AppTheme.colors.primaryDark
                    )
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF34D399),
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "CERTIFICATE ISSUED & VERIFIED",
                                style = AppTheme.typography.labelSmall.copy(
                                    color = Color(0xFF6EE7B7),
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Congratulations, Fellow!",
                            style = AppTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Your fellowship training for ${cert.title ?: "Mini Fellowship in Laparoscopic Surgery"} has been completed and officially certified by Society of Surgeons of Pakistan (Lahore Chapter).",
                            style = AppTheme.typography.bodySmall.copy(
                                color = Color.White.copy(alpha = 0.85f),
                                lineHeight = 18.sp
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color.White.copy(alpha = 0.12f))
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Serial: ${cert.serialNumber ?: "N/A"}",
                                style = AppTheme.typography.labelSmall.copy(
                                    color = Color(0xFF34D399),
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            cert.issueDate?.let { dt ->
                                Text(
                                    text = "  ·  Issued: ${dt.split("T").firstOrNull() ?: dt}",
                                    style = AppTheme.typography.labelSmall.copy(
                                        color = Color.White.copy(alpha = 0.7f)
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        SsplButton(
                            text = "Verify Online",
                            onClick = {
                                navController.navigate(Screen.VerifyCertificateScreen.route)
                            },
                            variant = SsplButtonVariant.Primary,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        // Progress & Stats Card
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
                                text = "FELLOW",
                                style = AppTheme.typography.labelSmall.copy(
                                    color = AppTheme.colors.muted,
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                            Text(
                                text = listOfNotNull(user?.prefix, user?.firstName, user?.lastName).joinToString(" ").ifBlank { "Trainee Fellow" },
                                style = AppTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = AppTheme.colors.heading
                                )
                            )
                        }

                        SsplBadge(
                            text = if (cert != null) "CERTIFIED" else "IN TRAINING",
                            variant = SsplBadgeVariant.Success
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    val approvedCount = casesList.count { it.status == "APPROVED" }
                    val progressFloat = (approvedCount.toFloat() / 120f).coerceIn(0f, 1f)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Operative Quota Progress",
                            style = AppTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold)
                        )
                        Text(
                            text = "$approvedCount / 120 Cases (${(progressFloat * 100).toInt()}%)",
                            style = AppTheme.typography.bodySmall.copy(
                                color = AppTheme.colors.secondary,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    LinearProgressIndicator(
                        progress = { progressFloat },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = AppTheme.colors.secondary,
                        trackColor = AppTheme.colors.border
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    SsplButton(
                        text = "+ Log New Surgical Case",
                        onClick = { showAddCaseDialog = true },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // Navigation Tabs
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(tabs) { (key, label) ->
                    val isSelected = selectedTab == key
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50.dp))
                            .background(
                                if (isSelected) AppTheme.colors.primaryDark else AppTheme.colors.boxColor
                            )
                            .border(
                                width = 1.dp,
                                color = if (isSelected) Color.Transparent else AppTheme.colors.border,
                                shape = RoundedCornerShape(50.dp)
                            )
                            .clickable { selectedTab = key }
                            .padding(horizontal = 16.dp, vertical = 9.dp)
                    ) {
                        Text(
                            text = label,
                            style = AppTheme.typography.labelMedium.copy(
                                color = if (isSelected) Color.White else AppTheme.colors.heading,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        )
                    }
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
        } else when (selectedTab) {
            "CASES" -> {
                if (casesList.isEmpty()) {
                    item {
                        SsplCard(modifier = Modifier.fillMaxWidth()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No cases logged yet. Tap '+ Log New Surgical Case' to begin.",
                                    style = AppTheme.typography.bodyMedium.copy(color = AppTheme.colors.muted)
                                )
                            }
                        }
                    }
                } else {
                    items(casesList) { caseItem ->
                        val isApproved = caseItem.status == "APPROVED"
                        val isPending = caseItem.status == "PENDING"

                        SsplCard(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = caseItem.procedure,
                                        style = AppTheme.typography.titleSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = AppTheme.colors.heading
                                        ),
                                        modifier = Modifier.weight(1f)
                                    )

                                    val badgeVariant = when {
                                        isApproved -> SsplBadgeVariant.Success
                                        isPending -> SsplBadgeVariant.Warning
                                        else -> SsplBadgeVariant.Error
                                    }

                                    SsplBadge(
                                        text = caseItem.status,
                                        variant = badgeVariant
                                    )
                                }

                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Diagnosis: ${caseItem.diagnosis}",
                                    style = AppTheme.typography.bodySmall.copy(color = AppTheme.colors.body)
                                )

                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Role: ${caseItem.role ?: "Assistant"}",
                                        style = AppTheme.typography.labelSmall.copy(
                                            color = AppTheme.colors.secondary,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    )
                                    caseItem.date?.let { dt ->
                                        Text(
                                            text = dt,
                                            style = AppTheme.typography.labelSmall.copy(color = AppTheme.colors.muted)
                                        )
                                    }
                                }

                                caseItem.supervisorRemarks?.let { remarks ->
                                    if (remarks.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = "Trainer note: $remarks",
                                            style = AppTheme.typography.labelSmall.copy(
                                                color = AppTheme.colors.muted,
                                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            "ATTENDANCE" -> {
                item {
                    SsplCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Clinical Duty Attendance",
                                style = AppTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Verified 85% attendance across surgical rotations, OT lists, and emergency duty.",
                                style = AppTheme.typography.bodySmall.copy(color = AppTheme.colors.muted)
                            )
                        }
                    }
                }
            }

            "ASSESSMENTS" -> {
                item {
                    SsplCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Workplace-Based Assessments (DOPS)",
                                style = AppTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Quarterly assessments conducted by unit supervisor.",
                                style = AppTheme.typography.bodySmall.copy(color = AppTheme.colors.muted)
                            )
                        }
                    }
                }
            }

            "RESEARCH" -> {
                item {
                    SsplCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Fellowship Research Study",
                                style = AppTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Mandatory dissertation / research paper for final board certification.",
                                style = AppTheme.typography.bodySmall.copy(color = AppTheme.colors.muted)
                            )
                        }
                    }
                }
            }
        }

        item {
            ModernSupportCard(
                title = "Fellowship Inquiries?",
                subtitle = "For quota verification, center placement questions, or completion certificate queries, contact the fellowship coordinator.",
                buttonText = "Contact Secretariat",
                onContactClick = { navController.navigate(Screen.ContactUsScreen.route) }
            )
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Add Surgical Case Dialog
    if (showAddCaseDialog) {
        AddCaseDialog(
            onDismiss = { showAddCaseDialog = false },
            onSubmit = { req ->
                scope.launch {
                    fellowshipRepository.createCase(req)
                    showAddCaseDialog = false
                    loadDashboard()
                }
            }
        )
    }
}

@Composable
private fun AddCaseDialog(
    onDismiss: () -> Unit,
    onSubmit: (CreateCaseRequest) -> Unit
) {
    var mrn by remember { mutableStateOf("") }
    var procedure by remember { mutableStateOf("") }
    var diagnosis by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("PRIMARY") }
    var date by remember { mutableStateOf("2026-11-20") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = AppTheme.colors.surface,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Log Surgical Procedure",
                        style = AppTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = AppTheme.colors.heading
                        )
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                SsplTextField(
                    value = procedure,
                    onValueChange = { procedure = it },
                    label = "Procedure Performed *",
                    placeholder = "e.g. Anterolateral Thigh Free Flap",
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                SsplTextField(
                    value = diagnosis,
                    onValueChange = { diagnosis = it },
                    label = "Pre-operative Diagnosis *",
                    placeholder = "e.g. Post-traumatic distal 1/3 tibia defect",
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                SsplTextField(
                    value = mrn,
                    onValueChange = { mrn = it },
                    label = "Patient MRN / Hospital Number *",
                    placeholder = "e.g. MRN-987654",
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                SsplTextField(
                    value = role,
                    onValueChange = { role = it },
                    label = "Candidate Role (PRIMARY / ASSISTANT)",
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                SsplButton(
                    text = "Save to Logbook",
                    onClick = {
                        if (procedure.isNotBlank() && diagnosis.isNotBlank() && mrn.isNotBlank()) {
                            onSubmit(
                                CreateCaseRequest(
                                    mrnNumber = mrn,
                                    procedure = procedure,
                                    diagnosis = diagnosis,
                                    role = role,
                                    date = date
                                )
                            )
                        }
                    },
                    enabled = procedure.isNotBlank() && diagnosis.isNotBlank() && mrn.isNotBlank(),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

