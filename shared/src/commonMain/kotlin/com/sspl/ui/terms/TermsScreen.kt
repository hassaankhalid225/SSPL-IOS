package com.sspl.ui.terms

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.sspl.designsystem.theme.AppTheme

data class TermsClause(
    val title: String,
    val points: List<String>
)

private val TERMS_DATA = listOf(
    TermsClause(
        title = "About the Society",
        points = listOf(
            "The Society of Surgeons of Pakistan, Lahore Chapter, is a professional, non-profit organization dedicated to academic excellence, professional development, research, and advancement of surgical sciences."
        )
    ),
    TermsClause(
        title = "Membership",
        points = listOf(
            "Membership is open to eligible medical professionals as per SSP rules.",
            "Membership categories, fees, and duration shall be determined by SSP Lahore Chapter and may be revised from time to time.",
            "The Society reserves the right to accept, reject, suspend, or terminate membership in case of violation of rules or unethical conduct."
        )
    ),
    TermsClause(
        title = "Conferences, Workshops & Events",
        points = listOf(
            "Registration is confirmed only after full payment of the applicable fee.",
            "Registration fees are refundable up to 10 days before the scheduled event.",
            "The Society reserves the right to modify event schedules, faculty, venue, or program content if required.",
            "Certificates will be issued only to registered participants who meet the mandatory attendance and feedback criteria."
        )
    ),
    TermsClause(
        title = "Code of Conduct",
        points = listOf(
            "All members and delegates are expected to maintain professional, ethical, and respectful behavior.",
            "Any form of misconduct, harassment, or misrepresentation may result in disciplinary action and removal from the event without refund."
        )
    ),
    TermsClause(
        title = "Intellectual Property",
        points = listOf(
            "All conference materials, slides, recordings, logos, and digital publications are the intellectual property of SSP Lahore Chapter unless stated otherwise.",
            "Reproduction, recording, or distribution without prior written permission is strictly prohibited."
        )
    ),
    TermsClause(
        title = "Personal Information & Privacy",
        points = listOf(
            "Personal data provided during registration will be used solely for official SSP activities, CME tracking, and communication.",
            "SSP Lahore Chapter will not share personal information with third parties without consent, except where required by law."
        )
    ),
    TermsClause(
        title = "Liability",
        points = listOf(
            "SSP Lahore Chapter shall not be responsible for any injury, accident, or personal loss during events.",
            "Participants attend all clinical and practical workshops at their own discretion and risk."
        )
    ),
    TermsClause(
        title = "Amendments",
        points = listOf(
            "The Society reserves the right to amend these Terms & Conditions at any time.",
            "Updated terms become effective immediately upon publication in the app and website."
        )
    ),
    TermsClause(
        title = "Governing Law",
        points = listOf(
            "These Terms & Conditions shall be governed and interpreted in accordance with the laws of the Islamic Republic of Pakistan."
        )
    ),
    TermsClause(
        title = "Acceptance",
        points = listOf(
            "By registering, applying for membership, or participating in SSP Lahore activities, you acknowledge that you have read, understood, and agreed to these Terms & Conditions."
        )
    )
)

@Composable
fun TermsScreen(navController: NavController) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(AppTheme.colors.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Hero Header
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = AppTheme.colors.primary)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(Color.White.copy(alpha = 0.2f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Gavel,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "LEGAL & POLICIES",
                            style = AppTheme.typography.labelSmall,
                            color = AppTheme.colors.secondary,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.3.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Terms & Conditions",
                        style = AppTheme.typography.headlineMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Official regulations governing SSP Lahore membership, conference registrations, and intellectual property.",
                        style = AppTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.88f)
                    )
                }
            }
        }

        // Terms Sections
        itemsIndexed(TERMS_DATA) { index, clause ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = AppTheme.colors.surface),
                border = BorderStroke(1.dp, AppTheme.colors.border)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .background(AppTheme.colors.primary.copy(alpha = 0.1f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${index + 1}",
                                style = AppTheme.typography.labelSmall,
                                color = AppTheme.colors.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = clause.title,
                            style = AppTheme.typography.titleMedium,
                            color = AppTheme.colors.onSurface,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    clause.points.forEach { point ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(
                                text = "• ",
                                style = AppTheme.typography.bodyMedium,
                                color = AppTheme.colors.primary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = point,
                                style = AppTheme.typography.bodyMedium,
                                color = AppTheme.colors.onSurface.copy(alpha = 0.85f),
                                lineHeight = 20.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

