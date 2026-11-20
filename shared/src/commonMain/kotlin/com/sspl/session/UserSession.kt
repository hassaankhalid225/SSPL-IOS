package com.sspl.session

import com.sspl.core.models.User
import com.sspl.storage.Storage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Created by M Imran
 * Senior Software Engineer at
 * BhimSoft on 12/12/2024.
 * se.muhammadimran@gmail.com
 */
class UserSession(private val storage: Storage) {
    companion object {
        private const val TOKEN_KEY = "user_token_key"
        private const val REFRESH_TOKEN_KEY = "refresh_token_key"
        private const val USER_KEY = "user_object_key"
        private const val DEVICE_TOKEN_KEY = "device_token_key"
        private const val PROFILE_IMAGE_KEY = "profile_image_key"
    }

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    private val _userFlow = MutableStateFlow<User?>(null)
    val userFlow = _userFlow.asStateFlow()

    private val _profileImageFlow = MutableStateFlow<String?>(null)
    val profileImageFlow = _profileImageFlow.asStateFlow()

    init {
        // Initialize flow with stored value
        runBlocking {
            try {
                val userString = storage.getString(USER_KEY)
                if (userString != null) {
                    _userFlow.value = json.decodeFromString<User>(userString)
                }
            } catch (e: Exception) {
                println("UserSession init: Failed to decode cached user: ${e.message}")
            }
            try {
                _profileImageFlow.value = storage.getString(PROFILE_IMAGE_KEY)
            } catch (e: Exception) {
                println("UserSession init: Failed to get profile image: ${e.message}")
            }
        }
    }

    var token: String? = null
        private set
        get() {
            if (field == null) {
                runBlocking { field = getToken() }
            }
            return field
        }

    var refreshToken: String? = null
        private set
        get() {
            if (field == null) {
                runBlocking { field = getRefreshToken() }
            }
            return field
        }

    var deviceToken: String? = null
        private set
        get() {
            if (field == null) {
                runBlocking { field = getDeviceToken() }
            }
            return field
        }

    private suspend fun getToken(): String? = storage.getString(TOKEN_KEY)
    private suspend fun getRefreshToken(): String? = storage.getString(REFRESH_TOKEN_KEY)
    private suspend fun getDeviceToken(): String? = storage.getString(DEVICE_TOKEN_KEY)

    suspend fun setToken(token: String) {
        this.token = token
        storage.putString(TOKEN_KEY, token)
    }

    suspend fun setRefreshToken(token: String) {
        this.refreshToken = token
        storage.putString(REFRESH_TOKEN_KEY, token)
    }

    suspend fun setDeviceToken(token: String) {
        this.deviceToken = token
        storage.putString(DEVICE_TOKEN_KEY, token)
    }

    suspend fun resetToken() {
        storage.remove(TOKEN_KEY)
        this.token = null
    }

    suspend fun resetRefreshToken() {
        storage.remove(REFRESH_TOKEN_KEY)
        this.refreshToken = null
    }

    suspend fun setUser(user: User) {
        try {
            val userString = json.encodeToString(user)
            storage.putString(USER_KEY, userString)
            _userFlow.value = user
        } catch (e: Exception) {
            println("UserSession setUser error: ${e.message}")
        }
    }

    suspend fun getUser(): User? {
        val userString = storage.getString(USER_KEY)
        val user = if (userString != null) {
            try {
                json.decodeFromString<User>(userString)
            } catch (e: Exception) {
                println("UserSession getUser error: ${e.message}")
                null
            }
        } else {
            null
        }
        _userFlow.value = user
        return user
    }

    suspend fun resetUser() {
        storage.remove(TOKEN_KEY)
        storage.remove(REFRESH_TOKEN_KEY)
        storage.remove(USER_KEY)
        storage.remove(PROFILE_IMAGE_KEY)
        _userFlow.value = null
        _profileImageFlow.value = null
        token = null
        refreshToken = null
    }

    suspend fun setProfileImage(base64: String?) {
        if (base64 == null) {
            storage.remove(PROFILE_IMAGE_KEY)
        } else {
            storage.putString(PROFILE_IMAGE_KEY, base64)
        }
        _profileImageFlow.value = base64
    }

    suspend fun getProfileImage(): String? {
        val img = storage.getString(PROFILE_IMAGE_KEY)
        _profileImageFlow.value = img
        return img
    }
}