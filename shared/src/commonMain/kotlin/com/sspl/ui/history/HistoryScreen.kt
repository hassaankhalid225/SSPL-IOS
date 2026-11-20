package com.sspl.ui.history

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.sspl.Screen
import com.sspl.designsystem.components.NavTile
import com.sspl.designsystem.theme.AppTheme
import com.sspl.ui.components.InfoSurface
import com.sspl.ui.components.NumberDot
import com.sspl.ui.components.PageIntroHero

/** Web: pages/HistoryPage.jsx (HISTORY_CHAPTERS). */
private data class Chapter(val era: String, val title: String, val body: String)

private val CHAPTERS = listOf(
    Chapter(
        "1950s", "Association of Surgeons of Lahore",
        "The surgeons of Lahore organized an association in the early 1950s to discuss and share their clinical and operative experiences with colleagues. Anesthetists, pathologists, and radiologists were also included as associate members of this body, which became known as the Association of Surgeons of Lahore."
    ),
    Chapter(
        "1980s", "Revival under Professor Bashir Ahmad",
        "In the early 1980s, this association was revived under the convenorship of Professor Bashir Ahmad, one of the life members of the earlier Association of Surgeons of Lahore. Following a membership drive, elections were conducted and Professor Ijaz Ahsan was elected as President, Professor Riaz Ahsan Siddique as General Secretary, and Dr. Saeed Choudhary as Finance Secretary. The association organized several clinical meetings; however, due to the diverse background of its members, including surgeons from multiple specialties, the association could not continue its activities for a long period."
    ),
    Chapter(
        "24 Oct 1984", "A dedicated forum for Lahore surgeons",
        "On 24th October 1984, Professor S.A.R. Gardezi convened a meeting of general surgeons from all teaching institutions of Lahore and highlighted the need for a dedicated forum for surgeons in Lahore under the umbrella of the Society of Surgeons. He informed participants about the successful functioning of the Society of Surgeons in Karachi, which regularly conducted clinical meetings, and proposed the establishment of independent chapters in centers where more than ten members were present."
    ),
    Chapter(
        "Founding meeting", "King Edward Medical College & formal registration",
        "A meeting was subsequently arranged in the committee room of King Edward Medical College. Approximately forty surgeons from various hospitals of Lahore attended the meeting. Among the notable participants were Professor Rashid Ahmad Qureshi, Professor Masood Ahmed, Professor S. Zafar Haider, Professor Ehsanullah, Professor Zafarullah Chaudhry, Dr. Jahangir Alam, Colonel M.A. Cheema, Professor Muhammad Rafi, and Dr. Abdul Majeed Choudhary. The Society later got itself formally registered after approval of the provisional constitution."
    ),
    Chapter(
        "1985", "First elected office bearers",
        "The first election of the Society was held in 1985. Professor Rashid Ahmad Qureshi was elected as the Founding President, Dr. Abdul Majeed Choudhary as General Secretary, and Dr. Mazhar Saeed Choudhary as Finance Secretary."
    ),
    Chapter(
        "1985 — Present", "Growth, conferences & democratic leadership",
        "Since then, the Society of Surgeons of Pakistan – Lahore Chapter has continued to organize regular monthly, fortnightly, and even multiple clinical meetings within a month. National and international conferences have also been organized on alternate years. Office bearers continue to change every two years through free, fair, transparent, and democratically conducted elections. Over the decades, the Society of Surgeons of Pakistan – Lahore Chapter has emerged as one of the leading academic surgical organizations in the country, promoting surgical education, professional development, research, and collaboration among surgeons."
    )
)

@Composable
fun HistoryScreen(navController: NavController) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().background(AppTheme.colors.background),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 32.dp)
    ) {
        item(key = "hero") {
            PageIntroHero(
                eyebrow = "Our story",
                eyebrowIcon = Icons.Default.History,
                title = "History of SSP Lahore",
                summary = "From the 1950s Association of Surgeons of Lahore to a registered chapter with democratic leadership.",
                stats = listOf("1950s" to "First association", "1985" to "First election", "2 yrs" to "Office term")
            )
            Spacer(Modifier.height(22.dp))
            Text("TIMELINE", style = AppTheme.typography.overline)
            Text("Milestones", style = AppTheme.typography.titleMedium.copy(color = AppTheme.colors.heading))
            Spacer(Modifier.height(14.dp))
        }
        itemsIndexed(CHAPTERS, key = { _, c -> c.era }) { index, chapter ->
            TimelineEntry(index = index, chapter = chapter, isLast = index == CHAPTERS.lastIndex)
        }
        item(key = "council") {
            Spacer(Modifier.height(10.dp))
            NavTile(
                icon = Icons.Default.Groups,
                title = "Today's leadership",
                subtitle = "Meet the cabinet and executive council",
                onClick = { navController.navigate(Screen.ExecutiveCouncilScreen.route) { launchSingleTop = true } }
            )
        }
    }
}

@Composable
private fun TimelineEntry(index: Int, chapter: Chapter, isLast: Boolean) {
    var expanded by remember { mutableStateOf(index == 0) }
    val colors = AppTheme.colors
    Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
        // Rail: numbered dot + connecting line.
        Column(Modifier.width(32.dp).fillMaxHeight(), horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
            NumberDot("${index + 1}", filled = index == 0 || isLast)
            if (!isLast) {
                Box(
                    Modifier
                        .width(2.dp)
                        .weight(1f)
                        .background(
                            Brush.verticalGradient(listOf(colors.primary.copy(alpha = 0.45f), colors.border))
                        )
                )
            }
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f).padding(bottom = if (isLast) 0.dp else 14.dp)) {
            Text(chapter.era.uppercase(), style = AppTheme.typography.overline, modifier = Modifier.padding(top = 6.dp))
            Spacer(Modifier.height(6.dp))
            InfoSurface(onClick = { expanded = !expanded }, padding = 14.dp) {
                Text(chapter.title, style = AppTheme.typography.titleSmall)
                Spacer(Modifier.height(6.dp))
                Text(
                    chapter.body,
                    style = AppTheme.typography.bodyMedium.copy(color = colors.body),
                    maxLines = if (expanded) Int.MAX_VALUE else 3,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    if (expanded) "Show less" else "Read more",
                    style = AppTheme.typography.labelMedium.copy(color = colors.primary, fontWeight = FontWeight.SemiBold),
                    modifier = Modifier.clip(AppTheme.shapes.small).heightIn(min = 32.dp).padding(top = 8.dp)
                )
            }
        }
    }
}
