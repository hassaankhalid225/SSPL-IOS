package com.sspl.ui.conference

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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.CircularProgressIndicator
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
import com.sspl.core.models.EventItem
import com.sspl.core.models.EventRole
import com.sspl.designsystem.components.BadgeVariant
import com.sspl.designsystem.components.FeedbackBanner
import com.sspl.designsystem.components.SsplBadge
import com.sspl.designsystem.components.SsplCard
import com.sspl.designsystem.components.SsplPrimaryButton
import com.sspl.designsystem.tokens.SsplColorTokens
import com.sspl.designsystem.tokens.SsplSpacingTokens
import com.sspl.designsystem.tokens.SsplTypographyTokens
import com.sspl.platform.shared.UrlOpener
import com.sspl.session.UserSession
import kotlinx.serialization.json.Json
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun EventRegistrationScreen(
    navController: NavController,
    viewModel: EventDetailViewModel = koinViewModel(),
    userSession: UserSession = koinInject(),
    urlOpener: UrlOpener = koinInject()
) {
    val eventIdArg = navController.previousBackStackEntry?.savedStateHandle?.get<Long>("eventId") ?: 0L
    val eventJsonArg = navController.previousBackStackEntry?.savedStateHandle?.get<String>("eventJson")

    LaunchedEffect(eventIdArg) {
        if (eventIdArg > 0) {
            viewModel.loadEvent(eventIdArg)
        }
    }

    val uiState by viewModel.uiState.collectAsState()
    val event = uiState.event ?: eventJsonArg?.let {
        try { Json.decodeFromString<EventItem>(it) } catch (e: Exception) { null }
    }

    val user by userSession.userFlow.collectAsState()

    var selectedRoleId by remember {
        mutableStateOf<Long?>(event?.roles?.firstOrNull()?.id)
    }

    // Auto select first role if not set
    LaunchedEffect(event?.roles) {
        if (selectedRoleId == null && !event?.roles.isNullOrEmpty()) {
            selectedRoleId = event.roles.first().id
        }
    }

    val selectedRole: EventRole? = event?.roles?.firstOrNull { it.id == selectedRoleId }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SsplColorTokens.background)
    ) {
        // Modern App bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(SsplColorTokens.background)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(SsplColorTokens.surface)
                    .clickable { navController.popBackStack() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = SsplColorTokens.heading,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.size(12.dp))
            Text(
                text = "Event Registration",
                style = SsplTypographyTokens.headlineSmall.copy(fontSize = 18.sp, fontWeight = FontWeight.Bold),
                color = SsplColorTokens.heading,
                modifier = Modifier.weight(1f)
            )
        }

        if (event == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = SsplColorTokens.primary)
            }
            return
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(SsplSpacingTokens.md),
            verticalArrangement = Arrangement.spacedBy(SsplSpacingTokens.md)
        ) {
            // Event Summary Banner
            SsplCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(SsplSpacingTokens.md)) {
                    SsplBadge(text = event.readableType, variant = BadgeVariant.PRIMARY)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = event.title,
                        style = SsplTypographyTokens.headlineSmall.copy(fontSize = 18.sp),
                        color = SsplColorTokens.textPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${event.formattedDateRange} • ${event.effectiveVenue}",
                        style = SsplTypographyTokens.caption.copy(color = SsplColorTokens.textSecondary)
                    )
                }
            }

            // Registrant Info Card
            SsplCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(SsplSpacingTokens.md)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = SsplColorTokens.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Registrant Details",
                            style = SsplTypographyTokens.headlineSmall.copy(fontSize = 16.sp),
                            color = SsplColorTokens.textPrimary
                        )
                    }
                    Spacer(modifier = Modifier.height(SsplSpacingTokens.sm))
                    user?.let { u ->
                        Text(
                            text = u.fullName,
                            style = SsplTypographyTokens.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = SsplColorTokens.textPrimary
                        )
                        if (!u.pmdcNo.isNullOrBlank()) {
                            Text(
                                text = "PMDC / Reg No: ${u.pmdcNo}",
                                style = SsplTypographyTokens.caption.copy(color = SsplColorTokens.textSecondary)
                            )
                        }
                        if (!u.institution.isNullOrBlank()) {
                            Text(
                                text = "Institution: ${u.institution}",
                                style = SsplTypographyTokens.caption.copy(color = SsplColorTokens.textSecondary)
                            )
                        }
                        if (!u.email.isNullOrBlank()) {
                            Text(
                                text = "Email: ${u.email}",
                                style = SsplTypographyTokens.caption.copy(color = SsplColorTokens.textSecondary)
                            )
                        }
                    } ?: run {
                        Text(
                            text = "Standard Attendee Registration",
                            style = SsplTypographyTokens.bodySmall.copy(color = SsplColorTokens.textSecondary)
                        )
                    }
                }
            }

            // Package / Role Selection
            if (event.roles.isNotEmpty()) {
                Text(
                    text = "Select Registration Role",
                    style = SsplTypographyTokens.headlineSmall.copy(fontSize = 16.sp),
                    color = SsplColorTokens.textPrimary
                )

                event.roles.forEach { role ->
                    val isSelected = selectedRoleId == role.id
                    val borderColor = if (isSelected) SsplColorTokens.primary else SsplColorTokens.divider
                    val bgColor = if (isSelected) SsplColorTokens.primary.copy(alpha = 0.05f) else SsplColorTokens.surface

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(bgColor)
                            .border(1.5.dp, borderColor, RoundedCornerShape(12.dp))
                            .clickable { selectedRoleId = role.id }
                            .padding(SsplSpacingTokens.md)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(SsplSpacingTokens.sm)
                        ) {
                            // Radio indicator
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

                            // Role details
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

                            // Fee
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "PKR ${role.currentFee.toInt()}",
                                    style = SsplTypographyTokens.bodyLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = SsplColorTokens.primary
                                    )
                                )
                                if (role.isEarlyBirdActive) {
                                    SsplBadge(text = "Early Bird", variant = BadgeVariant.WARNING)
                                }
                            }
                        }
                    }
                }
            }

            // Fee Summary
            SsplCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(SsplSpacingTokens.md)) {
                    Text(
                        text = "Payment Summary",
                        style = SsplTypographyTokens.headlineSmall.copy(fontSize = 16.sp),
                        color = SsplColorTokens.textPrimary
                    )
                    Spacer(modifier = Modifier.height(SsplSpacingTokens.sm))

                    val payableAmount = selectedRole?.currentFee?.toInt() ?: 0

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Registration Fee",
                            style = SsplTypographyTokens.bodyMedium,
                            color = SsplColorTokens.textSecondary
                        )
                        Text(
                            text = "PKR $payableAmount",
                            style = SsplTypographyTokens.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = SsplColorTokens.textPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    HorizontalDivider(color = SsplColorTokens.divider)
                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Total Payable",
                            style = SsplTypographyTokens.headlineSmall.copy(fontSize = 16.sp),
                            color = SsplColorTokens.textPrimary
                        )
                        Text(
                            text = "PKR $payableAmount",
                            style = SsplTypographyTokens.headlineSmall.copy(
                                fontSize = 18.sp,
                                color = SsplColorTokens.primary
                            )
                        )
                    }
                }
            }

            // Error or Success Banner
            if (!uiState.errorMessage.isNullOrBlank()) {
                FeedbackBanner(message = uiState.errorMessage!!, isError = true)
            }
            if (!uiState.registrationMessage.isNullOrBlank()) {
                FeedbackBanner(message = uiState.registrationMessage!!, isError = false)
            }

            // Submit Button
            SsplPrimaryButton(
                text = if (uiState.isRegistering) "Processing..." else "Proceed to Payment",
                onClick = {
                    viewModel.register(selectedRoleId) { paymentUrl ->
                        urlOpener.openUrl(paymentUrl)
                        navController.navigate(Screen.PaymentStatusScreen.route)
                    }
                },
                enabled = !uiState.isRegistering,
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = {
                    if (uiState.isRegistering) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.CreditCard,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            )

            Spacer(modifier = Modifier.height(SsplSpacingTokens.lg))
        }
    }
}

