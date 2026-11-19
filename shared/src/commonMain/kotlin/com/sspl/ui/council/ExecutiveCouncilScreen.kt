package com.sspl.ui.council

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.sspl.designsystem.components.AvatarImage
import com.sspl.designsystem.components.BadgeVariant
import com.sspl.designsystem.components.SsplBadge
import com.sspl.designsystem.theme.AppTheme

data class CouncilLeader(
    val name: String,
    val role: String,
    val isPrimary: Boolean = false
)

private val EXECUTIVE_COUNCIL = listOf(
    CouncilLeader("Prof. Dr. Ali Azhar Naeem", "President", isPrimary = true),
    CouncilLeader("Prof. Dr. Tariq Saeed", "President Elect", isPrimary = true),
    CouncilLeader("Prof. Dr. Saleem Qamar", "Vice President"),
    CouncilLeader("Prof. Dr. Faisal Mushtaq", "Honorary Secretary"),
    CouncilLeader("Prof. Dr. Naveed Akhtar", "Joint Secretary"),
    CouncilLeader("Prof. Dr. Irfan Adam", "Treasurer"),
    CouncilLeader("Prof. Dr. Muhammad Umar", "Editor Publications"),
    CouncilLeader("Prof. Dr. Ayaz Farooq", "Chairman Education"),
    CouncilLeader("Prof. Dr. M. Shahid Iqbal", "Chairman Events"),
    CouncilLeader("Prof. Dr. Nadia Tahir", "Executive Member")
)

private val CABINET_2026 = listOf(
    CouncilLeader("Prof. Farooq Ahmad Rana", "President", isPrimary = true),
    CouncilLeader("Dr. Awais Amjad Malik", "Vice President", isPrimary = true),
    CouncilLeader("Dr. Ahmad Naeem Akhtar", "General Secretary"),
    CouncilLeader("Dr. Muhammad Aamir Jameel", "Joint Secretary"),
    CouncilLeader("Dr. Hafiz Mahmood Ahmed", "Finance Secretary"),
    CouncilLeader("Dr. Zubair Ahmed Khan", "Publication Secretary"),
    CouncilLeader("Prof. Muhammad Imran Khokhar", "Executive Member"),
    CouncilLeader("Prof. Abul Fazal Ali Khan", "Executive Member"),
    CouncilLeader("Prof. Faisal Masood", "Executive Member"),
    CouncilLeader("Prof. Syed Asghar Naqi", "Executive Member"),
    CouncilLeader("Prof. Moeed Iqbal Qureshi", "Executive Member"),
    CouncilLeader("Prof. Muhammad Imran Anwar", "Executive Member"),
    CouncilLeader("Dr. Muhammad Arshad Cheema", "Executive Member"),
    CouncilLeader("Dr. Muhammad Umar Warraich", "Executive Member"),
    CouncilLeader("Prof. Shahzad Alam Shah", "Executive Member"),
    CouncilLeader("Prof. Ch. Mohammad Kamran", "Executive Member"),
    CouncilLeader("Prof. Tayyab Abbas", "Executive Member"),
    CouncilLeader("Prof. Muhammad Zeeshan Sarwar", "Executive Member")
)

@Composable
fun ExecutiveCouncilScreen(navController: NavController) {
    var selectedTab by remember { mutableStateOf(0) } // 0: Executive Council, 1: Cabinet 2026-2028

    val activeList = if (selectedTab == 0) EXECUTIVE_COUNCIL else CABINET_2026

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(AppTheme.colors.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Hero Header
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = AppTheme.colors.primary)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "SOCIETY LEADERSHIP",
                        style = AppTheme.typography.labelSmall,
                        color = AppTheme.colors.secondary,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.3.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Executive Council & Cabinet",
                        style = AppTheme.typography.headlineMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "The distinguished surgical professors and consultants guiding SSP Lahore's academic and clinical vision.",
                        style = AppTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.88f)
                    )
                }
            }
        }

        // Tab Selector Row
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(AppTheme.colors.surface, RoundedCornerShape(12.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                CouncilTabButton(
                    title = "Executive Council",
                    isSelected = selectedTab == 0,
                    modifier = Modifier.weight(1f),
                    onClick = { selectedTab = 0 }
                )
                CouncilTabButton(
                    title = "Cabinet 2026–2028",
                    isSelected = selectedTab == 1,
                    modifier = Modifier.weight(1f),
                    onClick = { selectedTab = 1 }
                )
            }
        }

        // Council Member Cards
        items(activeList) { leader ->
            CouncilMemberCard(leader = leader)
        }
    }
}

@Composable
private fun CouncilTabButton(
    title: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .background(
                color = if (isSelected) AppTheme.colors.primary else Color.Transparent,
                shape = RoundedCornerShape(10.dp)
            )
            .clickable { onClick() }
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            style = AppTheme.typography.labelMedium,
            color = if (isSelected) Color.White else AppTheme.colors.textMuted,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
        )
    }
}

@Composable
private fun CouncilMemberCard(leader: CouncilLeader) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = AppTheme.colors.surface),
        border = BorderStroke(1.dp, AppTheme.colors.border)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AvatarImage(
                name = leader.name,
                size = 50.dp
            )
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = leader.name,
                    style = AppTheme.typography.titleSmall,
                    color = AppTheme.colors.onSurface,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                SsplBadge(
                    text = leader.role,
                    variant = when {
                        leader.isPrimary -> BadgeVariant.PRIMARY
                        leader.role.contains("Secretary") || leader.role.contains("Treasurer") -> BadgeVariant.INFO
                        else -> BadgeVariant.NEUTRAL
                    }
                )
            }
        }
    }
}

