package com.sspl.designsystem.theme

import androidx.compose.runtime.Composable
import com.sspl.storage.Storage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

enum class ThemeMode(val label: String) {
    SYSTEM("System"), LIGHT("Light"), DARK("Dark")
}

/**
 * Persists the user's light / dark / system choice.
 * Read synchronously once at start so the first frame is already in the right theme (no flash).
 */
class ThemePreferences(private val storage: Storage) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _mode = MutableStateFlow(
        runBlocking {
            runCatching { storage.getString(KEY) }.getOrNull()
                ?.let { saved -> ThemeMode.entries.firstOrNull { it.name == saved } }
                ?: ThemeMode.SYSTEM
        }
    )
    val mode: StateFlow<ThemeMode> = _mode.asStateFlow()

    fun setMode(mode: ThemeMode) {
        _mode.value = mode
        scope.launch { runCatching { storage.putString(KEY, mode.name) } }
    }

    private companion object {
        const val KEY = "sspl_theme_mode"
    }
}

/**
 * Platform hook: set status/navigation bar icon colours.
 * @param lightStatusIcons true when the area behind the status bar is dark (gradient headers).
 * @param darkTheme current app theme (drives navigation-bar icon colour).
 */
@Composable
expect fun SystemBarsAppearance(lightStatusIcons: Boolean, darkTheme: Boolean)
