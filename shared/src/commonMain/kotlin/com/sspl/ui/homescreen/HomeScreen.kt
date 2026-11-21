package com.sspl.ui.homescreen

import com.sspl.designsystem.icons.SsplIcons

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CardMembership
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.HowToVote
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material.icons.outlined.Notifications
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.sspl.Screen
import com.sspl.core.models.EventItem
import com.sspl.core.models.FellowshipStats
import com.sspl.core.models.SocietyNewsItem
import com.sspl.designsystem.components.AvatarImage
import com.sspl.designsystem.components.BannerCarousel
import com.sspl.designsystem.components.DecorativeRings
import com.sspl.designsystem.components.ErrorState
import com.sspl.designsystem.components.GlassIconButton
import com.sspl.designsystem.components.GlassPill
import com.sspl.designsystem.components.GradientHeroCard
import com.sspl.designsystem.components.HeroStat
import com.sspl.designsystem.components.IconTile
import com.sspl.designsystem.components.ListRowCard
import com.sspl.designsystem.components.LoadState
import com.sspl.designsystem.components.LocalShellActions
import com.sspl.designsystem.components.NavTile
import com.sspl.designsystem.components.QuickActionTile
import com.sspl.designsystem.components.SectionHeader
import com.sspl.designsystem.components.SkeletonBox
import com.sspl.designsystem.components.SkeletonMediaCard
import com.sspl.designsystem.components.SsplImage
import com.sspl.designsystem.components.SsplRefreshBox
import com.sspl.designsystem.theme.AppTheme
import com.sspl.ui.about.ABOUT_MISSION
import com.sspl.ui.about.ABOUT_VISION
import com.sspl.ui.council.CABINET_AND_EXECUTIVES
import com.sspl.ui.council.CABINET_TERM_LABEL
import com.sspl.ui.council.CouncilMember
import com.sspl.ui.fellowship.FellowshipNavArgs
import com.sspl.ui.fellowship.Program
import com.sspl.utils.DisplayDates
import org.koin.compose.viewmodel.koinViewModel

private data class QuickAction(
    val icon: ImageVector,
    val label: String,
    val requiresAuth: Boolean = false,
    val go: () -> Unit
)

/**
 * B1: Home.
 * Hero header (draws behind status bar) → banners overlapping the hero edge (max 3) →
 * quick actions → featured event + carousel → education (fellowship + open programmes) → news →
 * leadership strip → mission/vision → society → help.
 */
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeScreenViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val shell = LocalShellActions.current

    fun open(route: String) = navController.navigate(route) { launchSingleTop = true }
    fun openEvent(e: EventItem) {
        // Pass only the id (never the event JSON: it carries a multi-MB base64 image).
        navController.currentBackStackEntry?.savedStateHandle?.set("eventId", e.effectiveId)
        open(Screen.EventDetailScreen.route)
    }
    fun gated(action: () -> Unit) = if (shell.isGuest) shell.onAvatar() else action()
    // Same hand-off FellowshipHubScreen uses (routes carry no args).
    fun applyFellow(programId: Long?) {
        FellowshipNavArgs.programId = programId
        FellowshipNavArgs.centerId = null
        open(Screen.ApplyAsFellowScreen.route)
    }
    fun applyCenter() {
        FellowshipNavArgs.programId = null
        open(Screen.ApplyAsTrainingCentreScreen.route)
    }

    val quickActions = listOf(
        QuickAction(SsplIcons.Event, "Events") { open(Screen.ScientificProgramsScreen.route) },
        QuickAction(SsplIcons.CalendarMonth, "Conferences") { open(Screen.ConferencesScreen.route) },
        QuickAction(SsplIcons.School, "Workshops") { open(Screen.WorkshopsScreen.route) },
        QuickAction(SsplIcons.WorkspacePremium, "Fellowship") { open(Screen.FellowshipHubScreen.route) },
        QuickAction(SsplIcons.Assignment, "My events", requiresAuth = true) { gated { open(Screen.MyRegistrationsScreen.route) } },
        QuickAction(SsplIcons.Badge, "Certificates", requiresAuth = true) { gated { open(Screen.MyCertificatesScreen.route) } },
        QuickAction(SsplIcons.Verified, "Verify") { open(Screen.VerifyCertificateScreen.route) },
        QuickAction(SsplIcons.Download, "Downloads") { open(Screen.DownloadsScreen.route) }
    )

    SsplRefreshBox(
        isRefreshing = state.isRefreshing,
        onRefresh = viewModel::refresh,
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 28.dp)
        ) {
            // ---- Hero + banners ---------------------------------------------------------
            item(key = "hero") {
                val banners = state.banners
                val hasBanners = banners is LoadState.Ready && banners.data.isNotEmpty()
                val bannersLoading = banners is LoadState.Loading
                Box {
                    HomeHero(bottomSpace = if (hasBanners || bannersLoading) 92.dp else 24.dp)
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .offset(y = if (hasBanners || bannersLoading) 0.dp else 0.dp)
                    ) {
                        when {
                            bannersLoading -> SkeletonBox(
                                Modifier
                                    .padding(horizontal = 20.dp)
                                    .fillMaxWidth()
                                    .aspectRatio(16f / 7f)
                                    .offset(y = 72.dp),
                                AppTheme.shapes.banner
                            )
                            hasBanners -> BannerCarousel(
                                banners = (banners as LoadState.Ready).data,
                                modifier = Modifier.offset(y = 72.dp)
                            )
                        }
                    }
                }
                if (hasBanners || bannersLoading) Spacer(Modifier.height(84.dp)) else Spacer(Modifier.height(8.dp))
            }

            // ---- Quick actions ---------------------------------------------------------
            item(key = "quick") {
                Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    SectionHeader(title = "Quick access")
                    Spacer(Modifier.height(6.dp))
                    quickActions.chunked(4).forEachIndexed { rowIndex, row ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            row.forEachIndexed { colIndex, qa ->
                                QuickActionTile(
                                    // Staggered draw-on wave across the grid.
                                    animationDelayMillis = (rowIndex * 4 + colIndex) * 70,
                                    icon = qa.icon,
                                    label = qa.label,
                                    onClick = qa.go,
                                    locked = qa.requiresAuth && shell.isGuest,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }

            // ---- Events -----------------------------------------------------------------
            item(key = "events") {
                Column(Modifier.padding(top = 12.dp)) {
                    SectionHeader(
                        title = "Happening next",
                        actionText = "All events",
                        onActionClick = { open(Screen.ScientificProgramsScreen.route) },
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    Spacer(Modifier.height(8.dp))
                    when (val ev = state.events) {
                        is LoadState.Loading -> Column(Modifier.padding(horizontal = 16.dp)) {
                            SkeletonMediaCard(Modifier.fillMaxWidth(), imageAspect = 2f)
                        }
                        is LoadState.Error -> ErrorState(
                            title = "Events didn't load",
                            message = ev.message,
                            onRetry = viewModel::retryEvents
                        )
                        is LoadState.Ready -> if (ev.data.isEmpty()) {
                            NoEventsCard(onBrowse = { open(Screen.EventArchiveScreen.route) })
                        } else {
                            FeaturedEventCard(
                                event = ev.data.first(),
                                onClick = { openEvent(ev.data.first()) },
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                            if (ev.data.size > 1) {
                                Spacer(Modifier.height(14.dp))
                                LazyRow(
                                    contentPadding = PaddingValues(horizontal = 16.dp),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    items(ev.data.drop(1), key = { it.effectiveId }) { e ->
                                        CompactEventCard(e, onClick = { openEvent(e) })
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // ---- Education & training (web EducationSection) --------------------------------
            item(key = "education") {
                Column(Modifier.padding(top = 24.dp)) {
                    SectionHeader(
                        title = "Education & training",
                        actionText = "Explore",
                        onActionClick = { open(Screen.FellowshipHubScreen.route) },
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    Spacer(Modifier.height(8.dp))
                    FellowshipImpactCard(
                        stats = state.stats,
                        onClick = { open(Screen.FellowshipHubScreen.route) },
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    when (val pr = state.programs) {
                        is LoadState.Loading -> {
                            Spacer(Modifier.height(12.dp))
                            Row(
                                Modifier.padding(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                repeat(2) { SkeletonBox(Modifier.width(240.dp).height(196.dp), AppTheme.shapes.card) }
                            }
                        }
                        is LoadState.Ready -> if (pr.data.isNotEmpty()) {
                            Spacer(Modifier.height(14.dp))
                            Text(
                                if (pr.data.size > 1) "OPEN MINI FELLOWSHIPS · ${pr.data.size}" else "OPEN MINI FELLOWSHIP",
                                style = AppTheme.typography.overline,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                            Spacer(Modifier.height(6.dp))
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(pr.data, key = { "prog_" + (it.id?.toString() ?: it.name) }) { prog ->
                                    ProgramPosterCard(prog, onClick = { applyFellow(prog.id) })
                                }
                            }
                        }
                        else -> Unit
                    }
                    Spacer(Modifier.height(12.dp))
                    Row(
                        Modifier.padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        HelpChip(SsplIcons.HowToReg, "Apply", { applyFellow(null) }, Modifier.weight(1f))
                        HelpChip(SsplIcons.Business, "Add centre", { applyCenter() }, Modifier.weight(1f))
                        HelpChip(SsplIcons.Apartment, "Centres", { open(Screen.TrainingCentresScreen.route) }, Modifier.weight(1f))
                    }
                }
            }

            // ---- News (web WhatsNewSection: 6 items) -------------------------------------
            if (state.news.isNotEmpty()) {
                item(key = "newsHeader") {
                    SectionHeader(
                        title = "Announcements",
                        actionText = "View all",
                        onActionClick = { open(Screen.NotificationsScreen.route) },
                        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 24.dp)
                    )
                }
                items(state.news, key = { "news_" + it.effectiveId }) { n ->
                    NewsRow(n, onClick = { open(Screen.NotificationsScreen.route) })
                }
            }

            // ---- Leadership (web LeadershipSection: President + cabinet strip) ----------------
            item(key = "leadership") {
                LeadershipSection(
                    onViewAll = { open(Screen.ExecutiveCouncilScreen.route) },
                    modifier = Modifier.padding(top = 24.dp)
                )
            }

            // ---- Mission & vision (web MissionVisionSection; same copy as About) -------------
            item(key = "missionVision") {
                Column(
                    Modifier.padding(start = 16.dp, end = 16.dp, top = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    SectionHeader(
                        title = "Mission and vision",
                        actionText = "About SSPL",
                        onActionClick = { open(Screen.AboutScreen.route) }
                    )
                    MissionVisionCard(SsplIcons.Flag, "Mission", ABOUT_MISSION, animationDelayMillis = 0)
                    MissionVisionCard(SsplIcons.Visibility, "Vision", ABOUT_VISION, animationDelayMillis = 90)
                }
            }

            // ---- Society ----------------------------------------------------------------
            item(key = "society") {
                Column(Modifier.padding(start = 16.dp, end = 16.dp, top = 24.dp)) {
                    SectionHeader(title = "Your society")
                    Spacer(Modifier.height(8.dp))
                    val tiles = listOf(
                        Triple(SsplIcons.AccountBalance, "About SSPL", Screen.AboutScreen.route),
                        Triple(SsplIcons.Groups, "Executive council", Screen.ExecutiveCouncilScreen.route),
                        Triple(SsplIcons.HowToVote, "Elections", Screen.ElectionsScreen.route),
                        Triple(SsplIcons.Badge, "Members directory", Screen.MembersDirectoryScreen.route),
                        Triple(SsplIcons.CardMembership, "Membership plans", Screen.MembershipPlansScreen.route),
                        Triple(SsplIcons.MenuBook, "Our history", Screen.HistoryScreen.route)
                    )
                    tiles.chunked(2).forEachIndexed { rowIndex, row ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            row.forEachIndexed { colIndex, (icon, label, route) ->
                                NavTile(
                                    icon = icon, title = label, onClick = { open(route) },
                                    animationDelayMillis = (rowIndex * 2 + colIndex) * 80,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }

            // ---- Help ------------------------------------------------------------------
            item(key = "help") {
                HelpStrip(
                    onContact = { open(Screen.ContactUsScreen.route) },
                    onFaq = { open(Screen.FaqsScreen.route) },
                    onGuide = { open(Screen.MemberGuideScreen.route) },
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 20.dp)
                )
            }
        }
    }
}

// =====================================================================================
// Hero
// =====================================================================================

@Composable
private fun HomeHero(bottomSpace: androidx.compose.ui.unit.Dp) {
    val shell = LocalShellActions.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
            .background(AppTheme.colors.headerGradient)
    ) {
        DecorativeRings(Modifier.matchParentSize())
        Column(
            modifier = Modifier
                .statusBarsPadding()
                .padding(start = 18.dp, end = 18.dp, top = 8.dp, bottom = bottomSpace)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                GlassIconButton(SsplIcons.Menu, "Open menu", shell.openDrawer)
                Spacer(Modifier.weight(1f))
                GlassIconButton(
                    SsplIcons.Notifications, "Notifications", shell.onNotifications,
                    badgeCount = shell.unreadCount
                )
                Spacer(Modifier.width(10.dp))
                AvatarImage(
                    url = shell.avatarUrl,
                    initials = shell.initials,
                    size = 42.dp,
                    modifier = Modifier
                        .clip(CircleShape)
                        .border(2.dp, Color.White.copy(alpha = 0.55f), CircleShape)
                        .clickable(onClick = shell.onAvatar)
                )
            }
            Spacer(Modifier.height(22.dp))
            Text(
                text = DisplayDates.greeting().uppercase(),
                style = AppTheme.typography.overline.copy(color = AppTheme.colors.heroAccent)
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = shell.displayName?.let { "Dr. ${it.removePrefix("Dr. ").removePrefix("Dr ")}" } ?: "Welcome to SSPL",
                style = AppTheme.typography.displaySmall.copy(color = Color.White),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "Society of Surgeons of Pakistan · Lahore",
                style = AppTheme.typography.bodyMedium.copy(color = Color.White.copy(alpha = 0.72f))
            )
            if (shell.isGuest) {
                Spacer(Modifier.height(14.dp))
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(AppTheme.colors.heroChip)
                        .clickable(onClick = shell.onAvatar)
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Sign in to your member portal",
                        style = AppTheme.typography.labelMedium.copy(color = AppTheme.colors.onHeroChip, fontWeight = FontWeight.SemiBold)
                    )
                    Spacer(Modifier.width(6.dp))
                    Icon(SsplIcons.ArrowForward, null, tint = AppTheme.colors.onHeroChip, modifier = Modifier.size(14.dp))
                }
            }
        }
    }
}

// =====================================================================================
// Events
// =====================================================================================

@Composable
private fun FeaturedEventCard(event: EventItem, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = AppTheme.colors
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(AppTheme.shapes.card)
            .background(colors.surface)
            .border(1.dp, colors.border.copy(alpha = 0.7f), AppTheme.shapes.card)
            .clickable(onClick = onClick)
    ) {
        Box(Modifier.fillMaxWidth().aspectRatio(2f)) {
            SsplImage(
                source = event.effectiveImage,
                contentDescription = event.title,
                modifier = Modifier.fillMaxSize(),
                fallbackIcon = SsplIcons.Event,
                fallbackIconSize = 44.dp
            )
            Box(
                Modifier.matchParentSize().background(
                    Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.55f)))
                )
            )
            DateBlock(event, Modifier.align(Alignment.TopStart).padding(12.dp))
            Row(
                Modifier.align(Alignment.TopEnd).padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (event.computedStatus == "ongoing") GlassPill("LIVE NOW")
                GlassPill(event.readableType.uppercase())
            }
            val rel = DisplayDates.relative(event.effectiveStart)
            if (rel.isNotEmpty() && event.computedStatus == "upcoming") {
                GlassPill(rel, modifier = Modifier.align(Alignment.BottomStart).padding(12.dp), icon = SsplIcons.Schedule)
            }
        }
        Column(Modifier.padding(16.dp)) {
            Text(event.title, style = AppTheme.typography.titleMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(8.dp))
            MetaLine(SsplIcons.CalendarMonth, listOf(event.formattedDateRange, event.formattedTimeRange).filter { it.isNotBlank() }.joinToString(" · "))
            Spacer(Modifier.height(4.dp))
            MetaLine(SsplIcons.Place, event.effectiveVenue)
            Spacer(Modifier.height(14.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (event.isRegistrationAllowed) "Registration open" else "View details",
                    style = AppTheme.typography.labelMedium.copy(
                        color = if (event.isRegistrationAllowed) colors.success else colors.muted,
                        fontWeight = FontWeight.SemiBold
                    ),
                    modifier = Modifier.weight(1f)
                )
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(colors.primaryGradient)
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        if (event.isRegistrationAllowed) "Register" else "Details",
                        style = AppTheme.typography.labelMedium.copy(color = Color.White, fontWeight = FontWeight.SemiBold)
                    )
                    Spacer(Modifier.width(4.dp))
                    Icon(SsplIcons.ArrowForward, null, tint = Color.White, modifier = Modifier.size(14.dp))
                }
            }
        }
    }
}

@Composable
private fun CompactEventCard(event: EventItem, onClick: () -> Unit) {
    val colors = AppTheme.colors
    Column(
        modifier = Modifier
            .width(250.dp)
            .clip(AppTheme.shapes.card)
            .background(colors.surface)
            .border(1.dp, colors.border.copy(alpha = 0.7f), AppTheme.shapes.card)
            .clickable(onClick = onClick)
    ) {
        Box(Modifier.fillMaxWidth().height(118.dp)) {
            SsplImage(event.effectiveImage, event.title, Modifier.fillMaxSize(), fallbackIcon = SsplIcons.Event)
            DateBlock(event, Modifier.align(Alignment.TopStart).padding(10.dp), compact = true)
        }
        Column(Modifier.padding(12.dp)) {
            Text(
                event.readableType.uppercase(),
                style = AppTheme.typography.overline
            )
            Spacer(Modifier.height(2.dp))
            Text(event.title, style = AppTheme.typography.titleSmall, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(6.dp))
            MetaLine(SsplIcons.Place, event.effectiveVenue)
        }
    }
}

@Composable
private fun DateBlock(event: EventItem, modifier: Modifier = Modifier, compact: Boolean = false) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(AppTheme.colors.surfaceElevated)
            .padding(horizontal = if (compact) 9.dp else 11.dp, vertical = if (compact) 5.dp else 7.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            event.startDayNumber,
            style = AppTheme.typography.number.copy(
                color = AppTheme.colors.heading,
                fontSize = if (compact) AppTheme.typography.titleMedium.fontSize else AppTheme.typography.number.fontSize
            )
        )
        Text(
            event.startMonthName.uppercase(),
            style = AppTheme.typography.labelSmall.copy(color = AppTheme.colors.primary, fontWeight = FontWeight.Bold)
        )
    }
}

@Composable
private fun MetaLine(icon: ImageVector, text: String) {
    if (text.isBlank()) return
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = AppTheme.colors.muted, modifier = Modifier.size(14.dp))
        Spacer(Modifier.width(6.dp))
        Text(text, style = AppTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun NoEventsCard(onBrowse: () -> Unit) {
    ListRowCard(
        title = "No upcoming events right now",
        subtitle = "New CME events and workshops are announced here first. Browse past events meanwhile.",
        leading = { IconTile(SsplIcons.CalendarMonth) },
        onClick = onBrowse,
        modifier = Modifier.padding(horizontal = 16.dp)
    )
}

// =====================================================================================
// Fellowship / news / help
// =====================================================================================

@Composable
private fun FellowshipImpactCard(stats: FellowshipStats?, onClick: () -> Unit, modifier: Modifier = Modifier) {
    GradientHeroCard(modifier = modifier, onClick = onClick) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                GlassPill("FELLOWSHIP PROGRAMME", icon = SsplIcons.WorkspacePremium)
                Spacer(Modifier.height(10.dp))
                Text(
                    "Training the next generation of surgeons",
                    style = AppTheme.typography.titleMedium.copy(color = Color.White),
                    maxLines = 2
                )
            }
            Icon(SsplIcons.ArrowForward, contentDescription = "Open fellowship", tint = Color.White, modifier = Modifier.size(20.dp))
        }
        // Entry stays visible even when every counter is 0 (new programme).
        if (stats != null) {
            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                HeroStat("${stats.activeFellows}", "Active fellows", Modifier.weight(1f))
                HeroStat("${stats.graduatedFellows}", "Completed", Modifier.weight(1f))
                HeroStat("${stats.trainingCenters}", "Centres", Modifier.weight(1f))
                if (stats.totalCasesLogged > 0) HeroStat("${stats.totalCasesLogged}", "Procedures", Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun ProgramPosterCard(program: Program, onClick: () -> Unit) {
    val colors = AppTheme.colors
    Column(
        modifier = Modifier
            .width(240.dp)
            .clip(AppTheme.shapes.card)
            .background(colors.surface)
            .border(1.dp, colors.border.copy(alpha = 0.7f), AppTheme.shapes.card)
            .clickable(onClickLabel = "Apply for ${program.name}", onClick = onClick)
    ) {
        // Posters are admin-uploaded artwork: fit, never crop the text out.
        SsplImage(
            source = program.posters.firstOrNull(),
            contentDescription = "${program.name} poster",
            modifier = Modifier.fillMaxWidth().height(124.dp).background(colors.surfaceTint),
            contentScale = ContentScale.Fit,
            fallbackIcon = SsplIcons.WorkspacePremium,
            fallbackIconSize = 36.dp
        )
        Column(Modifier.padding(12.dp)) {
            if (program.shortName.isNotBlank()) {
                Text(program.shortName.uppercase(), style = AppTheme.typography.overline, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Text(program.name, style = AppTheme.typography.titleSmall, maxLines = 2, minLines = 2, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    listOfNotNull(program.duration, program.specialty).joinToString(" · "),
                    style = AppTheme.typography.labelSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    "Apply",
                    style = AppTheme.typography.labelMedium.copy(color = colors.primary, fontWeight = FontWeight.SemiBold)
                )
                Icon(SsplIcons.ArrowForward, null, tint = colors.primary, modifier = Modifier.padding(start = 4.dp).size(14.dp))
            }
        }
    }
}

// =====================================================================================
// Leadership / mission
// =====================================================================================

@Composable
private fun LeadershipSection(onViewAll: () -> Unit, modifier: Modifier = Modifier) {
    val colors = AppTheme.colors
    val president = CABINET_AND_EXECUTIVES.firstOrNull { it.role == "President" }
    val roster = CABINET_AND_EXECUTIVES.filter { it != president }
    Column(modifier) {
        SectionHeader(
            title = "Leadership $CABINET_TERM_LABEL",
            actionText = "View all",
            onActionClick = onViewAll,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(Modifier.height(8.dp))
        if (president != null) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth()
                    .clip(AppTheme.shapes.card)
                    .background(colors.surface)
                    .border(1.dp, colors.border.copy(alpha = 0.7f), AppTheme.shapes.card)
                    .clickable(onClickLabel = "View executive council", onClick = onViewAll)
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SsplImage(
                    source = president.imageUrl,
                    contentDescription = president.name,
                    modifier = Modifier.size(width = 72.dp, height = 88.dp).clip(AppTheme.shapes.small),
                    fallbackIcon = SsplIcons.Person
                )
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text("PRESIDENT", style = AppTheme.typography.overline)
                    Text(president.name, style = AppTheme.typography.titleMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    Text(
                        "Guiding surgical excellence in Lahore",
                        style = AppTheme.typography.bodySmall,
                        maxLines = 2
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
        }
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(roster, key = { "leader_" + it.name }) { m -> LeaderChip(m, onClick = onViewAll) }
        }
    }
}

@Composable
private fun LeaderChip(member: CouncilMember, onClick: () -> Unit) {
    val colors = AppTheme.colors
    Column(
        modifier = Modifier
            .width(112.dp)
            .clip(AppTheme.shapes.card)
            .background(colors.surface)
            .border(1.dp, colors.border.copy(alpha = 0.7f), AppTheme.shapes.card)
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        SsplImage(
            source = member.imageUrl,
            contentDescription = member.name,
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .border(2.dp, colors.primaryContainer, CircleShape),
            fallbackIcon = SsplIcons.Person,
            fallbackIconSize = 26.dp
        )
        Spacer(Modifier.height(8.dp))
        Text(
            member.name,
            style = AppTheme.typography.labelMedium.copy(color = colors.heading, fontWeight = FontWeight.SemiBold),
            maxLines = 2,
            minLines = 2,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
        Text(
            member.role,
            style = AppTheme.typography.labelSmall.copy(color = colors.accent),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun MissionVisionCard(icon: ImageVector, title: String, body: String, animationDelayMillis: Int) {
    val colors = AppTheme.colors
    var expanded by rememberSaveable(title) { mutableStateOf(false) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(AppTheme.shapes.card)
            .background(colors.surface)
            .border(1.dp, colors.border.copy(alpha = 0.7f), AppTheme.shapes.card)
            .clickable(onClickLabel = if (expanded) "Show less" else "Read more") { expanded = !expanded }
            .animateContentSize()
            .padding(14.dp)
    ) {
        IconTile(icon, size = 36.dp, iconSize = 18.dp, corner = 11.dp, animate = true, animationDelayMillis = animationDelayMillis)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(title, style = AppTheme.typography.titleSmall)
            Spacer(Modifier.height(2.dp))
            Text(
                body,
                style = AppTheme.typography.bodySmall.copy(color = colors.body),
                maxLines = if (expanded) Int.MAX_VALUE else 3,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                if (expanded) "Show less" else "Read more",
                style = AppTheme.typography.labelMedium.copy(color = colors.primary, fontWeight = FontWeight.SemiBold),
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
private fun NewsRow(item: SocietyNewsItem, onClick: () -> Unit) {
    ListRowCard(
        title = item.title ?: "Announcement",
        subtitle = (item.message ?: item.description)?.take(140),
        meta = DisplayDates.medium(item.createdAt).takeIf { it.isNotBlank() },
        leading = { IconTile(SsplIcons.Campaign, tint = AppTheme.colors.accent, background = AppTheme.colors.infoContainer) },
        onClick = onClick,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 5.dp)
    )
}

@Composable
private fun HelpStrip(onContact: () -> Unit, onFaq: () -> Unit, onGuide: () -> Unit, modifier: Modifier = Modifier) {
    val colors = AppTheme.colors
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(AppTheme.shapes.card)
            .background(colors.surfaceTint)
            .padding(16.dp)
    ) {
        Text("Need a hand?", style = AppTheme.typography.titleSmall)
        Text(
            "Guides, answers and the secretariat are a tap away.",
            style = AppTheme.typography.bodySmall
        )
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            HelpChip(SsplIcons.SupportAgent, "Contact", onContact, Modifier.weight(1f))
            HelpChip(SsplIcons.QuestionAnswer, "FAQs", onFaq, Modifier.weight(1f))
            HelpChip(SsplIcons.MenuBook, "Guide", onGuide, Modifier.weight(1f))
        }
    }
}

@Composable
private fun HelpChip(icon: ImageVector, label: String, onClick: () -> Unit, modifier: Modifier) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(AppTheme.colors.surface)
            .clickable(onClick = onClick)
            .heightIn(min = 48.dp)
            .padding(horizontal = 6.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = AppTheme.colors.primary, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(6.dp))
        Text(label, style = AppTheme.typography.labelMedium.copy(color = AppTheme.colors.heading, fontWeight = FontWeight.SemiBold))
    }
}
