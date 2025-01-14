package com.sspl.ui.posts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sspl.ui.components.FullScreenProgressIndicator
import com.sspl.ui.components.LoadMoreLazyColumn
import com.sspl.ui.components.NoItems
import kotlinx.coroutines.flow.distinctUntilChanged

import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel

/**
 * Created by M Imran
 * Senior Software Engineer at
 * BhimSoft on 09/01/2025.
 * se.muhammadimran@gmail.com
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostScreen(
    modifier: Modifier = Modifier, viewModel: PostViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val isLoadingMore by viewModel.isLoadingMore.collectAsState()
    val lazyListState = rememberLazyListState()

    when (uiState) {
        is PostUiState.LoadingPosts -> {
            FullScreenProgressIndicator()
        }

        is PostUiState.NoPostFound -> {
            NoItems()
        }

        is PostUiState.Success -> {
            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = { viewModel.onEvent(PostScreenEvents.RefreshPosts) },
                modifier = modifier
            ) {
                LoadMoreLazyColumn(
                    items = (uiState as PostUiState.Success).posts,
                    isLoadingMore = isLoadingMore,
                    onLoadMore = { viewModel.onEvent(PostScreenEvents.LoadMorePosts) },
                    itemContent = { post ->
                        PostItem(post = post,
                            onLikePost = { viewModel.onEvent(PostScreenEvents.LikePost(post.postId)) },
                            onDislikePost = { viewModel.onEvent(PostScreenEvents.DislikePost(post.postId)) },
                            onCommentPost = { viewModel.onEvent(PostScreenEvents.CommentPost(post.postId)) })
                    },
                    modifier = modifier.fillMaxSize().padding(vertical = 4.dp),
                    contentPadding = PaddingValues(vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                )
            }
        }
    }
}


@Preview
@Composable
fun PostScreenPreview() {
    PostScreen()
}