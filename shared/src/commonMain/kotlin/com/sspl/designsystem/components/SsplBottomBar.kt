package com.sspl.designsystem.components

import com.sspl.designsystem.icons.SsplIcons

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.Widgets
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sspl.designsystem.theme.AppTheme
import com.sspl.navigation.BottomBarTab

data class BottomNavItem(
    val tab: BottomBarTab,
    val icon: ImageVector,
    val selectedIcon: ImageVector = icon
)

val BOTTOM_NAV_ITEMS = listOf(
    BottomNavItem(BottomBarTab.HOME, SsplIcons.Home, SsplIcons.Home),
    BottomNavItem(BottomBarTab.EVENTS, SsplIcons.CalendarMonth, SsplIcons.CalendarMonth),
    BottomNavItem(BottomBarTab.FELLOWSHIP, SsplIcons.School, SsplIcons.School),
    BottomNavItem(BottomBarTab.MY_PORTAL, SsplIcons.Dashboard, SsplIcons.Dashboard),
    BottomNavItem(BottomBarTab.MORE, SsplIcons.Widgets, SsplIcons.Widgets)
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
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        shadowElevation = if (AppTheme.isDark) 0.dp else 18.dp,
        // Shadows vanish on dark backgrounds; a hairline defines the edge instead.
        border = if (AppTheme.isDark) androidx.compose.foundation.BorderStroke(1.dp, AppTheme.colors.border) else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .height(68.dp)
                .padding(horizontal = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BOTTOM_NAV_ITEMS.forEach { item ->
                BottomTab(
                    item = item,
                    selected = currentTab == item.tab,
                    onClick = { onTabSelected(item.tab) }
                )
            }
        }
    }
}

@Composable
private fun BottomTab(item: BottomNavItem, selected: Boolean, onClick: () -> Unit) {
    val colors = AppTheme.colors
    val tint by animateColorAsState(if (selected) Color.White else colors.muted, label = "tabTint")
    Row(
        modifier = Modifier
            .height(46.dp)
            .clip(RoundedCornerShape(50))
            .background(if (selected) colors.primaryGradient else SolidColor(Color.Transparent))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = androidx.compose.material3.ripple(bounded = true),
                role = Role.Tab,
                onClick = onClick
            )
            .semantics {
                this.selected = selected
                contentDescription = item.tab.label
            }
            .padding(horizontal = if (selected) 16.dp else 13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Selected icon: small spring "pop" each time the tab becomes active.
        val pop = androidx.compose.runtime.remember { androidx.compose.animation.core.Animatable(1f) }
        androidx.compose.runtime.LaunchedEffect(selected) {
            if (selected) {
                pop.snapTo(0.72f)
                pop.animateTo(1f, spring(dampingRatio = 0.42f, stiffness = 420f))
            }
        }
        Icon(
            imageVector = if (selected) item.selectedIcon else item.icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier
                .size(22.dp)
                .graphicsLayer { scaleX = pop.value; scaleY = pop.value }
        )
        AnimatedVisibility(
            visible = selected,
            enter = fadeIn() + expandHorizontally(spring(stiffness = 500f)),
            exit = fadeOut() + shrinkHorizontally(spring(stiffness = 500f))
        ) {
            Row {
                Spacer(Modifier.width(8.dp))
                Text(
                    text = item.tab.label,
                    style = AppTheme.typography.labelMedium.copy(color = Color.White, fontWeight = FontWeight.SemiBold),
                    maxLines = 1
                )
            }
        }
    }
}
