package com.sspl.designsystem.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import coil3.compose.AsyncImage
import com.sspl.core.models.Banner
import com.sspl.designsystem.theme.AppTheme
import com.sspl.utils.BASE_URL
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue

@Composable
fun BannerCarousel(
    banners: List<Banner>,
    modifier: Modifier = Modifier,
    onBannerClick: ((Banner) -> Unit)? = null,
    autoScrollDelayMs: Long = 4000L
) {
    // Behaviour frozen: SLIDER or untyped banners, max 3 on Home.
    val sliderBanners = banners.filter { it.type == "SLIDER" || it.type == null }.take(3)
    if (sliderBanners.isEmpty()) return

    val pagerState = rememberPagerState(pageCount = { sliderBanners.size })
    val scope = rememberCoroutineScope()

    LaunchedEffect(sliderBanners.size) {
        if (sliderBanners.size > 1) {
            while (true) {
                delay(autoScrollDelayMs)
                val nextPage = if (pagerState.currentPage + 1 >= sliderBanners.size) 0 else pagerState.currentPage + 1
                scope.launch {
                    pagerState.animateScrollToPage(nextPage)
                }
            }
        }
    }

    val rawBaseUrl = BASE_URL.trimEnd('/')

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 6.dp),
            pageSpacing = 6.dp
        ) { page ->
            val banner = sliderBanners[page]
            val fullImageUrl = when {
                banner.imageUrl.isNullOrBlank() -> ""
                banner.imageUrl.startsWith("http://") || banner.imageUrl.startsWith("https://") -> banner.imageUrl
                banner.imageUrl.startsWith("/") -> "$rawBaseUrl${banner.imageUrl}"
                else -> "$rawBaseUrl/${banner.imageUrl}"
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 165.dp)
                    .aspectRatio(16f / 7f)
                    .graphicsLayer {
                        val pageOffset = (
                            (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction
                        ).absoluteValue
                        alpha = lerp(
                            start = 0.5f,
                            stop = 1f,
                            fraction = 1f - pageOffset.coerceIn(0f, 1f)
                        )
                        scaleY = lerp(
                            start = 0.85f,
                            stop = 1f,
                            fraction = 1f - pageOffset.coerceIn(0f, 1f)
                        )
                    }
                    .clip(AppTheme.shapes.banner)
                    .background(AppTheme.colors.surfaceTint)
                    .then(
                        if (onBannerClick != null) Modifier.clickable { onBannerClick(banner) } else Modifier
                    )
            ) {
                if (fullImageUrl.isNotBlank()) {
                    // Shimmer while the (often large, slow) image downloads; branded fallback on error.
                    SsplImage(
                        source = fullImageUrl,
                        contentDescription = banner.title ?: "Banner",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.matchParentSize()
                    )
                }
            }
        }

        if (sliderBanners.size > 1) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 2.dp)
            ) {
                repeat(sliderBanners.size) { index ->
                    val isSelected = pagerState.currentPage == index
                    val width by animateDpAsState(if (isSelected) 18.dp else 6.dp)
                    val color = if (isSelected) AppTheme.colors.primary else AppTheme.colors.border

                    Box(
                        modifier = Modifier
                            .height(6.dp)
                            .width(width)
                            .clip(CircleShape)
                            .background(color)
                    )
                }
            }
        }
    }
}

