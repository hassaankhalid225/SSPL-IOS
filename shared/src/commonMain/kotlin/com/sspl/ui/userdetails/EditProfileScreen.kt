package com.sspl.ui.userdetails

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.sspl.resources.Res
import com.sspl.resources.app_logo
import com.sspl.resources.edit_profile
import com.sspl.resources.login_screen_background
import com.sspl.resources.update_info
import com.sspl.ui.kyc.HeaderSection
import com.sspl.utils.IconResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun EditProfileScreen(navController: NavController) {

    val isPersonalDetails =
        navController.previousBackStackEntry?.savedStateHandle?.get<Boolean>("isPersonalDetails")

    Box(Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(Res.drawable.login_screen_background),
            contentDescription = null,
            contentScale = ContentScale.FillBounds
        )

        Box(
            modifier = Modifier.fillMaxWidth().fillMaxHeight(0.15f).background(
                brush = Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.primary.copy(alpha = .8F),
                        MaterialTheme.colorScheme.primary.copy(alpha = .5F),
                        MaterialTheme.colorScheme.primary.copy(alpha = .1F),
                        Color.Transparent
                    )
                )
            )
        )

        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.weight(0.15f))
            HeaderSection(
                iconResource = IconResource.fromDrawableResource(drawable = Res.drawable.app_logo),
                title = stringResource(Res.string.edit_profile),
                subTitle = stringResource(Res.string.update_info)
            )

            if (isPersonalDetails == true) {
                EditPersonalDetails()
            } else {
                EditProfessionalDetails()
            }
        }
    }
}


@Composable
fun EditPersonalDetails(modifier: Modifier = Modifier) {

}

@Composable
fun EditProfessionalDetails(modifier: Modifier = Modifier) {

}