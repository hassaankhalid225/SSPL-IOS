package com.sspl


import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import com.sspl.ui.ForumScreen
import com.sspl.ui.HomeScreen
import com.sspl.ui.ProfileScreen
import com.sspl.utils.IconResource
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.vectorResource
import sspl.shared.generated.resources.Res
import sspl.shared.generated.resources.home
import sspl.shared.generated.resources.profile
import sspl.shared.generated.resources.forum
import sspl.shared.generated.resources.ic_forum

/**
 * Created by M Imran
 * Senior Software Engineer at
 * BhimSoft on 02/01/2025.
 * se.muhammadimran@gmail.com
 */
data class NavItem(
    val screen: Screen,
    val component: @Composable (navController: NavHostController) -> Unit
)

sealed class Screen(
    val route: String,
    val title: StringResource? = null,
    val icon: IconResource? = null
) {
    data object None : Screen(
        route = "none",
    )

    data object HomeScreen : Screen(
        title = Res.string.home,
        route = "HomeScreen",
        icon = IconResource.fromImageVector(imageVector = Icons.Default.Home)
    )

    data object ProfileScreen : Screen(
        title = Res.string.profile,
        route = "ProfileScreen",
        icon = IconResource.fromImageVector(imageVector = Icons.Default.Person)
    )

    data object ForumScreen : Screen(
        title = Res.string.forum,
        route = "ForumScreen",
        icon = IconResource.fromDrawableResource(drawable = Res.drawable.ic_forum)
    )
}

fun screenByRoute(route: String?): Screen? =
    (navItems() + otherScreens()).firstOrNull { it.screen.route == route }?.screen

fun navItems(): List<NavItem> = listOf(
    NavItem(
        screen = Screen.HomeScreen,
        component = { HomeScreen(navController = it) }
    ),
    NavItem(
        screen = Screen.ForumScreen,
        component = { ForumScreen(navController = it) }
    )
)

fun otherScreens(): List<NavItem> = listOf(
    NavItem(
        screen = Screen.ProfileScreen,
        component = { ProfileScreen(navController = it) }
    ),

    )
