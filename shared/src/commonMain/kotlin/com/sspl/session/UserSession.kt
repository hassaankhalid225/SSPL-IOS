package com.sspl.session

import com.sspl.storage.LocalStorage

/**
 * Created by M Imran
 * Senior Software Engineer at
 * BhimSoft on 12/12/2024.
 * se.muhammadimran@gmail.com
 */
class UserSession(private val localStorage: LocalStorage) {
    companion object {
        private const val TOKEN_KEY = "user_token_key"
    }

    suspend fun getToken(): String? = localStorage.getString(TOKEN_KEY)
}