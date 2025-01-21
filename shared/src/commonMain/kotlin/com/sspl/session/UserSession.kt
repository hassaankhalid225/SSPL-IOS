package com.sspl.session

import com.sspl.storage.Storage
import kotlinx.coroutines.runBlocking

/**
 * Created by M Imran
 * Senior Software Engineer at
 * BhimSoft on 12/12/2024.
 * se.muhammadimran@gmail.com
 */
class UserSession(private val storage: Storage) {
    companion object {
        private const val TOKEN_KEY = "user_token_key"
    }

    var token: String? = null
        private set
        get() {
            if (field == null) {
                runBlocking { field = getToken() }
            }
            return field
        }

    private suspend fun getToken(): String? = storage.getString(TOKEN_KEY)
    suspend fun setToken(token: String) {
        println(">>>Token: Saving $token")
        this.token = token
        storage.putString(TOKEN_KEY, token)
    }
}