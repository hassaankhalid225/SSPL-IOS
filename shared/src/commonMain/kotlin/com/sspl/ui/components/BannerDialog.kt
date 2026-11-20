package com.sspl.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.sspl.core.models.Banner
import com.sspl.designsystem.components.GlassIconButton
import com.sspl.designsystem.components.PrimaryButton
import com.sspl.designsystem.components.shimmer
import com.sspl.designsystem.theme.AppTheme

/**
 * Welcome pop-up banner (shown by App.kt when a WELCOME banner is active).
 * Behaviour unchanged: same banners, same URL rule (`baseUrl` + `/` if missing + `image_url`),
 * dismiss via close / button / swipe. Visuals: elevated sheet card, skeleton while the image loads,
 * branded fallback if it fails.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BannerDialog(
    banners: List<Banner>,
    baseUrl: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (banners.isEmpty()) {
        LaunchedEffect(Unit) { onDismiss() }
        return
    }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.Transparent,
        dragHandle = null
    ) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(16.dp)
                .clip(AppTheme.shapes.card)
                .background(AppTheme.colors.surfaceElevated)
                .animateContentSize()
        ) {
            Box {
                if (banners.size == 1) {
                    BannerItem(banner = banners[0], baseUrl = baseUrl)
                } else {
                    BannerPager(banners = banners, baseUrl = baseUrl)
                }
                // Close sits on the image (white on a glass chip is allowed on image surfaces).
                Box(Modifier.align(Alignment.TopEnd).padding(10.dp)) {
                    GlassIconButton(Icons.Default.Close, "Close", onDismiss)
                }
            }
            PrimaryButton(
                text = "Continue to app",
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            )
        }
    }
}

@Composable
private fun BannerPager(banners: List<Banner>, baseUrl: String) {
    val pagerState = rememberPagerState(pageCount = { banners.size })
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        HorizontalPager(state = pagerState, contentPadding = PaddingValues(0.dp)) { page ->
            BannerItem(banner = banners[page], baseUrl = baseUrl)
        }
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            repeat(banners.size) { index ->
                val selected = index == pagerState.currentPage
                Box(
                    Modifier
                        .height(6.dp)
                        .width(if (selected) 18.dp else 6.dp)
                        .clip(AppTheme.shapes.chip)
                        .background(if (selected) AppTheme.colors.primary else AppTheme.colors.border)
                )
            }
        }
    }
}

@Composable
private fun BannerItem(banner: Banner, baseUrl: String) {
    // Frozen URL rule for banners.
    val fullImageUrl = if (banner.imageUrl.startsWith("/")) "$baseUrl${banner.imageUrl}" else "$baseUrl/${banner.imageUrl}"
    var state by remember(fullImageUrl) { mutableStateOf(0) } // 0 loading, 1 ok, 2 failed

    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            Modifier
                .fillMaxWidth()
                .then(if (state != 1) Modifier.aspectRatio(4f / 5f) else Modifier),
            contentAlignment = Alignment.Center
        ) {
            if (state != 2) {
                AsyncImage(
                    model = fullImageUrl,
                    contentDescription = banner.title ?: "Welcome banner",
                    contentScale = ContentScale.FillWidth,
                    modifier = Modifier.fillMaxWidth(),
                    onSuccess = { state = 1 },
                    onError = { state = 2 }
                )
            }
            if (state == 0) Box(Modifier.fillMaxSize().shimmer(RectangleShape))
            if (state == 2) {
                Box(
                    Modifier.fillMaxSize().background(AppTheme.colors.headerGradient),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Image, contentDescription = null, tint = Color.White.copy(alpha = 0.55f), modifier = Modifier.size(44.dp))
                }
            }
        }
        if (!banner.title.isNullOrBlank()) {
            Text(
                text = banner.title,
                style = AppTheme.typography.titleMedium.copy(color = AppTheme.colors.heading),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 14.dp)
            )
        }
    }
}
