package com.sspl.ui.members

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.sspl.core.models.DirectoryMemberRecord
import com.sspl.core.models.DirectoryStatistics
import com.sspl.core.repositories.PublicRepository
import com.sspl.designsystem.components.AvatarImage
import com.sspl.designsystem.components.BadgeVariant
import com.sspl.designsystem.components.SsplBadge
import com.sspl.designsystem.components.SsplTextField
import com.sspl.designsystem.components.StatCard
import com.sspl.designsystem.theme.AppTheme
import com.sspl.utils.BASE_URL
import org.koin.compose.koinInject

data class MemberFilterOption(
    val label: String,
    val value: String?
)

private val MEMBER_FILTERS = listOf(
    MemberFilterOption("All Members", null),
    MemberFilterOption("Life Members", "LIFE_MEMBER"),
    MemberFilterOption("Regular Members", "REGULAR_MEMBER"),
    MemberFilterOption("Deceased Members", "DECEASED_MEMBER")
)

@Composable
fun MembersDirectoryScreen(
    navController: NavController,
    publicRepository: PublicRepository = koinInject()
) {
    var selectedRole by remember { mutableStateOf<String?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var currentPage by remember { mutableStateOf(1) }
    var totalPages by remember { mutableStateOf(1) }
    var statistics by remember { mutableStateOf(DirectoryStatistics()) }
    var members by remember { mutableStateOf<List<DirectoryMemberRecord>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    fun loadMembers() {
        isLoading = true
        publicRepository.getMembersDirectory(
            page = currentPage,
            role = selectedRole,
            search = searchQuery
        ).let { flow ->
            // In a composable coroutine scope or LaunchedEffect
        }
    }

    LaunchedEffect(selectedRole, searchQuery, currentPage) {
        isLoading = true
        publicRepository.getMembersDirectory(
            page = currentPage,
            role = selectedRole,
            search = searchQuery
        ).collect { resp ->
            members = resp.records
            statistics = resp.statistics
            totalPages = resp.meta.totalPages.coerceAtLeast(1)
            isLoading = false
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(AppTheme.colors.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Hero Header
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = AppTheme.colors.primary)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "FELLOWSHIP & REGISTRATION",
                        style = AppTheme.typography.labelSmall,
                        color = AppTheme.colors.secondary,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.3.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Members Directory",
                        style = AppTheme.typography.headlineMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Verified roster of life and regular surgical members of SSP Lahore.",
                        style = AppTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.88f)
                    )
                }
            }
        }

        // Statistics Badges Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatCard(
                    value = "${statistics.lifeMembers}",
                    label = "Life",
                    description = "Members",
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    value = "${statistics.regularMembers}",
                    label = "Regular",
                    description = "Members",
                    modifier = Modifier.weight(1f)
                )
                if (statistics.deceasedMembers > 0) {
                    StatCard(
                        value = "${statistics.deceasedMembers}",
                        label = "In Memory",
                        description = "Deceased",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Search Input
        item {
            SsplTextField(
                value = searchQuery,
                onValueChange = {
                    searchQuery = it
                    currentPage = 1
                },
                label = "Search Directory",
                placeholder = "Search doctor name or PMDC number...",
                leadingIcon = Icons.Default.Search,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Role Filter Tabs
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 2.dp)
            ) {
                items(MEMBER_FILTERS) { opt ->
                    val isSelected = selectedRole == opt.value
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) AppTheme.colors.primary else AppTheme.colors.surface)
                            .clickable {
                                selectedRole = opt.value
                                currentPage = 1
                            }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = opt.label,
                            style = AppTheme.typography.labelSmall,
                            color = if (isSelected) Color.White else AppTheme.colors.onSurface,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }
        }

        // Members List
        if (isLoading) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = AppTheme.colors.primary)
                }
            }
        } else if (members.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Groups,
                            contentDescription = null,
                            tint = AppTheme.colors.textMuted,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No members found",
                            style = AppTheme.typography.titleSmall,
                            color = AppTheme.colors.onSurface
                        )
                    }
                }
            }
        } else {
            items(members) { record ->
                MemberDirectoryCard(record = record)
            }

            // Pagination Controls
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { if (currentPage > 1) currentPage-- },
                        enabled = currentPage > 1
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Previous Page",
                            tint = if (currentPage > 1) AppTheme.colors.primary else AppTheme.colors.textMuted
                        )
                    }
                    Text(
                        text = "Page $currentPage of $totalPages",
                        style = AppTheme.typography.bodyMedium,
                        color = AppTheme.colors.onSurface,
                        fontWeight = FontWeight.SemiBold
                    )
                    IconButton(
                        onClick = { if (currentPage < totalPages) currentPage++ },
                        enabled = currentPage < totalPages
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Next Page",
                            tint = if (currentPage < totalPages) AppTheme.colors.primary else AppTheme.colors.textMuted
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MemberDirectoryCard(record: DirectoryMemberRecord) {
    val user = record.account?.user
    val memNo = record.account?.memNo ?: "N/A"
    val isLife = record.role == "LIFE_MEMBER"
    val isDeceased = record.role == "DECEASED_MEMBER"

    val roleLabel = when {
        isDeceased -> "Deceased Member"
        isLife -> "Life Member"
        else -> "Regular Member"
    }

    val badgeVariant = when {
        isDeceased -> BadgeVariant.NEUTRAL
        isLife -> BadgeVariant.SUCCESS
        else -> BadgeVariant.PRIMARY
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = AppTheme.colors.surface),
        border = BorderStroke(1.dp, AppTheme.colors.border)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val photoUrl = user?.profilePicture?.let { pic ->
                if (pic.startsWith("http")) pic else "$BASE_URL/${pic.removePrefix("/")}"
            }
            AvatarImage(
                url = photoUrl,
                name = user?.fullName ?: "Member",
                size = 50.dp
            )
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = user?.fullName ?: "Unknown Member",
                        style = AppTheme.typography.titleSmall,
                        color = AppTheme.colors.onSurface,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    SsplBadge(
                        text = roleLabel,
                        variant = badgeVariant
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Membership No: $memNo",
                    style = AppTheme.typography.bodySmall,
                    color = AppTheme.colors.textMuted,
                    fontWeight = FontWeight.Medium
                )
                if (!user?.pmdcNumber.isNullOrBlank()) {
                    Text(
                        text = "PMDC: ${user.pmdcNumber}",
                        style = AppTheme.typography.bodySmall,
                        color = AppTheme.colors.textMuted
                    )
                }
                if (!user?.city.isNullOrBlank()) {
                    Text(
                        text = user.city,
                        style = AppTheme.typography.bodySmall,
                        color = AppTheme.colors.primary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

