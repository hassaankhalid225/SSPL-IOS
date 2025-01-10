package com.sspl.ui.postscreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sspl.core.models.Post
import com.sspl.core.models.usecases.GetPostsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn

class PostViewModel(
    private val getPostsUseCase: GetPostsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<PostUiState>(PostUiState.LoadingPosts)
    val uiState: StateFlow<PostUiState> = _uiState.onStart {
        getPostsUseCase().collect { posts ->
            if (posts.isEmpty()) {
                _uiState.value = PostUiState.NoPostFound
            } else {
                _uiState.value = PostUiState.Success(posts)
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = PostUiState.LoadingPosts
    )

}

sealed interface PostUiState {
    data object LoadingPosts : PostUiState
    data object NoPostFound : PostUiState
    data class Success(val posts: List<Post>) : PostUiState
}