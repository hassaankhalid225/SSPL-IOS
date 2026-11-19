package com.sspl.designsystem.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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
    NavigationBar(
        modifier = modifier.fillMaxWidth(),
        containerColor = AppTheme.colors.surface,
        tonalElevation = 6.dp
    ) {
        BOTTOM_NAV_ITEMS.forEach { item ->
            val isSelected = currentTab == item.tab
            val selectedColor = AppTheme.colors.primary
            val unselectedColor = AppTheme.colors.muted

            NavigationBarItem(
                selected = isSelected,
                onClick = { onTabSelected(item.tab) },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.tab.label,
                        modifier = Modifier.size(22.dp)
                    )
                },
                label = {
                    Text(
                        text = item.tab.label,
                        style = AppTheme.typography.labelSmall.copy(
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = selectedColor,
                    selectedTextColor = selectedColor,
                    unselectedIconColor = unselectedColor,
                    unselectedTextColor = unselectedColor,
                    indicatorColor = AppTheme.colors.surfaceTint
                )
            )
        }
    }
}

