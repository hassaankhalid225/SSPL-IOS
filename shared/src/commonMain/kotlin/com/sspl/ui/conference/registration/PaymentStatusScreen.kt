package com.sspl.ui.conference.registration

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.sspl.Screen
import com.sspl.designsystem.components.FeedbackBanner
import com.sspl.designsystem.components.SsplCard
import com.sspl.designsystem.components.SsplPrimaryButton
import com.sspl.designsystem.components.SsplSecondaryButton
import com.sspl.designsystem.tokens.SsplColorTokens
import com.sspl.designsystem.tokens.SsplSpacingTokens
import com.sspl.designsystem.tokens.SsplTypographyTokens
import com.sspl.platform.shared.UrlOpener
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun PaymentStatusScreen(
    navController: NavController,
    viewModel: PaymentStatusViewModel = koinViewModel(),
    urlOpener: UrlOpener = koinInject()
) {
    val previousHandle = navController.previousBackStackEntry?.savedStateHandle
    val customerTransactionId = previousHandle?.get<String>("customerTransactionId")
    val explicitStatus = previousHandle?.get<String>("status")
    val explicitMessage = previousHandle?.get<String>("message")
    val registrationId = previousHandle?.get<Long>("registrationId") ?: 0L

    LaunchedEffect(customerTransactionId, explicitStatus) {
        if (!customerTransactionId.isNullOrBlank()) {
            viewModel.verifyTransaction(customerTransactionId)
        } else if (!explicitStatus.isNullOrBlank()) {
            viewModel.setExplicitStatus(
                status = explicitStatus,
                message = explicitMessage ?: if (explicitStatus == "success") "Payment completed successfully." else "Transaction could not be completed."
            )
        }
    }

    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SsplColorTokens.background)
            .padding(SsplSpacingTokens.lg),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        when (uiState.status) {
            "processing" -> {
                CircularProgressIndicator(
                    modifier = Modifier.size(64.dp),
                    color = SsplColorTokens.primary,
                    strokeWidth = 4.dp
                )
                Spacer(modifier = Modifier.height(SsplSpacingTokens.lg))
                Text(
                    text = "Processing Payment...",
                    style = SsplTypographyTokens.headlineMedium,
                    color = SsplColorTokens.textPrimary
                )
                Spacer(modifier = Modifier.height(SsplSpacingTokens.sm))
                Text(
                    text = uiState.message,
                    style = SsplTypographyTokens.bodyMedium.copy(color = SsplColorTokens.textSecondary),
                    textAlign = TextAlign.Center
                )
            }

            "success" -> {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(SsplColorTokens.success.copy(alpha = 0.12f))
                        .border(3.dp, SsplColorTokens.success, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Success",
                        tint = SsplColorTokens.success,
                        modifier = Modifier.size(52.dp)
                    )
                }

                Spacer(modifier = Modifier.height(SsplSpacingTokens.lg))

                Text(
                    text = "Payment Successful!",
                    style = SsplTypographyTokens.headlineLarge.copy(fontSize = 24.sp),
                    color = SsplColorTokens.textPrimary
                )

                Spacer(modifier = Modifier.height(SsplSpacingTokens.sm))

                Text(
                    text = uiState.message,
                    style = SsplTypographyTokens.bodyMedium.copy(color = SsplColorTokens.textSecondary),
                    textAlign = TextAlign.Center
                )

                if (!customerTransactionId.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(SsplSpacingTokens.sm))
                    Text(
                        text = "Transaction ID: $customerTransactionId",
                        style = SsplTypographyTokens.caption.copy(
                            fontWeight = FontWeight.Bold,
                            color = SsplColorTokens.muted
                        )
                    )
                }

                Spacer(modifier = Modifier.height(SsplSpacingTokens.xl))

                SsplPrimaryButton(
                    text = "View My Registrations",
                    onClick = {
                        navController.navigate(Screen.MyRegistrationsScreen.route) {
                            popUpTo(Screen.HomeScreen.route)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(SsplSpacingTokens.sm))

                SsplSecondaryButton(
                    text = "Go to Home",
                    onClick = {
                        navController.navigate(Screen.HomeScreen.route) {
                            popUpTo(Screen.HomeScreen.route) { inclusive = true }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            "pending" -> {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(SsplColorTokens.secondary.copy(alpha = 0.15f))
                        .border(3.dp, SsplColorTokens.secondary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.HourglassTop,
                        contentDescription = "Pending",
                        tint = SsplColorTokens.secondary,
                        modifier = Modifier.size(48.dp)
                    )
                }

                Spacer(modifier = Modifier.height(SsplSpacingTokens.lg))

                Text(
                    text = "Payment Pending",
                    style = SsplTypographyTokens.headlineLarge.copy(fontSize = 24.sp),
                    color = SsplColorTokens.textPrimary
                )

                Spacer(modifier = Modifier.height(SsplSpacingTokens.sm))

                Text(
                    text = uiState.message,
                    style = SsplTypographyTokens.bodyMedium.copy(color = SsplColorTokens.textSecondary),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(SsplSpacingTokens.xl))

                SsplPrimaryButton(
                    text = "View Registrations",
                    onClick = {
                        navController.navigate(Screen.MyRegistrationsScreen.route) {
                            popUpTo(Screen.HomeScreen.route)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(SsplSpacingTokens.sm))

                SsplSecondaryButton(
                    text = "Go to Home",
                    onClick = {
                        navController.navigate(Screen.HomeScreen.route) {
                            popUpTo(Screen.HomeScreen.route) { inclusive = true }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            else -> {
                // "failed"
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(SsplColorTokens.error.copy(alpha = 0.12f))
                        .border(3.dp, SsplColorTokens.error, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Failed",
                        tint = SsplColorTokens.error,
                        modifier = Modifier.size(52.dp)
                    )
                }

                Spacer(modifier = Modifier.height(SsplSpacingTokens.lg))

                Text(
                    text = "Payment Failed",
                    style = SsplTypographyTokens.headlineLarge.copy(fontSize = 24.sp),
                    color = SsplColorTokens.textPrimary
                )

                Spacer(modifier = Modifier.height(SsplSpacingTokens.sm))

                FeedbackBanner(
                    message = uiState.message,
                    isError = true
                )

                Spacer(modifier = Modifier.height(SsplSpacingTokens.xl))

                if (registrationId > 0) {
                    SsplPrimaryButton(
                        text = if (uiState.isLoading) "Re-initiating..." else "Try Again",
                        onClick = {
                            viewModel.retryPayment(registrationId) { url ->
                                urlOpener.openUrl(url)
                            }
                        },
                        enabled = !uiState.isLoading,
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                        }
                    )
                    Spacer(modifier = Modifier.height(SsplSpacingTokens.sm))
                }

                SsplSecondaryButton(
                    text = "Back to Events",
                    onClick = {
                        navController.navigate(Screen.ScientificProgramsScreen.route) {
                            popUpTo(Screen.HomeScreen.route)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(18.dp))
                    }
                )
            }
        }
    }
}
