package com.sspl.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sspl.designsystem.components.LoadingDots
import com.sspl.designsystem.components.SkeletonListItem
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter

/**
 * LazyColumn that asks for the next page when the user nears the end.
 * The load-more footer is a skeleton row + [LoadingDots] (no spinner).
 */
@Composable
fun <T> LoadMoreLazyColumn(
    items: List<T>,
    isLoadingMore: Boolean,
    onLoadMore: () -> Unit,
    itemContent: @Composable (T) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start
) {
    val lazyListState = rememberLazyListState()
    val currentItems by rememberUpdatedState(items)
    val loading by rememberUpdatedState(isLoadingMore)
    val loadMore by rememberUpdatedState(onLoadMore)

    LazyColumn(
        state = lazyListState,
        modifier = modifier.fillMaxSize(),
        contentPadding = contentPadding,
        verticalArrangement = verticalArrangement,
        horizontalAlignment = horizontalAlignment
    ) {
        items(items, key = { it.hashCode() }) { item ->
            itemContent(item)
        }
        if (isLoadingMore) {
            item(key = "load_more_indicator") { LoadMoreIndicator() }
        }
    }

    // Trigger when the last visible item is within 2 of the end (uses the latest list/flags).
    LaunchedEffect(lazyListState) {
        snapshotFlow { lazyListState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .distinctUntilChanged()
            .filter { it != null }
            .collect { lastVisibleIndex ->
                if (currentItems.isNotEmpty() && lastVisibleIndex!! >= currentItems.lastIndex - 1 && !loading) {
                    loadMore()
                }
            }
    }
}

/** Load-more footer: one skeleton row and soft dots. */
@Composable
fun LoadMoreIndicator() {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        SkeletonListItem(Modifier.fillMaxWidth())
        Spacer(Modifier.height(10.dp))
        LoadingDots()
    }
}
