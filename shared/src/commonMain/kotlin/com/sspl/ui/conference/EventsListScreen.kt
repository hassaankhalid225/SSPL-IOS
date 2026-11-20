package com.sspl.ui.conference

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.HowToReg
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.sspl.Screen
import com.sspl.core.models.EventItem
import com.sspl.designsystem.components.BadgeVariant
import com.sspl.designsystem.components.ModernCapsuleSearchBar
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
fun EventsListScreen(
    navController: NavController,
    viewModel: EventsListViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SsplColorTokens.background)
    ) {
        // Search & Filter header
        EventsHeader(
            searchQuery = uiState.searchQuery,
            onSearchChange = viewModel::setSearchQuery,
            selectedCategory = uiState.selectedCategory,
            onCategoryChange = viewModel::setCategory,
            selectedStatusTab = uiState.selectedStatusTab,
            onStatusTabChange = viewModel::setStatusTab,
            ongoingCount = uiState.ongoingCount,
            upcomingCount = uiState.upcomingCount,
            pastCount = uiState.pastCount
        )

        // Content
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = SsplColorTokens.primary)
            }
        } else if (uiState.filteredEvents.isEmpty()) {
            EmptyEventsView(
                searchQuery = uiState.searchQuery,
                onViewAll = {
                    viewModel.setStatusTab("upcoming")
                    viewModel.setCategory("ALL")
                    viewModel.setSearchQuery("")
                }
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(SsplSpacingTokens.md),
                verticalArrangement = Arrangement.spacedBy(SsplSpacingTokens.md)
            ) {
                items(uiState.filteredEvents, key = { it.effectiveId }) { event ->
                    EventCardItem(
                        event = event,
                        onViewDetails = {
                            navController.currentBackStackEntry?.savedStateHandle?.set("eventId", event.effectiveId)
                            navController.currentBackStackEntry?.savedStateHandle?.set("eventJson", Json.encodeToString(event))
                            navController.navigate(Screen.EventDetailScreen.route)
                        },
                        onRegister = {
                            navController.currentBackStackEntry?.savedStateHandle?.set("eventId", event.effectiveId)
                            navController.currentBackStackEntry?.savedStateHandle?.set("eventJson", Json.encodeToString(event))
                            navController.navigate(Screen.EventRegistrationScreen.route)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun EventsHeader(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    selectedCategory: String,
    onCategoryChange: (String) -> Unit,
    selectedStatusTab: String,
    onStatusTabChange: (String) -> Unit,
    ongoingCount: Int,
    upcomingCount: Int,
    pastCount: Int
) {
    Surface(
        color = SsplColorTokens.surface,
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = SsplSpacingTokens.md, vertical = SsplSpacingTokens.sm)
        ) {
            // Modern Capsule Search Bar
            ModernCapsuleSearchBar(
                query = searchQuery,
                onQueryChange = onSearchChange,
                placeholder = "Search events by title or venue...",
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(SsplSpacingTokens.sm))

            // Category Filter Pills
            val categories = listOf(
                "ALL" to "All Events",
                "CONFERENCE" to "Conferences",
                "WORKSHOP" to "Workshops",
                "MONTHLY_MEETING" to "Monthly Meetings",
                "SEMINAR" to "CME & Seminars"
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(SsplSpacingTokens.xs)
            ) {
                categories.forEach { (key, label) ->
                    val isSelected = selectedCategory == key
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50.dp))
                            .background(
                                if (isSelected) SsplColorTokens.primaryDark else SsplColorTokens.boxColor
                            )
                            .clickable { onCategoryChange(key) }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = label,
                            style = SsplTypographyTokens.bodySmall.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else SsplColorTokens.textPrimary
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(SsplSpacingTokens.sm))

            // Status Tabs (Ongoing, Upcoming, Past)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(50.dp))
                    .background(SsplColorTokens.boxColor)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                StatusTabButton(
                    label = "Ongoing",
                    count = ongoingCount,
                    isSelected = selectedStatusTab == "ongoing",
                    onClick = { onStatusTabChange("ongoing") },
                    modifier = Modifier.weight(1f)
                )
                StatusTabButton(
                    label = "Upcoming",
                    count = upcomingCount,
                    isSelected = selectedStatusTab == "upcoming",
                    onClick = { onStatusTabChange("upcoming") },
                    modifier = Modifier.weight(1f)
                )
                StatusTabButton(
                    label = "Past",
                    count = pastCount,
                    isSelected = selectedStatusTab == "past",
                    onClick = { onStatusTabChange("past") },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun StatusTabButton(
    label: String,
    count: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(50.dp))
            .background(if (isSelected) SsplColorTokens.primaryDark else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = label,
                style = SsplTypographyTokens.bodySmall.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) Color.White else SsplColorTokens.textSecondary
                )
            )
            Spacer(modifier = Modifier.width(6.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50.dp))
                    .background(
                        if (isSelected) Color.White.copy(alpha = 0.25f)
                        else SsplColorTokens.muted.copy(alpha = 0.15f)
                    )
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = count.toString(),
                    style = TextStyle(
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) Color.White else SsplColorTokens.muted
                    )
                )
            }
        }
    }
}

private typealias TextStyle = androidx.compose.ui.text.TextStyle

@Composable
fun EventCardItem(
    event: EventItem,
    onViewDetails: () -> Unit,
    onRegister: () -> Unit
) {
    SsplCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onViewDetails
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Optional preview banner image
            if (!event.effectiveImage.isNullOrBlank()) {
                AsyncImage(
                    model = event.effectiveImage,
                    contentDescription = event.title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
                    contentScale = ContentScale.Crop
                )
            }

            Column(modifier = Modifier.padding(SsplSpacingTokens.md)) {
                // Top row: Date Badge + Badges & Title
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(SsplSpacingTokens.sm)
                ) {
                    // Date Badge
                    DateBadge(
                        month = event.startMonthName,
                        day = event.startDayNumber,
                        year = event.startYear
                    )

                    // Details Column
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Status Badge
                            val statusVariant = when (event.computedStatus) {
                                "ongoing" -> BadgeVariant.ERROR
                                "upcoming" -> BadgeVariant.SUCCESS
                                else -> BadgeVariant.NEUTRAL
                            }
                            SsplBadge(
                                text = event.computedStatus.uppercase(),
                                variant = statusVariant
                            )

                            // Type Badge
                            SsplBadge(
                                text = event.readableType,
                                variant = BadgeVariant.PRIMARY
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // Title
                        Text(
                            text = event.title,
                            style = SsplTypographyTokens.headlineSmall.copy(fontSize = 17.sp),
                            color = SsplColorTokens.textPrimary,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.height(SsplSpacingTokens.sm))

                // Description
                Text(
                    text = event.effectiveDescription,
                    style = SsplTypographyTokens.bodySmall.copy(color = SsplColorTokens.textSecondary),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(SsplSpacingTokens.sm))

                // Venue
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Venue",
                        modifier = Modifier.size(16.dp),
                        tint = SsplColorTokens.accent
                    )
                    Text(
                        text = event.effectiveVenue,
                        style = SsplTypographyTokens.caption.copy(color = SsplColorTokens.textSecondary),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Timing
                if (event.formattedTimeRange.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = "Time",
                            modifier = Modifier.size(16.dp),
                            tint = SsplColorTokens.accent
                        )
                        Text(
                            text = event.formattedTimeRange,
                            style = SsplTypographyTokens.caption.copy(color = SsplColorTokens.textSecondary)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(SsplSpacingTokens.md))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(SsplSpacingTokens.sm)
                ) {
                    if (event.isRegistrationAllowed) {
                        SsplSecondaryButton(
                            text = "Register",
                            onClick = onRegister,
                            modifier = Modifier.weight(1f),
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.HowToReg,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        )
                    }

                    SsplPrimaryButton(
                        text = "View Details",
                        onClick = onViewDetails,
                        modifier = Modifier.weight(1f),
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun DateBadge(
    month: String,
    day: String,
    year: String
) {
    Box(
        modifier = Modifier
            .size(68.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(SsplColorTokens.surface)
            .padding(2.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = month,
                style = SsplTypographyTokens.caption.copy(
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = SsplColorTokens.primary
                )
            )
            Text(
                text = day,
                style = SsplTypographyTokens.headlineSmall.copy(
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = SsplColorTokens.textPrimary
                )
            )
            if (year.isNotBlank()) {
                Text(
                    text = year,
                    style = SsplTypographyTokens.caption.copy(
                        fontSize = 9.sp,
                        color = SsplColorTokens.muted
                    )
                )
            }
        }
    }
}

@Composable
private fun EmptyEventsView(
    searchQuery: String,
    onViewAll: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(SsplSpacingTokens.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.CalendarMonth,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = SsplColorTokens.muted
        )
        Spacer(modifier = Modifier.height(SsplSpacingTokens.md))
        Text(
            text = "No Events Found",
            style = SsplTypographyTokens.headlineSmall,
            color = SsplColorTokens.textPrimary
        )
        Spacer(modifier = Modifier.height(SsplSpacingTokens.xs))
        Text(
            text = if (searchQuery.isNotBlank()) {
                "No events matched your search: \"$searchQuery\"."
            } else {
                "There are no events listed under this category at the moment. Please check back later."
            },
            style = SsplTypographyTokens.bodySmall.copy(color = SsplColorTokens.textSecondary),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(SsplSpacingTokens.lg))
        SsplPrimaryButton(
            text = "View All Upcoming Events",
            onClick = onViewAll
        )
    }
}

