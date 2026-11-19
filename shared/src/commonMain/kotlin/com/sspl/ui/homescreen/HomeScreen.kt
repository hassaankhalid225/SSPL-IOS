package com.sspl.ui.homescreen

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Newspaper
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.sspl.Screen
import com.sspl.core.auth.RoleUtils
import com.sspl.core.models.Banner
import com.sspl.core.models.PublicEventItem
import com.sspl.core.models.SocietyNewsItem
import com.sspl.ui.components.AppHorizontalPager
import com.sspl.designsystem.components.AvatarImage
import com.sspl.designsystem.components.BadgeVariant
import com.sspl.designsystem.components.PrimaryButton
import com.sspl.designsystem.components.SecondaryButton
import com.sspl.designsystem.components.SectionHeader
import com.sspl.designsystem.components.SsplBadge
import com.sspl.designsystem.components.SsplCard
import com.sspl.designsystem.components.StatCard
import com.sspl.designsystem.theme.AppTheme
import com.sspl.session.UserSession
import com.sspl.ui.components.GuestUserWarning
import com.sspl.ui.userdetails.ProfileViewModel
import com.sspl.utils.BASE_URL
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun HomeScreen(
    navController: NavController,
    homeViewModel: HomeScreenViewModel = koinViewModel(),
    profileViewModel: ProfileViewModel = koinViewModel(),
    userSession: UserSession = koinInject()
) {
    val isGuestUser by profileViewModel.isGuestUser.collectAsState()
    val user by userSession.userFlow.collectAsState()
    val banners by homeViewModel.banners.collectAsState()
    val upcomingEvents by homeViewModel.upcomingEvents.collectAsState()
    val latestNews by homeViewModel.latestNews.collectAsState()
    val scope = rememberCoroutineScope()

    val sliderBanners = banners.filter { it.type == "SLIDER" || it.type == null }
    val pagerState = rememberPagerState(pageCount = { sliderBanners.size })

    // Auto-advance banner carousel every 4 seconds
    LaunchedEffect(sliderBanners) {
        if (sliderBanners.isNotEmpty()) {
            while (true) {
                delay(4000L)
                val currentPage = pagerState.currentPage
                scope.launch {
                    if (currentPage + 1 >= sliderBanners.size) {
                        pagerState.animateScrollToPage(0)
                    } else {
                        pagerState.animateScrollToPage(currentPage + 1)
                    }
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        homeViewModel.loadHomeData()
    }

    val greetingName = when {
        user != null && !user!!.fullName.isNullOrBlank() -> "Dr. ${user!!.fullName}"
        user != null && !user!!.firstName.isNullOrBlank() -> "Dr. ${user!!.firstName}"
        else -> "Guest"
    }

    val membershipStatusText = when {
        user == null || user!!.isGuest() -> "Guest"
        RoleUtils.isLifeMember(user) -> "Life Member"
        RoleUtils.isRegularMember(user) -> "Regular Member"
        else -> "Member"
    }

    val membershipBadgeVariant = when {
        user == null || user!!.isGuest() -> BadgeVariant.NEUTRAL
        RoleUtils.isLifeMember(user) -> BadgeVariant.SUCCESS
        else -> BadgeVariant.PRIMARY
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(AppTheme.colors.background),
        contentPadding = PaddingValues(bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Guest mode banner
        if (isGuestUser == true || user == null || user?.isGuest() == true) {
            item {
                GuestUserWarning(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }

        // Welcome / Greeting Header Card
        item {
            HomeGreetingCard(
                greeting = "Welcome, $greetingName",
                chapter = "Society of Surgeons of Pakistan — Lahore",
                membershipStatus = membershipStatusText,
                badgeVariant = membershipBadgeVariant,
                avatarUrl = RoleUtils.getProfileImageUrl(user?.profilePicture),
                initials = RoleUtils.getUserInitials(user),
                onProfileClick = {
                    if (user != null && !user!!.isGuest()) {
                        navController.navigate(Screen.ProfileScreen.route)
                    } else {
                        navController.navigate(Screen.SignInScreen.route)
                    }
                }
            )
        }

        // Banner Slider (Keeping original logic & full fidelity)
        if (sliderBanners.isNotEmpty()) {
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    AppHorizontalPager(
                        list = sliderBanners,
                        pagerState = pagerState,
                        showIndicator = true
                    ) { modifier, item, _ ->
                        val fullImageUrl = if (item.imageUrl.startsWith("/")) {
                            "$BASE_URL${item.imageUrl}"
                        } else {
                            "$BASE_URL/${item.imageUrl}"
                        }

                        Card(
                            modifier = modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            shape = RoundedCornerShape(14.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            AsyncImage(
                                model = fullImageUrl,
                                contentDescription = item.title ?: "Banner",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                }
            }
        }

        // Quick Actions Grid (4 Primary Actions)
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text(
                    text = "QUICK ACTIONS",
                    style = AppTheme.typography.labelSmall,
                    color = AppTheme.colors.primary,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    QuickActionCard(
                        title = "Events & CME",
                        subtitle = "Conferences & Meetings",
                        icon = Icons.Default.CalendarMonth,
                        modifier = Modifier.weight(1f),
                        onClick = { navController.navigate(Screen.ScientificProgramsScreen.route) }
                    )
                    QuickActionCard(
                        title = "Annual Congress",
                        subtitle = "Scientific Sessions",
                        icon = Icons.Default.WorkspacePremium,
                        modifier = Modifier.weight(1f),
                        onClick = { navController.navigate(Screen.ScientificProgramsScreen.route) }
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    QuickActionCard(
                        title = "Fellowship",
                        subtitle = "Programs & Centers",
                        icon = Icons.Default.School,
                        modifier = Modifier.weight(1f),
                        onClick = { navController.navigate(Screen.FellowshipHubScreen.route) }
                    )
                    QuickActionCard(
                        title = "Directory",
                        subtitle = "Life & Regular Members",
                        icon = Icons.Default.Groups,
                        modifier = Modifier.weight(1f),
                        onClick = { navController.navigate(Screen.MembersDirectoryScreen.route) }
                    )
                }
            }
        }

        // Stats Band
        item {
            HomeStatsBand()
        }

        // Upcoming Events Section
        if (upcomingEvents.isNotEmpty()) {
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    SectionHeader(
                        title = "Upcoming Events",
                        action = "View All",
                        onAction = { navController.navigate(Screen.ScientificProgramsScreen.route) },
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(upcomingEvents) { event ->
                            UpcomingEventCard(event = event) {
                                navController.navigate(Screen.ScientificProgramsScreen.route)
                            }
                        }
                    }
                }
            }
        }

        // Society News Section
        if (latestNews.isNotEmpty()) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    SectionHeader(
                        title = "Society Announcements",
                        action = "See More",
                        onAction = { navController.navigate(Screen.NotificationsScreen.route) }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    latestNews.forEach { newsItem ->
                        SocietyNewsCard(item = newsItem) {
                            navController.navigate(Screen.NotificationsScreen.route)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }

        // Fellowship CTA Banner
        item {
            FellowshipCtaCard(
                onExplore = { navController.navigate(Screen.FellowshipHubScreen.route) }
            )
        }

        // President's Message Teaser Card
        item {
            PresidentMessageTeaser(
                onReadMore = { navController.navigate(Screen.AboutScreen.route) }
            )
        }

        // Executive Leadership Preview Strip
        item {
            LeadershipPreviewCard(
                onViewCouncil = { navController.navigate(Screen.ExecutiveCouncilScreen.route) }
            )
        }
    }
}

@Composable
private fun HomeGreetingCard(
    greeting: String,
    chapter: String,
    membershipStatus: String,
    badgeVariant: BadgeVariant,
    avatarUrl: String?,
    initials: String,
    onProfileClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = AppTheme.colors.surface),
        border = BorderStroke(1.dp, AppTheme.colors.border)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = greeting,
                    style = AppTheme.typography.titleMedium,
                    color = AppTheme.colors.onSurface,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = chapter,
                    style = AppTheme.typography.bodySmall,
                    color = AppTheme.colors.textMuted
                )
                Spacer(modifier = Modifier.height(8.dp))
                SsplBadge(
                    text = membershipStatus,
                    variant = badgeVariant
                )
            }
            AvatarImage(
                url = avatarUrl,
                initials = initials,
                size = 50.dp,
                modifier = Modifier.clickable { onProfileClick() }
            )
        }
    }
}

@Composable
private fun QuickActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = AppTheme.colors.surface),
        border = BorderStroke(1.dp, AppTheme.colors.border)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(AppTheme.colors.primary.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = AppTheme.colors.primary,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = title,
                style = AppTheme.typography.titleSmall,
                color = AppTheme.colors.onSurface,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = AppTheme.typography.bodySmall,
                color = AppTheme.colors.textMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun HomeStatsBand() {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatCard(value = "1,200+", label = "Surgeons", description = "Verified Members", modifier = Modifier.weight(1f))
            StatCard(value = "150+", label = "CME Sessions", description = "Organized Annually", modifier = Modifier.weight(1f))
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatCard(value = "45+", label = "Workshops", description = "Hands-on Training", modifier = Modifier.weight(1f))
            StatCard(value = "25+", label = "Years", description = "Surgical Excellence", modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun UpcomingEventCard(
    event: PublicEventItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(260.dp)
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = AppTheme.colors.surface),
        border = BorderStroke(1.dp, AppTheme.colors.border)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .background(AppTheme.colors.accent.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = event.type ?: "CONFERENCE",
                        style = AppTheme.typography.labelSmall,
                        color = AppTheme.colors.accent,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                Icon(
                    imageVector = Icons.Default.Event,
                    contentDescription = null,
                    tint = AppTheme.colors.textMuted,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = event.title,
                style = AppTheme.typography.titleSmall,
                color = AppTheme.colors.onSurface,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = AppTheme.colors.primary,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = event.effectiveVenue,
                    style = AppTheme.typography.bodySmall,
                    color = AppTheme.colors.textMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun SocietyNewsCard(
    item: SocietyNewsItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = AppTheme.colors.surface),
        border = BorderStroke(1.dp, AppTheme.colors.border)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(AppTheme.colors.primary.copy(alpha = 0.1f), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Newspaper,
                    contentDescription = null,
                    tint = AppTheme.colors.primary,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title ?: "Society Update",
                    style = AppTheme.typography.titleSmall,
                    color = AppTheme.colors.onSurface,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = item.effectiveBody.take(70),
                    style = AppTheme.typography.bodySmall,
                    color = AppTheme.colors.textMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = AppTheme.colors.textMuted
            )
        }
    }
}

@Composable
private fun FellowshipCtaCard(onExplore: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = AppTheme.colors.primary)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Text(
                text = "SURGICAL FELLOWSHIP",
                style = AppTheme.typography.labelSmall,
                color = AppTheme.colors.secondary,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Advance Your Surgical Career",
                style = AppTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Apply for accredited training centers, clinical case logs, and competency assessments under SSP Lahore.",
                style = AppTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.85f)
            )
            Spacer(modifier = Modifier.height(14.dp))
            SecondaryButton(
                text = "Explore Fellowship",
                onClick = onExplore,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun PresidentMessageTeaser(onReadMore: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = AppTheme.colors.surface),
        border = BorderStroke(1.dp, AppTheme.colors.border)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "PRESIDENT'S MESSAGE",
                style = AppTheme.typography.labelSmall,
                color = AppTheme.colors.primary,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.1.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "\"Our chapter has a rich history of advancing surgical knowledge, fostering collaboration, and promoting the highest standards of patient care.\"",
                style = AppTheme.typography.bodyMedium,
                fontStyle = FontStyle.Italic,
                color = AppTheme.colors.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Prof. Dr. Ali Azhar Naeem",
                        style = AppTheme.typography.titleSmall,
                        color = AppTheme.colors.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "President, SSP Lahore",
                        style = AppTheme.typography.bodySmall,
                        color = AppTheme.colors.textMuted
                    )
                }
                Text(
                    text = "Read More →",
                    style = AppTheme.typography.labelMedium,
                    color = AppTheme.colors.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { onReadMore() }
                )
            }
        }
    }
}

@Composable
private fun LeadershipPreviewCard(onViewCouncil: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable { onViewCouncil() },
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
            Icon(
                imageVector = Icons.Default.Groups,
                contentDescription = null,
                tint = AppTheme.colors.primary,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Executive Council & Cabinet",
                    style = AppTheme.typography.titleSmall,
                    color = AppTheme.colors.onSurface,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Meet the elected leaders guiding SSP Lahore",
                    style = AppTheme.typography.bodySmall,
                    color = AppTheme.colors.textMuted
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = AppTheme.colors.primary,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
