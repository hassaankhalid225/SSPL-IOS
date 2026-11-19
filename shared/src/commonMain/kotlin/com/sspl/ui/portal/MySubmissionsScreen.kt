package com.sspl.ui.portal

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.sspl.Screen
import com.sspl.core.models.AbstractSubmission
import com.sspl.core.repositories.PortalRepository
import com.sspl.designsystem.components.SsplBadge
import com.sspl.designsystem.components.SsplBadgeVariant
import com.sspl.designsystem.components.SsplButton
import com.sspl.designsystem.components.SsplButtonVariant
import com.sspl.designsystem.components.SsplCard
import com.sspl.designsystem.theme.AppTheme
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

/**
 * D7: User's Abstract Submissions List
 */
@Composable
fun MySubmissionsScreen(
    navController: NavController,
    portalRepository: PortalRepository = koinInject()
) {
    val scope = rememberCoroutineScope()
    var submissions by remember { mutableStateOf<List<AbstractSubmission>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var selectedFilter by remember { mutableStateOf("ALL") }

    val loadSubmissions = {
        scope.launch {
            isLoading = true
            portalRepository.getMySubmissions().collect { list ->
                submissions = list
                isLoading = false
            }
        }
    }

    LaunchedEffect(Unit) {
        loadSubmissions()
    }

    val filteredList = when (selectedFilter) {
        "PENDING" -> submissions.filter { it.status.equals("PENDING", ignoreCase = true) }
        "ACCEPTED" -> submissions.filter { it.status.equals("ACCEPTED", ignoreCase = true) }
        "REJECTED" -> submissions.filter { it.status.equals("REJECTED", ignoreCase = true) }
        else -> submissions
    }

    val filterOptions = listOf("ALL" to "All", "PENDING" to "Pending", "ACCEPTED" to "Accepted", "REJECTED" to "Rejected")

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
                        text = "SCHOLARSHIP",
                        style = AppTheme.typography.labelSmall.copy(
                            color = AppTheme.colors.secondary,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "My Submissions",
                        style = AppTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = AppTheme.colors.heading
                        )
                    )
                }

                IconButton(onClick = { loadSubmissions() }) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh",
                        tint = AppTheme.colors.secondary
                    )
                }
            }
            Text(
                text = "Review the review status of abstracts sent for scientific programmes.",
                style = AppTheme.typography.bodySmall.copy(color = AppTheme.colors.muted)
            )
        }

        // Status Filter Chips
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(filterOptions) { (key, label) ->
                    val isSelected = selectedFilter == key
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) AppTheme.colors.primary else AppTheme.colors.surface)
                            .border(
                                1.dp,
                                if (isSelected) AppTheme.colors.primary else AppTheme.colors.border,
                                RoundedCornerShape(8.dp)
                            )
                            .clickable { selectedFilter = key }
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
        } else if (filteredList.isEmpty()) {
            item {
                SsplCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No abstract submissions found.",
                            style = AppTheme.typography.bodyMedium.copy(color = AppTheme.colors.muted)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        SsplButton(
                            text = "Submit an Abstract",
                            onClick = { navController.navigate(Screen.AbstractSubmissionScreen.route) }
                        )
                    }
                }
            }
        } else {
            items(filteredList) { item ->
                val isAccepted = item.status.equals("ACCEPTED", ignoreCase = true)
                val isRejected = item.status.equals("REJECTED", ignoreCase = true)

                SsplCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "ID: #${item.id}",
                                style = AppTheme.typography.labelSmall.copy(
                                    color = AppTheme.colors.secondary,
                                    fontWeight = FontWeight.Bold
                                )
                            )

                            val badgeVariant = when {
                                isAccepted -> SsplBadgeVariant.Success
                                isRejected -> SsplBadgeVariant.Error
                                else -> SsplBadgeVariant.Warning
                            }

                            SsplBadge(
                                text = item.status.uppercase(),
                                variant = badgeVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = item.title,
                            style = AppTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = AppTheme.colors.heading
                            )
                        )

                        item.authors?.let { authors ->
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Authors: $authors",
                                style = AppTheme.typography.bodySmall.copy(color = AppTheme.colors.body)
                            )
                        }

                        item.affiliations?.let { aff ->
                            Text(
                                text = "Affiliation: $aff",
                                style = AppTheme.typography.bodySmall.copy(color = AppTheme.colors.muted)
                            )
                        }

                        item.reviewerComments?.let { comments ->
                            Spacer(modifier = Modifier.height(8.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(AppTheme.colors.background)
                                    .padding(8.dp)
                            ) {
                                Text(
                                    text = "Reviewer: $comments",
                                    style = AppTheme.typography.bodySmall.copy(
                                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                        color = AppTheme.colors.muted
                                    )
                                )
                            }
                        }

                        if (isAccepted) {
                            Spacer(modifier = Modifier.height(14.dp))
                            SsplButton(
                                text = "Upload Presentation Slides",
                                onClick = { navController.navigate(Screen.PresentationsScreen.route) },
                                variant = SsplButtonVariant.Primary,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

