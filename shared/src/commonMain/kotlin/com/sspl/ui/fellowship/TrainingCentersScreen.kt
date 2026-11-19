package com.sspl.ui.fellowship

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
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
import com.sspl.core.models.TrainingCenter
import com.sspl.core.repositories.FellowshipRepository
import com.sspl.designsystem.components.SsplBadge
import com.sspl.designsystem.components.SsplBadgeVariant
import com.sspl.designsystem.components.SsplCard
import com.sspl.designsystem.theme.AppTheme
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

/**
 * E2: Training Centres Directory
 */
@Composable
fun TrainingCentersScreen(
    navController: NavController,
    fellowshipRepository: FellowshipRepository = koinInject()
) {
    val scope = rememberCoroutineScope()
    var centers by remember { mutableStateOf<List<TrainingCenter>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var selectedCity by remember { mutableStateOf("ALL") }

    val cities = listOf("ALL", "Karachi", "Hyderabad", "Sukkur", "Larkana")

    val loadCenters = {
        scope.launch {
            isLoading = true
            fellowshipRepository.getTrainingCenters(if (selectedCity == "ALL") null else selectedCity)
                .collect { list ->
                    centers = list
                    isLoading = false
                }
        }
    }

    LaunchedEffect(selectedCity) {
        loadCenters()
    }

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
                        text = "ACCREDITATION",
                        style = AppTheme.typography.labelSmall.copy(
                            color = AppTheme.colors.secondary,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Training Centres",
                        style = AppTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = AppTheme.colors.heading
                        )
                    )
                }

                IconButton(onClick = { loadCenters() }) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh",
                        tint = AppTheme.colors.secondary
                    )
                }
            }
            Text(
                text = "Approved teaching hospitals and clinical units affiliated for postgraduate fellowships.",
                style = AppTheme.typography.bodySmall.copy(color = AppTheme.colors.muted)
            )
        }

        // City Filters
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(cities) { city ->
                    val isSelected = selectedCity == city
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) AppTheme.colors.primary else AppTheme.colors.surface)
                            .border(
                                1.dp,
                                if (isSelected) AppTheme.colors.primary else AppTheme.colors.border,
                                RoundedCornerShape(8.dp)
                            )
                            .clickable { selectedCity = city }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = if (city == "ALL") "All Cities" else city,
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
        } else if (centers.isEmpty()) {
            item {
                SsplCard(modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No training centres found for $selectedCity.",
                            style = AppTheme.typography.bodyMedium.copy(color = AppTheme.colors.muted)
                        )
                    }
                }
            }
        } else {
            items(centers) { center ->
                SsplCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = center.name,
                                style = AppTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = AppTheme.colors.heading
                                ),
                                modifier = Modifier.weight(1f)
                            )
                            SsplBadge(
                                text = center.status ?: "ACCREDITED",
                                variant = SsplBadgeVariant.Success
                            )
                        }

                        center.hospital?.let { hosp ->
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = hosp,
                                style = AppTheme.typography.bodySmall.copy(color = AppTheme.colors.body)
                            )
                        }

                        center.city?.let { city ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(top = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = AppTheme.colors.muted,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = city,
                                    style = AppTheme.typography.bodySmall.copy(color = AppTheme.colors.muted)
                                )
                            }
                        }

                        center.supervisorName?.let { sup ->
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = AppTheme.colors.secondary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Supervisor: $sup",
                                    style = AppTheme.typography.bodySmall.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        color = AppTheme.colors.secondary
                                    )
                                )
                            }
                        }

                        if (center.slots != null) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Training Capacity: ${center.slots} fellows / year",
                                style = AppTheme.typography.bodySmall.copy(color = AppTheme.colors.muted)
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

