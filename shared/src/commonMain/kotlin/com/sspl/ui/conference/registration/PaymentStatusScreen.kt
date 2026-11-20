package com.sspl.ui.conference.registration

import com.sspl.designsystem.icons.SsplIcons

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.sspl.Screen
import com.sspl.designsystem.components.FactRow
import com.sspl.designsystem.components.LoadingDots
import com.sspl.designsystem.components.OutlineButton
import com.sspl.designsystem.components.PrimaryButton
import com.sspl.designsystem.components.SecondaryButton
import com.sspl.designsystem.components.SsplCard
import com.sspl.designsystem.components.StatusBadge
import com.sspl.designsystem.theme.AppTheme
import com.sspl.utils.DisplayDates
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

/**
 * C9: Payment status. Inputs (previous entry's savedStateHandle, all small primitives):
 *  - `customerTransactionId` → verify via POST /swich/verify-status
 *  - `awaitingTransactionId` → payment page was just opened; user checks status on return
 *  - `status` + `message`    → explicit result
 *  - `registrationId`        → enables "Try again" (POST /external/events/registrations/{id}/pay)
 */
@Composable
fun PaymentStatusScreen(
    navController: NavController,
    viewModel: PaymentStatusViewModel = koinViewModel()
) {
    var inAppPaymentUrl by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf<String?>(null) }
    val handle = remember { navController.previousBackStackEntry?.savedStateHandle }
    val customerTransactionId = remember { handle?.get<String>("customerTransactionId") }
    val awaitingTransactionId = remember { handle?.get<String>("awaitingTransactionId") }
    val explicitStatus = remember { handle?.get<Any>("status")?.let { if (it == true) "success" else it as? String } }
    val explicitMessage = remember { handle?.get<String>("message") }
    val registrationId = remember { handle?.get<Long>("registrationId") ?: 0L }

    LaunchedEffect(Unit) {
        when {
            !customerTransactionId.isNullOrBlank() -> viewModel.verifyTransaction(customerTransactionId)
            !explicitStatus.isNullOrBlank() -> viewModel.setExplicitStatus(
                status = explicitStatus,
                message = explicitMessage ?: if (explicitStatus == "success") "Payment completed successfully." else "Transaction could not be completed."
            )
            else -> viewModel.setAwaiting(awaitingTransactionId)
        }
    }

    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val colors = AppTheme.colors

    inAppPaymentUrl?.let { url ->
        val done: () -> Unit = {
            inAppPaymentUrl = null
            viewModel.checkAwaiting()
        }
        com.sspl.ui.components.PaymentWebView(url = url, onPaymentSuccess = done, onPaymentFailed = done, onClose = done)
    }
    val today = remember { DisplayDates.medium(DisplayDates.today().toString()) }

    fun goRegistrations() = navController.navigate(Screen.MyRegistrationsScreen.route) {
        popUpTo(Screen.HomeScreen.route)
        launchSingleTop = true
    }
    fun goHome() = navController.navigate(Screen.HomeScreen.route) {
        popUpTo(Screen.HomeScreen.route) { inclusive = true }
    }

    AnimatedContent(
        targetState = state.status,
        transitionSpec = { fadeIn(tween(240)) togetherWith fadeOut(tween(160)) },
        label = "paymentStatus",
        modifier = Modifier.fillMaxSize()
    ) { status ->
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val (icon, tint, bg, title) = when (status) {
                "success" -> Quad(SsplIcons.Check, colors.success, colors.successContainer, "Payment successful")
                "pending" -> Quad(SsplIcons.HourglassTop, colors.warning, colors.warningContainer, "Payment pending")
                "awaiting" -> Quad(SsplIcons.CreditCard, colors.primary, colors.primaryContainer, "Finish your payment")
                "processing" -> Quad(SsplIcons.Receipt, colors.primary, colors.primaryContainer, "Checking your payment")
                else -> Quad(SsplIcons.Close, colors.error, colors.errorContainer, "Payment failed")
            }
            StatusMedallion(icon, tint, bg, busy = status == "processing")
            Spacer(Modifier.height(18.dp))
            Text(title, style = AppTheme.typography.displaySmall, textAlign = TextAlign.Center)
            Spacer(Modifier.height(8.dp))
            Text(state.message, style = AppTheme.typography.bodyMedium, textAlign = TextAlign.Center)
            Spacer(Modifier.height(22.dp))

            if (status != "processing") {
                SsplCard {
                    Text("RECEIPT", style = AppTheme.typography.overline)
                    Spacer(Modifier.height(10.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        FactRow(SsplIcons.Payments, "Status", when (status) {
                            "success" -> "Paid"
                            "pending" -> "Pending verification"
                            "awaiting" -> "Awaiting payment"
                            else -> "Not completed"
                        })
                        state.transactionId?.takeIf { it.isNotBlank() }?.let {
                            FactRow(SsplIcons.ConfirmationNumber, "Transaction ID", it)
                        }
                        if (registrationId > 0) FactRow(SsplIcons.Event, "Registration", "#$registrationId")
                        FactRow(SsplIcons.Receipt, "Date", today)
                    }
                    Spacer(Modifier.height(10.dp))
                    StatusBadge(
                        when (status) {
                            "success" -> "Paid"
                            "failed" -> "Failed"
                            else -> "Pending"
                        }
                    )
                }
                Spacer(Modifier.height(22.dp))
            }

            when (status) {
                "success" -> {
                    PrimaryButton("View my registrations", ::goRegistrations)
                    Spacer(Modifier.height(10.dp))
                    SecondaryButton("Go to home", ::goHome)
                }
                "pending" -> {
                    if (!state.transactionId.isNullOrBlank()) {
                        PrimaryButton("Check again", viewModel::checkAwaiting, icon = SsplIcons.Refresh, loading = state.isLoading)
                        Spacer(Modifier.height(10.dp))
                    }
                    SecondaryButton("View my registrations", ::goRegistrations)
                }
                "awaiting" -> {
                    if (!state.transactionId.isNullOrBlank()) {
                        PrimaryButton("I've paid — check status", viewModel::checkAwaiting, loading = state.isLoading)
                        Spacer(Modifier.height(10.dp))
                    }
                    if (registrationId > 0) {
                        OutlineButton(
                            "Reopen payment page",
                            onClick = { viewModel.retryPayment(registrationId) { url -> inAppPaymentUrl = url } },
                            loading = state.isLoading,
                            icon = SsplIcons.OpenInNew
                        )
                        Spacer(Modifier.height(10.dp))
                    }
                    SecondaryButton("View my registrations", ::goRegistrations)
                }
                "processing" -> Unit
                else -> {
                    if (registrationId > 0) {
                        PrimaryButton(
                            "Try again",
                            onClick = { viewModel.retryPayment(registrationId) { url -> inAppPaymentUrl = url } },
                            loading = state.isLoading,
                            icon = SsplIcons.Refresh
                        )
                        Spacer(Modifier.height(10.dp))
                    }
                    SecondaryButton("Back to events", onClick = {
                        navController.navigate(Screen.ScientificProgramsScreen.route) {
                            popUpTo(Screen.HomeScreen.route)
                            launchSingleTop = true
                        }
                    })
                }
            }
            if (status != "processing") {
                Spacer(Modifier.height(16.dp))
                Text(
                    "Questions about a payment? Contact the SSPL secretariat with your transaction ID.",
                    style = AppTheme.typography.bodySmall,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

private data class Quad(val icon: ImageVector, val tint: Color, val bg: Color, val title: String)

@Composable
private fun StatusMedallion(icon: ImageVector, tint: Color, bg: Color, busy: Boolean) {
    Box(
        Modifier.size(112.dp).clip(CircleShape).background(bg.copy(alpha = 0.6f)),
        contentAlignment = Alignment.Center
    ) {
        Box(
            Modifier.size(80.dp).clip(CircleShape).background(bg),
            contentAlignment = Alignment.Center
        ) {
            if (busy) LoadingDots(color = tint, dotSize = 9.dp)
            else Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(42.dp))
        }
    }
}
