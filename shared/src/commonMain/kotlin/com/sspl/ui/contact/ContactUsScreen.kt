package com.sspl.ui.contact

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.sspl.core.models.ContactInquiryRequest
import com.sspl.core.repositories.PublicRepository
import com.sspl.designsystem.components.AvatarImage
import com.sspl.designsystem.components.BadgeVariant
import com.sspl.designsystem.components.FeedbackBanner
import com.sspl.designsystem.components.PrimaryButton
import com.sspl.designsystem.components.SsplBadge
import com.sspl.designsystem.components.SsplTextField
import com.sspl.designsystem.theme.AppTheme
import com.sspl.platform.shared.UrlOpener
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
fun ContactUsScreen(
    navController: NavController,
    publicRepository: PublicRepository = koinInject(),
    urlOpener: UrlOpener = koinInject()
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(AppTheme.colors.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Hero Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = AppTheme.colors.primary)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "GET IN TOUCH",
                        style = AppTheme.typography.labelSmall,
                        color = AppTheme.colors.secondary,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.3.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Contact SSPL Secretariat",
                        style = AppTheme.typography.headlineMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Connect directly with chapter office bearers for membership, conferences, and partnerships.",
                        style = AppTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.88f)
                    )
                }
            }
        }

        // Quick Intent Actions Grid
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ContactActionPill(
                    icon = Icons.Default.Phone,
                    label = "Call Us",
                    modifier = Modifier.weight(1f),
                    onClick = { urlOpener.openUrl("tel:+924237420101") }
                )
                ContactActionPill(
                    icon = Icons.Default.Email,
                    label = "Email",
                    modifier = Modifier.weight(1f),
                    onClick = { urlOpener.openUrl("mailto:info@ssplahore.com") }
                )
                ContactActionPill(
                    icon = Icons.Default.Message,
                    label = "WhatsApp",
                    modifier = Modifier.weight(1f),
                    onClick = { urlOpener.openUrl("https://wa.me/923001234567") }
                )
                ContactActionPill(
                    icon = Icons.Default.Map,
                    label = "Map",
                    modifier = Modifier.weight(1f),
                    onClick = { urlOpener.openUrl("https://maps.google.com/?q=PMA+Trade+Center+66+Ferozepur+Road+Lahore") }
                )
            }
        }

        // Office Location Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = AppTheme.colors.surface),
                border = BorderStroke(1.dp, AppTheme.colors.border)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(AppTheme.colors.primary.copy(alpha = 0.1f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = AppTheme.colors.primary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Lahore Secretariat",
                            style = AppTheme.typography.titleMedium,
                            color = AppTheme.colors.onSurface,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Office # 8, 2nd Floor, PMA Trade Center, 66 Ferozepur Road, Lahore, Pakistan",
                            style = AppTheme.typography.bodyMedium,
                            color = AppTheme.colors.textMuted,
                            lineHeight = 20.sp
                        )
                    }
                }
            }
        }

        // Society Secretary Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = AppTheme.colors.surface),
                border = BorderStroke(1.dp, AppTheme.colors.border)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AvatarImage(
                        name = "Dr. Ahmad Naeem Akhtar",
                        size = 52.dp
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        SsplBadge(
                            text = "General Secretary",
                            variant = BadgeVariant.PRIMARY
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Dr. Ahmad Naeem Akhtar",
                            style = AppTheme.typography.titleMedium,
                            color = AppTheme.colors.onSurface,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Primary contact for official correspondence & secretariat affairs.",
                            style = AppTheme.typography.bodySmall,
                            color = AppTheme.colors.textMuted
                        )
                    }
                }
            }
        }

        // Contact Form
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = AppTheme.colors.surface),
                border = BorderStroke(1.dp, AppTheme.colors.border)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Send a Message",
                        style = AppTheme.typography.titleMedium,
                        color = AppTheme.colors.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Have questions regarding membership or events? Leave your message below.",
                        style = AppTheme.typography.bodySmall,
                        color = AppTheme.colors.textMuted
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    if (successMessage != null) {
                        FeedbackBanner(
                            message = successMessage!!,
                            isError = false
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    if (errorMessage != null) {
                        FeedbackBanner(
                            message = errorMessage!!,
                            isError = true
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    SsplTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = "Your Name",
                        placeholder = "e.g. Dr. Muhammad Ahmed",
                        leadingIcon = Icons.Default.Person,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    SsplTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = "Email Address",
                        placeholder = "e.g. doctor@hospital.org",
                        leadingIcon = Icons.Default.Email,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    SsplTextField(
                        value = message,
                        onValueChange = { message = it },
                        label = "Message",
                        placeholder = "Write your inquiry or feedback here...",
                        singleLine = false,
                        modifier = Modifier.fillMaxWidth().height(110.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    PrimaryButton(
                        text = "Send Message",
                        icon = Icons.Default.Send,
                        isLoading = isSubmitting,
                        enabled = name.isNotBlank() && email.isNotBlank() && message.isNotBlank(),
                        onClick = {
                            scope.launch {
                                isSubmitting = true
                                errorMessage = null
                                successMessage = null
                                publicRepository.submitContactInquiry(
                                    ContactInquiryRequest(
                                        name = name.trim(),
                                        email = email.trim(),
                                        message = message.trim()
                                    )
                                ).collect { resp ->
                                    isSubmitting = false
                                    if (resp.success) {
                                        successMessage = resp.message ?: "Thank you! Your message has been sent to the SSPL Secretariat."
                                        name = ""
                                        email = ""
                                        message = ""
                                    } else {
                                        errorMessage = resp.message ?: "Failed to deliver message. Please try again."
                                    }
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
private fun ContactActionPill(
    icon: ImageVector,
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = AppTheme.colors.surface),
        border = BorderStroke(1.dp, AppTheme.colors.border)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(AppTheme.colors.primary.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = AppTheme.colors.primary,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = label,
                style = AppTheme.typography.labelSmall,
                color = AppTheme.colors.onSurface,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

