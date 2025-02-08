package com.sspl.ui.kyc

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
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
import com.sspl.resources.reset_password
import com.sspl.resources.enter_email_to_reset_password
import com.sspl.resources.enter_your_mail
import com.sspl.resources.login_screen_background
import com.sspl.ui.components.AppTextField
import com.sspl.utils.IconResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/**
 * Created by M Imran
 * Senior Software Engineer at
 * BhimSoft on 08/02/2025.
 * se.muhammadimran@gmail.com
 */
@Composable
fun ForgotPasswordScreen(navController: NavController) {
    ForgotPasswordContent(onBackPressed = {
        println(">>> onBackPressed")
        navController.popBackStack()
    })
}

@Composable
private fun ForgotPasswordContent(onBackPressed: () -> Unit) {
    Box(Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(Res.drawable.login_screen_background),
            contentDescription = null,
            contentScale = ContentScale.FillBounds
        )
        Column(
            modifier = Modifier
                .fillMaxSize().verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Title Section
            Spacer(Modifier.weight(0.15f))
            HeaderSection(
                iconResource = IconResource.fromDrawableResource(drawable = Res.drawable.app_logo),
                title = stringResource(Res.string.reset_password),
                subTitle = stringResource(Res.string.enter_email_to_reset_password)
            )
            Spacer(Modifier.weight(0.15f))
            AnimatedVisibility(visible = true) {
                ForgetPasswordFiled(
                    email = "",
                    emailErrorMsg = null,
                    onEmailChange = {}
                )
            }
            Spacer(Modifier.weight(1f))
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.15f)
                .background(
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
        ) {
            IconButton(
                modifier = Modifier.padding(top = 8.dp),
                onClick = onBackPressed,
                colors = IconButtonDefaults.iconButtonColors()
                    .copy(contentColor = MaterialTheme.colorScheme.onPrimaryContainer)
            ) {
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "")
            }
        }
    }
}

@Composable
private fun ForgetPasswordFiled(
    email: String,
    emailErrorMsg: String?,
    onEmailChange: (String) -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        AppTextField(
            placeholder = stringResource(Res.string.enter_your_mail),
            modifier = Modifier.fillMaxWidth(),
            text = email,
            errorMessage = emailErrorMsg,
            isError = emailErrorMsg != null,
            onTextChange = onEmailChange
        )
    }
}