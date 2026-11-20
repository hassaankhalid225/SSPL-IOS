package com.sspl

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.compose.setSingletonImageLoaderFactory
import coil3.network.ktor3.KtorNetworkFetcherFactory
import coil3.request.crossfade
import coil3.util.DebugLogger
import coil3.disk.DiskCache
import coil3.memory.MemoryCache
import okio.FileSystem
import coil3.util.Logger
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import com.sspl.core.ApiStates
import com.sspl.core.models.Banner
import com.sspl.core.usecases.GetBannersUseCase
import com.sspl.designsystem.theme.AppTheme
import com.sspl.designsystem.theme.SsplTheme
import com.sspl.designsystem.theme.ThemeMode
import com.sspl.designsystem.theme.ThemePreferences
import com.sspl.theme.ProvidePlatformConfig
import com.sspl.ui.components.BannerDialog
import com.sspl.utils.BASE_URL
import org.koin.compose.KoinContext
import org.koin.compose.koinInject

/**
 * Root composable.
 *
 * Insets: the app draws edge-to-edge. Nothing here pads for system bars — each surface
 * (gradient top bar, bottom bar, auth screens) owns its own insets so there is never a
 * blank band above the header.
 */
@Composable
fun App() {
    KoinContext {
        setSingletonImageLoaderFactory { context -> getAsyncImageLoader(context) }

        val themePreferences: ThemePreferences = koinInject()
        val mode by themePreferences.mode.collectAsState()
        val dark = when (mode) {
            ThemeMode.SYSTEM -> isSystemInDarkTheme()
            ThemeMode.LIGHT -> false
            ThemeMode.DARK -> true
        }

        SsplTheme(darkTheme = dark) {
            ProvidePlatformConfig {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(AppTheme.colors.background)
                ) {
                    RootNavigation()
                }
            }
        }
    }
}

@Composable
private fun RootNavigation(
    getBannersUseCase: GetBannersUseCase = koinInject()
) {
    val navController = rememberNavController()

    // Welcome pop-up banner (type == WELCOME) — unchanged behaviour.
    val showBannerDialog = remember { mutableStateOf(false) }
    val banners = remember { mutableStateOf<List<Banner>>(emptyList()) }
    LaunchedEffect(Unit) {
        getBannersUseCase().collect { state ->
            if (state is ApiStates.Success) {
                val welcome = state.data?.banners
                    ?.filter { it.isActive }
                    ?.find { it.type == "WELCOME" }
                if (welcome != null) {
                    banners.value = listOf(welcome)
                    showBannerDialog.value = true
                }
            }
        }
    }
    if (showBannerDialog.value && banners.value.isNotEmpty()) {
        BannerDialog(
            banners = banners.value,
            baseUrl = BASE_URL,
            onDismiss = { showBannerDialog.value = false }
        )
    }

    NavHost(
        navController = navController,
        startDestination = Screen.SplashScreen.route,
        modifier = Modifier.fillMaxSize(),
        enterTransition = { fadeIn(tween(260)) },
        exitTransition = { fadeOut(tween(200)) },
        popEnterTransition = { fadeIn(tween(260)) },
        popExitTransition = { fadeOut(tween(200)) }
    ) {
        composable(route = Screen.MainScreen.route) {
            MainScreen(onParentGraph = {
                navController.navigate(Screen.SignInScreen.route) {
                    popUpTo(Screen.MainScreen.route) { inclusive = true }
                }
            })
        }
        kycScreens().forEach { navItem ->
            composable(route = navItem.screen.route) {
                navItem.component.invoke(navController)
            }
        }
    }
}

/**
 * Image loader. Uses its own Ktor client (no JSON content-type, no auth header, no body logging)
 * with timeouts + retry, because the network here drops connections and some images are > 1 MB.
 * Coil logs failures at WARN so a broken image always leaves a trace in logcat ("Coil").
 */
fun getAsyncImageLoader(context: PlatformContext) =
    ImageLoader.Builder(context)
        .components {
            add(KtorNetworkFetcherFactory(httpClient = { imageHttpClient() }))
        }
        // Persistent cache: images download once (the server is slow), then show instantly.
        .memoryCache { MemoryCache.Builder().maxSizePercent(context, 0.25).build() }
        .diskCache {
            DiskCache.Builder()
                .directory(FileSystem.SYSTEM_TEMPORARY_DIRECTORY / "sspl_image_cache")
                .maxSizeBytes(150L * 1024 * 1024)
                .build()
        }
        .logger(DebugLogger(Logger.Level.Warn))
        .crossfade(true)
        .build()

private fun imageHttpClient() = HttpClient {
    install(HttpTimeout) {
        connectTimeoutMillis = 15_000
        socketTimeoutMillis = 45_000
        requestTimeoutMillis = 120_000
    }
    install(HttpRequestRetry) {
        maxRetries = 3
        retryOnExceptionOrServerErrors(maxRetries = 3)
        exponentialDelay()
    }
    defaultRequest { header(HttpHeaders.UserAgent, "SSPL Application") }
}
