package com.sspl.ui.about

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.sspl.Screen
import com.sspl.designsystem.components.AvatarImage
import com.sspl.designsystem.components.FactRow
import com.sspl.designsystem.components.IconTile
import com.sspl.designsystem.components.NavTile
import com.sspl.designsystem.components.SectionHeader
import com.sspl.designsystem.components.SkeletonLine
import com.sspl.designsystem.components.SsplRefreshBox
import com.sspl.designsystem.components.StatTile
import com.sspl.designsystem.theme.AppTheme
import com.sspl.platform.shared.UrlOpener
import com.sspl.ui.components.InfoSurface
import com.sspl.ui.components.PageIntroHero
import com.sspl.ui.contact.ContactInfo
import com.sspl.ui.contact.SOCIAL_LINKS
import com.sspl.ui.council.CABINET_AND_EXECUTIVES
import com.sspl.ui.council.CABINET_TERM_LABEL
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

// ---- Static copy (web: pages/About.jsx, data/homepage/missionVision.ts, pillars.ts, stats.ts) ----

private const val MISSION =
    "The Society of Surgeons of Pakistan (Lahore) is a professional academic organization committed to the promotion of excellence in surgical practice, education, and research. The Society provides a structured platform for surgeons and medical professionals to share clinical experience, discuss challenging cases, and stay updated with advancements in surgical sciences."
private const val VISION =
    "Through regular clinical discussions and academic engagements, the Society aims to enhance professional competence, encourage ethical surgical practice, and promote collaboration among surgeons for the betterment of patient care."

private val MEMBERSHIP_INTRO = listOf(
    "Surgeons and medical professionals are encouraged to become members of the Society and actively participate in its academic and professional activities. The Society regularly conducts monthly clinical meetings, where clinical cases, recent advances, and practical experiences are discussed in an interactive and educational environment. In addition, the Society organizes seminars, hands-on workshops, conferences, and scientific sessions from time to time.",
    "Members are entitled to attend and actively participate in the Society's academic activities, including monthly clinical meetings. Registration fees for conferences, workshops, hands-on courses, and other special academic events are charged separately and determined according to the nature of each event. SSPL members are eligible for discounted registration fees for selected events, subject to the criteria and policies defined for each activity."
)

private const val OBJECTIVE =
    "We offer professional memberships and registrations for surgical events and workshops. Our activities are purely academic and professional in nature, aimed at promoting surgical education and collaboration."
private const val POLICY =
    "Memberships and event registrations may be cancelled up to ten (10) days before the scheduled event. After this period, cancellations or refunds will not be possible due to administrative and logistical commitments."

private data class Pillar(val icon: ImageVector, val title: String, val body: String, val route: String)

private val PILLARS = listOf(
    Pillar(Icons.Default.Groups, "Membership", "Join a distinguished community of surgeons dedicated to professional growth.", Screen.MembershipPlansScreen.route),
    Pillar(Icons.Default.WorkspacePremium, "Fellowship", "Structured fellowship with accredited centres and mentorship.", Screen.FellowshipHubScreen.route),
    Pillar(Icons.Default.CalendarMonth, "Conferences", "National and regional congresses with world-class faculty.", Screen.ConferencesScreen.route),
    Pillar(Icons.AutoMirrored.Filled.Article, "Publications", "Society notices, guidelines and announcements.", Screen.NotificationsScreen.route)
)

private val STATS = listOf(
    Triple("1,200+", "Members", Icons.Default.Groups),
    Triple("150+", "CME activities / year", Icons.Default.MenuBook),
    Triple("45+", "Training programs", Icons.Default.WorkspacePremium),
    Triple("25+", "Years of service", Icons.Default.Schedule)
)

@Composable
fun AboutScreen(
    navController: NavController,
    viewModel: AboutSocietyViewModel = koinViewModel(),
    urlOpener: UrlOpener = koinInject()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    fun open(route: String) = navController.navigate(route) { launchSingleTop = true }

    SsplRefreshBox(
        isRefreshing = state.isRefreshing,
        onRefresh = viewModel::refresh,
        modifier = Modifier.fillMaxSize().background(AppTheme.colors.background)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(22.dp)
        ) {
            item(key = "hero") {
                PageIntroHero(
                    eyebrow = "Heritage · Leadership · Healing",
                    title = "Advancing surgery.\nServing Pakistan.",
                    summary = "Society of Surgeons of Pakistan, Lahore Chapter — advancing surgical excellence through education, research, ethics, and collaboration.",
                    stats = listOf("1,200+" to "Members", "150+" to "CME / year", "25+" to "Years")
                )
            }

            item(key = "mv") {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    SectionHeader(title = "Mission and vision")
                    MissionCard(Icons.Default.Flag, "Mission", MISSION)
                    MissionCard(Icons.Default.Visibility, "Vision", VISION)
                }
            }

            item(key = "pillars") {
                Column {
                    SectionHeader(title = "What we do")
                    Spacer(Modifier.height(8.dp))
                    PILLARS.chunked(2).forEach { row ->
                        Row(
                            Modifier.fillMaxWidth().padding(bottom = 10.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            row.forEach { p ->
                                NavTile(
                                    icon = p.icon,
                                    title = p.title,
                                    subtitle = p.body,
                                    onClick = { open(p.route) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }

            item(key = "stats") {
                Column {
                    SectionHeader(title = "SSP Lahore in numbers")
                    Spacer(Modifier.height(8.dp))
                    STATS.chunked(2).forEach { row ->
                        Row(
                            Modifier.fillMaxWidth().padding(bottom = 10.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            row.forEach { (v, l, i) -> StatTile(value = v, label = l, icon = i, modifier = Modifier.weight(1f)) }
                        }
                    }
                }
            }

            item(key = "membership") { MembershipCard() }

            item(key = "objectives") {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    SectionHeader(title = "Objectives and policies")
                    InfoSurface {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconTile(Icons.Default.TrackChanges)
                            Spacer(Modifier.width(12.dp))
                            Text("Objective", style = AppTheme.typography.titleSmall)
                        }
                        Spacer(Modifier.height(10.dp))
                        Text(OBJECTIVE, style = AppTheme.typography.bodyMedium.copy(color = AppTheme.colors.body))
                    }
                    InfoSurface {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconTile(Icons.Default.Gavel, tint = AppTheme.colors.info, background = AppTheme.colors.infoContainer)
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text("Returns and cancellation", style = AppTheme.typography.titleSmall)
                                Text("10-day policy", style = AppTheme.typography.labelMedium.copy(color = AppTheme.colors.primary))
                            }
                        }
                        Spacer(Modifier.height(10.dp))
                        Text(POLICY, style = AppTheme.typography.bodyMedium.copy(color = AppTheme.colors.body))
                    }
                }
            }

            item(key = "leaders") {
                Column {
                    SectionHeader(
                        title = "Office bearers $CABINET_TERM_LABEL",
                        actionText = "Full council",
                        onActionClick = { open(Screen.ExecutiveCouncilScreen.route) }
                    )
                    Spacer(Modifier.height(8.dp))
                    InfoSurface(padding = 6.dp) {
                        listOf("President", "Vice President", "General Secretary").mapNotNull { r ->
                            CABINET_AND_EXECUTIVES.firstOrNull { it.role == r }
                        }.forEach { m ->
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .clip(AppTheme.shapes.small)
                                    .clickable { open(Screen.ExecutiveCouncilScreen.route) }
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AvatarImage(url = m.imageUrl, name = m.name, size = 48.dp)
                                Spacer(Modifier.width(12.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(m.name, style = AppTheme.typography.titleSmall)
                                    Text(m.role, style = AppTheme.typography.labelMedium.copy(color = AppTheme.colors.accent))
                                }
                            }
                        }
                    }
                }
            }

            item(key = "contact") {
                Column {
                    SectionHeader(
                        title = "Official contact",
                        actionText = "Contact us",
                        onActionClick = { open(Screen.ContactUsScreen.route) }
                    )
                    Spacer(Modifier.height(8.dp))
                    OfficialContactCard(state.contactLoading, state.contact, urlOpener::openUrl)
                }
            }

            item(key = "payment") { PaymentInfoCard() }

            item(key = "history") {
                NavTile(
                    icon = Icons.Default.MenuBook,
                    title = "Our history",
                    subtitle = "From the 1950s Association of Surgeons of Lahore to today",
                    onClick = { open(Screen.HistoryScreen.route) }
                )
            }
        }
    }
}

@Composable
private fun MissionCard(icon: ImageVector, title: String, body: String) {
    InfoSurface {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconTile(icon)
            Spacer(Modifier.width(12.dp))
            Text(title, style = AppTheme.typography.titleSmall)
        }
        Spacer(Modifier.height(10.dp))
        Text(body, style = AppTheme.typography.bodyMedium.copy(color = AppTheme.colors.body))
    }
}

@Composable
private fun MembershipCard() {
    var expanded by remember { mutableStateOf(false) }
    InfoSurface {
        Text("MEMBERSHIP", style = AppTheme.typography.overline)
        Spacer(Modifier.height(4.dp))
        Text("Be part of the Society", style = AppTheme.typography.titleMedium.copy(color = AppTheme.colors.heading))
        Spacer(Modifier.height(8.dp))
        Text(
            MEMBERSHIP_INTRO.first(),
            style = AppTheme.typography.bodyMedium.copy(color = AppTheme.colors.body),
            maxLines = if (expanded) Int.MAX_VALUE else 4,
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
        )
        if (expanded) {
            MEMBERSHIP_INTRO.drop(1).forEach {
                Spacer(Modifier.height(8.dp))
                Text(it, style = AppTheme.typography.bodyMedium.copy(color = AppTheme.colors.body))
            }
        }
        Text(
            if (expanded) "Show less" else "Read more",
            style = AppTheme.typography.labelMedium.copy(color = AppTheme.colors.primary, fontWeight = FontWeight.SemiBold),
            modifier = Modifier
                .clip(AppTheme.shapes.small)
                .clickable { expanded = !expanded }
                .heightIn(min = 40.dp)
                .padding(top = 12.dp, end = 12.dp)
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun OfficialContactCard(loading: Boolean, contact: ContactInfo, openUrl: (String) -> Unit) {
    InfoSurface {
        if (loading) {
            repeat(3) {
                SkeletonLine(widthFraction = 0.4f, height = 10.dp)
                Spacer(Modifier.height(6.dp))
                SkeletonLine(widthFraction = 0.85f)
                Spacer(Modifier.height(14.dp))
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                ContactLine(Icons.Default.Place, "Office address · ${contact.location}", contact.address) { openUrl(contact.mapUrl) }
                ContactLine(Icons.Default.Phone, "Phone and WhatsApp", contact.phone) { openUrl(contact.telUri) }
                ContactLine(Icons.Default.Email, "Email", contact.email) { openUrl(contact.mailUri) }
            }
            Spacer(Modifier.height(10.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SOCIAL_LINKS.forEach { s ->
                    Row(
                        Modifier
                            .clip(AppTheme.shapes.chip)
                            .background(AppTheme.colors.primaryContainer)
                            .clickable { openUrl(s.url) }
                            .heightIn(min = 40.dp)
                            .padding(horizontal = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Public, contentDescription = null, tint = AppTheme.colors.onPrimaryContainer, modifier = Modifier.padding(end = 6.dp).width(16.dp))
                        Text(s.label, style = AppTheme.typography.labelMedium.copy(color = AppTheme.colors.onPrimaryContainer, fontWeight = FontWeight.SemiBold))
                    }
                }
            }
        }
    }
}

@Composable
private fun ContactLine(icon: ImageVector, label: String, value: String, onClick: () -> Unit) {
    FactRow(
        icon = icon,
        label = label,
        value = value,
        modifier = Modifier
            .clip(AppTheme.shapes.small)
            .clickable(onClick = onClick)
            .heightIn(min = 52.dp)
            .padding(vertical = 6.dp)
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PaymentInfoCard() {
    val colors = AppTheme.colors
    Column(
        Modifier
            .fillMaxWidth()
            .clip(AppTheme.shapes.card)
            .background(colors.surfaceTint)
            .border(1.dp, colors.border.copy(alpha = 0.7f), AppTheme.shapes.card)
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconTile(Icons.Default.CreditCard, background = colors.surface)
            Spacer(Modifier.width(12.dp))
            Column {
                Text("Payment information", style = AppTheme.typography.titleSmall)
                Text("Charged in Pakistani rupees (PKR)", style = AppTheme.typography.labelMedium.copy(color = colors.primary))
            }
        }
        Spacer(Modifier.height(10.dp))
        Text(
            "All payments made through SSPL are processed in Pakistani Rupees (PKR). We accept all major Pakistani debit and credit cards issued by local banks.",
            style = AppTheme.typography.bodyMedium.copy(color = colors.body)
        )
        Spacer(Modifier.height(12.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("Visa", "Mastercard", "Local bank").forEach {
                Text(
                    it,
                    style = AppTheme.typography.labelMedium.copy(color = colors.body),
                    modifier = Modifier
                        .clip(AppTheme.shapes.chip)
                        .background(colors.surface)
                        .border(1.dp, colors.border, AppTheme.shapes.chip)
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }
    }
}

