package com.sspl.ui.history

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
import androidx.compose.material.icons.filled.History
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

data class HistoryMilestone(
    val era: String,
    val title: String,
    val description: String
)

private val HISTORY_MILESTONES = listOf(
    HistoryMilestone(
        era = "1950s",
        title = "Association of Surgeons of Lahore",
        description = "The surgeons of Lahore organized an association in the early 1950s to discuss and share their clinical and operative experiences with colleagues. Anesthetists, pathologists, and radiologists were also included as associate members of this body, which became known as the Association of Surgeons of Lahore."
    ),
    HistoryMilestone(
        era = "1980s",
        title = "Revival Under Professor Bashir Ahmad",
        description = "In the early 1980s, this association was revived under the convenorship of Professor Bashir Ahmad, one of the life members of the earlier Association. Following a membership drive, elections were conducted: Professor Ijaz Ahsan was elected as President, Professor Riaz Ahsan Siddique as General Secretary, and Dr. Saeed Choudhary as Finance Secretary."
    ),
    HistoryMilestone(
        era = "24 Oct 1984",
        title = "A Dedicated Forum for Lahore Surgeons",
        description = "On 24th October 1984, Professor S.A.R. Gardezi convened a meeting of general surgeons from all teaching institutions of Lahore and highlighted the need for a dedicated chapter for surgeons in Lahore under the umbrella of the Society of Surgeons of Pakistan."
    ),
    HistoryMilestone(
        era = "Founding Meeting",
        title = "King Edward Medical College & Registration",
        description = "A historic meeting was arranged in the committee room of King Edward Medical College. Approximately forty senior surgeons attended, including Prof. Rashid Ahmad Qureshi, Prof. Masood Ahmed, Prof. S. Zafar Haider, and Dr. Abdul Majeed Choudhary. The Society was formally registered after approval of its provisional constitution."
    ),
    HistoryMilestone(
        era = "1985",
        title = "First Elected Office Bearers",
        description = "The first official election of the Society was held in 1985. Professor Rashid Ahmad Qureshi was elected as the Founding President, Dr. Abdul Majeed Choudhary as General Secretary, and Dr. Mazhar Saeed Choudhary as Finance Secretary."
    ),
    HistoryMilestone(
        era = "1985 — Present",
        title = "Growth, Conferences & Democratic Leadership",
        description = "Since then, the Society of Surgeons of Pakistan – Lahore Chapter has organized regular clinical meetings, biennial national and international surgical conferences, hands-on workshops, and biannual democratic elections. Over four decades, SSPL has emerged as one of the leading academic surgical bodies in the nation."
    )
)

@Composable
fun HistoryScreen(navController: NavController) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(AppTheme.colors.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
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
                                imageVector = Icons.Default.History,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "OUR STORY",
                            style = AppTheme.typography.labelSmall,
                            color = AppTheme.colors.secondary,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.3.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "History of SSP Lahore",
                        style = AppTheme.typography.headlineMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Over four decades of clinical heritage, democratic leadership, and commitment to surgical excellence in Pakistan.",
                        style = AppTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
            }
        }

        // Timeline milestones
        itemsIndexed(HISTORY_MILESTONES) { index, milestone ->
            MilestoneCard(
                stepNumber = index + 1,
                milestone = milestone,
                isLast = index == HISTORY_MILESTONES.size - 1
            )
        }
    }
}

@Composable
private fun MilestoneCard(
    stepNumber: Int,
    milestone: HistoryMilestone,
    isLast: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(AppTheme.colors.primary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$stepNumber",
                    style = AppTheme.typography.titleSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(120.dp)
                        .background(AppTheme.colors.border)
                )
            }
        }

        Card(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = AppTheme.colors.surface),
            border = BorderStroke(1.dp, AppTheme.colors.border)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = milestone.era.uppercase(),
                    style = AppTheme.typography.labelSmall,
                    color = AppTheme.colors.accent,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = milestone.title,
                    style = AppTheme.typography.titleMedium,
                    color = AppTheme.colors.onSurface,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = milestone.description,
                    style = AppTheme.typography.bodyMedium,
                    color = AppTheme.colors.onSurface.copy(alpha = 0.85f),
                    lineHeight = 20.sp
                )
            }
        }
    }
}

