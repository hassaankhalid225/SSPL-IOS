package com.sspl.designsystem.components

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
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.draw.clip
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
            DrawerItem("Overview", Icons.Default.Person, Route.PortalOverview, requiresAuth = true),
            DrawerItem("Profile", Icons.Default.Badge, Route.Profile, requiresAuth = true),
            DrawerItem("Membership", Icons.Default.CardMembership, Route.Membership, requiresAuth = true),
            DrawerItem("My Certificates", Icons.Default.WorkspacePremium, Route.MyCertificates, requiresAuth = true),
            DrawerItem("Settings", Icons.Default.Settings, Route.Settings)
        )
    ),
    DrawerGroup(
        label = "Events & Conferences",
        items = listOf(
            DrawerItem("Browse Events", Icons.Default.Event, Route.Events),
            DrawerItem("Annual Conferences", Icons.Default.CalendarMonth, Route.Conferences),
            DrawerItem("Workshops", Icons.Default.School, Route.Workshops()),
            DrawerItem("My Registrations", Icons.Default.Assignment, Route.MyRegistrations, requiresAuth = true),
            DrawerItem("Event Portal", Icons.Default.Verified, Route.EventPortal(), requiresAuth = true),
            DrawerItem("Event Archive", Icons.Default.Archive, Route.EventArchive)
        )
    ),
    DrawerGroup(
        label = "Scholarship",
        items = listOf(
            DrawerItem("Submit Abstract", Icons.Default.Send, Route.SubmitAbstract(), requiresAuth = true),
            DrawerItem("My Submissions", Icons.Default.Description, Route.MySubmissions, requiresAuth = true),
            DrawerItem("Presentations", Icons.Default.PresentToAll, Route.Presentations, requiresAuth = true)
        )
    ),
    DrawerGroup(
        label = "Fellowship",
        items = listOf(
            DrawerItem("Fellowship Hub", Icons.Default.School, Route.FellowshipHub),
            DrawerItem("Training Centres", Icons.Default.Public, Route.TrainingCentres),
            DrawerItem("Guidelines", Icons.Default.Book, Route.FellowshipGuidelines),
            DrawerItem("Apply as Fellow", Icons.Default.Person, Route.ApplyAsFellow, requiresAuth = true),
            DrawerItem("Apply as Centre", Icons.Default.Public, Route.ApplyAsTrainingCentre, requiresAuth = true),
            DrawerItem("Fellow Dashboard", Icons.Default.School, Route.FellowDashboard, requiresAuth = true),
            DrawerItem("Trainer Dashboard", Icons.Default.School, Route.TrainerDashboard, requiresAuth = true, requiredRole = "FELLOWSHIP_TRAINER")
        )
    ),
    DrawerGroup(
        label = "Society",
        items = listOf(
            DrawerItem("About Us", Icons.Default.Info, Route.About),
            DrawerItem("History", Icons.Default.History, Route.History),
            DrawerItem("Executive Council", Icons.Default.Groups, Route.ExecutiveCouncil),
            DrawerItem("Elections", Icons.Default.HowToVote, Route.Elections),
            DrawerItem("Members Directory", Icons.Default.Groups, Route.MembersDirectory),
            DrawerItem("Membership Plans", Icons.Default.CardMembership, Route.MembershipPlans),
            DrawerItem("News & Announcements", Icons.Default.Newspaper, Route.News)
        )
    ),
    DrawerGroup(
        label = "Resources",
        items = listOf(
            DrawerItem("Downloads", Icons.Default.Download, Route.Downloads),
            DrawerItem("Verify Certificate", Icons.Default.Security, Route.VerifyCertificate()),
            DrawerItem("Member Guide", Icons.Default.Book, Route.MemberGuide),
            DrawerItem("FAQs", Icons.Default.Help, Route.Faqs),
            DrawerItem("Contact Us", Icons.Default.ContactMail, Route.Contact),
            DrawerItem("Terms & Conditions", Icons.Default.Description, Route.Terms)
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
    modifier: Modifier = Modifier
) {
    val displayName = RoleUtils.getUserDisplayName(user)
    val initials = RoleUtils.getUserInitials(user)
    val membershipLabel = RoleUtils.getMembershipLabel(user)
    val roles = RoleUtils.getUserRoles(user)
    val profilePhoto = RoleUtils.getProfileImageUrl(user?.profilePicture)

    ModalDrawerSheet(
        modifier = modifier.width(310.dp),
        drawerContainerColor = AppTheme.colors.background
    ) {
        Column(modifier = Modifier.fillMaxHeight()) {
            // Gradient Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(AppTheme.colors.primary, AppTheme.colors.tertiary)
                        )
                    )
                    .padding(horizontal = 16.dp, vertical = 20.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        AvatarImage(
                            url = profilePhoto,
                            initials = initials,
                            size = 54.dp
                        )
                        IconButton(onClick = onCloseDrawer) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close drawer",
                                tint = Color.White
                            )
                        }
                    }

                    Text(
                        text = if (isGuest) "Guest User" else displayName,
                        style = AppTheme.typography.titleMedium.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        )
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        MembershipBadge(label = membershipLabel)
                    }

                    if (!isGuest) {
                        Text(
                            text = "View Profile →",
                            style = AppTheme.typography.labelSmall.copy(
                                color = AppTheme.colors.surfaceTint,
                                fontWeight = FontWeight.SemiBold
                            ),
                            modifier = Modifier
                                .clickable {
                                    onCloseDrawer()
                                    onNavigate(Route.Profile)
                                }
                                .padding(top = 2.dp)
                        )
                    }
                }
            }

            // Scrollable Menu Groups
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                DRAWER_GROUPS.forEach { group ->
                    item {
                        Text(
                            text = group.label.uppercase(),
                            style = AppTheme.typography.labelSmall.copy(
                                color = AppTheme.colors.muted,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.8.sp
                            ),
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }

                    items(group.items.size) { index ->
                        val item = group.items[index]

                        // If item requires a role and user doesn't have it, skip
                        if (item.requiredRole != null && !roles.contains(item.requiredRole.uppercase())) {
                            return@items
                        }

                        val isLocked = item.requiresAuth && isGuest

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onCloseDrawer()
                                    if (isLocked) {
                                        onSignIn()
                                    } else {
                                        onNavigate(item.route)
                                    }
                                }
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = null,
                                tint = if (isLocked) AppTheme.colors.muted else AppTheme.colors.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = item.title,
                                style = AppTheme.typography.bodyMedium.copy(
                                    color = if (isLocked) AppTheme.colors.muted else AppTheme.colors.heading,
                                    fontWeight = FontWeight.Medium
                                ),
                                modifier = Modifier.weight(1f)
                            )
                            if (isLocked) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "Requires Login",
                                    tint = AppTheme.colors.muted,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    item {
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                            color = AppTheme.colors.border
                        )
                    }
                }
            }

            // Footer
            HorizontalDivider(color = AppTheme.colors.border)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        onCloseDrawer()
                        if (isGuest) onSignIn() else onLogout()
                    }
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = if (isGuest) Icons.Default.Login else Icons.AutoMirrored.Filled.Logout,
                    contentDescription = null,
                    tint = if (isGuest) AppTheme.colors.primary else AppTheme.colors.error,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = if (isGuest) "Sign In / Register" else "Sign Out",
                    style = AppTheme.typography.bodyMedium.copy(
                        color = if (isGuest) AppTheme.colors.primary else AppTheme.colors.error,
                        fontWeight = FontWeight.SemiBold
                    ),
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "v2.0",
                    style = AppTheme.typography.labelSmall.copy(color = AppTheme.colors.muted)
                )
            }
        }
    }
}

