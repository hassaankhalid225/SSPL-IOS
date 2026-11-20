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
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import com.sspl.designsystem.components.AvatarImage
import com.sspl.designsystem.components.BadgeVariant
import com.sspl.designsystem.components.ModernCapsuleSearchBar
import com.sspl.designsystem.components.ModernMediaCard
import com.sspl.designsystem.components.ModernQuickCategoryChip
import com.sspl.designsystem.components.ModernSupportCard
import com.sspl.designsystem.components.PrimaryButton
import com.sspl.designsystem.components.SecondaryButton
import com.sspl.designsystem.components.SectionHeader
import com.sspl.designsystem.components.SsplBadge
import com.sspl.designsystem.components.SsplCard
import com.sspl.designsystem.components.StatCard
import com.sspl.designsystem.theme.AppTheme
import com.sspl.session.UserSession
import com.sspl.ui.components.AppHorizontalPager
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
    var searchQuery by remember { mutableStateOf("") }

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
        else -> "Doctor"
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
        contentPadding = PaddingValues(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Guest mode banner
        if (isGuestUser == true || user == null || user?.isGuest() == true) {
            item {
                GuestUserWarning(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }
        }

        // Modern Greeting & Header
        item {
            ModernHomeGreetingHeader(
                greetingName = greetingName,
                membershipStatus = membershipStatusText,
                badgeVariant = membershipBadgeVariant,
                onProfileClick = {
                    if (user != null && !user!!.isGuest()) {
                        navController.navigate(Screen.ProfileScreen.route)
                    } else {
                        navController.navigate(Screen.SignInScreen.route)
                    }
                }
            )
        }

        // Modern Capsule Search Bar
        item {
            ModernCapsuleSearchBar(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                placeholder = "Search events, CME, conferences...",
                modifier = Modifier.padding(horizontal = 16.dp),
                onFilterClick = { navController.navigate(Screen.ScientificProgramsScreen.route) }
            )
        }

        // Quick Category Action Row (4 icons: Events, Congress, Fellowship, Directory)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ModernQuickCategoryChip(
                    title = "Events",
                    icon = Icons.Default.CalendarMonth,
                    isSelected = true,
                    onClick = { navController.navigate(Screen.ScientificProgramsScreen.route) },
                    modifier = Modifier.weight(1f)
                )
                ModernQuickCategoryChip(
                    title = "Congress",
                    icon = Icons.Default.WorkspacePremium,
                    isSelected = false,
                    onClick = { navController.navigate(Screen.ConferencesScreen.route) },
                    modifier = Modifier.weight(1f)
                )
                ModernQuickCategoryChip(
                    title = "Fellowship",
                    icon = Icons.Default.School,
                    isSelected = false,
                    onClick = { navController.navigate(Screen.FellowshipHubScreen.route) },
                    modifier = Modifier.weight(1f)
                )
                ModernQuickCategoryChip(
                    title = "Directory",
                    icon = Icons.Default.Groups,
                    isSelected = false,
                    onClick = { navController.navigate(Screen.MembersDirectoryScreen.route) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Hero Featured Banner Card (Modern Villa luxury style with specs, badges, and bookmark)
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
                            shape = RoundedCornerShape(22.dp),
                            colors = CardDefaults.cardColors(containerColor = AppTheme.colors.surface),
                            border = BorderStroke(1.dp, AppTheme.colors.border),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(185.dp)
                                ) {
                                    AsyncImage(
                                        model = fullImageUrl,
                                        contentDescription = item.title ?: "Banner",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                    // Floating "Featured" badge top-left
                                    Box(
                                        modifier = Modifier
                                            .padding(12.dp)
                                            .clip(RoundedCornerShape(50.dp))
                                            .background(Color.Black.copy(alpha = 0.55f))
                                            .padding(horizontal = 10.dp, vertical = 4.dp)
                                            .align(Alignment.TopStart)
                                    ) {
                                        Text(
                                            text = "Featured CME",
                                            style = AppTheme.typography.labelSmall.copy(
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 10.sp
                                            )
                                        )
                                    }
                                    // Floating bookmark button top-right
                                    var isSaved by remember { mutableStateOf(false) }
                                    Box(
                                        modifier = Modifier
                                            .padding(12.dp)
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(Color.White.copy(alpha = 0.85f))
                                            .clickable { isSaved = !isSaved }
                                            .align(Alignment.TopEnd),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = if (isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                            contentDescription = "Save",
                                            tint = AppTheme.colors.heading,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }

                                // Card Content below image
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(
                                        text = item.title ?: "Annual Surgical Conference",
                                        style = AppTheme.typography.titleMedium.copy(
                                            color = AppTheme.colors.heading,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.LocationOn,
                                            contentDescription = null,
                                            tint = AppTheme.colors.muted,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Text(
                                            text = "Pearl Continental, Lahore",
                                            style = AppTheme.typography.bodySmall.copy(
                                                color = AppTheme.colors.muted,
                                                fontSize = 12.sp
                                            )
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(AppTheme.colors.boxColor)
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.WorkspacePremium,
                                                contentDescription = null,
                                                tint = AppTheme.colors.primaryDark,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Text(
                                                text = "12 CME Points",
                                                style = AppTheme.typography.labelSmall.copy(
                                                    color = AppTheme.colors.heading,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 10.5.sp
                                                )
                                            )
                                        }
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(AppTheme.colors.boxColor)
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Groups,
                                                contentDescription = null,
                                                tint = AppTheme.colors.primaryDark,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Text(
                                                text = "In-Person & Live",
                                                style = AppTheme.typography.labelSmall.copy(
                                                    color = AppTheme.colors.heading,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 10.5.sp
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Horizontal Section: Upcoming Events (Popular Locations style)
        if (upcomingEvents.isNotEmpty()) {
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    SectionHeader(
                        title = "Popular Events & CME",
                        action = "View all",
                        onAction = { navController.navigate(Screen.ScientificProgramsScreen.route) },
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(upcomingEvents) { event ->
                            val eventImg = if (!event.imageUrl.isNullOrBlank()) {
                                if (event.imageUrl.startsWith("/")) "$BASE_URL${event.imageUrl}" else "$BASE_URL/${event.imageUrl}"
                            } else null
                            val dateFormatted = event.startDate?.take(10) ?: "Upcoming"
                            val city = event.venue ?: "Lahore"

                            ModernMediaCard(
                                title = event.title,
                                subtitle = "$dateFormatted · $city",
                                imageUrl = eventImg,
                                badgeText = event.type ?: "CME",
                                onClick = {
                                    if (event.id != null && event.id > 0) {
                                        navController.currentBackStackEntry?.savedStateHandle?.set("eventId", event.id)
                                        navController.navigate(Screen.EventDetailScreen.route)
                                    } else {
                                        navController.navigate(Screen.ScientificProgramsScreen.route)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }

        // Stats Band
        item {
            HomeStatsBand()
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

        // Modern Support Card (Screen 3 style)
        item {
            ModernSupportCard(
                title = "Need Help?",
                subtitle = "Our support team is here to assist you with registration, CME certificates, and membership queries.",
                buttonText = "Contact Support",
                onContactClick = { navController.navigate(Screen.ContactUsScreen.route) },
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    }
}

@Composable
private fun ModernHomeGreetingHeader(
    greetingName: String,
    membershipStatus: String,
    badgeVariant: BadgeVariant,
    onProfileClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Hi, $greetingName 👋",
                    style = AppTheme.typography.titleMedium.copy(
                        color = AppTheme.colors.muted,
                        fontWeight = FontWeight.Medium,
                        fontSize = 15.sp
                    )
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Find your upcoming CME & sessions",
                    style = AppTheme.typography.displaySmall.copy(
                        color = AppTheme.colors.heading,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50.dp))
                    .clickable(onClick = onProfileClick)
            ) {
                SsplBadge(
                    text = membershipStatus,
                    variant = badgeVariant
                )
            }
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
private fun SocietyNewsCard(
    item: SocietyNewsItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = AppTheme.colors.surface),
        border = BorderStroke(1.dp, AppTheme.colors.border)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(AppTheme.colors.boxColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Newspaper,
                    contentDescription = null,
                    tint = AppTheme.colors.primaryDark,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title ?: "Society Update",
                    style = AppTheme.typography.titleSmall,
                    color = AppTheme.colors.heading,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = item.effectiveBody.take(70),
                    style = AppTheme.typography.bodySmall,
                    color = AppTheme.colors.muted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = AppTheme.colors.muted
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
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = AppTheme.colors.primaryDark)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
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
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = AppTheme.colors.surface),
        border = BorderStroke(1.dp, AppTheme.colors.border)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
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
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Prof. Dr. Ali Azhar Naeem",
                        style = AppTheme.typography.titleSmall,
                        color = AppTheme.colors.primaryDark,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "President, SSP Lahore",
                        style = AppTheme.typography.bodySmall,
                        color = AppTheme.colors.muted
                    )
                }
                Text(
                    text = "Read More →",
                    style = AppTheme.typography.labelMedium,
                    color = AppTheme.colors.primaryDark,
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
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = AppTheme.colors.surface),
        border = BorderStroke(1.dp, AppTheme.colors.border)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(AppTheme.colors.boxColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Groups,
                    contentDescription = null,
                    tint = AppTheme.colors.primaryDark,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Executive Council & Cabinet",
                    style = AppTheme.typography.titleSmall,
                    color = AppTheme.colors.heading,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Meet the elected leaders guiding SSP Lahore",
                    style = AppTheme.typography.bodySmall,
                    color = AppTheme.colors.muted
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = AppTheme.colors.primaryDark,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
