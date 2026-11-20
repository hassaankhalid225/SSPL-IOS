package com.sspl.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.sspl.designsystem.components.LoadingDots
import com.sspl.designsystem.theme.AppTheme

/**
 * Legacy loading indicator, kept for existing callers. Renders the design-system
 * [LoadingDots] (no circular spinner). Prefer skeletons for content.
 */
@Composable
fun AppProgressBar(modifier: Modifier = Modifier.fillMaxSize(), color: Color? = null) {
    Box(modifier, contentAlignment = Alignment.Center) {
        LoadingDots(color = color ?: AppTheme.colors.primary)
    }
}
