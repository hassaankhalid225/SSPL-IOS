package com.sspl.ui.downloads

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
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
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
import com.sspl.Screen
import com.sspl.core.models.DownloadItem
import com.sspl.core.repositories.PublicRepository
import com.sspl.designsystem.components.BadgeVariant
import com.sspl.designsystem.components.ModernCapsuleSearchBar
import com.sspl.designsystem.components.ModernSupportCard
import com.sspl.designsystem.components.SecondaryButton
import com.sspl.designsystem.components.SsplBadge
import com.sspl.designsystem.components.SsplTextField
import com.sspl.designsystem.theme.AppTheme
import com.sspl.platform.shared.UrlOpener
import com.sspl.utils.BASE_URL
import org.koin.compose.koinInject

data class DownloadCategory(
    val label: String,
    val value: String
)

private val DOWNLOAD_CATEGORIES = listOf(
    DownloadCategory("All", ""),
    DownloadCategory("Case Presentation", "CASE_PRESENTATION"),
    DownloadCategory("MDT Presentation", "MDT_PRESENTATION"),
    DownloadCategory("Research", "RESEARCH_PRESENTATION"),
    DownloadCategory("Abstract", "ABSTRACT"),
    DownloadCategory("E-Poster", "E_POSTER"),
    DownloadCategory("Speaker Profile", "SPEAKER_PROFILE"),
    DownloadCategory("Logbooks", "LOGBOOK"),
    DownloadCategory("Guidelines", "GUIDELINES_BROCHURES"),
    DownloadCategory("Documents", "DOCUMENTS"),
    DownloadCategory("Others", "OTHERS")
)

@Composable
fun DownloadsScreen(
    navController: NavController,
    publicRepository: PublicRepository = koinInject(),
    urlOpener: UrlOpener = koinInject()
) {
    var selectedCategory by remember { mutableStateOf("") }
    var searchQuery by remember { mutableStateOf("") }
    var items by remember { mutableStateOf<List<DownloadItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(selectedCategory, searchQuery) {
        isLoading = true
        publicRepository.getDownloads(category = selectedCategory, search = searchQuery).collect { resp ->
            items = resp.items
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
                        text = "RESOURCES & TEMPLATES",
                        style = AppTheme.typography.labelSmall,
                        color = AppTheme.colors.secondary,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.3.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Downloads & Documents",
                        style = AppTheme.typography.headlineMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Official presentation templates, logbooks, surgical case guidelines, and abstract formats.",
                        style = AppTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.88f)
                    )
                }
            }
        }

        // Modern Capsule Search Bar
        item {
            ModernCapsuleSearchBar(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                placeholder = "Search templates, forms, logbooks...",
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Category Horizontal Chips (Modern Capsule Pills)
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 2.dp)
            ) {
                items(DOWNLOAD_CATEGORIES) { cat ->
                    val isSelected = selectedCategory == cat.value
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50.dp))
                            .background(if (isSelected) AppTheme.colors.primaryDark else AppTheme.colors.boxColor)
                            .clickable { selectedCategory = cat.value }
                            .padding(horizontal = 16.dp, vertical = 9.dp)
                    ) {
                        Text(
                            text = cat.label,
                            style = AppTheme.typography.labelSmall,
                            color = if (isSelected) Color.White else AppTheme.colors.heading,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }
        }

        // Items List
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
        } else if (items.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .background(AppTheme.colors.border.copy(alpha = 0.5f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.FolderOpen,
                                contentDescription = null,
                                tint = AppTheme.colors.primary,
                                modifier = Modifier.size(30.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "No files found",
                            style = AppTheme.typography.titleMedium,
                            color = AppTheme.colors.onSurface,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Try adjusting your search query or category filter.",
                            style = AppTheme.typography.bodySmall,
                            color = AppTheme.colors.textMuted
                        )
                    }
                }
            }
        } else {
            items(items, key = { it.id }) { file ->
                DownloadItemCard(
                    file = file,
                    onDownload = {
                        val downloadUrl = "$BASE_URL/downloads/${file.id}/file?download=1"
                        urlOpener.openUrl(downloadUrl)
                    }
                )
            }

            item {
                ModernSupportCard(
                    title = "Need Custom Templates?",
                    subtitle = "Official abstract, poster, and presentation slide templates can also be requested from academic committee.",
                    buttonText = "Contact Committee",
                    onContactClick = { navController.navigate(Screen.ContactUsScreen.route) }
                )
            }
        }
    }
}

@Composable
private fun DownloadItemCard(
    file: DownloadItem,
    onDownload: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = AppTheme.colors.surface),
        border = BorderStroke(1.dp, AppTheme.colors.border)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(AppTheme.colors.primary.copy(alpha = 0.1f), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Description,
                        contentDescription = null,
                        tint = AppTheme.colors.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    if (!file.category.isNullOrBlank()) {
                        SsplBadge(
                            text = file.category.replace("_", " "),
                            variant = BadgeVariant.PRIMARY
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                    Text(
                        text = file.title,
                        style = AppTheme.typography.titleMedium,
                        color = AppTheme.colors.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                    if (!file.description.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = file.description,
                            style = AppTheme.typography.bodySmall,
                            color = AppTheme.colors.textMuted,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val sizeText = file.fileSize?.let { formatFileSize(it) } ?: ""
                Text(
                    text = sizeText,
                    style = AppTheme.typography.bodySmall,
                    color = AppTheme.colors.textMuted
                )
                SecondaryButton(
                    text = "Download File",
                    icon = Icons.Default.Download,
                    onClick = onDownload
                )
            }
        }
    }
}

private fun formatFileSize(bytes: Long): String {
    if (bytes <= 0) return ""
    return if (bytes < 1024 * 1024) {
        "${bytes / 1024} KB"
    } else {
        "${bytes / (1024 * 1024)} MB"
    }
}

