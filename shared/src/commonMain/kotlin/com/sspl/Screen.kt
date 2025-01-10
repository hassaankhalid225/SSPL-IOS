package com.sspl


import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import com.sspl.resources.Res
import com.sspl.resources.conference
import com.sspl.resources.conference_detail
import com.sspl.resources.forum
import com.sspl.resources.home
import com.sspl.resources.ic_forum
import com.sspl.resources.log_in
import com.sspl.resources.profile
import com.sspl.resources.sign_up
import com.sspl.ui.conferencescreen.ConferenceDetailScreen
import com.sspl.ui.ForumScreen
import com.sspl.ui.HomeScreen
import com.sspl.ui.ProfileScreen
import com.sspl.ui.conferencescreen.ConferenceScreen
import com.sspl.ui.postscreen.PostScreen
import com.sspl.utils.IconResource
import org.jetbrains.compose.resources.StringResource


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

    data object ConferenceScreen : Screen(
        title = Res.string.conference,
        route = "ConferenceDetailScreen",
    )

    data object ConferenceDetailScreen : Screen(
        title = Res.string.conference_detail,
        route = "ConferenceDetailScreen",
    )

    data object SignInScreen : Screen(
        title = Res.string.log_in,
        route = "SignInScreen",
    )

    data object SignUpScreen : Screen(
        title = Res.string.sign_up,
        route = "SignUpScreen",
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
        component = { PostScreen(/*navController = it*/) }
    )
)

fun otherScreens(): List<NavItem> = listOf(
    NavItem(
        screen = Screen.ProfileScreen,
        component = { ProfileScreen(navController = it) }
    ),
    NavItem(
        screen = Screen.ConferenceScreen,
        component = { ConferenceScreen(navController = it) }
    ),
    NavItem(
        screen = Screen.ConferenceDetailScreen,
        component = { ConferenceDetailScreen(navController = it) }
    ),

    )
