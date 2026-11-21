package com.sspl.ui.conference

import com.sspl.designsystem.icons.SsplIcons

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.Phone
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Icon
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.sspl.Screen
import com.sspl.core.models.EventRole
import com.sspl.core.models.eventSchedulePdfUrl
import com.sspl.designsystem.components.ChipRow
import com.sspl.designsystem.components.ImageSources
import com.sspl.designsystem.components.BadgeVariant
import com.sspl.designsystem.components.ErrorState
import com.sspl.designsystem.components.ExpandableText
import com.sspl.designsystem.components.FactRow
import com.sspl.designsystem.components.GlassPill
import com.sspl.designsystem.components.IconTile
import com.sspl.designsystem.components.ListRowCard
import com.sspl.designsystem.components.LocalShellActions
import com.sspl.designsystem.components.OutlineButton
import com.sspl.designsystem.components.SectionHeader
import com.sspl.designsystem.components.SegmentedTabs
import com.sspl.designsystem.components.SkeletonDetail
import com.sspl.designsystem.components.SsplCard
import com.sspl.designsystem.components.SsplImage
import com.sspl.designsystem.components.StatusBadge
import com.sspl.designsystem.components.StickyCtaBar
import com.sspl.designsystem.theme.AppTheme
import com.sspl.platform.shared.UrlOpener
import com.sspl.ui.conference.components.AddOnSetsList
import com.sspl.ui.conference.components.SessionScheduleRow
import com.sspl.utils.DisplayDates
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

private enum class DetailTab(val label: String) { OVERVIEW("Overview"), SCHEDULE("Schedule"), FEES("Fees"), ADDONS("Add-ons") }

/**
 * C2: Event detail. Receives ONLY `eventId` (Long) via the previous entry's savedStateHandle;
 * the event comes from the repository cache instantly and is refreshed from
 * GET /external/events/{id}. (Putting the event JSON in saved state carried the base64 image
 * into the Android Bundle → TransactionTooLargeException on background.)
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun EventDetailScreen(
    navController: NavController,
    viewModel: EventDetailViewModel = koinViewModel(),
    urlOpener: UrlOpener = koinInject()
) {
    val eventIdArg = remember { navController.previousBackStackEntry?.savedStateHandle?.get<Long>("eventId") ?: 0L }
    LaunchedEffect(eventIdArg) { viewModel.loadEvent(eventIdArg) }

    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val shell = LocalShellActions.current
    val event = state.event
    val card = state.card

    // Re-check registration when coming back from registration / payment (screen re-enters composition).
    LaunchedEffect(Unit) { if (viewModel.uiState.value.event != null) viewModel.checkRegistration() }

    if (event == null || card == null) {
        if (state.loadError != null) {
            ErrorState(
                title = "Event unavailable",
                message = state.loadError ?: "",
                onRetry = viewModel::retry,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            SkeletonDetail(Modifier.fillMaxSize())
        }
        return
    }

    val tabs = remember(event) {
        buildList {
            add(DetailTab.OVERVIEW)
            if (event.sessions.isNotEmpty()) add(DetailTab.SCHEDULE)
            if (event.roles.isNotEmpty()) add(DetailTab.FEES)
            if (event.addOnSets.isNotEmpty()) add(DetailTab.ADDONS)
        }
    }
    var tabIndex by rememberSaveable { mutableIntStateOf(0) }
    var scheduleSessionId by rememberSaveable { mutableLongStateOf(-1L) }
    val tab = tabs.getOrElse(tabIndex) { DetailTab.OVERVIEW }

    val reg = state.registrationData
    val isRegistered = reg?.isRegistered == true

    fun openRegistration() {
        navController.currentBackStackEntry?.savedStateHandle?.set("eventId", event.effectiveId)
        navController.navigate(Screen.EventRegistrationScreen.route) { launchSingleTop = true }
    }

    Box(Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 110.dp)
        ) {
            item(key = "hero") { DetailHero(card) }
            item(key = "facts") {
                FactsCard(card, state.minFee, DisplayDates.medium(event.registrationDeadline), event.maxAttendees, Modifier.padding(start = 16.dp, end = 16.dp, top = 14.dp))
            }
            if (tabs.size > 1) {
                stickyHeader(key = "tabs") {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .background(AppTheme.colors.background)
                            .padding(horizontal = 16.dp, vertical = 10.dp)
                    ) {
                        SegmentedTabs(
                            options = tabs.map { it.label },
                            selectedIndex = tabs.indexOf(tab),
                            onSelect = { tabIndex = it },
                            counts = tabs.map {
                                when (it) {
                                    DetailTab.SCHEDULE -> event.sessions.count { s -> !s.isBreak }
                                    DetailTab.FEES -> event.roles.size
                                    else -> null
                                }
                            }
                        )
                    }
                }
            } else {
                item(key = "gap") { Spacer(Modifier.height(10.dp)) }
            }

            when (tab) {
                DetailTab.OVERVIEW -> {
                    if (isRegistered) {
                        item(key = "registered") {
                            // Web EventDetail.jsx: PENDING → "Pending admin approval" (fee 0) / "Pending payment".
                            val r = reg?.registration
                            val pending = r?.paymentStatus.equals("PENDING", ignoreCase = true)
                            val status = when {
                                r == null -> "Registered"
                                r.isConfirmed -> "Confirmed"
                                pending && r.feeAmount == 0.0 -> "Pending admin approval"
                                pending -> "Pending payment"
                                else -> r.paymentStatus.lowercase().replaceFirstChar { it.uppercase() }
                            }
                            val roleName = r?.role?.name?.takeIf { it.isNotBlank() }
                            ListRowCard(
                                title = if (pending) "Registration submitted" else "You're registered",
                                subtitle = listOfNotNull(
                                    roleName?.let { "Role: $it" },
                                    "Sessions, attendance, feedback and certificates are in the Event Portal once confirmed."
                                ).joinToString(" · "),
                                leading = {
                                    if (pending) IconTile(SsplIcons.HourglassTop, tint = AppTheme.colors.warning, background = AppTheme.colors.warningContainer)
                                    else IconTile(SsplIcons.CheckCircle, tint = AppTheme.colors.success, background = AppTheme.colors.successContainer)
                                },
                                trailing = {
                                    if (r?.isConfirmed == true) StatusBadge(status, variant = BadgeVariant.SUCCESS)
                                    else if (pending) StatusBadge(status, variant = BadgeVariant.WARNING)
                                    else StatusBadge(status)
                                },
                                onClick = { navController.navigate(Screen.EventPortalScreen.route) { launchSingleTop = true } },
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                            )
                        }
                    } else if (reg != null && !reg.canRegister) {
                        item(key = "cannot") {
                            ListRowCard(
                                title = "Registration unavailable",
                                subtitle = reg.reason?.takeIf { it.isNotBlank() }
                                    ?: "You can't register for this event right now. Check My registrations for an active booking.",
                                leading = { IconTile(SsplIcons.HourglassTop, tint = AppTheme.colors.warning, background = AppTheme.colors.warningContainer) },
                                onClick = { navController.navigate(Screen.MyRegistrationsScreen.route) { launchSingleTop = true } },
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                            )
                        }
                    }
                    item(key = "about") {
                        SsplCard(Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                            Text("ABOUT THIS EVENT", style = AppTheme.typography.overline)
                            Spacer(Modifier.height(6.dp))
                            ExpandableText(event.effectiveDescription, maxLines = 6)
                        }
                    }
                    val dates = listOfNotNull(
                        event.abstractDeadline?.takeIf { event.isAbstractEnabled }?.let { "Abstract deadline" to DisplayDates.medium(it) },
                        event.presentationDeadline?.takeIf { event.isPresentationEnabled }?.let { "Presentation deadline" to DisplayDates.medium(it) }
                    ).filter { it.second.isNotBlank() }
                    if (dates.isNotEmpty()) {
                        item(key = "dates") {
                            SsplCard(Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                                Text("KEY DATES", style = AppTheme.typography.overline)
                                Spacer(Modifier.height(8.dp))
                                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    dates.forEach { (label, value) -> FactRow(SsplIcons.EventAvailable, label, value) }
                                }
                            }
                        }
                    }
                    if (event.showAbstractCall && card.status != "past") {
                        item(key = "abstract") {
                            ListRowCard(
                                title = "Abstract submissions open",
                                subtitle = if (shell.isGuest) "Sign in to submit your abstract for this event." else "Submit your abstract from the member portal.",
                                leading = { IconTile(SsplIcons.Description, tint = AppTheme.colors.accent, background = AppTheme.colors.infoContainer) },
                                onClick = {
                                    if (shell.isGuest) shell.onAvatar()
                                    else navController.navigate(Screen.AbstractSubmissionScreen.route) { launchSingleTop = true }
                                },
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                            )
                        }
                    }
                    val contacts = listOfNotNull(
                        event.contactEmail?.takeIf { it.isNotBlank() }?.let { Triple(SsplIcons.Email, "Email", it) },
                        event.contactPhone?.takeIf { it.isNotBlank() }?.let { Triple(SsplIcons.Phone, "Phone", it) }
                    )
                    if (contacts.isNotEmpty()) {
                        item(key = "contact") {
                            SsplCard(Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                                Text("EVENT CONTACT", style = AppTheme.typography.overline)
                                Spacer(Modifier.height(8.dp))
                                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    contacts.forEach { (icon, label, value) ->
                                        Box(Modifier.fillMaxWidth().clip(AppTheme.shapes.small).clickable {
                                            urlOpener.openUrl(if (label == "Email") "mailto:$value" else "tel:${value.filter { c -> c.isDigit() || c == '+' }}")
                                        }) {
                                            FactRow(icon, label, value)
                                        }
                                    }
                                }
                            }
                        }
                    }
                    if (tabs.size > 1) {
                        item(key = "glance") {
                            Row(
                                Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                if (event.sessions.isNotEmpty()) GlanceTile("${event.sessions.count { !it.isBreak }}", "Sessions", Modifier.weight(1f))
                                if (event.roles.isNotEmpty()) GlanceTile("${event.roles.size}", "Fee categories", Modifier.weight(1f))
                                if (event.addOnSets.isNotEmpty()) GlanceTile("${event.addOnSets.sumOf { it.addOns.orEmpty().size }}", "Add-ons", Modifier.weight(1f))
                            }
                        }
                    }
                    val webLink = event.webLink?.takeIf { it.isNotBlank() }
                    val documents = event.validDocuments
                    if (webLink != null || documents.isNotEmpty()) {
                        item(key = "docsHeader") {
                            SectionHeader(
                                title = "Documents & downloads",
                                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 14.dp)
                            )
                        }
                    }
                    if (webLink != null) {
                        item(key = "web") {
                            ListRowCard(
                                title = "Downloadables (Google Drive)",
                                subtitle = "Access event materials",
                                leading = { IconTile(SsplIcons.Download) },
                                trailing = {
                                    Icon(SsplIcons.OpenInNew, contentDescription = "Open link", tint = AppTheme.colors.muted)
                                },
                                onClick = { urlOpener.openUrl(webLink) },
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 5.dp)
                            )
                        }
                    }
                    itemsIndexed(documents, key = { i, d -> "doc_${d.id ?: i}" }) { _, doc ->
                        val url = resolveDocumentUrl(doc.fileUrl)
                        ListRowCard(
                            title = doc.title?.takeIf { it.isNotBlank() } ?: "Event document",
                            subtitle = doc.type?.takeIf { it.isNotBlank() }?.replace('_', ' ')?.lowercase()
                                ?.replaceFirstChar { it.uppercase() },
                            leading = { IconTile(SsplIcons.Description) },
                            trailing = {
                                Icon(SsplIcons.Download, contentDescription = "Download document", tint = AppTheme.colors.primary)
                            },
                            onClick = url?.let { u -> { urlOpener.openUrl(u) } },
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 5.dp)
                        )
                    }
                    if (state.relatedWorkshops.isNotEmpty()) {
                        item(key = "wsHeader") {
                            SectionHeader(
                                title = "Pre-event workshops",
                                actionText = "View all",
                                onActionClick = { navController.navigate(Screen.WorkshopsScreen.route) { launchSingleTop = true } },
                                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 14.dp)
                            )
                        }
                        items(state.relatedWorkshops, key = { "ws_${it.id}" }) { w ->
                            ListRowCard(
                                title = w.title,
                                subtitle = listOf(w.dateRange, w.venue).filter { it.isNotBlank() }.joinToString(" · "),
                                leading = { IconTile(SsplIcons.School) },
                                trailing = { if (w.registrationOpen) StatusBadge("Open", variant = BadgeVariant.SUCCESS) },
                                onClick = { navController.openEventDetail(w.id) },
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 5.dp)
                            )
                        }
                    }
                }

                DetailTab.SCHEDULE -> {
                    // Web ConferenceSchedule.jsx: "All sessions (n)" + "Session i: title" tabs and a PDF download.
                    val realSessions = state.sessions.filter { !it.isBreak }
                    val selectedSession = realSessions.firstOrNull { it.id == scheduleSessionId }
                    item(key = "schedActions") {
                        Column(Modifier.padding(top = 4.dp, bottom = 6.dp)) {
                            if (realSessions.size > 1) {
                                val allLabel = "All sessions (${realSessions.size})"
                                val labels = realSessions.mapIndexed { idx, s ->
                                    "Session ${idx + 1}: ${s.title.ifBlank { "Untitled" }}"
                                }
                                ChipRow(
                                    options = listOf(allLabel) + labels,
                                    selected = selectedSession?.let { labels[realSessions.indexOf(it)] } ?: allLabel,
                                    onSelect = { label ->
                                        val idx = labels.indexOf(label)
                                        scheduleSessionId = if (idx >= 0) realSessions[idx].id else -1L
                                    }
                                )
                                Spacer(Modifier.height(10.dp))
                            }
                            OutlineButton(
                                text = if (selectedSession != null) "Download session schedule (PDF)" else "Download schedule (PDF)",
                                onClick = { urlOpener.openUrl(eventSchedulePdfUrl(event.effectiveId, selectedSession?.id)) },
                                icon = SsplIcons.Download,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                    }
                    val visibleSessions = if (selectedSession != null) listOf(selectedSession) else state.sessions
                    var lastDay = ""
                    visibleSessions.forEachIndexed { i, s ->
                        val day = DisplayDates.medium(s.startsAt)
                        if (day.isNotBlank() && day != lastDay) {
                            lastDay = day
                            item(key = "day_$day") {
                                Text(
                                    "${DisplayDates.weekdayShort(s.startsAt)} · $day".uppercase(),
                                    style = AppTheme.typography.overline,
                                    modifier = Modifier.padding(start = 20.dp, end = 16.dp, top = 10.dp, bottom = 4.dp)
                                )
                            }
                        }
                        item(key = "s_${s.id}_$i") {
                            SessionScheduleRow(
                                session = s,
                                onClick = if (s.isBreak) null else {
                                    {
                                        navController.currentBackStackEntry?.savedStateHandle?.set("data", Json.encodeToString(s))
                                        navController.navigate(Screen.SessionDetailScreen.route) { launchSingleTop = true }
                                    }
                                },
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 5.dp)
                            )
                        }
                    }
                }

                DetailTab.FEES -> {
                    items(event.roles, key = { "role_${it.id}" }) { role ->
                        RoleFeeCard(role, Modifier.padding(horizontal = 16.dp, vertical = 5.dp))
                    }
                    item(key = "feeNote") {
                        Text(
                            "Choose your category on the next step. Early-bird prices apply automatically until the deadline.",
                            style = AppTheme.typography.bodySmall,
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                        )
                    }
                }

                DetailTab.ADDONS -> item(key = "addons") {
                    AddOnSetsList(event.addOnSets, Modifier.padding(horizontal = 16.dp, vertical = 5.dp))
                }
            }
        }

        // ---- Sticky CTA ------------------------------------------------------------------
        val fee = state.minFee
        val priceText = when {
            isRegistered -> null
            fee == null -> null
            fee <= 0.0 -> "Free"
            event.roles.size > 1 -> formatPkr(fee)
            else -> formatPkr(fee)
        }
        val priceSub = if (priceText != null && event.roles.size > 1 && (fee ?: 0.0) > 0.0) "From" else if (priceText != null) "Fee" else null
        val cta: Triple<String, Boolean, () -> Unit> = when {
            isRegistered -> Triple("View my registration", true, { navController.navigate(Screen.MyRegistrationsScreen.route) { launchSingleTop = true } })
            !card.registrationOpen -> Triple(if (card.status == "past") "Event has ended" else "Registration closed", false, {})
            shell.isGuest -> Triple("Sign in to register", true, shell.onAvatar)
            reg != null && !reg.canRegister -> Triple("Registration unavailable", false, {})
            // Web: `pgEvent.is_payment_enabled ? "Pay Now" : "Register Now"`.
            else -> Triple(if (event.isPaymentEnabled == true) "Pay now" else "Register now", true, { openRegistration() })
        }
        val (ctaText, ctaEnabled, onCta) = cta
        StickyCtaBar(
            primaryText = ctaText,
            onPrimaryClick = onCta,
            enabled = ctaEnabled,
            priceText = priceText,
            priceSubtext = priceSub,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                // The shell's bottom bar already pads for the navigation bar.
                .consumeWindowInsets(WindowInsets.navigationBars)
        )
    }
}

@Composable
private fun DetailHero(card: EventCardUi) {
    Box(Modifier.fillMaxWidth().aspectRatio(16f / 10f)) {
        SsplImage(
            source = card.image,
            contentDescription = card.title,
            modifier = Modifier.fillMaxSize(),
            fallbackIcon = SsplIcons.Event,
            fallbackIconSize = 52.dp
        )
        Box(
            Modifier.matchParentSize().background(
                Brush.verticalGradient(
                    0f to Color.Black.copy(alpha = 0.10f),
                    0.45f to Color.Transparent,
                    1f to Color.Black.copy(alpha = 0.78f)
                )
            )
        )
        Column(Modifier.align(Alignment.BottomStart).padding(16.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                GlassPill(card.typeLabel.uppercase())
                GlassPill(
                    when (card.status) {
                        "ongoing" -> "LIVE NOW"
                        "past" -> "COMPLETED"
                        else -> card.relative.ifBlank { "UPCOMING" }.uppercase()
                    },
                    icon = SsplIcons.Schedule
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                card.title,
                style = AppTheme.typography.headlineSmall.copy(color = Color.White),
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun FactsCard(card: EventCardUi, minFee: Double?, registrationDeadline: String, maxAttendees: Int?, modifier: Modifier = Modifier) {
    SsplCard(modifier) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            FactRow(SsplIcons.CalendarMonth, "Date", card.dateRange)
            if (card.timeRange.isNotBlank()) FactRow(SsplIcons.Schedule, "Time", card.timeRange)
            FactRow(SsplIcons.Place, "Venue", card.venue)
            // Web shows capacity + the registration deadline regardless of open/closed state.
            FactRow(SsplIcons.Groups, "Capacity", if (maxAttendees != null && maxAttendees > 0) "$maxAttendees guests" else "Open registration")
            if (registrationDeadline.isNotBlank()) FactRow(SsplIcons.EventAvailable, "Registration deadline", registrationDeadline)
            if (minFee != null) FactRow(SsplIcons.Sell, "Registration", if (minFee <= 0.0) "Free" else "From ${formatPkr(minFee)}")
        }
    }
}

@Composable
private fun GlanceTile(value: String, label: String, modifier: Modifier = Modifier) {
    val colors = AppTheme.colors
    Column(
        modifier
            .clip(AppTheme.shapes.small)
            .background(colors.surfaceTint)
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Text(value, style = AppTheme.typography.titleMedium.copy(color = colors.primary, fontWeight = FontWeight.Bold))
        Text(label, style = AppTheme.typography.labelSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
internal fun RoleFeeCard(role: EventRole, modifier: Modifier = Modifier) {
    val colors = AppTheme.colors
    Row(
        modifier
            .fillMaxWidth()
            .clip(AppTheme.shapes.card)
            .background(colors.surface)
            .border(1.dp, colors.border.copy(alpha = 0.7f), AppTheme.shapes.card)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(role.name, style = AppTheme.typography.titleSmall)
            role.description?.takeIf { it.isNotBlank() }?.let {
                Spacer(Modifier.height(2.dp))
                Text(it, style = AppTheme.typography.bodySmall, maxLines = 3, overflow = TextOverflow.Ellipsis)
            }
            if (role.isEarlyBirdActive) {
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    StatusBadge("Early bird", variant = BadgeVariant.WARNING)
                    DisplayDates.medium(role.earlyBirdDeadline).takeIf { it.isNotBlank() }?.let {
                        Spacer(Modifier.width(6.dp))
                        Text("until $it", style = AppTheme.typography.labelSmall)
                    }
                }
            }
        }
        Spacer(Modifier.width(10.dp))
        Column(horizontalAlignment = Alignment.End) {
            Text(formatPkr(role.currentFee), style = AppTheme.typography.titleMedium.copy(color = colors.primary, fontWeight = FontWeight.Bold))
            if (role.isEarlyBirdActive && role.fee > role.currentFee) {
                Text(formatPkr(role.fee), style = AppTheme.typography.labelSmall.copy(textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough))
            }
        }
    }
}

/** documents[].file_url → absolute URL (web: http… as-is, else EXTERNAL_API + "/" + file_url). */
private fun resolveDocumentUrl(path: String?): String? =
    ImageSources.resolveUrl(path?.replace('\\', '/'))
