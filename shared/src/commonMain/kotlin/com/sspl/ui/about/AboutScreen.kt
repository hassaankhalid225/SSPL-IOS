package com.sspl.ui.about

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.sspl.Screen
import com.sspl.designsystem.components.AvatarImage
import com.sspl.designsystem.components.BadgeVariant
import com.sspl.designsystem.components.SectionHeader
import com.sspl.designsystem.components.SsplBadge
import com.sspl.designsystem.theme.AppTheme

@Composable
fun AboutScreen(navController: NavController) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(AppTheme.colors.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero / Tagline card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = AppTheme.colors.primary)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "HERITAGE. LEADERSHIP. HEALING.",
                        style = AppTheme.typography.labelSmall,
                        color = AppTheme.colors.secondary,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.3.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Advancing Surgery.\nServing Pakistan.",
                        style = AppTheme.typography.headlineMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "The Society of Surgeons of Pakistan (Lahore) is a premier academic surgical organization dedicated to surgical excellence, education, and research across Pakistan.",
                        style = AppTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.88f)
                    )
                }
            }
        }

        // Mission & Vision
        item {
            Text(
                text = "MISSION & VISION",
                style = AppTheme.typography.labelSmall,
                color = AppTheme.colors.primary,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = AppTheme.colors.surface),
                border = BorderStroke(1.dp, AppTheme.colors.border)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(AppTheme.colors.primary.copy(alpha = 0.1f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = AppTheme.colors.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Our Mission",
                            style = AppTheme.typography.titleMedium,
                            color = AppTheme.colors.onSurface,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "To promote the highest standards of surgical practice, ethical education, and clinical research. We provide a structured platform for surgeons to share experiences, discuss challenging cases, and adopt cutting-edge advancements.",
                        style = AppTheme.typography.bodyMedium,
                        color = AppTheme.colors.onSurface.copy(alpha = 0.85f),
                        lineHeight = 22.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(AppTheme.colors.accent.copy(alpha = 0.12f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lightbulb,
                                contentDescription = null,
                                tint = AppTheme.colors.accent,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Our Vision",
                            style = AppTheme.typography.titleMedium,
                            color = AppTheme.colors.onSurface,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Through regular clinical meetings and academic engagements, enhance surgical competence, foster collaboration, and deliver exceptional patient care across Pakistan and beyond.",
                        style = AppTheme.typography.bodyMedium,
                        color = AppTheme.colors.onSurface.copy(alpha = 0.85f),
                        lineHeight = 22.sp
                    )
                }
            }
        }

        // Four Society Pillars
        item {
            Text(
                text = "OUR PILLARS",
                style = AppTheme.typography.labelSmall,
                color = AppTheme.colors.primary,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            PillarCard(
                title = "Membership",
                description = "Join a distinguished community of certified surgeons dedicated to professional growth and collaborative excellence.",
                icon = Icons.Default.Groups
            )
            Spacer(modifier = Modifier.height(8.dp))
            PillarCard(
                title = "Fellowship",
                description = "Structured surgical fellowship with accredited training centers, case logbooks, and competency-based assessment.",
                icon = Icons.Default.School
            )
            Spacer(modifier = Modifier.height(8.dp))
            PillarCard(
                title = "Conferences & CME",
                description = "National and regional surgical congresses, live symposia, and hands-on workshops featuring world-class faculty.",
                icon = Icons.Default.CalendarMonth
            )
            Spacer(modifier = Modifier.height(8.dp))
            PillarCard(
                title = "Publications & Research",
                description = "Scientific journals, research presentations, and clinical guidelines advancing evidence-based surgery.",
                icon = Icons.Default.Book
            )
        }

        // President's Message
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = AppTheme.colors.surface),
                border = BorderStroke(1.dp, AppTheme.colors.border)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "PRESIDENT'S MESSAGE",
                        style = AppTheme.typography.labelSmall,
                        color = AppTheme.colors.primary,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.1.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AvatarImage(
                            name = "Prof. Dr. Ali Azhar Naeem",
                            size = 54.dp
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Prof. Dr. Ali Azhar Naeem",
                                style = AppTheme.typography.titleMedium,
                                color = AppTheme.colors.onSurface,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "President, Society of Surgeons Lahore",
                                style = AppTheme.typography.bodySmall,
                                color = AppTheme.colors.textMuted
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "\"I stand before you today with profound gratitude and a deep sense of responsibility as the President of the Society of Surgeons, Lahore Chapter.\n\nOur chapter has a rich history of advancing surgical knowledge, fostering collaboration, and promoting the highest standards of patient care. As we look towards the future, let us continue to build on these foundations and explore new horizons in surgical innovation, education, and advocacy.\"",
                        style = AppTheme.typography.bodyMedium,
                        fontStyle = FontStyle.Italic,
                        color = AppTheme.colors.onSurface.copy(alpha = 0.9f),
                        lineHeight = 22.sp
                    )
                }
            }
        }

        // Direct Chapter Links
        item {
            SectionHeader(title = "Explore More")
            Spacer(modifier = Modifier.height(6.dp))
            NavigationCard(
                title = "Society History & Timeline",
                subtitle = "Milestones from the 1950s to present day",
                icon = Icons.Default.History,
                onClick = { navController.navigate(Screen.HistoryScreen.route) }
            )
            Spacer(modifier = Modifier.height(8.dp))
            NavigationCard(
                title = "Executive Council & Cabinet",
                subtitle = "Elected leadership and cabinet 2026–2028",
                icon = Icons.Default.Groups,
                onClick = { navController.navigate(Screen.ExecutiveCouncilScreen.route) }
            )
            Spacer(modifier = Modifier.height(8.dp))
            NavigationCard(
                title = "Member Guide & FAQs",
                subtitle = "Everything you need to know about joining and events",
                icon = Icons.Default.MenuBook,
                onClick = { navController.navigate(Screen.MemberGuideScreen.route) }
            )
        }
    }
}

@Composable
private fun PillarCard(
    title: String,
    description: String,
    icon: ImageVector
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = AppTheme.colors.surface),
        border = BorderStroke(1.dp, AppTheme.colors.border)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(AppTheme.colors.primary.copy(alpha = 0.1f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = AppTheme.colors.primary,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = AppTheme.typography.titleSmall,
                    color = AppTheme.colors.onSurface,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    style = AppTheme.typography.bodySmall,
                    color = AppTheme.colors.textMuted,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

@Composable
private fun NavigationCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
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
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = AppTheme.colors.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = AppTheme.typography.titleSmall,
                    color = AppTheme.colors.onSurface,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = subtitle,
                    style = AppTheme.typography.bodySmall,
                    color = AppTheme.colors.textMuted
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = AppTheme.colors.textMuted,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
