package com.sspl.storage

import com.russhwolf.settings.Settings
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


/**
 * Created by M Imran
 * Senior Software Engineer at
 * BhimSoft on 11/01/2025.
 * se.muhammadimran@gmail.com
 */
class LocalStorage {
    private val defaultDispatcher: CoroutineDispatcher = Dispatchers.Default
    private val setting = Settings()

    suspend fun saveString(key: String, value: String): Boolean =
        withContext(defaultDispatcher) { setting.putString(key, value); true }

    suspend fun getString(key: String, defaultValue: String): String =
        withContext(defaultDispatcher) { setting.getString(key, defaultValue) }

    suspend fun getString(key: String): String? =
        withContext(defaultDispatcher) { setting.getStringOrNull(key) }

    suspend fun saveLong(key: String, value: Long): Boolean =
        withContext(defaultDispatcher) { setting.putLong(key, value); true }

    suspend fun getLong(key: String, defaultValue: Long = 0): Long =
        withContext(defaultDispatcher) { setting.getLong(key, defaultValue) }
}