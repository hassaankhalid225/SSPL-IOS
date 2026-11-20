package com.sspl.ui.conference.programs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.sspl.core.models.Session
import com.sspl.designsystem.components.EmptyState
import com.sspl.designsystem.components.ExpandableText
import com.sspl.designsystem.components.FactRow
import com.sspl.designsystem.components.GlassPill
import com.sspl.designsystem.components.GradientHeroCard
import com.sspl.designsystem.components.OutlineButton
import com.sspl.designsystem.components.SsplCard
import com.sspl.designsystem.theme.AppTheme
import com.sspl.platform.shared.UrlOpener
import com.sspl.ui.components.SessionBreakDown
import com.sspl.ui.conference.components.AddOnSetsList
import com.sspl.ui.conference.components.safeTime
import com.sspl.utils.DisplayDates
import kotlinx.serialization.json.Json
import org.koin.compose.koinInject

/** C6: Session detail (scientific programme). Receives the small Session JSON as `data`. */
@Composable
fun SessionDetailScreen(
    navController: NavController,
    urlOpener: UrlOpener = koinInject()
) {
    val session: Session? = remember {
        navController.previousBackStackEntry?.savedStateHandle?.get<String>("data")?.let {
            runCatching { Json { ignoreUnknownKeys = true; coerceInputValues = true }.decodeFromString<Session>(it) }.getOrNull()
        }
    }
    if (session == null) {
        EmptyState(
            title = "Session unavailable",
            message = "Open the session again from the event schedule.",
            icon = Icons.Default.EventBusy,
            modifier = Modifier.fillMaxSize()
        )
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item(key = "hero") {
            GradientHeroCard {
                GlassPill(if (session.isBreak) "BREAK" else "SESSION")
                Spacer(Modifier.height(10.dp))
                Text(session.title.trim(), style = AppTheme.typography.headlineSmall.copy(color = Color.White))
            }
        }
        item(key = "facts") {
            SsplCard {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    DisplayDates.medium(session.startsAt).takeIf { it.isNotBlank() }?.let {
                        FactRow(Icons.Default.CalendarMonth, "Date", "${DisplayDates.weekdayShort(session.startsAt)}, $it")
                    }
                    val time = listOf(safeTime(session.startsAt), safeTime(session.endsAt)).filter { it.isNotBlank() }.joinToString(" – ")
                    if (time.isNotBlank()) FactRow(Icons.Default.Schedule, "Time", time)
                    val venue = listOfNotNull(session.venue?.title ?: session.venue?.name, session.venue?.address)
                        .filter { it.isNotBlank() }.joinToString(", ")
                    if (venue.isNotBlank()) FactRow(Icons.Default.Place, "Venue", venue)
                }
            }
        }
        session.summary?.takeIf { it.isNotBlank() }?.let { summary ->
            item(key = "summary") {
                SsplCard {
                    Text("OVERVIEW", style = AppTheme.typography.overline)
                    Spacer(Modifier.height(6.dp))
                    ExpandableText(summary.trim(), maxLines = 6)
                }
            }
        }
        session.breakdown?.takeIf { it.isNotEmpty() }?.let { breakdown ->
            item(key = "breakdown") {
                SsplCard {
                    Text("RUNDOWN & SPEAKERS", style = AppTheme.typography.overline)
                    Spacer(Modifier.height(6.dp))
                    SessionBreakDown(breakdown = breakdown, isClickEnabled = false)
                }
            }
        }
        session.addOnSets?.takeIf { it.isNotEmpty() }?.let { sets ->
            item(key = "addons") {
                Column {
                    Text("ADD-ONS", style = AppTheme.typography.overline, modifier = Modifier.padding(start = 4.dp, bottom = 8.dp))
                    AddOnSetsList(sets)
                }
            }
        }
        session.webLink?.takeIf { it.isNotBlank() }?.let { link ->
            item(key = "link") {
                OutlineButton("Open session link", onClick = { urlOpener.openUrl(link) }, icon = Icons.AutoMirrored.Filled.OpenInNew)
            }
        }
    }
}
