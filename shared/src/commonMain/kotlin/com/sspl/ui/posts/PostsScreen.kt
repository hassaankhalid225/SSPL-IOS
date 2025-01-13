package com.sspl.ui.posts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sspl.ui.components.FullScreenProgressIndicator
import com.sspl.ui.components.NoItems

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
fun PostScreen(modifier: Modifier = Modifier, viewModel: PostViewModel = koinViewModel()) {

    val uiState by viewModel.uiState.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()

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
                LazyColumn(
                    modifier = modifier.fillMaxSize().padding(vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    posts(
                        (uiState as PostUiState.Success).posts,
                        onLikePost = { viewModel.onEvent(PostScreenEvents.LikePost(it)) },
                        onDislikePost = { viewModel.onEvent(PostScreenEvents.DislikePost(it)) },
                        onCommentPost = { viewModel.onEvent(PostScreenEvents.CommentPost(it)) },
                        modifier = Modifier
                    )
                }
            }
        }
    }

}


@Preview
@Composable
fun PostScreenPreview() {
    PostScreen()
}