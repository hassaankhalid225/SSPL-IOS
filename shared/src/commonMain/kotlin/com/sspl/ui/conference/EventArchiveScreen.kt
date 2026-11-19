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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.PresentToAll
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.sspl.core.models.ArchiveMaterial
import com.sspl.core.models.EventArchiveRecord
import com.sspl.designsystem.components.BadgeVariant
import com.sspl.designsystem.components.SsplBadge
import com.sspl.designsystem.components.SsplCard
import com.sspl.designsystem.tokens.SsplColorTokens
import com.sspl.designsystem.tokens.SsplSpacingTokens
import com.sspl.designsystem.tokens.SsplTypographyTokens
import com.sspl.platform.shared.UrlOpener
import com.sspl.utils.BASE_URL
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun EventArchiveScreen(
    navController: NavController,
    viewModel: EventArchiveViewModel = koinViewModel(),
    urlOpener: UrlOpener = koinInject()
) {
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
                .background(SsplColorTokens.surface)
                .padding(horizontal = SsplSpacingTokens.sm, vertical = SsplSpacingTokens.xs),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = SsplColorTokens.textPrimary
                )
            }
            Text(
                text = "Event Archive",
                style = SsplTypographyTokens.headlineSmall.copy(fontSize = 18.sp),
                color = SsplColorTokens.textPrimary
            )
        }

        // Search Bar
        Surface(
            color = SsplColorTokens.surface,
            shadowElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = viewModel::setSearchQuery,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(SsplSpacingTokens.md),
                placeholder = {
                    Text(
                        "Search archives, presentations, documents...",
                        style = SsplTypographyTokens.bodySmall.copy(color = SsplColorTokens.muted)
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = SsplColorTokens.primary
                    )
                },
                trailingIcon = {
                    if (uiState.searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.setSearchQuery("") }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear",
                                tint = SsplColorTokens.muted
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = SsplColorTokens.primary,
                    unfocusedBorderColor = SsplColorTokens.divider,
                    focusedContainerColor = SsplColorTokens.surface,
                    unfocusedContainerColor = SsplColorTokens.surface
                )
            )
        }

        // Content
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = SsplColorTokens.primary)
            }
        } else if (uiState.filteredArchives.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(SsplSpacingTokens.xl),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.FolderZip,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = SsplColorTokens.muted
                    )
                    Spacer(modifier = Modifier.height(SsplSpacingTokens.md))
                    Text(
                        text = "No Archives Found",
                        style = SsplTypographyTokens.headlineSmall,
                        color = SsplColorTokens.textPrimary
                    )
                    Spacer(modifier = Modifier.height(SsplSpacingTokens.xs))
                    Text(
                        text = if (uiState.searchQuery.isNotBlank()) {
                            "No materials matched \"${uiState.searchQuery}\"."
                        } else {
                            "There are currently no public event archives available."
                        },
                        style = SsplTypographyTokens.bodySmall.copy(color = SsplColorTokens.textSecondary),
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(SsplSpacingTokens.md),
                verticalArrangement = Arrangement.spacedBy(SsplSpacingTokens.md)
            ) {
                items(uiState.filteredArchives, key = { it.id }) { archive ->
                    ArchiveEventCard(
                        archive = archive,
                        onDownloadMaterial = { material ->
                            val url = buildFileUrl(material.fileUrl)
                            if (url != null) {
                                urlOpener.openUrl(url)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ArchiveEventCard(
    archive: EventArchiveRecord,
    onDownloadMaterial: (ArchiveMaterial) -> Unit
) {
    SsplCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(SsplSpacingTokens.md)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = archive.conference?.title ?: "Past Event",
                        style = SsplTypographyTokens.headlineSmall.copy(fontSize = 17.sp),
                        color = SsplColorTokens.textPrimary
                    )
                    if (!archive.conference?.startDate.isNullOrBlank()) {
                        Text(
                            text = archive.conference!!.startDate!!.split("T").first(),
                            style = SsplTypographyTokens.caption.copy(color = SsplColorTokens.muted)
                        )
                    }
                }
                SsplBadge(
                    text = archive.conference?.type?.replace("_", " ") ?: "EVENT",
                    variant = BadgeVariant.PRIMARY
                )
            }

            if (!archive.notes.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(SsplSpacingTokens.sm))
                Text(
                    text = archive.notes,
                    style = SsplTypographyTokens.bodySmall.copy(color = SsplColorTokens.textSecondary)
                )
            }

            Spacer(modifier = Modifier.height(SsplSpacingTokens.md))
            HorizontalDivider(color = SsplColorTokens.divider)
            Spacer(modifier = Modifier.height(SsplSpacingTokens.sm))

            // Materials list
            if (archive.materials.isEmpty()) {
                Text(
                    text = "No downloadable materials listed for this event.",
                    style = SsplTypographyTokens.caption.copy(color = SsplColorTokens.muted)
                )
            } else {
                Text(
                    text = "Archived Materials (${archive.materials.size})",
                    style = SsplTypographyTokens.bodySmall.copy(fontWeight = FontWeight.Bold),
                    color = SsplColorTokens.textPrimary
                )
                Spacer(modifier = Modifier.height(SsplSpacingTokens.xs))

                archive.materials.forEach { material ->
                    MaterialRowItem(
                        material = material,
                        onClick = { onDownloadMaterial(material) }
                    )
                }
            }
        }
    }
}

@Composable
private fun MaterialRowItem(
    material: ArchiveMaterial,
    onClick: () -> Unit
) {
    val icon = when (material.type?.uppercase()) {
        "IMAGE", "POSTER" -> Icons.Default.Image
        "PRESENTATION" -> Icons.Default.PresentToAll
        "ABSTRACT", "ABSTRACT_BOOK" -> Icons.Default.PictureAsPdf
        else -> Icons.Default.Description
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(SsplSpacingTokens.sm)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(SsplColorTokens.primary.copy(alpha = 0.08f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = SsplColorTokens.primary,
                modifier = Modifier.size(18.dp)
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = material.title ?: "Document",
                style = SsplTypographyTokens.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = SsplColorTokens.textPrimary
            )
            Text(
                text = material.type ?: "FILE",
                style = SsplTypographyTokens.caption.copy(color = SsplColorTokens.muted)
            )
        }

        IconButton(onClick = onClick) {
            Icon(
                imageVector = Icons.Default.Download,
                contentDescription = "Download",
                tint = SsplColorTokens.primary
            )
        }
    }
}

private fun buildFileUrl(path: String?): String? {
    if (path.isNullOrBlank()) return null
    if (path.startsWith("http")) return path
    return "${BASE_URL.trimEnd('/')}/${path.trimStart('/')}"
}

