package com.sspl.ui.fellowship

import com.sspl.designsystem.icons.SsplIcons

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.automirrored.filled.EventNote
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.automirrored.filled.FactCheck
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.sspl.Screen
import com.sspl.designsystem.components.FactRow
import com.sspl.designsystem.components.LocalShellActions
import com.sspl.designsystem.components.OutlineButton
import com.sspl.designsystem.components.SecondaryButton
import com.sspl.designsystem.components.SsplCard
import com.sspl.designsystem.components.StatTile
import com.sspl.designsystem.components.StickyCtaBar
import com.sspl.designsystem.theme.AppTheme
import com.sspl.platform.shared.UrlOpener
import org.koin.compose.koinInject

private const val SECRETARIAT_WHATSAPP = "https://wa.me/923334254002"
private const val SAGES_PROOF_WHATSAPP =
    "https://wa.me/923334254002?text=Assalam%20o%20Alaikum%2C%20I%20am%20submitting%20my%20SAGES%20Safe%20Cholecystectomy%20completion%20proof%20for%20SSPL%20Mini%20Fellowship."
private const val SAGES_URL = "https://www.sages.org/safe-cholecystectomy-program/"
private const val SECRETARIAT_EMAIL = "societyofsurgeons1@gmail.com"

private class GuidelineSection(
    val title: String,
    val summary: String,
    val body: @Composable (Actions) -> Unit
)

private class Actions(val open: (String) -> Unit, val url: (String) -> Unit)

/**
 * E3: Programme guidelines & mandatory SOPs (FellowshipGuidelines.jsx), as an accordion.
 */
@Composable
fun FellowshipGuidelinesScreen(
    navController: NavController,
    urlOpener: UrlOpener = koinInject()
) {
    val actions = Actions(
        open = { route -> navController.navigate(route) { launchSingleTop = true } },
        url = { urlOpener.openUrl(it) }
    )

    Column(Modifier.fillMaxSize().background(AppTheme.colors.background)) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item(key = "hero") {
                FellowshipHero(
                    eyebrow = "Official programme guidelines",
                    title = "Instructions & mandatory requirements",
                    summary = "SSPL Mini Fellowship in Laparoscopic Surgery — Society of Surgeons of Pakistan, Lahore Chapter.",
                    icon = SsplIcons.Shield
                )
            }
            item(key = "intro") {
                Text(
                    "The Mini Fellowship enhances the knowledge, technical skills and operative experience of surgeons through structured training at SSPL-approved centres under designated trainers. All enrolled fellows must complete the requirements below to qualify for the official completion certificate.",
                    style = AppTheme.typography.bodyMedium
                )
            }
            item(key = "snapshot") {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        StatTile("4–6 weeks", "Duration", Modifier.weight(1f), icon = SsplIcons.Schedule)
                        StatTile("4–5 hours", "SAGES online (free)", Modifier.weight(1f), icon = SsplIcons.MenuBook)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        StatTile("Web portal", "Digital logbook", Modifier.weight(1f), icon = SsplIcons.FactCheck)
                        StatTile("SURGICON 2027", "Award ceremony", Modifier.weight(1f), icon = SsplIcons.EmojiEvents)
                    }
                }
            }
            itemsIndexed(SECTIONS, key = { i, _ -> "s$i" }) { i, s ->
                GuidelineCard(index = i + 1, section = s, actions = actions, initiallyExpanded = false)
            }
            item(key = "contact") { SecretariatCard(actions) }
            item(key = "footer") {
                Text(
                    "Society of Surgeons of Pakistan – Lahore Chapter (SSPL) · Promoting excellence in surgical education, training & professional development.",
                    style = AppTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
        StickyCtaBar(
            primaryText = "Apply for fellowship",
            onPrimaryClick = {
                FellowshipNavArgs.centerId = null
                actions.open(Screen.ApplyAsFellowScreen.route)
            }
        )
    }
}

@Composable
private fun GuidelineCard(index: Int, section: GuidelineSection, actions: Actions, initiallyExpanded: Boolean) {
    var expanded by rememberSaveable(index) { mutableStateOf(initiallyExpanded) }
    val colors = AppTheme.colors
    SsplCard(Modifier.animateContentSize()) {
        Row(
            Modifier.fillMaxWidth().clickable { expanded = !expanded }.heightIn(min = 56.dp).padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(Modifier.size(32.dp).clip(CircleShape).background(colors.primaryGradient), contentAlignment = Alignment.Center) {
                Text("$index", style = AppTheme.typography.titleSmall.copy(color = Color.White))
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(section.title, style = AppTheme.typography.titleSmall.copy(color = colors.heading))
                if (!expanded) Text(section.summary, style = AppTheme.typography.bodySmall, maxLines = 1)
            }
            Icon(
                if (expanded) SsplIcons.ExpandLess else SsplIcons.ExpandMore,
                contentDescription = if (expanded) "Collapse" else "Expand",
                tint = colors.muted
            )
        }
        AnimatedVisibility(expanded) {
            Column(Modifier.padding(start = 14.dp, end = 14.dp, bottom = 16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                HorizontalDivider(color = colors.divider)
                section.body(actions)
            }
        }
    }
}

@Composable private fun P(text: String) = Text(text, style = AppTheme.typography.bodyMedium)

@Composable
private fun Bullet(text: String, check: Boolean = false) {
    Row(verticalAlignment = Alignment.Top) {
        if (check) Icon(SsplIcons.CheckCircle, null, tint = AppTheme.colors.success, modifier = Modifier.size(16.dp).padding(top = 2.dp))
        else Text("•", style = AppTheme.typography.bodyMedium.copy(color = AppTheme.colors.primary, fontWeight = FontWeight.Bold))
        Spacer(Modifier.width(8.dp))
        Text(text, style = AppTheme.typography.bodyMedium.copy(color = AppTheme.colors.heading))
    }
}

@Composable
private fun KeyValueBox(rows: List<Pair<String, String>>) {
    Column(
        Modifier.fillMaxWidth().clip(AppTheme.shapes.small).background(AppTheme.colors.boxColor).padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        rows.forEach { (k, v) ->
            Row {
                Text(k, style = AppTheme.typography.labelMedium, modifier = Modifier.weight(1f))
                Text(v, style = AppTheme.typography.bodyMedium.copy(color = AppTheme.colors.heading, fontWeight = FontWeight.SemiBold))
            }
        }
    }
}

private val SECTIONS = listOf(
    GuidelineSection("Mandatory SSPL life membership", "Compulsory for every enrolled fellow") { a ->
        P("Life Membership of the Society of Surgeons of Pakistan – Lahore Chapter is compulsory for all participants enrolled in the Mini Fellowship Programme.")
        KeyValueBox(listOf("Membership category" to "Life Member (LM)", "New applicant fee" to "PKR 10,000/-", "Requirement status" to "Mandatory"))
        InlineNote(
            "Surgeons who already hold a valid SSPL Life Membership do not need to pay the fee again. Provide your LM number / membership certificate during registration and verification.",
            tone = NoteTone.Success,
            title = "Existing life members are exempt"
        )
        SecondaryButton(text = "Apply for life membership", onClick = { a.open(Screen.MembershipPlansScreen.route) })
    },
    GuidelineSection("Digital surgical logbook", "Log every case on the SSPL portal") { a ->
        P("Every fellow must maintain an accurate, up-to-date digital surgical logbook throughout training. Cases are entered on the SSPL portal so supervising consultants can verify entries in real time.")
        Text("Standardised participation codes", style = AppTheme.typography.titleSmall.copy(color = AppTheme.colors.heading))
        listOf(
            Triple("A", "Observer", "Observed the laparoscopic surgical procedure."),
            Triple("B", "Assisted", "Assisted the supervising surgeon during the procedure."),
            Triple("C", "Performed under supervision", "Performed the procedure under the direct operative supervision of the designated trainer.")
        ).forEach { (code, level, desc) ->
            Row(Modifier.fillMaxWidth().clip(AppTheme.shapes.small).background(AppTheme.colors.boxColor).padding(10.dp)) {
                Box(Modifier.size(28.dp).clip(CircleShape).background(AppTheme.colors.primaryContainer), contentAlignment = Alignment.Center) {
                    Text(code, style = AppTheme.typography.titleSmall.copy(color = AppTheme.colors.onPrimaryContainer))
                }
                Spacer(Modifier.width(10.dp))
                Column {
                    Text(level, style = AppTheme.typography.bodyMedium.copy(color = AppTheme.colors.heading, fontWeight = FontWeight.SemiBold))
                    Text(desc, style = AppTheme.typography.bodySmall)
                }
            }
        }
        InlineNote("Record procedures regularly and chronologically. Ensure patient references and operative notes are complete. A duly verified digital logbook is compulsory for certification.", title = "Logbook instructions")
        SecondaryButton(text = "Open fellow dashboard", onClick = { a.open(Screen.FellowDashboardScreen.route) })
    },
    GuidelineSection("SAGES Safe Cholecystectomy (online)", "Complete before practical training") { a ->
        P("All fellows must complete the online educational activities of the SAGES Safe Cholecystectomy Programme before commencing practical training.")
        KeyValueBox(listOf("Estimated duration" to "4–5 hours (self-paced)", "Access" to "Free", "Complete within" to "7–10 days of enrolment"))
        Text("Key learning areas", style = AppTheme.typography.titleSmall.copy(color = AppTheme.colors.heading))
        Bullet("Principles of safe laparoscopic cholecystectomy", check = true)
        Bullet("Critical View of Safety (CVS) & BDI prevention", check = true)
        Bullet("Camera handling, ergonomic setup & port placement", check = true)
        Bullet("Intraoperative decision-making in difficult gallbladders", check = true)
        InlineNote(
            "After completing the modules, screenshot your completion certificate and WhatsApp it to the SSPL Secretariat at +92 333 425 4002 before starting hospital training.",
            tone = NoteTone.Warning,
            title = "Mandatory WhatsApp submission"
        )
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlineButton(text = "SAGES modules", onClick = { a.url(SAGES_URL) }, modifier = Modifier.weight(1f))
            SecondaryButton(text = "Send proof", onClick = { a.url(SAGES_PROOF_WHATSAPP) }, modifier = Modifier.weight(1f))
        }
    },
    GuidelineSection("Practical fellowship training", "4 weeks at your assigned centre") { _ ->
        P("Fellows train at their assigned SSPL-approved centre under direct consultant mentorship.")
        Bullet("Report to the assigned training centre in accordance with the approved schedule.")
        Bullet("Follow the operative guidance and instructions of the designated supervising consultant / trainer.")
        Bullet("Participate actively in clinical rounds, operative procedures and academic sessions.")
        Bullet("Maintain the highest standards of professional conduct, punctuality and institutional discipline.")
        Bullet("Regularly update your digital surgical logbook on the SSPL portal.")
        InlineNote("4 weeks (extendable to 6 weeks where applicable and approved).", title = "Training duration")
    },
    GuidelineSection("Final checklist for certification", "Five steps to your certificate") { _ ->
        P("To qualify for the SSPL Mini Fellowship Completion Certificate, each fellow must fulfil the following:")
        listOf(
            "SSPL life membership" to "Valid LM number confirmed (new member fee PKR 10,000/- or existing LM verified).",
            "SAGES online modules" to "Completed safe cholecystectomy modules and WhatsApped proof to +92 333 425 4002.",
            "Practical rotations" to "Completed 4–6 weeks practical training at the assigned approved centre.",
            "Digital surgical logbook" to "All procedures logged on the SSPL portal using codes A, B and C.",
            "Trainer verification" to "Logbook and competency sign-off reviewed and approved by the supervising consultant."
        ).forEach { (t, d) ->
            Row(verticalAlignment = Alignment.Top) {
                Icon(SsplIcons.CheckCircle, null, tint = AppTheme.colors.success, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(10.dp))
                Column {
                    Text(t, style = AppTheme.typography.bodyMedium.copy(color = AppTheme.colors.heading, fontWeight = FontWeight.SemiBold))
                    Text(d, style = AppTheme.typography.bodySmall)
                }
            }
        }
        InlineNote("Fellows who fail to complete any mandatory requirement may have their completion certificates withheld until all criteria are fulfilled.", tone = NoteTone.Warning, title = "Certification policy")
    },
    GuidelineSection("Certification & SURGICON 2027", "Digital certificate and award ceremony") { _ ->
        Text("Online digital certificate", style = AppTheme.typography.titleSmall.copy(color = AppTheme.colors.heading))
        P("After trainer sign-off and administrative verification, download your official completion certificate from your SSPL dashboard. Conference delegate registration is not mandatory to receive the digital certificate.")
        Spacer(Modifier.height(4.dp))
        Text("SURGICON 2027 award ceremony", style = AppTheme.typography.titleSmall.copy(color = AppTheme.colors.heading))
        FactRow(SsplIcons.EventNote, "Date", "9–10 April 2027")
        FactRow(SsplIcons.LocationOn, "Venue", "Pearl Continental (PC) Hotel, Lahore")
        P("Eligible fellows will be recognised and formally presented their certificates during the international surgical conference.")
    }
)

@Composable
private fun SecretariatCard(a: Actions) {
    SsplCard {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("PROGRAMME INQUIRIES", style = AppTheme.typography.overline)
            Text("SSPL Secretariat", style = AppTheme.typography.titleMedium.copy(color = AppTheme.colors.heading))
            P("For enrolment, portal logbook assistance or SAGES certificate submission:")
            FactRow(SsplIcons.Shield, "Focal person", "Mr. Ahmad Qasim · Society Secretary, SSP Lahore")
            Row(Modifier.fillMaxWidth().clip(AppTheme.shapes.small).clickable(onClickLabel = "WhatsApp secretariat") { a.url(SECRETARIAT_WHATSAPP) }) {
                FactRow(SsplIcons.Call, "Call / WhatsApp", "+92 333 425 4002")
            }
            Row(Modifier.fillMaxWidth().clip(AppTheme.shapes.small).clickable(onClickLabel = "Email secretariat") { a.url("mailto:$SECRETARIAT_EMAIL") }) {
                FactRow(SsplIcons.Email, "Official email", SECRETARIAT_EMAIL)
            }
        }
    }
}
