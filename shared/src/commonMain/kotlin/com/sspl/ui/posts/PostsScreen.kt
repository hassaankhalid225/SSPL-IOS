package com.sspl.ui.posts

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.sspl.Screen
import com.sspl.ui.components.FullScreenProgressIndicator
import com.sspl.ui.components.LoadMoreLazyColumn
import com.sspl.ui.components.NoItems
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
    navController: NavController, modifier: Modifier = Modifier, viewModel: PostViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
    val isLoadingMore by viewModel.isLoadingMore.collectAsStateWithLifecycle()


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
                        PostItem(post = post, modifier = Modifier,
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


