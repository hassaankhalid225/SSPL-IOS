package com.sspl.ui.userdetails

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sspl.core.models.User
import com.sspl.session.UserSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val userSession: UserSession
) : ViewModel() {

    private val _user = MutableStateFlow<User?>(null)
    val user = _user.asStateFlow()

    init {
        viewModelScope.launch {
            println("Called ProfileViewModel")
            _user.value = userSession.getUser()
        }
    }

    fun onSignOut(){
        viewModelScope.launch {
            userSession.resetToken()
            userSession.resetUser()
        }
    }
}
