package com.sspl.ui.userdetails

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sspl.core.ApiStates
import com.sspl.core.models.User
import com.sspl.core.models.UserProfile
import com.sspl.core.repositories.UserRepository
import com.sspl.core.usecases.GetUserDetailsUseCase
import com.sspl.session.UserSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val userSession: UserSession,
    private val getUserDetailsUseCase: GetUserDetailsUseCase
) : ViewModel() {

    private val _user = MutableStateFlow<User?>(null)
    val user = _user.asStateFlow()

    private val _userDetails  = MutableStateFlow<ApiStates<UserProfile?>>(ApiStates.Loading)
    val userDetails = _userDetails.asStateFlow()

    init {
        viewModelScope.launch {
            println("Called ProfileViewModel")
            _user.value = userSession.getUser()

        }
    }
    fun getUserDetails() {
        viewModelScope.launch {
            getUserDetailsUseCase.getUserProfile().collect {
                _userDetails.value = it
            }
        }
    }


    fun updateUser(user: User){
        _user.update { user }
    }
    fun onSignOut(){
        viewModelScope.launch {
            userSession.resetToken()
            userSession.resetUser()
        }
    }
}
