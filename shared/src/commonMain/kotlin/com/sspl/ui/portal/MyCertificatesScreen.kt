package com.sspl.ui.portal

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.WorkspacePremium
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.sspl.Screen
import com.sspl.core.models.UserCertificate
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
 * D9: My Certificates Screen
 */
@Composable
fun MyCertificatesScreen(
    navController: NavController,
    portalRepository: PortalRepository = koinInject()
) {
    val scope = rememberCoroutineScope()
    val clipboardManager = LocalClipboardManager.current
    var certificates by remember { mutableStateOf<List<UserCertificate>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var copiedSerial by remember { mutableStateOf<String?>(null) }

    val loadCertificates = {
        scope.launch {
            isLoading = true
            portalRepository.getMyCertificates().collect { list ->
                certificates = list
                isLoading = false
            }
        }
    }

    LaunchedEffect(Unit) {
        loadCertificates()
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
                        text = "CREDENTIALS",
                        style = AppTheme.typography.labelSmall.copy(
                            color = AppTheme.colors.secondary,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "My Certificates",
                        style = AppTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = AppTheme.colors.heading
                        )
                    )
                }

                IconButton(onClick = { loadCertificates() }) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh",
                        tint = AppTheme.colors.secondary
                    )
                }
            }
            Text(
                text = "Official CME, conference participation, and fellowship certificates.",
                style = AppTheme.typography.bodySmall.copy(color = AppTheme.colors.muted)
            )
        }

        if (copiedSerial != null) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(AppTheme.colors.accent.copy(alpha = 0.15f))
                        .padding(12.dp)
                ) {
                    Text(
                        text = "Serial $copiedSerial copied to clipboard!",
                        style = AppTheme.typography.bodySmall.copy(
                            color = AppTheme.colors.secondary,
                            fontWeight = FontWeight.Medium
                        )
                    )
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
        } else if (certificates.isEmpty()) {
            item {
                SsplCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.WorkspacePremium,
                            contentDescription = null,
                            tint = AppTheme.colors.muted.copy(alpha = 0.5f),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No certificates issued yet.",
                            style = AppTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = AppTheme.colors.heading
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Certificates unlock after verified attendance and feedback submission at conferences.",
                            style = AppTheme.typography.bodySmall.copy(color = AppTheme.colors.muted),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        SsplButton(
                            text = "Verify an Existing Certificate",
                            onClick = { navController.navigate(Screen.VerifyCertificateScreen.route) },
                            variant = SsplButtonVariant.Outline
                        )
                    }
                }
            }
        } else {
            items(certificates) { cert ->
                SsplCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = cert.title ?: "Certificate of Attendance",
                                style = AppTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = AppTheme.colors.heading
                                ),
                                modifier = Modifier.weight(1f)
                            )
                            SsplBadge(
                                text = cert.type ?: "CONFERENCE",
                                variant = SsplBadgeVariant.Success
                            )
                        }

                        cert.eventTitle?.let { evt ->
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = evt,
                                style = AppTheme.typography.bodySmall.copy(color = AppTheme.colors.secondary)
                            )
                        }

                        cert.serialNumber?.let { serial ->
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "Serial: $serial",
                                    style = AppTheme.typography.labelSmall.copy(
                                        color = AppTheme.colors.muted,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                )
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copy Serial",
                                    tint = AppTheme.colors.secondary,
                                    modifier = Modifier
                                        .size(14.dp)
                                        .clickable {
                                            clipboardManager.setText(AnnotatedString(serial))
                                            copiedSerial = serial
                                        }
                                )
                            }
                        }

                        cert.issueDate?.let { date ->
                            Text(
                                text = "Issued: $date",
                                style = AppTheme.typography.bodySmall.copy(color = AppTheme.colors.muted)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            SsplButton(
                                text = "Verify Authenticity",
                                onClick = {
                                    navController.navigate(Screen.VerifyCertificateScreen.route)
                                },
                                variant = SsplButtonVariant.Outline,
                                modifier = Modifier.weight(1f)
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

