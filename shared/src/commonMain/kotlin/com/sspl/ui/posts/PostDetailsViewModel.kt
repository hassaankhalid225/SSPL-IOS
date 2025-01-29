package com.sspl.ui.posts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sspl.core.ApiStates
import com.sspl.core.usecases.GetPostDetailsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PostDetailsViewModel(
    private val getPostDetailsUseCase: GetPostDetailsUseCase,
) : ViewModel() {

    private val _postDetails: MutableStateFlow<ApiStates<Boolean>> =
        MutableStateFlow(ApiStates.Loading)
    val postDetails = _postDetails.asStateFlow()
init {

}

    fun getPostDetails(id: Long) {
        viewModelScope.launch {
            getPostDetailsUseCase.invoke(id).collect { state->
                println("Hello $state")
                _postDetails.update { state }
            }
        }
    }


}