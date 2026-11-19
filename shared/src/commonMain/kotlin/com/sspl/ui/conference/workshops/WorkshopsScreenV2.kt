package com.sspl.ui.conference.workshops

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.HowToReg
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.sspl.Screen
import com.sspl.core.ApiStates
import com.sspl.core.models.Conference
import com.sspl.core.models.ConferenceType
import com.sspl.designsystem.components.BadgeVariant
import com.sspl.designsystem.components.SsplBadge
import com.sspl.designsystem.components.SsplCard
import com.sspl.designsystem.components.SsplPrimaryButton
import com.sspl.designsystem.components.SsplSecondaryButton
import com.sspl.designsystem.tokens.SsplColorTokens
import com.sspl.designsystem.tokens.SsplSpacingTokens
import com.sspl.designsystem.tokens.SsplTypographyTokens
import com.sspl.platform.formatDateTime
import com.sspl.ui.components.Error
import com.sspl.ui.conference.ConferenceViewModel
import com.sspl.ui.conference.DateBadge
import com.sspl.utils.FORMAT_DAY_MONTH
import com.sspl.utils.FORMAT_DEFAULT_DATE_TIME
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun WorkshopsScreenV2(
    navController: NavController,
    viewModel: ConferenceViewModel = koinViewModel()
) {
    val uiState by viewModel.conferences.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) {
        viewModel.getAllConferences(ConferenceType.WORKSHOP)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SsplColorTokens.background)
    ) {
        when (val state = uiState) {
            is ApiStates.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = SsplColorTokens.primary)
                }
            }

            is ApiStates.Failure -> {
                Error(
                    modifier = Modifier.fillMaxSize(),
                    error = state.error
                )
            }

            ApiStates.Idle -> {}

            is ApiStates.Success -> {
                val conferences = state.data ?: emptyList()
                if (conferences.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize().padding(SsplSpacingTokens.xl),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Build,
                                contentDescription = null,
                                modifier = Modifier.size(56.dp),
                                tint = SsplColorTokens.muted
                            )
                            Spacer(modifier = Modifier.height(SsplSpacingTokens.sm))
                            Text(
                                text = "No Workshops Available",
                                style = SsplTypographyTokens.headlineSmall,
                                color = SsplColorTokens.textPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Pre-event workshops and hands-on courses will appear here.",
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
                        items(conferences, key = { it.id }) { item ->
                            ModernWorkshopCard(
                                item = item,
                                onViewDetails = {
                                    navController.currentBackStackEntry?.savedStateHandle?.set(
                                        "data",
                                        Json.encodeToString(item)
                                    )
                                    navController.navigate(route = Screen.WorkshopDetailScreen.route)
                                },
                                onRegister = {
                                    navController.currentBackStackEntry?.savedStateHandle?.set(
                                        "data",
                                        Json.encodeToString(item)
                                    )
                                    navController.navigate(route = Screen.ConferenceRegistrationScreen.route)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ModernWorkshopCard(
    item: Conference,
    onViewDetails: () -> Unit,
    onRegister: () -> Unit
) {
    SsplCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onViewDetails
    ) {
        Column(modifier = Modifier.padding(SsplSpacingTokens.md)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(SsplSpacingTokens.sm)
            ) {
                // Date badge
                val dayStr = try {
                    formatDateTime(
                        dateTime = item.startDate ?: "",
                        inFormat = FORMAT_DEFAULT_DATE_TIME,
                        outFormat = "dd"
                    )
                } catch (e: Exception) { "--" }

                val monthStr = try {
                    formatDateTime(
                        dateTime = item.startDate ?: "",
                        inFormat = FORMAT_DEFAULT_DATE_TIME,
                        outFormat = "MMM"
                    ).uppercase()
                } catch (e: Exception) { "TBA" }

                val yearStr = try {
                    formatDateTime(
                        dateTime = item.startDate ?: "",
                        inFormat = FORMAT_DEFAULT_DATE_TIME,
                        outFormat = "yyyy"
                    )
                } catch (e: Exception) { "" }

                DateBadge(month = monthStr, day = dayStr, year = yearStr)

                Column(modifier = Modifier.weight(1f)) {
                    SsplBadge(text = "WORKSHOP", variant = BadgeVariant.PRIMARY)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = item.title ?: "Surgical Workshop",
                        style = SsplTypographyTokens.headlineSmall.copy(fontSize = 17.sp),
                        color = SsplColorTokens.textPrimary
                    )
                }
            }

            if (!item.summary.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(SsplSpacingTokens.sm))
                Text(
                    text = item.summary,
                    style = SsplTypographyTokens.bodySmall.copy(color = SsplColorTokens.textSecondary),
                    maxLines = 2
                )
            }

            if (item.venue != null) {
                Spacer(modifier = Modifier.height(SsplSpacingTokens.xs))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = SsplColorTokens.accent
                    )
                    Text(
                        text = item.venue.address ?: item.venue.name ?: "Venue TBA",
                        style = SsplTypographyTokens.caption.copy(color = SsplColorTokens.muted)
                    )
                }
            }

            Spacer(modifier = Modifier.height(SsplSpacingTokens.md))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(SsplSpacingTokens.sm)
            ) {
                SsplSecondaryButton(
                    text = "Register",
                    onClick = onRegister,
                    modifier = Modifier.weight(1f),
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.HowToReg, contentDescription = null, modifier = Modifier.size(16.dp))
                    }
                )
                SsplPrimaryButton(
                    text = "View Details",
                    onClick = onViewDetails,
                    modifier = Modifier.weight(1f),
                    trailingIcon = {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                    }
                )
            }
        }
    }
}
