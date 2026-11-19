package com.sspl.ui.fellowship

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.sspl.Screen
import com.sspl.designsystem.components.SsplButton
import com.sspl.designsystem.components.SsplCard
import com.sspl.designsystem.theme.AppTheme

/**
 * E3: Fellowship Guidelines & Curriculum Screen
 */
@Composable
fun FellowshipGuidelinesScreen(
    navController: NavController
) {
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
                text = "CURRICULUM & REGULATIONS",
                style = AppTheme.typography.labelSmall.copy(
                    color = AppTheme.colors.secondary,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Fellowship Guidelines",
                style = AppTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = AppTheme.colors.heading
                )
            )
            Text(
                text = "Academic regulations, case quotas, and requirements for completion.",
                style = AppTheme.typography.bodySmall.copy(color = AppTheme.colors.muted)
            )
        }

        // Section 1: Candidate Eligibility
        item {
            SsplCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "1. Candidate Eligibility",
                        style = AppTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = AppTheme.colors.heading
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    val eligibilityItems = listOf(
                        "Valid, active PMDC / PMC permanent registration.",
                        "Postgraduate qualification: FCPS, MS, FRCS or equivalent recognized qualification in Plastic Surgery or General Surgery.",
                        "Regular or Life Member of Sindh Society of Plastic Surgeons in good standing.",
                        "No objection certificate (NOC) from parent employing institution if applicable."
                    )

                    eligibilityItems.forEach { item ->
                        GuidelinePoint(text = item)
                    }
                }
            }
        }

        // Section 2: Clinical Logbook & Case Requirements
        item {
            SsplCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "2. Clinical Logbook & Quota",
                        style = AppTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = AppTheme.colors.heading
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    val logbookItems = listOf(
                        "Every candidate must log all surgical procedures digitally in the Fellow Logbook within 48 hours of surgery.",
                        "Minimum 120 operative procedures as Primary Surgeon / Supervised First Assistant.",
                        "Logbook must encompass Reconstructive Microvascular, Burn Care, Aesthetic, Hand Trauma, and Cleft/Craniofacial subdisciplines.",
                        "Monthly sign-off and clinical audit verification by the designated Master Trainer."
                    )

                    logbookItems.forEach { item ->
                        GuidelinePoint(text = item)
                    }
                }
            }
        }

        // Section 3: Attendance & Assessment
        item {
            SsplCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "3. Attendance & Assessment Milestones",
                        style = AppTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = AppTheme.colors.heading
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    val assessmentItems = listOf(
                        "Minimum mandatory attendance of 80% throughout the 12-month program duration.",
                        "Quarterly structured objective workplace-based assessments (DOPS / Mini-CEX).",
                        "One publication or presented paper in an indexed journal / society conference.",
                        "Final evaluation board viva and exit examination before certification issuance."
                    )

                    assessmentItems.forEach { item ->
                        GuidelinePoint(text = item)
                    }
                }
            }
        }

        // Apply CTA
        item {
            SsplButton(
                text = "Apply for Fellowship",
                onClick = { navController.navigate(Screen.ApplyAsFellowScreen.route) },
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun GuidelinePoint(text: String) {
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
            text = text,
            style = AppTheme.typography.bodySmall.copy(color = AppTheme.colors.body)
        )
    }
}

