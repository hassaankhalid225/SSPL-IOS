package com.sspl.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerScope
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import kotlin.math.absoluteValue

/**
 * Created by M Imran
 * Senior Software Engineer at
 * BhimSoft on 09/01/2025.
 * se.muhammadimran@gmail.com
 */

@Composable
fun <T> AppHorizontalPager(
    list: List<T>,
    showIndicator: Boolean = true,
    content: @Composable PagerScope.(modifier: Modifier, item: T, page: Int) -> Unit
) {
    val indicator = remember { showIndicator }
    val pagerState = rememberPagerState(pageCount = {
        list.size
    })
    val horizontalPadding = 16.dp
    val itemSpacing = horizontalPadding / 3

    Box(
        modifier = Modifier.fillMaxWidth()
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(
                start = horizontalPadding,
                end = horizontalPadding,
                top = horizontalPadding
            ),
            pageSpacing = itemSpacing
        ) { page ->
            content(
                Modifier.fillMaxWidth()
                    .graphicsLayer {
                        val pageOffSet = (
                                (pagerState.currentPage - page) + pagerState
                                    .currentPageOffsetFraction
                                ).absoluteValue
                        alpha = lerp(
                            start = 0.5f,
                            stop = 1f,
                            fraction = 1f - pageOffSet.coerceIn(0f, 1f)
                        )
                        scaleY = lerp(
                            start = 0.85f,
                            stop = 1f,
                            fraction = 1f - pageOffSet.coerceIn(0f, 1f)
                        )
                    },
                list[page],
                page
            )
        }
        if (indicator) {
            Row(
                Modifier
                    .wrapContentHeight()
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(bottom = horizontalPadding),
                horizontalArrangement = Arrangement.Center
            ) {
                repeat(pagerState.pageCount) { iteration ->
                    val color = if (pagerState.currentPage == iteration) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.inversePrimary
                    }
                    val width = if (pagerState.currentPage == iteration) 20.dp else 14.dp
                    Box(
                        modifier = Modifier
                            .padding(2.dp)
                            .clip(CircleShape)
                            .background(color)
                            .size(width = width, height = 6.dp)
                    )
                }
            }
        }
    }
}