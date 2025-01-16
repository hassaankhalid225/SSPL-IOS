package com.sspl

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.PaddingValues
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
fun MainScreen(paddingValues: PaddingValues) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()

    val currentScreen = screenByRoute(backStackEntry?.destination?.route) ?: Screen.HomeScreen
    val navItems = navItems()
    val navBackStackEntry by navController.currentBackStackEntryAsState()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = windowBackGround,
        topBar = {
            MyTopAppBar(
                currentScreen = currentScreen,
                onHamburgClicked = { navController.navigate(route = Screen.SignInScreen.route) },
                canNavigateBack = navController.previousBackStackEntry != null,
                navigateUp = { navController.navigateUp() },
            )
        },
        bottomBar = {
            AnimatedVisibility(
                navItems.any { it.screen.route == navBackStackEntry?.destination?.route },
                enter = slideInVertically(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioLowBouncy,
                        stiffness = Spring.StiffnessLow
                    ),
                    initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(
                    animationSpec = tween(durationMillis = 300, easing = LinearEasing),
                    targetOffsetY = { it }) + fadeOut()
            ) {
                AppBottomBar(
                    bottomNavItems = navItems,
                    navController = navController
                )
            }
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