package com.sspl.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.sspl.designsystem.components.EmptyState
import com.sspl.designsystem.theme.AppTheme

/** Legacy empty placeholder, now rendered with the design-system [EmptyState]. */
@Composable
fun NoItems(
    modifier: Modifier = Modifier,
    title: String? = "Start by adding a new item",
    showFloatingActionButton: Boolean = false,
    onFloatingActionButtonClick: () -> Unit = {},
) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        EmptyState(
            title = "Nothing here yet",
            message = title ?: "",
            icon = Icons.Default.Inbox
        )
        AnimatedVisibility(
            showFloatingActionButton,
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp)
        ) {
            AppFloatingActionButton(onButtonClick = onFloatingActionButtonClick)
        }
    }
}

@Composable
fun AppFloatingActionButton(
    modifier: Modifier = Modifier, onButtonClick: () -> Unit = {}
) {
    Box(
        modifier = modifier
            .size(56.dp)
            .clip(AppTheme.shapes.chip)
            .background(AppTheme.colors.primaryGradient)
            .clickable(onClick = onButtonClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(Icons.Default.Add, contentDescription = "Add", tint = Color.White, modifier = Modifier.size(26.dp))
    }
}
