package com.sspl.ui.terms

import com.sspl.designsystem.icons.SsplIcons

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.sspl.Screen
import com.sspl.designsystem.components.NavTile
import com.sspl.designsystem.theme.AppTheme
import com.sspl.ui.components.BulletText
import com.sspl.ui.components.InfoSurface
import com.sspl.ui.components.NumberDot
import com.sspl.ui.components.PageIntroHero

/** Web: pages/TermsPage.jsx + data/homepage/terms.ts. */
private const val TERMS_INTRO =
    "By registering with, participating in, or using the services of the Society of Surgeons of Pakistan (SSP), Lahore Chapter, you agree to comply with the following Terms & Conditions:"

private data class TermsSection(val title: String, val points: List<String>)

private val TERMS = listOf(
    TermsSection("About the Society", listOf(
        "The Society of Surgeons of Pakistan, Lahore Chapter, is a professional, non-profit organization dedicated to academic excellence, professional development, research, and advancement of surgical sciences."
    )),
    TermsSection("Membership", listOf(
        "Membership is open to eligible medical professionals as per SSP rules.",
        "Membership categories, fees, and duration shall be determined by SSP Lahore Chapter and may be revised from time to time.",
        "The Society reserves the right to accept, reject, suspend, or terminate membership in case of violation of rules or unethical conduct."
    )),
    TermsSection("Registration for Conferences, Workshops & Events", listOf(
        "Registration is confirmed only after full payment of the applicable fee.",
        "Registration fees are refundable 10 Days before event.",
        "The Society reserves the right to modify event schedules, faculty, venue, or program content if required.",
        "Certificates will be issued only to registered participants who meet attendance criteria."
    )),
    TermsSection("Code of Conduct", listOf(
        "All members and participants are expected to maintain professional, ethical, and respectful behavior.",
        "Any form of misconduct, harassment, or misrepresentation may result in removal from the event or disciplinary action without refund."
    )),
    TermsSection("Intellectual Property", listOf(
        "All conference material, logos, publications, recordings, and digital content are the intellectual property of SSP Lahore Chapter unless stated otherwise.",
        "Reproduction or distribution without prior written permission is strictly prohibited."
    )),
    TermsSection("Use of Personal Information", listOf(
        "Personal information provided during registration will be used solely for official SSP activities and communication.",
        "SSP Lahore Chapter will not share personal data with third parties without consent, except where required by law."
    )),
    TermsSection("Liability", listOf(
        "SSP Lahore Chapter shall not be responsible for any loss, injury, accident, or damage to personal property during events or activities.",
        "Participants attend events at their own risk."
    )),
    TermsSection("Amendments", listOf(
        "The Society reserves the right to amend these Terms & Conditions at any time without prior notice.",
        "Updated terms will be effective immediately upon publication."
    )),
    TermsSection("Governing Law", listOf(
        "These Terms & Conditions shall be governed and interpreted in accordance with the laws of the Islamic Republic of Pakistan."
    )),
    TermsSection("Acceptance", listOf(
        "By registering, applying for membership, or participating in SSP Lahore Chapter activities, you acknowledge that you have read, understood, and agreed to these Terms & Conditions."
    ))
)

@Composable
fun TermsScreen(navController: NavController) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().background(AppTheme.colors.background),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item(key = "hero") {
            PageIntroHero(
                eyebrow = "Legal compliance",
                eyebrowIcon = SsplIcons.Gavel,
                title = "Terms & conditions",
                summary = "Society of Surgeons of Pakistan – Lahore Chapter",
                stats = listOf("${TERMS.size}" to "Sections", "10 days" to "Refund window")
            )
        }
        item(key = "intro") {
            Text(
                TERMS_INTRO,
                style = AppTheme.typography.bodyLarge.copy(color = AppTheme.colors.body),
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp)
            )
        }
        itemsIndexed(TERMS, key = { _, s -> s.title }) { index, section ->
            InfoSurface {
                Row {
                    NumberDot("${index + 1}")
                    Spacer(Modifier.width(12.dp))
                    Text(
                        section.title,
                        style = AppTheme.typography.titleMedium.copy(color = AppTheme.colors.heading),
                        modifier = Modifier.weight(1f).padding(top = 3.dp)
                    )
                }
                Spacer(Modifier.height(10.dp))
                HorizontalDivider(color = AppTheme.colors.divider)
                Spacer(Modifier.height(10.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    section.points.forEach { BulletText(it) }
                }
            }
        }
        item(key = "help") {
            Spacer(Modifier.height(4.dp))
            NavTile(
                icon = SsplIcons.SupportAgent,
                title = "Questions about these terms?",
                subtitle = "Contact the SSPL secretariat",
                onClick = { navController.navigate(Screen.ContactUsScreen.route) { launchSingleTop = true } },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
