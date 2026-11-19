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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import com.sspl.Screen
import com.sspl.core.models.MyRegistrationsData
import com.sspl.core.models.Registration
import com.sspl.core.repositories.EventRepository
import com.sspl.designsystem.components.SsplBadge
import com.sspl.designsystem.components.SsplBadgeVariant
import com.sspl.designsystem.components.SsplButton
import com.sspl.designsystem.components.SsplButtonVariant
import com.sspl.designsystem.components.SsplCard
import com.sspl.designsystem.theme.AppTheme
import com.sspl.session.UserSession
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

/**
 * D4: My Registrations Screen with Entry Pass / QR Badge Modal
 */
@Composable
fun MyRegistrationsScreen(
    conferenceId: Long = 0L,
    navController: NavController,
    eventRepository: EventRepository = koinInject(),
    userSession: UserSession = koinInject()
) {
    val user by userSession.userFlow.collectAsState()
    val scope = rememberCoroutineScope()

    var registrationsData by remember { mutableStateOf<MyRegistrationsData?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var selectedTab by remember { mutableStateOf("ALL") }
    var selectedBadgeRegistration by remember { mutableStateOf<Registration?>(null) }
    var retryingId by remember { mutableStateOf<Long?>(null) }

    val loadRegistrations = {
        scope.launch {
            isLoading = true
            eventRepository.getMyRegistrations().collect { data ->
                registrationsData = data
                isLoading = false
            }
        }
    }

    LaunchedEffect(Unit) {
        loadRegistrations()
    }

    val activeList = when (selectedTab) {
        "CONFERENCES" -> registrationsData?.conferences ?: emptyList()
        "WORKSHOPS" -> registrationsData?.workshops ?: emptyList()
        "MEETINGS" -> registrationsData?.monthlyMeetings ?: emptyList()
        else -> registrationsData?.all ?: (
            (registrationsData?.conferences ?: emptyList()) +
            (registrationsData?.workshops ?: emptyList()) +
            (registrationsData?.monthlyMeetings ?: emptyList())
        ).distinctBy { it.id }
    }

    val tabs = listOf(
        "ALL" to "All (${registrationsData?.all?.size ?: activeList.size})",
        "CONFERENCES" to "Conferences (${registrationsData?.conferences?.size ?: 0})",
        "WORKSHOPS" to "Workshops (${registrationsData?.workshops?.size ?: 0})",
        "MEETINGS" to "Monthly Meetings (${registrationsData?.monthlyMeetings?.size ?: 0})"
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(AppTheme.colors.background)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "PARTICIPATION",
                        style = AppTheme.typography.labelSmall.copy(
                            color = AppTheme.colors.secondary,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "My Registrations",
                        style = AppTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = AppTheme.colors.heading
                        )
                    )
                }

                IconButton(
                    onClick = { loadRegistrations() }
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh",
                        tint = AppTheme.colors.secondary
                    )
                }
            }
            Text(
                text = "Check payment status, view attendee pass & QR badge for entry.",
                style = AppTheme.typography.bodySmall.copy(color = AppTheme.colors.muted)
            )
        }

        // Filter Tabs
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(tabs) { (key, label) ->
                    val isSelected = selectedTab == key
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (isSelected) AppTheme.colors.primary else AppTheme.colors.surface
                            )
                            .border(
                                width = 1.dp,
                                color = if (isSelected) AppTheme.colors.primary else AppTheme.colors.border,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable { selectedTab = key }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = label,
                            style = AppTheme.typography.labelMedium.copy(
                                color = if (isSelected) AppTheme.colors.onPrimary else AppTheme.colors.body,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        )
                    }
                }
            }
        }

        if (isLoading) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = AppTheme.colors.secondary)
                }
            }
        } else if (activeList.isEmpty()) {
            item {
                SsplCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No registrations found in this category.",
                            style = AppTheme.typography.bodyMedium.copy(color = AppTheme.colors.muted),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        SsplButton(
                            text = "Browse Upcoming Events",
                            onClick = { navController.navigate(Screen.ScientificProgramsScreen.route) }
                        )
                    }
                }
            }
        } else {
            items(activeList) { reg ->
                val isSuccess = reg.paymentStatus == "SUCCESS"
                val isPending = reg.paymentStatus == "PENDING"
                val isFailed = reg.paymentStatus == "FAILED"

                SsplCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = reg.conference?.title ?: "Society Event",
                                style = AppTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = AppTheme.colors.heading
                                ),
                                modifier = Modifier.weight(1f)
                            )

                            val badgeVariant = when {
                                isSuccess -> SsplBadgeVariant.Success
                                isPending -> SsplBadgeVariant.Warning
                                else -> SsplBadgeVariant.Error
                            }

                            SsplBadge(
                                text = reg.paymentStatus,
                                variant = badgeVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        reg.role?.name?.let { roleName ->
                            Text(
                                text = "Registered as: $roleName",
                                style = AppTheme.typography.bodySmall.copy(
                                    color = AppTheme.colors.secondary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Fee: PKR ${reg.feeAmount.toLong()}",
                                style = AppTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = AppTheme.colors.body
                                )
                            )
                            reg.paymentDate?.let { dt ->
                                Text(
                                    text = dt.split("T").firstOrNull() ?: dt,
                                    style = AppTheme.typography.bodySmall.copy(color = AppTheme.colors.muted)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (isSuccess) {
                                SsplButton(
                                    text = "View Entry Badge",
                                    onClick = { selectedBadgeRegistration = reg },
                                    variant = SsplButtonVariant.Primary,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            } else if (isPending || isFailed) {
                                SsplButton(
                                    text = if (retryingId == reg.id) "Retrying..." else "Retry Payment",
                                    onClick = {
                                        scope.launch {
                                            retryingId = reg.id
                                            eventRepository.retryEventPayment(reg.id).collect { retryResp ->
                                                retryingId = null
                                                val url = retryResp.payment?.bestPaymentUrl
                                                if (!url.isNullOrBlank()) {
                                                    navController.navigate(Screen.PaymentStatusScreen.route)
                                                }
                                            }
                                        }
                                    },
                                    enabled = retryingId == null,
                                    variant = SsplButtonVariant.Outline,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Participation Badge Dialog
    selectedBadgeRegistration?.let { reg ->
        AttendeeBadgeDialog(
            registration = reg,
            userName = listOfNotNull(user?.prefix, user?.firstName, user?.lastName).joinToString(" ").ifBlank { "Delegate" },
            institution = user?.institution ?: user?.city ?: "SSP Member",
            onDismiss = { selectedBadgeRegistration = null }
        )
    }
}

/**
 * Attendee Pass & QR Badge Dialog Modal
 */
@Composable
private fun AttendeeBadgeDialog(
    registration: Registration,
    userName: String,
    institution: String,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = AppTheme.colors.surface,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "ENTRY PASS & BADGE",
                        style = AppTheme.typography.labelSmall.copy(
                            color = AppTheme.colors.secondary,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = AppTheme.colors.muted
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Role Banner
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(AppTheme.colors.primary)
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = (registration.role?.name ?: "CONFERENCE DELEGATE").uppercase(),
                        style = AppTheme.typography.labelMedium.copy(
                            color = AppTheme.colors.onPrimary,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Attendee Name & Affiliation
                Text(
                    text = userName,
                    style = AppTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = AppTheme.colors.heading
                    ),
                    textAlign = TextAlign.Center
                )
                Text(
                    text = institution,
                    style = AppTheme.typography.bodySmall.copy(color = AppTheme.colors.muted),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Registration ID
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(AppTheme.colors.accent.copy(alpha = 0.15f))
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "REG #${registration.id}",
                        style = AppTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = AppTheme.colors.secondary
                        )
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Simulated QR Box with Icon & Payload verification
                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .border(2.dp, AppTheme.colors.primary, RoundedCornerShape(12.dp))
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.QrCode,
                            contentDescription = "QR Code",
                            tint = AppTheme.colors.primary,
                            modifier = Modifier.size(100.dp)
                        )
                        Text(
                            text = "SSPL-REG-${registration.id}",
                            style = AppTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Present this QR code at the registration desk for badge printing and venue check-in.",
                    style = AppTheme.typography.bodySmall.copy(
                        color = AppTheme.colors.muted,
                        fontSize = 11.sp
                    ),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                SsplButton(
                    text = "Done",
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
