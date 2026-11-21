package com.sspl.designsystem.components

import com.sspl.designsystem.icons.SsplIcons

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.border
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import com.sspl.designsystem.theme.ThemeMode
import com.sspl.designsystem.theme.ThemePreferences
import org.koin.compose.koinInject
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CardMembership
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContactMail
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.HowToVote
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.Newspaper
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PresentToAll
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sspl.core.auth.RoleUtils
import com.sspl.core.models.User
import com.sspl.designsystem.theme.AppTheme
import com.sspl.navigation.Route

data class DrawerItem(
    val title: String,
    val icon: ImageVector,
    val route: Route,
    val requiresAuth: Boolean = false,
    val requiredRole: String? = null
)

data class DrawerGroup(
    val label: String,
    val items: List<DrawerItem>
)

val DRAWER_GROUPS = listOf(
    DrawerGroup(
        label = "Account",
        items = listOf(
            DrawerItem("Overview", SsplIcons.Person, Route.PortalOverview, requiresAuth = true),
            DrawerItem("Profile", SsplIcons.Badge, Route.Profile, requiresAuth = true),
            DrawerItem("Membership", SsplIcons.CardMembership, Route.Membership, requiresAuth = true),
            DrawerItem("My Certificates", SsplIcons.WorkspacePremium, Route.MyCertificates, requiresAuth = true),
            DrawerItem("Settings", SsplIcons.Settings, Route.Settings)
        )
    ),
    DrawerGroup(
        label = "Events & Conferences",
        items = listOf(
            DrawerItem("Browse Events", SsplIcons.Event, Route.Events),
            DrawerItem("Annual Conferences", SsplIcons.CalendarMonth, Route.Conferences),
            DrawerItem("Workshops", SsplIcons.School, Route.Workshops()),
            DrawerItem("My Registrations", SsplIcons.Assignment, Route.MyRegistrations, requiresAuth = true),
            DrawerItem("Event Portal", SsplIcons.Verified, Route.EventPortal(), requiresAuth = true),
            DrawerItem("Event Archive", SsplIcons.Archive, Route.EventArchive)
        )
    ),
    DrawerGroup(
        label = "Scholarship",
        items = listOf(
            DrawerItem("Submit Abstract", SsplIcons.Send, Route.SubmitAbstract(), requiresAuth = true),
            DrawerItem("My Submissions", SsplIcons.Description, Route.MySubmissions, requiresAuth = true),
            DrawerItem("Presentations", SsplIcons.PresentToAll, Route.Presentations, requiresAuth = true)
        )
    ),
    DrawerGroup(
        label = "Fellowship",
        items = listOf(
            DrawerItem("Fellowship Hub", SsplIcons.School, Route.FellowshipHub),
            DrawerItem("Training Centres", SsplIcons.Public, Route.TrainingCentres),
            DrawerItem("Guidelines", SsplIcons.Book, Route.FellowshipGuidelines),
            // Public on the web (POST /fellowship/fellows | /training-centers need no login) → open to guests.
            DrawerItem("Apply as Fellow", SsplIcons.Person, Route.ApplyAsFellow, requiresAuth = false),
            DrawerItem("Apply as Centre", SsplIcons.Public, Route.ApplyAsTrainingCentre, requiresAuth = false),
            DrawerItem("Fellow Dashboard", SsplIcons.School, Route.FellowDashboard, requiresAuth = true),
            DrawerItem("Trainer Dashboard", SsplIcons.School, Route.TrainerDashboard, requiresAuth = true, requiredRole = "FELLOWSHIP_TRAINER")
        )
    ),
    DrawerGroup(
        label = "Society",
        items = listOf(
            DrawerItem("About Us", SsplIcons.Info, Route.About),
            DrawerItem("History", SsplIcons.History, Route.History),
            DrawerItem("Executive Council", SsplIcons.Groups, Route.ExecutiveCouncil),
            DrawerItem("Elections", SsplIcons.HowToVote, Route.Elections),
            DrawerItem("Members Directory", SsplIcons.Groups, Route.MembersDirectory),
            DrawerItem("Membership Plans", SsplIcons.CardMembership, Route.MembershipPlans),
            DrawerItem("News & Announcements", SsplIcons.Newspaper, Route.News)
        )
    ),
    DrawerGroup(
        label = "Resources",
        items = listOf(
            DrawerItem("Downloads", SsplIcons.Download, Route.Downloads),
            DrawerItem("Verify Certificate", SsplIcons.Security, Route.VerifyCertificate()),
            DrawerItem("Member Guide", SsplIcons.Book, Route.MemberGuide),
            DrawerItem("FAQs", SsplIcons.Help, Route.Faqs),
            DrawerItem("Contact Us", SsplIcons.ContactMail, Route.Contact),
            DrawerItem("Terms & Conditions", SsplIcons.Description, Route.Terms)
        )
    )
)

@Composable
fun SsplDrawerContent(
    user: User?,
    isGuest: Boolean,
    onNavigate: (Route) -> Unit,
    onSignIn: () -> Unit,
    onLogout: () -> Unit,
    onCloseDrawer: () -> Unit,
    modifier: Modifier = Modifier,
    themePreferences: ThemePreferences = koinInject()
) {
    val displayName = RoleUtils.getUserDisplayName(user)
    val initials = RoleUtils.getUserInitials(user)
    val membershipLabel = RoleUtils.getMembershipLabel(user)
    val roles = RoleUtils.getUserRoles(user)
    val profilePhoto = RoleUtils.getProfileImageUrl(user?.profilePicture)
    val themeMode by themePreferences.mode.collectAsState()
    val colors = AppTheme.colors

    ModalDrawerSheet(
        modifier = modifier.width(318.dp),
        drawerContainerColor = colors.background,
        drawerShape = RoundedCornerShape(topEnd = 28.dp, bottomEnd = 28.dp),
        windowInsets = WindowInsets(0, 0, 0, 0)
    ) {
        Column(modifier = Modifier.fillMaxHeight()) {
            // ---- Header ---------------------------------------------------------------
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(bottomEnd = 28.dp))
                    .background(colors.headerGradientVertical)
                    .statusBarsPadding()
                    .padding(start = 18.dp, end = 10.dp, top = 12.dp, bottom = 20.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        AvatarImage(
                            url = profilePhoto,
                            initials = initials,
                            size = 58.dp,
                            modifier = Modifier.border(2.dp, Color.White.copy(alpha = 0.5f), CircleShape)
                        )
                        GlassIconButton(
                            icon = SsplIcons.Close,
                            contentDescription = "Close menu",
                            onClick = onCloseDrawer
                        )
                    }
                    Column {
                        Text(
                            text = if (isGuest) "Welcome, guest" else displayName,
                            style = AppTheme.typography.headlineSmall.copy(color = Color.White, fontWeight = FontWeight.Bold),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = if (isGuest) "Sign in to unlock your member portal" else (user?.email ?: ""),
                            style = AppTheme.typography.labelMedium.copy(color = Color.White.copy(alpha = 0.72f)),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (!isGuest) MembershipBadge(label = membershipLabel)
                        Text(
                            text = if (isGuest) "Sign in →" else "View profile →",
                            style = AppTheme.typography.labelMedium.copy(color = Color.White, fontWeight = FontWeight.SemiBold),
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(Color.White.copy(alpha = 0.14f))
                                .clickable {
                                    onCloseDrawer()
                                    if (isGuest) onSignIn() else onNavigate(Route.Profile)
                                }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            // ---- Menu -----------------------------------------------------------------
            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 10.dp)
            ) {
                DRAWER_GROUPS.forEach { group ->
                    item(key = "h_${group.label}") {
                        Text(
                            text = group.label.uppercase(),
                            style = AppTheme.typography.overline.copy(color = colors.muted),
                            modifier = Modifier.padding(start = 22.dp, end = 16.dp, top = 12.dp, bottom = 6.dp)
                        )
                    }
                    val visible = group.items.filter {
                        it.requiredRole == null || roles.contains(it.requiredRole.uppercase())
                    }
                    items(visible.size, key = { "${group.label}_${visible[it].title}" }) { index ->
                        val item = visible[index]
                        val isLocked = item.requiresAuth && isGuest
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .clickable {
                                    onCloseDrawer()
                                    if (isLocked) onSignIn() else onNavigate(item.route)
                                }
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isLocked) colors.boxColor else colors.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = null,
                                    tint = if (isLocked) colors.muted else colors.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Text(
                                text = item.title,
                                style = AppTheme.typography.bodyLarge.copy(
                                    color = if (isLocked) colors.muted else colors.heading,
                                    fontWeight = FontWeight.Medium
                                ),
                                modifier = Modifier.weight(1f)
                            )
                            if (isLocked) {
                                Icon(
                                    imageVector = SsplIcons.Lock,
                                    contentDescription = "Sign in required",
                                    tint = colors.muted,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        }
                    }
                }
            }

            // ---- Footer: theme + sign in/out -----------------------------------------
            HorizontalDivider(color = colors.divider)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ThemeModeSwitch(selected = themeMode, onSelect = themePreferences::setMode)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (isGuest) colors.primaryContainer else colors.errorContainer)
                        .clickable {
                            onCloseDrawer()
                            if (isGuest) onSignIn() else onLogout()
                        }
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = if (isGuest) SsplIcons.Login else SsplIcons.Logout,
                        contentDescription = null,
                        tint = if (isGuest) colors.primary else colors.error,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = if (isGuest) "Sign in or create account" else "Sign out",
                        style = AppTheme.typography.bodyLarge.copy(
                            color = if (isGuest) colors.primary else colors.error,
                            fontWeight = FontWeight.SemiBold
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    Text(text = "v2.0", style = AppTheme.typography.labelSmall)
                }
            }
        }
    }
}

/** Segmented System / Light / Dark switch. */
@Composable
fun ThemeModeSwitch(
    selected: ThemeMode,
    onSelect: (ThemeMode) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = AppTheme.colors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(50))
            .background(colors.boxColor)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        ThemeMode.entries.forEach { mode ->
            val isSel = mode == selected
            Row(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(50))
                    .background(if (isSel) colors.surface else Color.Transparent)
                    .clickable { onSelect(mode) }
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = when (mode) {
                        ThemeMode.SYSTEM -> SsplIcons.PhoneAndroid
                        ThemeMode.LIGHT -> SsplIcons.LightMode
                        ThemeMode.DARK -> SsplIcons.DarkMode
                    },
                    contentDescription = null,
                    tint = if (isSel) colors.primary else colors.muted,
                    modifier = Modifier.size(15.dp)
                )
                Text(
                    text = " ${mode.label}",
                    style = AppTheme.typography.labelMedium.copy(
                        color = if (isSel) colors.heading else colors.muted,
                        fontWeight = if (isSel) FontWeight.SemiBold else FontWeight.Medium
                    )
                )
            }
        }
    }
}
