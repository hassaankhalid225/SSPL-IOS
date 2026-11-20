package com.sspl.ui.portal

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.sspl.Screen
import com.sspl.core.models.UserCertificate
import com.sspl.designsystem.components.ChipRow
import com.sspl.designsystem.components.EmptyState
import com.sspl.designsystem.components.ErrorState
import com.sspl.designsystem.components.IconTile
import com.sspl.designsystem.components.ListRowCard
import com.sspl.designsystem.components.LoadState
import com.sspl.designsystem.components.LocalShellActions
import com.sspl.designsystem.components.NavTile
import com.sspl.designsystem.components.OutlineButton
import com.sspl.designsystem.components.SecondaryButton
import com.sspl.designsystem.components.SkeletonList
import com.sspl.designsystem.components.SsplRefreshBox
import com.sspl.designsystem.components.StatusBadge
import com.sspl.designsystem.components.BadgeVariant
import com.sspl.designsystem.theme.AppTheme
import com.sspl.platform.shared.UrlOpener
import com.sspl.utils.DisplayDates
import kotlinx.coroutines.delay
import org.koin.compose.koinInject

/**
 * D9: My certificates (GET /certificates/my). Download, copy serial, verify.
 * Certificates unlock after accepted attendance + feedback in the Event portal.
 */
@Composable
fun MyCertificatesScreen(
    navController: NavController,
    viewModel: MyCertificatesViewModel = portalViewModel { MyCertificatesViewModel(it.get()) },
    urlOpener: UrlOpener = koinInject()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val shell = LocalShellActions.current
    val clipboard = LocalClipboardManager.current
    if (shell.isGuest) {
        PortalGuestGate(onSignIn = shell.onAvatar, modifier = Modifier.fillMaxSize())
        return
    }
    LaunchedEffect(state.copiedSerial) {
        if (state.copiedSerial != null) {
            delay(2500)
            viewModel.dismissCopied()
        }
    }

    SsplRefreshBox(
        isRefreshing = state.isRefreshing,
        onRefresh = viewModel::refresh,
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val all = (state.certificates as? LoadState.Ready)?.data
            item(key = "hero") {
                PortalHero(
                    eyebrow = "Credentials",
                    title = "My certificates",
                    summary = "Official CME, conference participation and fellowship certificates issued by SSPL.",
                    icon = Icons.Default.WorkspacePremium,
                    stats = all?.let { listOf("${it.size}" to "Issued to you") }.orEmpty(),
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
            state.copiedSerial?.let { s ->
                item(key = "copied") {
                    PortalNotice("Serial $s copied to clipboard.", NoticeTone.Success, modifier = Modifier.padding(horizontal = 16.dp))
                }
            }
            when (val l = state.certificates) {
                is LoadState.Loading -> item(key = "sk") { SkeletonList(count = 4) }
                is LoadState.Error -> item(key = "err") {
                    ErrorState(title = "Certificates didn't load", message = l.message, onRetry = viewModel::retry)
                }
                is LoadState.Ready -> if (l.data.isEmpty()) {
                    item(key = "empty") {
                        EmptyState(
                            title = "No certificates yet",
                            message = "Certificates unlock after your attendance is accepted and you submit session feedback in the Event portal.",
                            icon = Icons.Default.WorkspacePremium,
                            actionText = "Open event portal",
                            onActionClick = { navController.openPortal(Screen.EventPortalScreen.route) }
                        )
                    }
                } else {
                    if (state.filters.size > 1) {
                        item(key = "chips") {
                            ChipRow(options = state.filters, selected = state.filter, onSelect = viewModel::setFilter)
                        }
                    }
                    items(state.visible, key = { it.id ?: it.serialNumber ?: it.hashCode() }) { cert ->
                        CertificateCard(
                            cert = cert,
                            onDownload = { resolvePortalFileUrl(cert.bestUrl)?.let(urlOpener::openUrl) },
                            onCopy = { serial ->
                                clipboard.setText(AnnotatedString(serial))
                                viewModel.onCopied(serial)
                            },
                            onVerify = { serial ->
                                navController.currentBackStackEntry?.savedStateHandle?.set("certSerial", serial)
                                navController.openPortal(Screen.VerifyCertificateScreen.route)
                            },
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }
            }
            item(key = "portal") {
                NavTile(
                    icon = Icons.Default.EventAvailable,
                    title = "Missing a certificate?",
                    subtitle = "Check attendance and feedback in the Event portal",
                    onClick = { navController.openPortal(Screen.EventPortalScreen.route) },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun CertificateCard(
    cert: UserCertificate,
    onDownload: () -> Unit,
    onCopy: (String) -> Unit,
    onVerify: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    val c = AppTheme.colors
    val hasFile = !cert.bestUrl.isNullOrBlank()
    ListRowCard(
        title = cert.title ?: "Certificate of participation",
        subtitle = cert.eventTitle,
        meta = DisplayDates.medium(cert.issueDate).takeIf { it.isNotBlank() }?.let { "Issued $it" },
        leading = { IconTile(Icons.Default.WorkspacePremium, tint = c.success, background = c.successContainer) },
        trailing = { StatusBadge(cert.typeLabel(), variant = BadgeVariant.INFO) },
        modifier = modifier,
        footer = {
            cert.serialNumber?.let { serial ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clip(AppTheme.shapes.small)
                        .background(c.boxColor)
                        .clickable { onCopy(serial) }
                        .padding(start = 12.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("SERIAL", style = AppTheme.typography.labelSmall)
                    Spacer(Modifier.width(10.dp))
                    Text(
                        serial,
                        style = AppTheme.typography.bodyMedium.copy(color = c.heading, fontWeight = FontWeight.SemiBold),
                        modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis
                    )
                    Box(Modifier.size(40.dp).clip(CircleShape), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy serial number", tint = c.primary, modifier = Modifier.size(18.dp))
                    }
                }
                Spacer(Modifier.padding(top = 10.dp))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                if (hasFile) {
                    SecondaryButton("Download", onClick = onDownload, icon = Icons.Default.Download, modifier = Modifier.weight(1f))
                }
                OutlineButton("Verify", onClick = { onVerify(cert.serialNumber) }, icon = Icons.Default.Verified, modifier = Modifier.weight(1f))
            }
        }
    )
}
