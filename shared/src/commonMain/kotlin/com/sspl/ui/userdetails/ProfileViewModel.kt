package com.sspl.ui.userdetails

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sspl.core.ApiStates
import com.sspl.core.models.ApiError
import com.sspl.core.models.User
import com.sspl.core.repositories.UserRepository
import com.sspl.core.usecases.GetUserDetailsUseCase
import com.sspl.session.UserSession
import io.ktor.client.call.body
import io.ktor.http.isSuccess
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val userSession: UserSession,
    private val getUserDetailsUseCase: GetUserDetailsUseCase,
    private val userRepository: UserRepository,
) : ViewModel() {

    /** True while the profile photo is being uploaded (screen shows BlockingLoader). */
    private val _isUploadingPhoto = MutableStateFlow(false)
    val isUploadingPhoto = _isUploadingPhoto.asStateFlow()

    /** One-off user messages (upload success / failure). */
    private val _messages = MutableSharedFlow<String>()
    val messages = _messages.asSharedFlow()
    
    val user = userSession.userFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )
    
    val isGuestUser = userSession.userFlow.map { user ->
        user?.account?.isGuestUser() ?: true
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    private val _userDetails = MutableStateFlow<ApiStates<User?>>(ApiStates.Loading)
    val userDetails = _userDetails.asStateFlow()

    val profileImage = userSession.profileImageFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    var onSignedOut = MutableStateFlow(false)
        private set

    fun updateUser(user: User) {
        viewModelScope.launch {
            userSession.setUser(user)
            _userDetails.update { ApiStates.Success(user) }
        }
    }

    fun updateUserDetails(userDetails: User) {
        viewModelScope.launch {
            userSession.setUser(userDetails)
            _userDetails.update { ApiStates.Success(userDetails) }
        }
    }

    fun getUserDetails() {
        viewModelScope.launch {
            getUserDetailsUseCase.invoke().collect { state ->
                _userDetails.value = state
                if (state is ApiStates.Success) {
                    state.data?.let { data ->
                        userSession.setUser(data)
                    }
                }
            }
        }
    }

    /**
     * Uploads the picked photo to the server (PUT /profiles/me multipart, field `profile_picture`,
     * same as the web), then re-reads the profile and stores it in the session so the avatar
     * updates everywhere (drawer, top bar, home hero). The old local base64 copy is cleared so the
     * server image wins. On failure nothing is cached locally and a clear message is shown.
     * Bytes come from the Peekaboo picker already resized to <= 1024 px JPEG.
     */
    fun updateProfileImage(byteArray: ByteArray) {
        if (_isUploadingPhoto.value) return
        viewModelScope.launch {
            _isUploadingPhoto.value = true
            try {
                val current = userSession.userFlow.value
                val response = userRepository.uploadProfilePicture(
                    imageBytes = byteArray,
                    firstName = current?.firstName,
                    lastName = current?.lastName
                )
                if (!response.status.isSuccess()) {
                    val msg = runCatching { response.body<ApiError>().message }.getOrNull().orEmpty()
                    _messages.emit(
                        if (msg.isNotBlank()) "Couldn't update your photo: $msg"
                        else "Couldn't update your photo (error ${response.status.value}). Please try again."
                    )
                    return@launch
                }
                // Some backends echo the updated user; use it as a fallback if the refresh fails.
                val echoed = runCatching { response.body<User>() }.getOrNull()

                // Server image wins from now on.
                userSession.setProfileImage(null)

                val refreshed = getUserDetailsUseCase().first { it !is ApiStates.Loading }
                val fresh = (refreshed as? ApiStates.Success)?.data
                    ?: echoed?.takeIf { !it.profilePicture.isNullOrBlank() }?.let { e ->
                        current?.copy(profilePicture = e.profilePicture) ?: e
                    }
                if (fresh != null) {
                    val merged = fresh.copy(
                        account = fresh.account ?: current?.account,
                        roles = fresh.roles.ifEmpty { current?.roles.orEmpty() },
                        role = fresh.role ?: current?.role
                    )
                    userSession.setUser(merged)
                    _userDetails.value = ApiStates.Success(merged)
                    _messages.emit("Profile photo updated")
                } else {
                    _messages.emit("Photo uploaded. Pull to refresh to see it.")
                }
            } catch (e: Exception) {
                _messages.emit("Couldn't upload your photo. Check your connection and try again.")
            } finally {
                _isUploadingPhoto.value = false
            }
        }
    }

    fun onSignOut() {
        viewModelScope.launch {
            userSession.resetToken()
            userSession.resetUser()
            onSignedOut.update { true }
        }
    }
}
