package com.sspl

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.compose.setSingletonImageLoaderFactory
import com.sspl.session.UserSession
import com.sspl.theme.MyApplicationTheme
import com.sspl.ui.userdetails.ProfileViewModel
import org.koin.compose.KoinContext
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

/**
 * Created by M Imran
 * Senior Software Engineer at
 * BhimSoft on 01/01/2025.
 * se.muhammadimran@gmail.com
 */

@Composable
fun App() {
    KoinContext {
        setSingletonImageLoaderFactory { context ->
            getAsyncImageLoader(context)
        }
        MyApplicationTheme {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                MainScreenContent(it)
            }
        }
    }
}

@Composable
private fun MainScreenContent(
    paddingValues: PaddingValues,
    userSession: UserSession = koinInject()
) {
    val navController = rememberNavController()
    var currentScreen by remember {
        mutableStateOf<Screen>(Screen.SplashScreen)
    }
    LaunchedEffect(Unit) {
        currentScreen = userSession.token?.let {
            Screen.MainScreen
        } ?: Screen.SignInScreen
    }
    NavHost(
        navController = navController,
        startDestination = currentScreen.route,
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
    ) {
        composable(route = Screen.MainScreen.route) {
            val viewModel: ProfileViewModel = koinViewModel() //single instance till main screen graph
            MainScreen(paddingValues,viewModel, onParentGraph = {
                navController.navigate(Screen.SignInScreen.route) {
                    popUpTo(Screen.MainScreen.route) {
                        inclusive = true
                    }
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

fun getAsyncImageLoader(context: PlatformContext) =
    ImageLoader.Builder(context).build()