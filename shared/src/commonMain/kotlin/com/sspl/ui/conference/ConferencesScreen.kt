package com.sspl.ui.conference

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.HowToReg
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.sspl.Screen
import com.sspl.core.models.EventItem
import com.sspl.designsystem.components.BadgeVariant
import com.sspl.designsystem.components.SsplBadge
import com.sspl.designsystem.components.SsplCard
import com.sspl.designsystem.components.SsplPrimaryButton
import com.sspl.designsystem.components.SsplSecondaryButton
import com.sspl.designsystem.tokens.SsplColorTokens
import com.sspl.designsystem.tokens.SsplSpacingTokens
import com.sspl.designsystem.tokens.SsplTypographyTokens
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ConferencesScreen(
    navController: NavController,
    viewModel: EventsListViewModel = koinViewModel()
) {
    LaunchedEffect(Unit) {
        viewModel.setCategory("CONFERENCE")
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
                text = "Annual Conferences",
                style = SsplTypographyTokens.headlineSmall.copy(fontSize = 18.sp, fontWeight = FontWeight.Bold),
                color = SsplColorTokens.heading,
                modifier = Modifier.weight(1f)
            )
        }

        // Info Banner
        SsplCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = SsplSpacingTokens.md, vertical = SsplSpacingTokens.sm),
            backgroundColor = SsplColorTokens.primary.copy(alpha = 0.06f)
        ) {
            Row(
                modifier = Modifier.padding(SsplSpacingTokens.sm),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(SsplSpacingTokens.xs)
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = SsplColorTokens.primary,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "Browse SSP Lahore annual conferences, scientific sessions, workshops, and register online.",
                    style = SsplTypographyTokens.caption.copy(color = SsplColorTokens.textSecondary, lineHeight = 16.sp)
                )
            }
        }

        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = SsplColorTokens.primary)
            }
        } else if (uiState.filteredEvents.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(SsplSpacingTokens.xl),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Event,
                        contentDescription = null,
                        modifier = Modifier.size(56.dp),
                        tint = SsplColorTokens.muted
                    )
                    Spacer(modifier = Modifier.height(SsplSpacingTokens.sm))
                    Text(
                        text = "No Conferences Scheduled",
                        style = SsplTypographyTokens.headlineSmall,
                        color = SsplColorTokens.textPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Upcoming annual surgical conferences will be announced here.",
                        style = SsplTypographyTokens.bodySmall.copy(color = SsplColorTokens.textSecondary)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(SsplSpacingTokens.md),
                verticalArrangement = Arrangement.spacedBy(SsplSpacingTokens.md)
            ) {
                items(uiState.filteredEvents, key = { it.effectiveId }) { conf ->
                    EventCardItem(
                        event = conf,
                        onViewDetails = {
                            navController.currentBackStackEntry?.savedStateHandle?.set("eventId", conf.effectiveId)
                            navController.currentBackStackEntry?.savedStateHandle?.set("eventJson", Json.encodeToString(conf))
                            navController.navigate(Screen.EventDetailScreen.route)
                        },
                        onRegister = {
                            navController.currentBackStackEntry?.savedStateHandle?.set("eventId", conf.effectiveId)
                            navController.currentBackStackEntry?.savedStateHandle?.set("eventJson", Json.encodeToString(conf))
                            navController.navigate(Screen.EventRegistrationScreen.route)
                        }
                    )
                }
            }
        }
    }
}

