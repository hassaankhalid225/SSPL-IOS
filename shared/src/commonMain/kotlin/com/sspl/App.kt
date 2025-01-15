package com.sspl

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.compose.setSingletonImageLoaderFactory
import com.sspl.theme.MyApplicationTheme
import com.sspl.ui.HomeScreen
import com.sspl.ui.kyc.LoginScreen
import com.sspl.ui.kyc.SignUpScreen
import com.sspl.ui.otp.OtpScreen
import org.koin.compose.KoinContext

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
private fun MainScreenContent(paddingValues: PaddingValues) {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = Screen.SignInScreen.route,
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
    ) {
        composable(route = Screen.MainScreen.route) {
            MainScreen(paddingValues)
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