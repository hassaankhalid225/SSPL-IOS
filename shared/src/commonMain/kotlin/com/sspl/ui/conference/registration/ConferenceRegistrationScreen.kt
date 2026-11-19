package com.sspl.ui.conference.registration

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.sspl.Screen
import com.sspl.core.models.Conference
import com.sspl.designsystem.components.BadgeVariant
import com.sspl.designsystem.components.FeedbackBanner
import com.sspl.designsystem.components.SsplBadge
import com.sspl.designsystem.components.SsplCard
import com.sspl.designsystem.components.SsplPrimaryButton
import com.sspl.designsystem.components.SsplSecondaryButton
import com.sspl.designsystem.tokens.SsplColorTokens
import com.sspl.designsystem.tokens.SsplSpacingTokens
import com.sspl.designsystem.tokens.SsplTypographyTokens
import kotlinx.serialization.json.Json
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ConferenceRegistrationScreen(
    navController: NavController,
    viewModel: ConferenceRegistrationViewModel = koinViewModel()
) {
    val dataString = navController.previousBackStackEntry?.savedStateHandle?.get<String>("data")
    val conferenceArg = remember(dataString) {
        dataString?.let {
            try { Json.decodeFromString<Conference>(it) } catch (e: Exception) { null }
        }
    }

    LaunchedEffect(conferenceArg) {
        conferenceArg?.let { viewModel.loadData(it.id) }
    }

    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SsplColorTokens.background)
    ) {
        // App bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(SsplColorTokens.surface)
                .padding(horizontal = SsplSpacingTokens.sm, vertical = SsplSpacingTokens.xs),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = SsplColorTokens.textPrimary
                )
            }
            Text(
                text = "Conference Registration",
                style = SsplTypographyTokens.headlineSmall.copy(fontSize = 18.sp),
                color = SsplColorTokens.textPrimary
            )
        }

        when (val state = uiState) {
            is RegistrationUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = SsplColorTokens.primary)
                }
            }
            is RegistrationUiState.Error -> {
                Box(modifier = Modifier.fillMaxSize().padding(SsplSpacingTokens.lg), contentAlignment = Alignment.Center) {
                    FeedbackBanner(message = state.message, isError = true)
                }
            }
            is RegistrationUiState.Content -> {
                ConferenceRegistrationContent(
                    state = state,
                    viewModel = viewModel,
                    navController = navController,
                    initialTitle = conferenceArg?.title
                )
            }
        }
    }
}

@Composable
fun ConferenceRegistrationContent(
    state: RegistrationUiState.Content,
    viewModel: ConferenceRegistrationViewModel,
    navController: NavController,
    initialTitle: String?
) {
    val roles = state.roles
    
    var selectedRoleIndex by remember(state.isRegistered, state.registration) { 
        val registeredRoleId = state.registration?.roleId
        val index = if (registeredRoleId != null) {
            roles.indexOfFirst { it.id == registeredRoleId }.takeIf { it >= 0 } ?: 0
        } else 0
        mutableStateOf(index)
    }

    // Payment WebView Dialog
    if (state.paymentUrl != null) {
        androidx.compose.ui.window.Dialog(
            onDismissRequest = { viewModel.onPaymentUrlOpened() },
            properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
        ) {
             com.sspl.ui.components.PaymentWebView(
                 url = state.paymentUrl,
                 onPaymentSuccess = {
                     viewModel.onPaymentUrlOpened()
                     viewModel.loadData(state.details.id) // Refresh status
                 },
                 onPaymentFailed = {
                     viewModel.onPaymentUrlOpened()
                 },
                 onClose = { viewModel.onPaymentUrlOpened() },
                 modifier = Modifier.fillMaxSize()
             )
        }
    }

    // If payment succeeds (status updated after refresh), navigate
    LaunchedEffect(state.registration?.paymentStatus) {
        if (state.registration?.paymentStatus == "SUCCESS") {
            navController.currentBackStackEntry?.savedStateHandle?.set("status", true)
            navController.navigate(Screen.PaymentStatusScreen.route)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(SsplSpacingTokens.md),
        verticalArrangement = Arrangement.spacedBy(SsplSpacingTokens.md)
    ) {
        // Conference Name
        SsplCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(SsplSpacingTokens.md)) {
                SsplBadge(text = "CONFERENCE", variant = BadgeVariant.PRIMARY)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = initialTitle ?: state.details.title ?: "Annual Conference",
                    style = SsplTypographyTokens.headlineSmall.copy(fontSize = 18.sp),
                    color = SsplColorTokens.textPrimary
                )
                if (!state.details.summary.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = state.details.summary,
                        style = SsplTypographyTokens.bodySmall.copy(color = SsplColorTokens.textSecondary),
                        maxLines = 2
                    )
                }
            }
        }

        // Registration status banner if registered
        if (state.isRegistered && state.registration != null) {
            val pStatus = state.registration.paymentStatus
            val isPaid = pStatus == "SUCCESS"
            SsplCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = if (isPaid) SsplColorTokens.success.copy(alpha = 0.08f) else SsplColorTokens.secondary.copy(alpha = 0.1f)
            ) {
                Row(
                    modifier = Modifier.padding(SsplSpacingTokens.md),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(SsplSpacingTokens.sm)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = if (isPaid) SsplColorTokens.success else SsplColorTokens.secondary,
                        modifier = Modifier.size(32.dp)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isPaid) "Registration Confirmed" else "Registration Submitted",
                            style = SsplTypographyTokens.bodyLarge.copy(fontWeight = FontWeight.Bold),
                            color = if (isPaid) SsplColorTokens.success else SsplColorTokens.textPrimary
                        )
                        Text(
                            text = "Payment Status: $pStatus",
                            style = SsplTypographyTokens.caption.copy(color = SsplColorTokens.textSecondary)
                        )
                    }
                }
            }
        }

        if (roles.isNotEmpty()) {
            Text(
                text = "Select Registration Role",
                style = SsplTypographyTokens.headlineSmall.copy(fontSize = 16.sp),
                color = SsplColorTokens.textPrimary
            )

            roles.forEachIndexed { index, role ->
                val isSelected = selectedRoleIndex == index
                val borderColor = if (isSelected) SsplColorTokens.primary else SsplColorTokens.divider
                val bgColor = if (isSelected) SsplColorTokens.primary.copy(alpha = 0.05f) else SsplColorTokens.surface

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(bgColor)
                        .border(1.5.dp, borderColor, RoundedCornerShape(12.dp))
                        .clickable(enabled = !state.isRegistered) { selectedRoleIndex = index }
                        .padding(SsplSpacingTokens.md)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(SsplSpacingTokens.sm)
                    ) {
                        // Radio circle
                        Box(
                            modifier = Modifier
                                .size(22.dp)
                                .clip(CircleShape)
                                .border(2.dp, if (isSelected) SsplColorTokens.primary else SsplColorTokens.muted, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .clip(CircleShape)
                                        .background(SsplColorTokens.primary)
                                )
                            }
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = role.name,
                                style = SsplTypographyTokens.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = SsplColorTokens.textPrimary
                            )
                            if (!role.description.isNullOrBlank()) {
                                Text(
                                    text = role.description,
                                    style = SsplTypographyTokens.caption.copy(color = SsplColorTokens.textSecondary)
                                )
                            }
                        }

                        Text(
                            text = "PKR ${role.fee.toInt()}",
                            style = SsplTypographyTokens.bodyLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = SsplColorTokens.primary
                            )
                        )
                    }
                }
            }
        }
        
        state.error?.let {
            FeedbackBanner(message = it, isError = true)
        }

        Spacer(modifier = Modifier.height(SsplSpacingTokens.sm))

        // Action Buttons
        val isRegistered = state.isRegistered
        val paymentStatus = state.registration?.paymentStatus

        when {
            !isRegistered -> {
                SsplPrimaryButton(
                    text = if (state.isLoading) "Registering..." else "Register for Conference",
                    onClick = {
                        roles.getOrNull(selectedRoleIndex)?.let { role ->
                            viewModel.register(role.id)
                        }
                    },
                    enabled = !state.isLoading && roles.isNotEmpty(),
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = {
                        if (state.isLoading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(imageVector = Icons.Default.CreditCard, contentDescription = null, modifier = Modifier.size(18.dp))
                        }
                    }
                )
            }

            paymentStatus == "PENDING" || paymentStatus == "FAILED" -> {
                SsplPrimaryButton(
                    text = if (state.isLoading) "Processing..." else "Pay Online Now",
                    onClick = { viewModel.initiatePayment() },
                    enabled = !state.isLoading,
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.CreditCard, contentDescription = null, modifier = Modifier.size(18.dp))
                    }
                )
            }

            paymentStatus == "SUCCESS" -> {
                SsplSecondaryButton(
                    text = "View Participation Pass",
                    onClick = { navController.navigate(Screen.MyRegistrationsScreen.route) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(modifier = Modifier.height(SsplSpacingTokens.lg))
    }
}
