package com.sspl.ui.contact

import com.sspl.designsystem.icons.SsplIcons

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.sspl.core.repositories.PublicRepository
import com.sspl.designsystem.components.AvatarImage
import com.sspl.designsystem.components.FeedbackBanner
import com.sspl.designsystem.components.IconTile
import com.sspl.designsystem.components.OutlineButton
import com.sspl.designsystem.components.PrimaryButton
import com.sspl.designsystem.components.QuickActionTile
import com.sspl.designsystem.components.SectionHeader
import com.sspl.designsystem.components.SkeletonLine
import com.sspl.designsystem.components.SsplRefreshBox
import com.sspl.designsystem.components.SsplTextField
import com.sspl.designsystem.theme.AppTheme
import com.sspl.platform.shared.UrlOpener
import com.sspl.ui.components.InfoSurface
import com.sspl.ui.components.PageIntroHero
import com.sspl.ui.council.SOCIETY_SECRETARY
import io.ktor.http.encodeURLParameter
import org.koin.compose.koinInject

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ContactUsScreen(
    navController: NavController,
    publicRepository: PublicRepository = koinInject(),
    urlOpener: UrlOpener = koinInject()
) {
    val viewModel: ContactViewModel = viewModel { ContactViewModel(publicRepository) }
    val state by viewModel.state.collectAsStateWithLifecycle()
    val focus = LocalFocusManager.current
    val contact = state.contact
    val open: (String) -> Unit = remember(urlOpener) { { url -> urlOpener.openUrl(url) } }

    SsplRefreshBox(
        isRefreshing = state.isRefreshing,
        onRefresh = viewModel::refresh,
        modifier = Modifier.fillMaxSize().background(AppTheme.colors.background)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize().imePadding(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item(key = "hero") {
                PageIntroHero(
                    eyebrow = "Contact SSP Lahore",
                    eyebrowIcon = SsplIcons.SupportAgent,
                    title = "Get in touch",
                    summary = "Reach the secretariat for membership, events, collaborations, and general inquiries."
                )
            }

            item(key = "actions") {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    QuickActionTile(SsplIcons.Phone, "Call", { open(contact.telUri) }, Modifier.weight(1f))
                    QuickActionTile(SsplIcons.Chat, "WhatsApp", { open(contact.whatsappUri) }, Modifier.weight(1f))
                    QuickActionTile(SsplIcons.Email, "Email", { open(contact.mailUri) }, Modifier.weight(1f))
                    QuickActionTile(SsplIcons.Directions, "Directions", { open(contact.mapUrl) }, Modifier.weight(1f))
                }
            }

            item(key = "details") {
                Column {
                    SectionHeader(title = "Secretariat")
                    Spacer(Modifier.height(8.dp))
                    InfoSurface {
                        if (state.loading) {
                            repeat(3) {
                                SkeletonLine(widthFraction = 0.35f, height = 10.dp)
                                Spacer(Modifier.height(6.dp))
                                SkeletonLine(widthFraction = 0.9f)
                                Spacer(Modifier.height(14.dp))
                            }
                        } else {
                            DetailRow(SsplIcons.Place, "Office address · ${contact.location}", contact.address) { open(contact.mapUrl) }
                            DetailRow(SsplIcons.Phone, "Phone and WhatsApp", contact.phone) { open(contact.telUri) }
                            DetailRow(SsplIcons.Email, "Email", contact.email) { open(contact.mailUri) }
                            Spacer(Modifier.height(8.dp))
                            OutlineButton(
                                text = "Open in Google Maps",
                                onClick = { open(contact.mapUrl) },
                                icon = SsplIcons.Directions,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }

            item(key = "secretary") {
                InfoSurface {
                    Text("SOCIETY SECRETARY", style = AppTheme.typography.overline)
                    Spacer(Modifier.height(10.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AvatarImage(url = SOCIETY_SECRETARY.imageUrl, name = SOCIETY_SECRETARY.name, size = 60.dp)
                        Spacer(Modifier.width(14.dp))
                        Column(Modifier.weight(1f)) {
                            Text(SOCIETY_SECRETARY.name, style = AppTheme.typography.titleMedium.copy(color = AppTheme.colors.heading))
                            Text(
                                "Primary contact for membership, events, and secretariat inquiries.",
                                style = AppTheme.typography.bodySmall.copy(color = AppTheme.colors.body)
                            )
                        }
                    }
                }
            }

            item(key = "form") {
                Column {
                    SectionHeader(title = "Send us a message")
                    Spacer(Modifier.height(8.dp))
                    InfoSurface {
                        SsplTextField(
                            value = state.form.name,
                            onValueChange = viewModel::onName,
                            label = "Full name *",
                            placeholder = "Your name",
                            leadingIcon = SsplIcons.Person,
                            error = state.form.nameError,
                            enabled = !state.submitting,
                            keyboardActions = KeyboardActions(
                                onNext = { focus.moveFocus(FocusDirection.Down) },
                                onDone = { focus.moveFocus(FocusDirection.Down) }
                            )
                        )
                        Spacer(Modifier.height(12.dp))
                        SsplTextField(
                            value = state.form.email,
                            onValueChange = viewModel::onEmail,
                            label = "Email address *",
                            placeholder = "you@example.com",
                            leadingIcon = SsplIcons.Email,
                            error = state.form.emailError,
                            enabled = !state.submitting,
                            keyboardType = KeyboardType.Email,
                            keyboardActions = KeyboardActions(
                                onNext = { focus.moveFocus(FocusDirection.Down) },
                                onDone = { focus.moveFocus(FocusDirection.Down) }
                            )
                        )
                        Spacer(Modifier.height(12.dp))
                        SsplTextField(
                            value = state.form.message,
                            onValueChange = viewModel::onMessage,
                            label = "Message *",
                            placeholder = "How can we help?",
                            error = state.form.messageError,
                            enabled = !state.submitting,
                            singleLine = false,
                            maxLines = 6
                        )
                        state.submitResult?.let { (msg, isError) ->
                            Spacer(Modifier.height(12.dp))
                            FeedbackBanner(message = msg, isError = isError)
                            // If the inquiry endpoint is unavailable, hand the same message to the mail app.
                            if (isError && state.form.message.isNotBlank() && state.form.nameError == null &&
                                state.form.emailError == null && state.form.messageError == null
                            ) {
                                Spacer(Modifier.height(8.dp))
                                OutlineButton(
                                    text = "Email the secretariat instead",
                                    onClick = {
                                        val subject = "Inquiry from ${state.form.name.trim()}".encodeURLParameter()
                                        val body = (state.form.message.trim() + "\n\n— " + state.form.name.trim() + " (" + state.form.email.trim() + ")").encodeURLParameter()
                                        open("${contact.mailUri}?subject=$subject&body=$body")
                                    },
                                    icon = SsplIcons.Email,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                        Spacer(Modifier.height(14.dp))
                        PrimaryButton(
                            text = if (state.submitting) "Sending…" else "Send message",
                            onClick = {
                                focus.clearFocus()
                                viewModel.submit()
                            },
                            loading = state.submitting,
                            enabled = !state.submitting,
                            trailingIcon = SsplIcons.Send,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            item(key = "social") {
                Column {
                    SectionHeader(title = "Follow SSP Lahore")
                    Spacer(Modifier.height(8.dp))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        SOCIAL_LINKS.forEach { s ->
                            Row(
                                Modifier
                                    .clip(AppTheme.shapes.chip)
                                    .background(AppTheme.colors.primaryContainer)
                                    .clickable { open(s.url) }
                                    .heightIn(min = 44.dp)
                                    .padding(horizontal = 14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(SsplIcons.Public, contentDescription = null, tint = AppTheme.colors.onPrimaryContainer, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text(s.label, style = AppTheme.typography.labelMedium.copy(color = AppTheme.colors.onPrimaryContainer, fontWeight = FontWeight.SemiBold))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(AppTheme.shapes.small)
            .clickable(onClick = onClick)
            .heightIn(min = 52.dp)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconTile(icon, size = 38.dp, iconSize = 18.dp, corner = 12.dp)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(label, style = AppTheme.typography.labelSmall)
            Text(value, style = AppTheme.typography.bodyMedium.copy(color = AppTheme.colors.primary, fontWeight = FontWeight.Medium))
        }
    }
}
