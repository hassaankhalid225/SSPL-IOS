package com.sspl.designsystem.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sspl.designsystem.theme.AppTheme
import com.sspl.navigation.BottomBarTab

data class BottomNavItem(
    val tab: BottomBarTab,
    val icon: ImageVector
)

val BOTTOM_NAV_ITEMS = listOf(
    BottomNavItem(BottomBarTab.HOME, Icons.Default.Home),
    BottomNavItem(BottomBarTab.EVENTS, Icons.Default.CalendarMonth),
    BottomNavItem(BottomBarTab.FELLOWSHIP, Icons.Default.School),
    BottomNavItem(BottomBarTab.MY_PORTAL, Icons.Default.Person),
    BottomNavItem(BottomBarTab.MORE, Icons.Default.MoreHoriz)
)

@Composable
fun SsplBottomBar(
    currentTab: BottomBarTab,
    onTabSelected: (BottomBarTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = AppTheme.colors.surface,
        shadowElevation = 8.dp,
        border = BorderStroke(1.dp, AppTheme.colors.border)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BOTTOM_NAV_ITEMS.forEach { item ->
                val isSelected = currentTab == item.tab
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { onTabSelected(item.tab) }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .height(32.dp)
                            .width(46.dp)
                            .clip(RoundedCornerShape(50.dp))
                            .background(
                                if (isSelected) AppTheme.colors.primaryDark else Color.Transparent
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.tab.label,
                            tint = if (isSelected) Color.White else AppTheme.colors.muted,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = item.tab.label,
                        style = AppTheme.typography.labelSmall.copy(
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) AppTheme.colors.heading else AppTheme.colors.muted
                        ),
                        maxLines = 1
                    )
                }
            }
        }
    }
}

