package com.sspl.ui.faqs

import com.sspl.designsystem.icons.SsplIcons

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.sspl.Screen
import com.sspl.designsystem.components.EmptyState
import com.sspl.designsystem.components.NavTile
import com.sspl.designsystem.components.SearchBar
import com.sspl.designsystem.theme.AppTheme
import com.sspl.ui.components.AccordionCard
import com.sspl.ui.components.PageIntroHero

/** Web: pages/Faqs.jsx + data/homepage/faqs.ts (customerSupportFaqs). */
private data class Faq(val question: String, val answer: String)

private val FAQS = listOf(
    Faq("Can MBBS students apply for membership?", "No. It is mandatory to be a qualified surgeon before applying for membership. Students are encouraged to join our academic sessions but cannot hold official membership."),
    Faq("Is a specialization degree in Surgery mandatory for membership?", "Yes. A recognized postgraduate specialization in surgery (such as FCPS, MS, or equivalent) is mandatory for membership in the Society of Surgeons of Pakistan."),
    Faq("Can allied healthcare workers apply for membership?", "No. Membership of the Society of Surgeons of Pakistan is strictly limited to qualified surgeons. Allied healthcare professionals are currently not eligible for membership."),
    Faq("Can membership continue when migrating abroad?", "Yes. Your SSP membership remains valid if you relocate abroad, provided your account is kept active and renewed according to SSP regulations."),
    Faq("Can government employees apply for membership?", "Yes. Government-employed doctors and surgical specialists are fully eligible and encouraged to apply for SSP membership."),
    Faq("Can I apply if there is a chapter in my city?", "Yes. You may apply for membership regardless of your location. Membership is open to all eligible surgeons across the country, irrespective of local chapter presence."),
    Faq("Can non-members register for society events?", "Yes. Non-members are welcome to register for conferences and workshops. However, official members usually enjoy significantly discounted registration fees."),
    Faq("Can non-members present cases at society events?", "Yes. Non-members may submit and present cases or research papers at SSP events, subject to abstract approval by the scientific committee."),
    Faq("Can I attend an event without registration?", "No. Prior registration is mandatory for all SSP events to ensure proper organization, venue security, and the issuance of attendance certificates.")
)

@Composable
fun FaqsScreen(navController: NavController) {
    var query by rememberSaveable { mutableStateOf("") }
    // Web opens the first question by default.
    var openQuestion by rememberSaveable { mutableStateOf<String?>(null) }
    val filtered = remember(query) {
        val q = query.trim()
        if (q.isEmpty()) FAQS
        else FAQS.filter { it.question.contains(q, ignoreCase = true) || it.answer.contains(q, ignoreCase = true) }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(AppTheme.colors.background),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item(key = "hero") {
            PageIntroHero(
                eyebrow = "Customer support",
                eyebrowIcon = SsplIcons.QuestionAnswer,
                title = "Frequently asked questions",
                summary = "Membership, events, and participation in SSP Lahore.",
                stats = listOf("${FAQS.size}" to "Answers")
            )
            Spacer(Modifier.height(6.dp))
        }
        item(key = "search") {
            SearchBar(
                query = query,
                onQueryChange = { query = it },
                placeholder = "Search questions",
                onClear = { query = "" }
            )
            Spacer(Modifier.height(4.dp))
        }
        if (filtered.isEmpty()) {
            item(key = "empty") {
                EmptyState(
                    title = "No matching questions",
                    message = "Try a different word, or contact the secretariat for help.",
                    icon = SsplIcons.SearchOff,
                    actionText = "Clear search",
                    onActionClick = { query = "" }
                )
            }
        } else {
            items(filtered, key = { it.question }) { faq ->
                val expanded = openQuestion == faq.question
                AccordionCard(
                    title = faq.question,
                    expanded = expanded,
                    onToggle = { openQuestion = if (openQuestion == faq.question) null else faq.question }
                ) {
                    Text(faq.answer, style = AppTheme.typography.bodyMedium.copy(color = AppTheme.colors.body))
                }
            }
        }
        item(key = "help") {
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                NavTile(
                    icon = SsplIcons.SupportAgent,
                    title = "Contact us",
                    subtitle = "Still need help?",
                    onClick = { navController.navigate(Screen.ContactUsScreen.route) { launchSingleTop = true } },
                    modifier = Modifier.weight(1f)
                )
                NavTile(
                    icon = SsplIcons.MenuBook,
                    title = "Member guide",
                    subtitle = "Step-by-step",
                    onClick = { navController.navigate(Screen.MemberGuideScreen.route) { launchSingleTop = true } },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

