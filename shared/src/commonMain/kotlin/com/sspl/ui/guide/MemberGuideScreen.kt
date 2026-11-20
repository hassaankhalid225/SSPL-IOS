package com.sspl.ui.guide

import com.sspl.designsystem.icons.SsplIcons

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.sspl.Screen
import com.sspl.designsystem.components.EmptyState
import com.sspl.designsystem.components.IconTile
import com.sspl.designsystem.components.LocalShellActions
import com.sspl.designsystem.components.NavTile
import com.sspl.designsystem.components.SearchBar
import com.sspl.designsystem.components.SecondaryButton
import com.sspl.designsystem.theme.AppTheme
import com.sspl.ui.components.AccordionCard
import com.sspl.ui.components.NumberDot
import com.sspl.ui.components.PageIntroHero

/** Web: pages/MemberGuidePage.jsx + data/homepage/memberGuide.ts — wording adapted to the app. */
data class GuideStepDetail(val stepTitle: String, val stepBody: String)

data class GuideTopic(
    val number: String,
    val title: String,
    val summary: String,
    val steps: List<GuideStepDetail>,
    val ctaLabel: String? = null,
    val ctaTarget: String? = null,
    val ctaNeedsLogin: Boolean = false
)

private val MEMBER_GUIDE_TOPICS = listOf(
    GuideTopic(
        "01", "How to become a member",
        "Create an account or sign in, choose a membership plan, and submit your application. Once approved, you become a society member.",
        listOf(
            GuideStepDetail("Open membership plans", "Open Membership plans from the drawer or the About page. You will see available plans such as regular and life membership."),
            GuideStepDetail("Select a plan and apply", "Choose the plan that fits you and tap Apply. If you are not signed in, you will need to sign in or register first."),
            GuideStepDetail("Complete and submit the form", "Fill in the required personal and professional details, upload any requested documents, and complete payment if applicable. Then check your status under My portal → Membership."),
            GuideStepDetail("Confirm approval or payment", "Some plans are free; others require payment or admin approval. Watch for a confirmation email and check your membership status.")
        ),
        "View membership plans", Screen.MembershipPlansScreen.route
    ),
    GuideTopic(
        "02", "Login and member desk",
        "You need to sign in to register for events, mark attendance, submit feedback, and download certificates.",
        listOf(
            GuideStepDetail("Open sign in", "Tap your avatar on the home screen, or Sign in from the drawer."),
            GuideStepDetail("Sign in with email and password", "Enter your registered email and password. If you forget your password, use Forgot password."),
            GuideStepDetail("Use My portal", "After signing in you can manage event registrations, membership status, abstracts, and the event portal (attendance, feedback, and certificates).")
        ),
        "Go to sign in", Screen.SignInScreen.route
    ),
    GuideTopic(
        "03", "How to apply for a conference, meeting, or workshop",
        "Monthly clinical meetings, conferences, and pre-conference workshops open from different listings — the registration flow is largely the same.",
        listOf(
            GuideStepDetail("Open the correct listing", "Use Events for monthly meetings and general events, Conferences for the annual conference, or Workshops for pre-conference workshops."),
            GuideStepDetail("Open the event details", "Tap an event card to see the date, venue, roles and fees, schedule, and registration section."),
            GuideStepDetail("Register now", "If you are not signed in, sign in first — you will return to the same event. Select a role if options are shown, then tap Register."),
            GuideStepDetail("Payment or approval", "If a fee applies, you will be taken to the payment gateway. For free or pending-approval events, check status under My registrations.")
        ),
        "Browse events", Screen.ScientificProgramsScreen.route
    ),
    GuideTopic(
        "04", "Conference and pre-conference workshops",
        "The annual conference and workshops are registered separately. Workshop registration must be completed on its own, in addition to conference registration.",
        listOf(
            GuideStepDetail("Annual conference", "Open Conferences for programme details, registration roles, and the session schedule."),
            GuideStepDetail("Pre-conference workshops", "Open Workshops. Each workshop has its own details and register flow — register separately for every workshop you plan to attend."),
            GuideStepDetail("Workshops on the conference page", "The conference detail page may also list pre-conference workshops with direct links to their details and registration.")
        ),
        "Open conferences", Screen.ConferencesScreen.route
    ),
    GuideTopic(
        "05", "How to mark attendance",
        "Attendance is available in the event portal for registered and confirmed participants — the same process for meetings, conferences, and workshops.",
        listOf(
            GuideStepDetail("Open the event portal", "After signing in, open My portal → Event portal. You will see your confirmed registrations."),
            GuideStepDetail("Select the event or session", "Open the meeting, conference, or workshop you registered for. The sessions list shows attendance actions where available."),
            GuideStepDetail("Submit attendance", "Follow the method shown for that session (for example GPS or selfie attendance). A success message confirms attendance was recorded."),
            GuideStepDetail("Important", "Attendance is usually only available during the event or session window. Registration must be confirmed first; pending payment or approval may lock attendance.")
        ),
        "Open event portal", Screen.EventPortalScreen.route, ctaNeedsLogin = true
    ),
    GuideTopic(
        "06", "How to submit feedback",
        "Feedback often unlocks after attendance. Completing feedback may be required before you can download a certificate.",
        listOf(
            GuideStepDetail("Use event portal session actions", "On your registered event, a feedback button appears when you are eligible (usually after attendance)."),
            GuideStepDetail("Answer the questions", "Complete the short questionnaire (ratings and comments). You will see a confirmation after you submit."),
            GuideStepDetail("Session vs overall feedback", "Some events ask for session-wise feedback; others use one overall form. Complete every option shown for your event.")
        ),
        "Go to event portal", Screen.EventPortalScreen.route, ctaNeedsLogin = true
    ),
    GuideTopic(
        "07", "Certificates — how you receive them",
        "A certificate becomes available when your registration is confirmed, required attendance is marked, and feedback is complete (if required).",
        listOf(
            GuideStepDetail("Eligibility", "Typical flow: register → confirm (payment or approval) → attend the session → submit feedback → download certificate."),
            GuideStepDetail("Download from the event portal", "In the Event portal, open your event. A certificate option appears when you are eligible."),
            GuideStepDetail("Verify certificate", "Use Verify certificate to check a serial number — useful for employers or CME verification."),
            GuideStepDetail("If your certificate is missing", "Confirm that registration is approved, attendance was marked, and feedback was submitted. If it still does not appear, contact the office or society secretary.")
        ),
        "Verify a certificate", Screen.VerifyCertificateScreen.route
    )
)

private fun topicIcon(number: String): ImageVector = when (number) {
    "01" -> SsplIcons.PersonAdd
    "02" -> SsplIcons.Login
    "03" -> SsplIcons.CalendarMonth
    "04" -> SsplIcons.MenuBook
    "05" -> SsplIcons.Assignment
    "06" -> SsplIcons.CheckCircle
    else -> SsplIcons.WorkspacePremium
}

private data class QuickLink(val label: String, val route: String, val needsLogin: Boolean = false)

private val QUICK_LINKS = listOf(
    QuickLink("Membership plans", Screen.MembershipPlansScreen.route),
    QuickLink("Events", Screen.ScientificProgramsScreen.route),
    QuickLink("Conferences", Screen.ConferencesScreen.route),
    QuickLink("Workshops", Screen.WorkshopsScreen.route),
    QuickLink("Event portal", Screen.EventPortalScreen.route, needsLogin = true),
    QuickLink("My registrations", Screen.MyRegistrationsScreen.route, needsLogin = true),
    QuickLink("FAQs", Screen.FaqsScreen.route),
    QuickLink("Contact us", Screen.ContactUsScreen.route)
)

@Composable
fun MemberGuideScreen(navController: NavController) {
    val shell = LocalShellActions.current
    var query by rememberSaveable { mutableStateOf("") }
    var openTopic by rememberSaveable { mutableStateOf<String?>(null) }
    val filtered = remember(query) {
        val q = query.trim()
        if (q.isEmpty()) MEMBER_GUIDE_TOPICS
        else MEMBER_GUIDE_TOPICS.filter { t ->
            t.title.contains(q, true) || t.summary.contains(q, true) ||
                t.steps.any { it.stepTitle.contains(q, true) || it.stepBody.contains(q, true) }
        }
    }

    fun go(route: String, needsLogin: Boolean) {
        if (needsLogin && shell.isGuest) shell.onAvatar()
        else navController.navigate(route) { launchSingleTop = true }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(AppTheme.colors.background),
        contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item(key = "hero") {
            PageIntroHero(
                eyebrow = "Member guide",
                eyebrowIcon = SsplIcons.MenuBook,
                title = "Complete guide for new members",
                summary = "Membership, event registration, attendance, feedback and certificates — in one place.",
                stats = listOf("${MEMBER_GUIDE_TOPICS.size}" to "Topics", "${MEMBER_GUIDE_TOPICS.sumOf { it.steps.size }}" to "Steps"),
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
        item(key = "path") {
            Row(
                Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth()
                    .clip(AppTheme.shapes.card)
                    .background(AppTheme.colors.infoContainer)
                    .padding(14.dp)
            ) {
                IconTile(SsplIcons.Lightbulb, tint = AppTheme.colors.info, background = AppTheme.colors.surface, size = 36.dp, iconSize = 18.dp)
                Spacer(Modifier.width(12.dp))
                Column {
                    Text("The usual path", style = AppTheme.typography.titleSmall)
                    Spacer(Modifier.height(2.dp))
                    Text(
                        "Become a member or sign in, register for an event, wait for confirmation, then use the event portal for attendance, feedback, and the certificate download.",
                        style = AppTheme.typography.bodySmall.copy(color = AppTheme.colors.body)
                    )
                }
            }
        }
        item(key = "links") {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(QUICK_LINKS, key = { it.label }) { link ->
                    Text(
                        link.label,
                        style = AppTheme.typography.labelMedium.copy(color = AppTheme.colors.primary, fontWeight = FontWeight.SemiBold),
                        modifier = Modifier
                            .clip(AppTheme.shapes.chip)
                            .background(AppTheme.colors.surface)
                            .border(1.dp, AppTheme.colors.border, AppTheme.shapes.chip)
                            .clickable { go(link.route, link.needsLogin) }
                            .heightIn(min = 40.dp)
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                    )
                }
            }
        }
        item(key = "search") {
            SearchBar(
                query = query,
                onQueryChange = { query = it },
                placeholder = "Search the guide",
                onClear = { query = "" },
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
        if (filtered.isEmpty()) {
            item(key = "empty") {
                EmptyState(
                    title = "Nothing in the guide matches",
                    message = "Try another word, or browse the FAQs.",
                    icon = SsplIcons.SearchOff,
                    actionText = "Clear search",
                    onActionClick = { query = "" }
                )
            }
        }
        items(filtered, key = { it.number }) { topic ->
            AccordionCard(
                title = topic.title,
                subtitle = "Step ${topic.number} · ${topic.steps.size} parts",
                expanded = openTopic == topic.number,
                onToggle = { openTopic = if (openTopic == topic.number) null else topic.number },
                leading = { IconTile(topicIcon(topic.number)) },
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Text(topic.summary, style = AppTheme.typography.bodyMedium.copy(color = AppTheme.colors.body))
                Spacer(Modifier.height(12.dp))
                topic.steps.forEachIndexed { i, step ->
                    Row(Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
                        NumberDot("${i + 1}", size = 26.dp)
                        Spacer(Modifier.width(10.dp))
                        Column(Modifier.weight(1f)) {
                            Text(step.stepTitle, style = AppTheme.typography.titleSmall)
                            Text(step.stepBody, style = AppTheme.typography.bodySmall.copy(color = AppTheme.colors.body))
                        }
                    }
                }
                if (topic.ctaLabel != null && topic.ctaTarget != null) {
                    // Sign-in CTA is pointless for signed-in users.
                    val hide = topic.ctaTarget == Screen.SignInScreen.route && !shell.isGuest
                    if (!hide) {
                        SecondaryButton(
                            text = topic.ctaLabel,
                            onClick = {
                                if (topic.ctaTarget == Screen.SignInScreen.route) shell.onAvatar()
                                else go(topic.ctaTarget, topic.ctaNeedsLogin)
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
        item(key = "help") {
            Row(
                Modifier.padding(horizontal = 16.dp, vertical = 10.dp).fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.Top
            ) {
                NavTile(
                    icon = SsplIcons.SupportAgent,
                    title = "Contact the office",
                    subtitle = "Membership, registration, certificates",
                    onClick = { go(Screen.ContactUsScreen.route, false) },
                    modifier = Modifier.weight(1f)
                )
                NavTile(
                    icon = SsplIcons.MenuBook,
                    title = "Read FAQs",
                    subtitle = "Quick answers",
                    onClick = { go(Screen.FaqsScreen.route, false) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
