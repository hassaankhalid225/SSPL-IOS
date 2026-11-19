package com.sspl.ui.portal

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
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
import com.sspl.core.models.Presentation
import com.sspl.core.repositories.PortalRepository
import com.sspl.designsystem.components.SsplBadge
import com.sspl.designsystem.components.SsplBadgeVariant
import com.sspl.designsystem.components.SsplButton
import com.sspl.designsystem.components.SsplButtonVariant
import com.sspl.designsystem.components.SsplCard
import com.sspl.designsystem.components.SsplTextField
import com.sspl.designsystem.theme.AppTheme
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

/**
 * D8: Presentation Deck Upload & List Screen
 */
@Composable
fun PresentationsScreen(
    navController: NavController,
    portalRepository: PortalRepository = koinInject()
) {
    val scope = rememberCoroutineScope()
    var presentations by remember { mutableStateOf<List<Presentation>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var newTitle by remember { mutableStateOf("") }
    var actionMessage by remember { mutableStateOf<String?>(null) }
    var isSubmitting by remember { mutableStateOf(false) }

    val loadPresentations = {
        scope.launch {
            isLoading = true
            portalRepository.getMyPresentations().collect { list ->
                presentations = list
                isLoading = false
            }
        }
    }

    LaunchedEffect(Unit) {
        loadPresentations()
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
            Text(
                text = "SCIENTIFIC PRESENTATIONS",
                style = AppTheme.typography.labelSmall.copy(
                    color = AppTheme.colors.secondary,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Presentation Uploads",
                style = AppTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = AppTheme.colors.heading
                )
            )
            Text(
                text = "Upload slide decks for accepted abstracts to be queued for the AV hall projection.",
                style = AppTheme.typography.bodySmall.copy(color = AppTheme.colors.muted)
            )
        }

        // Upload Presentation Notice Card
        item {
            SsplCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CloudUpload,
                            contentDescription = null,
                            tint = AppTheme.colors.secondary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "AV Hall Deck Guidelines",
                            style = AppTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = AppTheme.colors.heading
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "• Slides must be in 16:9 widescreen format.\n• Supported formats: PowerPoint (.pptx, .ppt) or PDF.\n• Please ensure videos/audio are embedded.\n• Decks should be uploaded at least 4 hours before the session.",
                        style = AppTheme.typography.bodySmall.copy(
                            color = AppTheme.colors.body,
                            lineHeight = 20.sp
                        )
                    )
                }
            }
        }

        item {
            Text(
                text = "Uploaded Presentations",
                style = AppTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = AppTheme.colors.heading
                )
            )
        }

        if (isLoading) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = AppTheme.colors.secondary)
                }
            }
        } else if (presentations.isEmpty()) {
            item {
                SsplCard(modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No presentation decks uploaded yet.",
                            style = AppTheme.typography.bodyMedium.copy(color = AppTheme.colors.muted)
                        )
                    }
                }
            }
        } else {
            items(presentations) { item ->
                SsplCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = item.title,
                                style = AppTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = AppTheme.colors.heading
                                ),
                                modifier = Modifier.weight(1f)
                            )
                            SsplBadge(
                                text = item.status ?: "UPLOADED",
                                variant = SsplBadgeVariant.Success
                            )
                        }

                        item.createdAt?.let { dt ->
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Uploaded: ${dt.split("T").firstOrNull() ?: dt}",
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

