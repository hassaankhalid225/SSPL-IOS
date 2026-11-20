package com.sspl.ui.certificate

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.sspl.core.repositories.PublicRepository
import com.sspl.designsystem.components.ErrorState
import com.sspl.designsystem.components.FactRow
import com.sspl.designsystem.components.IconTile
import com.sspl.designsystem.components.ImageSources
import com.sspl.designsystem.components.PrimaryButton
import com.sspl.designsystem.components.SkeletonLine
import com.sspl.designsystem.components.SsplTextField
import com.sspl.designsystem.components.StatusBadge
import com.sspl.designsystem.components.BadgeVariant
import com.sspl.designsystem.theme.AppTheme
import com.sspl.platform.shared.UrlOpener
import com.sspl.ui.components.InfoSurface
import com.sspl.ui.components.PageIntroHero
import com.sspl.utils.DisplayDates
import org.koin.compose.koinInject

@Composable
fun VerifyCertificateScreen(
    navController: NavController,
    initialSerial: String? = null,
    publicRepository: PublicRepository = koinInject(),
    urlOpener: UrlOpener = koinInject()
) {
    val viewModel: VerifyCertificateViewModel = viewModel { VerifyCertificateViewModel(publicRepository, initialSerial) }
    val state by viewModel.state.collectAsStateWithLifecycle()
    val focus = LocalFocusManager.current
    val loading = state.result is VerifyResult.Loading
    fun submit() {
        focus.clearFocus()
        viewModel.verify()
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(AppTheme.colors.background).imePadding(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item(key = "hero") {
            PageIntroHero(
                eyebrow = "Authenticity check",
                eyebrowIcon = Icons.Default.VerifiedUser,
                title = "Verify a certificate",
                summary = "Enter the serial number printed on an SSPL certificate to confirm it was officially issued."
            )
        }
        item(key = "input") {
            InfoSurface {
                SsplTextField(
                    value = state.serial,
                    onValueChange = viewModel::onSerial,
                    label = "Serial number",
                    placeholder = "e.g. CERT-2026-00125",
                    leadingIcon = Icons.Default.Tag,
                    error = state.inputError,
                    enabled = !loading,
                    keyboardActions = KeyboardActions(onDone = { submit() }, onSearch = { submit() }, onGo = { submit() })
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    "Fellowship certificates start with FELLOW-.",
                    style = AppTheme.typography.bodySmall
                )
                Spacer(Modifier.height(14.dp))
                PrimaryButton(
                    text = "Verify now",
                    onClick = ::submit,
                    loading = loading,
                    enabled = !loading && state.serial.isNotBlank(),
                    icon = Icons.Default.Search,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        item(key = "result") {
            AnimatedContent(
                targetState = state.result,
                transitionSpec = { fadeIn(tween(220)) togetherWith fadeOut(tween(160)) },
                contentKey = { it::class },
                label = "verifyResult"
            ) { result ->
                when (result) {
                    VerifyResult.Idle -> HowItWorks()
                    VerifyResult.Loading -> ResultSkeleton()
                    is VerifyResult.Failed -> ErrorState(
                        title = "Couldn't verify right now",
                        message = result.message,
                        onRetry = viewModel::verify
                    )
                    is VerifyResult.Invalid -> StatusPanel(
                        icon = Icons.Default.Cancel,
                        tint = AppTheme.colors.error,
                        background = AppTheme.colors.errorContainer,
                        title = "Verification failed",
                        message = result.message
                    )
                    is VerifyResult.Found -> FoundResult(result.certificate, onDownload = { url -> urlOpener.openUrl(url) })
                }
            }
        }
    }
}

@Composable
private fun StatusPanel(icon: ImageVector, tint: Color, background: Color, title: String, message: String) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(AppTheme.shapes.card)
            .background(background)
            .border(1.dp, tint.copy(alpha = 0.25f), AppTheme.shapes.card)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconTile(icon, tint = tint, background = AppTheme.colors.surface, size = 46.dp, iconSize = 24.dp)
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(title, style = AppTheme.typography.titleMedium.copy(color = tint))
            Text(message, style = AppTheme.typography.bodySmall.copy(color = AppTheme.colors.body))
        }
    }
}

@Composable
private fun FoundResult(c: VerifiedCertificate, onDownload: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (c.isRevoked) {
            StatusPanel(
                Icons.Default.Cancel, AppTheme.colors.error, AppTheme.colors.errorContainer,
                "Certificate revoked",
                "This certificate was revoked by the issuing authority and is no longer valid."
            )
        } else {
            StatusPanel(
                Icons.Default.Verified, AppTheme.colors.success, AppTheme.colors.successContainer,
                "Verified authenticity",
                "This certificate is valid and was officially issued by SSPL."
            )
        }
        InfoSurface {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Certificate details", style = AppTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                StatusBadge(
                    if (c.isRevoked) "Revoked" else "Valid",
                    variant = if (c.isRevoked) BadgeVariant.ERROR else BadgeVariant.SUCCESS
                )
            }
            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = AppTheme.colors.divider)
            Spacer(Modifier.height(12.dp))
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                FactRow(Icons.Default.Person, "Issued to", c.issuedTo)
                FactRow(Icons.Default.EventNote, "Session / event", c.eventName)
                c.trainingCenter?.let { FactRow(Icons.Default.LocationCity, "Training centre", it) }
                FactRow(Icons.Default.Badge, "Serial number", c.serial)
                FactRow(
                    Icons.Default.CalendarMonth, "Date of issue",
                    DisplayDates.medium(c.issuedAt).ifBlank { "N/A" }
                )
                FactRow(Icons.Default.Category, "Type", c.type.replace('_', ' ').lowercase().replaceFirstChar { it.uppercase() })
            }
            val url = c.fileUrl?.let { ImageSources.resolveUrl(it) }
            if (url != null && !c.isRevoked) {
                Spacer(Modifier.height(16.dp))
                PrimaryButton(
                    text = "Download official PDF",
                    onClick = { onDownload(url) },
                    icon = Icons.Default.Download,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun ResultSkeleton() {
    InfoSurface {
        repeat(4) {
            SkeletonLine(widthFraction = 0.3f, height = 10.dp)
            Spacer(Modifier.height(6.dp))
            SkeletonLine(widthFraction = 0.75f)
            Spacer(Modifier.height(14.dp))
        }
    }
}

@Composable
private fun HowItWorks() {
    InfoSurface {
        Text("WHERE TO FIND IT", style = AppTheme.typography.overline)
        Spacer(Modifier.height(8.dp))
        Text(
            "The serial number is printed on every SSPL certificate (conference, workshop, CME and fellowship). Employers and councils can use this check to confirm authenticity.",
            style = AppTheme.typography.bodyMedium.copy(color = AppTheme.colors.body)
        )
    }
}
