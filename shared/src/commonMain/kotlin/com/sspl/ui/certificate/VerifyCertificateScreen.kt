package com.sspl.ui.certificate

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Verified
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.sspl.core.models.CertificateVerifyResponse
import com.sspl.core.repositories.PublicRepository
import com.sspl.designsystem.components.BadgeVariant
import com.sspl.designsystem.components.FeedbackBanner
import com.sspl.designsystem.components.PrimaryButton
import com.sspl.designsystem.components.SsplBadge
import com.sspl.designsystem.components.SsplTextField
import com.sspl.designsystem.theme.AppTheme
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
fun VerifyCertificateScreen(
    navController: NavController,
    initialSerial: String? = null,
    publicRepository: PublicRepository = koinInject()
) {
    var serialInput by remember { mutableStateOf(initialSerial ?: "") }
    var isVerifying by remember { mutableStateOf(false) }
    var verificationResult by remember { mutableStateOf<CertificateVerifyResponse?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    fun runVerification(targetSerial: String) {
        if (targetSerial.isBlank()) return
        scope.launch {
            isVerifying = true
            errorMessage = null
            verificationResult = null
            publicRepository.verifyCertificate(targetSerial).collect { resp ->
                isVerifying = false
                if (resp.verified && resp.certificate != null) {
                    verificationResult = resp
                } else {
                    errorMessage = resp.message ?: "Certificate not found or invalid serial number."
                }
            }
        }
    }

    LaunchedEffect(initialSerial) {
        if (!initialSerial.isNullOrBlank()) {
            runVerification(initialSerial)
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(AppTheme.colors.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Header
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = AppTheme.colors.primary)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(Color.White.copy(alpha = 0.2f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "OFFICIAL VERIFICATION",
                            style = AppTheme.typography.labelSmall,
                            color = AppTheme.colors.secondary,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.3.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Verify Certificate",
                        style = AppTheme.typography.headlineMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Validate the authenticity of SSP Lahore conference, CME, fellowship, and workshop certificates.",
                        style = AppTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.88f)
                    )
                }
            }
        }

        // Search Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = AppTheme.colors.surface),
                border = BorderStroke(1.dp, AppTheme.colors.border)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Enter Serial Number",
                        style = AppTheme.typography.titleSmall,
                        color = AppTheme.colors.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    SsplTextField(
                        value = serialInput,
                        onValueChange = { serialInput = it },
                        label = "Certificate Serial",
                        placeholder = "e.g. CERT-2026-00125 or FELLOW-001",
                        leadingIcon = Icons.Default.QrCodeScanner,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    PrimaryButton(
                        text = "Verify Certificate",
                        icon = Icons.Default.Search,
                        isLoading = isVerifying,
                        enabled = serialInput.isNotBlank(),
                        onClick = { runVerification(serialInput.trim()) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // Error message
        if (errorMessage != null) {
            item {
                FeedbackBanner(
                    message = errorMessage!!,
                    isError = true
                )
            }
        }

        // Verification Result Card
        verificationResult?.certificate?.let { cert ->
            item {
                val isRevoked = verificationResult?.isRevoked == true || cert.status == "REVOKED"
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = AppTheme.colors.surface),
                    border = BorderStroke(
                        1.5.dp,
                        if (isRevoked) AppTheme.colors.error else AppTheme.colors.success
                    )
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (isRevoked) Icons.Default.Error else Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = if (isRevoked) AppTheme.colors.error else AppTheme.colors.success,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = if (isRevoked) "Certificate Revoked" else "Authentic Certificate",
                                style = AppTheme.typography.titleMedium,
                                color = if (isRevoked) AppTheme.colors.error else AppTheme.colors.success,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            SsplBadge(
                                text = cert.type ?: "CONFERENCE",
                                variant = BadgeVariant.PRIMARY
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Recipient Name
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = AppTheme.colors.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Issued To",
                                    style = AppTheme.typography.bodySmall,
                                    color = AppTheme.colors.textMuted
                                )
                                Text(
                                    text = cert.recipientName ?: "Doctor / Delegate",
                                    style = AppTheme.typography.titleMedium,
                                    color = AppTheme.colors.onSurface,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Event / Program
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Event,
                                contentDescription = null,
                                tint = AppTheme.colors.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Event / Program",
                                    style = AppTheme.typography.bodySmall,
                                    color = AppTheme.colors.textMuted
                                )
                                Text(
                                    text = cert.eventName ?: cert.programName ?: "SSP Lahore CME Activity",
                                    style = AppTheme.typography.bodyMedium,
                                    color = AppTheme.colors.onSurface,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Serial & Date
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "Serial Number",
                                    style = AppTheme.typography.bodySmall,
                                    color = AppTheme.colors.textMuted
                                )
                                Text(
                                    text = cert.serial ?: serialInput,
                                    style = AppTheme.typography.bodyMedium,
                                    color = AppTheme.colors.onSurface,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            cert.issuedAt?.let { date ->
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "Issue Date",
                                        style = AppTheme.typography.bodySmall,
                                        color = AppTheme.colors.textMuted
                                    )
                                    Text(
                                        text = date.take(10),
                                        style = AppTheme.typography.bodyMedium,
                                        color = AppTheme.colors.onSurface,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

