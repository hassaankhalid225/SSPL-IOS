package com.sspl.ui.postscreen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sspl.ui.components.FullScreenProgressIndicator
 import com.sspl.ui.components.NoItems
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun PostScreen(modifier: Modifier = Modifier, viewModel: PostViewModel = koinViewModel()) {

    val uiState by viewModel.uiState.collectAsState()

    when (uiState) {
        is PostUiState.LoadingPosts -> {
            FullScreenProgressIndicator()
        }

        is PostUiState.NoPostFound -> {
            NoItems()
        }

        is PostUiState.Success -> {
            LazyColumn(
                modifier = modifier
                    .fillMaxSize()
                    .padding(vertical = 4.dp), verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                posts((uiState as PostUiState.Success).posts)
            }
        }
    }

}


@Preview
@Composable
fun PostScreenPreview() {
    PostScreen()
}