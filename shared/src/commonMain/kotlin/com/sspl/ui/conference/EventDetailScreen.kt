package com.sspl.ui.conference

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material.icons.filled.HowToReg
import androidx.compose.material.icons.filled.Launch
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.sspl.Screen
import com.sspl.core.models.EventItem
import com.sspl.core.models.EventRole
import com.sspl.core.models.Session
import com.sspl.designsystem.components.BadgeVariant
import com.sspl.designsystem.components.FeedbackBanner
import com.sspl.designsystem.components.ModernSpecsGrid
import com.sspl.designsystem.components.SsplBadge
import com.sspl.designsystem.components.SsplCard
import com.sspl.designsystem.components.SsplPrimaryButton
import com.sspl.designsystem.components.SsplSecondaryButton
import com.sspl.designsystem.tokens.SsplColorTokens
import com.sspl.designsystem.tokens.SsplSpacingTokens
import com.sspl.designsystem.tokens.SsplTypographyTokens
import com.sspl.platform.shared.UrlOpener
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun EventDetailScreen(
    navController: NavController,
    viewModel: EventDetailViewModel = koinViewModel(),
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
    val scrollState = rememberScrollState()

    // Fallback if loading or using passed argument
    val event = uiState.event ?: eventJsonArg?.let {
        try { Json.decodeFromString<EventItem>(it) } catch (e: Exception) { null }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SsplColorTokens.background)
    ) {
        // Modern App bar with circular buttons
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
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = event?.readableType ?: "Event Details",
                style = SsplTypographyTokens.headlineSmall.copy(fontSize = 18.sp, fontWeight = FontWeight.Bold),
                color = SsplColorTokens.heading,
                modifier = Modifier.weight(1f)
            )
            if (!event?.webLink.isNullOrBlank()) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(SsplColorTokens.surface)
                        .clickable { urlOpener.openUrl(event!!.webLink!!) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Launch,
                        contentDescription = "Open Web Link",
                        tint = SsplColorTokens.heading,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        if (uiState.isLoading && event == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = SsplColorTokens.primary)
            }
            return
        }

        if (event == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = uiState.errorMessage ?: "Event details unavailable.",
                    style = SsplTypographyTokens.bodyMedium,
                    color = SsplColorTokens.textSecondary
                )
            }
            return
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(SsplSpacingTokens.md)
        ) {
            // Hero Banner Image
            if (!event.effectiveImage.isNullOrBlank()) {
                AsyncImage(
                    model = event.effectiveImage,
                    contentDescription = event.title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.height(SsplSpacingTokens.md))
            }

            // Badges & Title
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val statusVariant = when (event.computedStatus) {
                    "ongoing" -> BadgeVariant.ERROR
                    "upcoming" -> BadgeVariant.SUCCESS
                    else -> BadgeVariant.NEUTRAL
                }
                SsplBadge(text = event.computedStatus.uppercase(), variant = statusVariant)
                SsplBadge(text = event.readableType, variant = BadgeVariant.PRIMARY)
            }

            Spacer(modifier = Modifier.height(SsplSpacingTokens.sm))

            Text(
                text = event.title,
                style = SsplTypographyTokens.headlineLarge.copy(fontSize = 24.sp),
                color = SsplColorTokens.textPrimary
            )

            Spacer(modifier = Modifier.height(SsplSpacingTokens.md))

            // 4-box Specs Grid (Screen 2 pattern)
            val specs = listOf(
                Triple(Icons.Default.WorkspacePremium, "12 CME", "Accredited"),
                Triple(Icons.Default.Groups, if (event.type?.contains("CONFERENCE", ignoreCase = true) == true) "Congress" else "Meeting", "Format"),
                Triple(Icons.Default.Schedule, if (event.formattedTimeRange.isNotBlank()) event.formattedTimeRange.take(8) else "Full Day", "Time"),
                Triple(Icons.Default.LocationOn, if (event.effectiveVenue.isNotBlank()) event.effectiveVenue.take(12) else "Main Hall", "Venue")
            )
            ModernSpecsGrid(specs = specs)

            Spacer(modifier = Modifier.height(SsplSpacingTokens.md))

            // Info rows card
            SsplCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(SsplSpacingTokens.md),
                    verticalArrangement = Arrangement.spacedBy(SsplSpacingTokens.sm)
                ) {
                    // Date
                    EventDetailInfoRow(
                        icon = Icons.Default.CalendarMonth,
                        label = "DATE",
                        value = event.formattedDateRange
                    )

                    HorizontalDivider(color = SsplColorTokens.divider)

                    // Time
                    if (event.formattedTimeRange.isNotBlank()) {
                        EventDetailInfoRow(
                            icon = Icons.Default.Schedule,
                            label = "TIME",
                            value = event.formattedTimeRange
                        )
                        HorizontalDivider(color = SsplColorTokens.divider)
                    }

                    // Venue
                    EventDetailInfoRow(
                        icon = Icons.Default.LocationOn,
                        label = "VENUE",
                        value = event.effectiveVenue
                    )
                }
            }

            Spacer(modifier = Modifier.height(SsplSpacingTokens.md))

            // Registration Section
            RegistrationStatusBox(
                event = event,
                isLoggedIn = viewModel.isLoggedIn,
                registrationData = uiState.registrationData,
                onRegisterClick = {
                    navController.currentBackStackEntry?.savedStateHandle?.set("eventId", event.effectiveId)
                    navController.currentBackStackEntry?.savedStateHandle?.set("eventJson", Json.encodeToString(event))
                    navController.navigate(Screen.EventRegistrationScreen.route)
                },
                onLoginClick = {
                    navController.navigate(Screen.SignInScreen.route)
                },
                onViewPass = {
                    navController.navigate(Screen.MyRegistrationsScreen.route)
                }
            )

            Spacer(modifier = Modifier.height(SsplSpacingTokens.md))

            // Description / About
            SsplCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(SsplSpacingTokens.md)) {
                    Text(
                        text = "About This Event",
                        style = SsplTypographyTokens.headlineSmall.copy(fontSize = 18.sp),
                        color = SsplColorTokens.textPrimary
                    )
                    Spacer(modifier = Modifier.height(SsplSpacingTokens.sm))
                    Text(
                        text = event.effectiveDescription,
                        style = SsplTypographyTokens.bodyMedium.copy(
                            color = SsplColorTokens.textSecondary,
                            lineHeight = 22.sp
                        )
                    )
                }
            }

            // Roles & Pricing overview
            if (event.roles.isNotEmpty()) {
                Spacer(modifier = Modifier.height(SsplSpacingTokens.md))
                SsplCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(SsplSpacingTokens.md)) {
                        Text(
                            text = "Registration Packages",
                            style = SsplTypographyTokens.headlineSmall.copy(fontSize = 18.sp),
                            color = SsplColorTokens.textPrimary
                        )
                        Spacer(modifier = Modifier.height(SsplSpacingTokens.sm))
                        event.roles.forEach { role ->
                            RoleRowItem(role = role)
                        }
                    }
                }
            }

            // Sessions / Programme Schedule
            if (event.sessions.isNotEmpty()) {
                Spacer(modifier = Modifier.height(SsplSpacingTokens.md))
                SsplCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(SsplSpacingTokens.md)) {
                        Text(
                            text = "Scientific Programme",
                            style = SsplTypographyTokens.headlineSmall.copy(fontSize = 18.sp),
                            color = SsplColorTokens.textPrimary
                        )
                        Spacer(modifier = Modifier.height(SsplSpacingTokens.sm))
                        event.sessions.forEach { session ->
                            SessionRowItem(session = session)
                        }
                    }
                }
            }

            // Related Workshops
            if (uiState.relatedWorkshops.isNotEmpty()) {
                Spacer(modifier = Modifier.height(SsplSpacingTokens.md))
                SsplCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(SsplSpacingTokens.md)) {
                        Text(
                            text = "Pre-Event Workshops",
                            style = SsplTypographyTokens.headlineSmall.copy(fontSize = 18.sp),
                            color = SsplColorTokens.textPrimary
                        )
                        Spacer(modifier = Modifier.height(SsplSpacingTokens.sm))
                        uiState.relatedWorkshops.forEach { workshop ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        navController.currentBackStackEntry?.savedStateHandle?.set("eventId", workshop.effectiveId)
                                        navController.currentBackStackEntry?.savedStateHandle?.set("eventJson", Json.encodeToString(workshop))
                                        navController.navigate(Screen.EventDetailScreen.route)
                                    }
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = workshop.title,
                                        style = SsplTypographyTokens.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                        color = SsplColorTokens.textPrimary
                                    )
                                    Text(
                                        text = workshop.effectiveVenue,
                                        style = SsplTypographyTokens.caption.copy(color = SsplColorTokens.muted)
                                    )
                                }
                                SsplBadge(text = "View", variant = BadgeVariant.PRIMARY)
                            }
                            HorizontalDivider(color = SsplColorTokens.divider)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(SsplSpacingTokens.xl))
        }
    }
}

@Composable
private fun RegistrationStatusBox(
    event: EventItem,
    isLoggedIn: Boolean,
    registrationData: com.sspl.core.models.EventCheckData?,
    onRegisterClick: () -> Unit,
    onLoginClick: () -> Unit,
    onViewPass: () -> Unit
) {
    if (!isLoggedIn) {
        SsplCard(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = SsplColorTokens.primary.copy(alpha = 0.05f)
        ) {
            Column(
                modifier = Modifier.padding(SsplSpacingTokens.md),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Sign in to Register",
                    style = SsplTypographyTokens.headlineSmall.copy(fontSize = 17.sp),
                    color = SsplColorTokens.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Log into your SSP Lahore member account to secure your registration.",
                    style = SsplTypographyTokens.bodySmall.copy(color = SsplColorTokens.textSecondary)
                )
                Spacer(modifier = Modifier.height(SsplSpacingTokens.sm))
                SsplPrimaryButton(
                    text = "Sign In Now",
                    onClick = onLoginClick,
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Login, contentDescription = null, modifier = Modifier.size(16.dp))
                    }
                )
            }
        }
        return
    }

    if (registrationData?.isRegistered == true) {
        SsplCard(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = SsplColorTokens.success.copy(alpha = 0.08f)
        ) {
            Row(
                modifier = Modifier.padding(SsplSpacingTokens.md),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(SsplSpacingTokens.sm)
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = SsplColorTokens.success,
                    modifier = Modifier.size(36.dp)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "You are Registered!",
                        style = SsplTypographyTokens.headlineSmall.copy(fontSize = 17.sp),
                        color = SsplColorTokens.success
                    )
                    val status = registrationData.registration?.paymentStatus ?: "CONFIRMED"
                    Text(
                        text = "Status: $status",
                        style = SsplTypographyTokens.caption.copy(color = SsplColorTokens.textSecondary)
                    )
                }
                SsplSecondaryButton(
                    text = "View Pass",
                    onClick = onViewPass
                )
            }
        }
        return
    }

    if (!event.isRegistrationAllowed) {
        FeedbackBanner(
            message = "Registration for this event is currently closed or completed.",
            isError = false
        )
        return
    }

    // Registration open CTA
    SsplCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = SsplColorTokens.primary.copy(alpha = 0.05f)
    ) {
        Column(modifier = Modifier.padding(SsplSpacingTokens.md)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Registration Open",
                        style = SsplTypographyTokens.headlineSmall.copy(fontSize = 17.sp),
                        color = SsplColorTokens.primary
                    )
                    Text(
                        text = "Secure your seat for this event now",
                        style = SsplTypographyTokens.bodySmall.copy(color = SsplColorTokens.textSecondary)
                    )
                }
                SsplBadge(text = "AVAILABLE", variant = BadgeVariant.SUCCESS)
            }
            Spacer(modifier = Modifier.height(SsplSpacingTokens.md))
            SsplPrimaryButton(
                text = "Register Now",
                onClick = onRegisterClick,
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = {
                    Icon(imageVector = Icons.Default.HowToReg, contentDescription = null, modifier = Modifier.size(18.dp))
                }
            )
        }
    }
}

@Composable
private fun EventDetailInfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(SsplSpacingTokens.sm)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(SsplColorTokens.primary.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = SsplColorTokens.primary,
                modifier = Modifier.size(18.dp)
            )
        }
        Column {
            Text(
                text = label,
                style = SsplTypographyTokens.caption.copy(
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = SsplColorTokens.muted
                )
            )
            Text(
                text = value,
                style = SsplTypographyTokens.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = SsplColorTokens.textPrimary
            )
        }
    }
}

@Composable
private fun RoleRowItem(role: EventRole) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = role.name,
                style = SsplTypographyTokens.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = SsplColorTokens.textPrimary
            )
            if (!role.description.isNullOrBlank()) {
                Text(
                    text = role.description,
                    style = SsplTypographyTokens.caption.copy(color = SsplColorTokens.muted)
                )
            }
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "PKR ${role.currentFee.toInt()}",
                style = SsplTypographyTokens.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = SsplColorTokens.primary
                )
            )
            if (role.isEarlyBirdActive) {
                SsplBadge(text = "Early Bird", variant = BadgeVariant.WARNING)
            }
        }
    }
    HorizontalDivider(color = SsplColorTokens.divider)
}

@Composable
private fun SessionRowItem(session: Session) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = session.title,
                style = SsplTypographyTokens.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = SsplColorTokens.textPrimary,
                modifier = Modifier.weight(1f)
            )
            if (!session.startsAt.isNullOrBlank()) {
                SsplBadge(text = session.startsAt.takeLast(8), variant = BadgeVariant.NEUTRAL)
            }
        }
        if (!session.summary.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = session.summary,
                style = SsplTypographyTokens.caption.copy(color = SsplColorTokens.textSecondary)
            )
        }
    }
    HorizontalDivider(color = SsplColorTokens.divider)
}

