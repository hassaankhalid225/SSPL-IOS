package com.sspl

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.sspl.theme.windowBackGround
import com.sspl.ui.components.AppBottomBar
import com.sspl.ui.components.MyTopAppBar

/**
 * Created by M Imran
 * Senior Software Engineer at
 * BhimSoft on 01/01/2025.
 * se.muhammadimran@gmail.com
 */

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()

    val currentScreen = screenByRoute(backStackEntry?.destination?.route) ?: Screen.HomeScreen
    val navItems = navItems()
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = windowBackGround,
        topBar = {
            MyTopAppBar(
                currentScreen = currentScreen,
                canNavigateBack = navController.previousBackStackEntry != null,
                navigateUp = { navController.navigateUp() },
            )
        },
        bottomBar = {
            AppBottomBar(
                bottomNavItems = navItems,
                navController = navController
            )
        }
    ) {
        NavHost(
            navController = navController,
            startDestination = Screen.HomeScreen.route,
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
        ) {
            (otherScreens() + navItems).forEach { navItem ->
                composable(route = navItem.screen.route) {
                    navItem.component.invoke(navController)
                }
            }

        }
    }
}