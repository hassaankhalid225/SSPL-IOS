package com.sspl.ui.fellowship

import com.sspl.designsystem.icons.SsplIcons

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.sspl.Screen
import com.sspl.core.repositories.FellowshipRepository
import com.sspl.designsystem.components.ChipRow
import com.sspl.designsystem.components.EmptyState
import com.sspl.designsystem.components.ErrorState
import com.sspl.designsystem.components.FactRow
import com.sspl.designsystem.components.HeroStat
import com.sspl.designsystem.components.IconTile
import com.sspl.designsystem.components.LoadState
import com.sspl.designsystem.components.LocalShellActions
import com.sspl.designsystem.components.OutlineButton
import com.sspl.designsystem.components.PrimaryButton
import com.sspl.designsystem.components.SearchBar
import com.sspl.designsystem.components.SegmentedTabs
import com.sspl.designsystem.components.SkeletonList
import com.sspl.designsystem.components.SsplCard
import com.sspl.designsystem.components.SsplRefreshBox
import com.sspl.designsystem.theme.AppTheme
import com.sspl.platform.shared.UrlOpener
import org.koin.compose.koinInject

/**
 * E2: Accredited training centres & faculty directory (TrainingCenterDirectory.jsx).
 * Search + city chips + availability tabs; cards expand to show supervisors & facilities.
 */
@Composable
fun TrainingCentersScreen(
    navController: NavController,
    repository: FellowshipRepository = koinInject(),
    urlOpener: UrlOpener = koinInject()
) {
    val viewModel: TrainingCentersViewModel = viewModel { TrainingCentersViewModel(repository) }
    val state by viewModel.state.collectAsStateWithLifecycle()

    SsplRefreshBox(isRefreshing = state.isRefreshing, onRefresh = viewModel::refresh, modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize().background(AppTheme.colors.background),
            contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item(key = "hero") {
                val all = (state.centers as? LoadState.Ready)?.data
                FellowshipHero(
                    eyebrow = "Training centres & supervisors",
                    title = "Accredited centres & faculty",
                    summary = "Browse certified surgical training centres across Pakistan, review supervisors and apply to your preferred hospital.",
                    icon = SsplIcons.Business,
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    if (all != null) {
                        Spacer(Modifier.height(16.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            HeroStat("${all.size}", "Centres")
                            HeroStat("${state.cities.size - 1}", "Cities")
                            HeroStat("${all.count { it.availability == "OPEN" }}", "Open now")
                            HeroStat("${all.sumOf { it.supervisors.size }}", "Faculty")
                        }
                    }
                }
            }

            item(key = "search") {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    SearchBar(
                        query = state.query,
                        onQueryChange = viewModel::setQuery,
                        placeholder = "Search hospital, trainer or facility",
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    if (state.cities.size > 1) {
                        ChipRow(options = state.cities, selected = state.city, onSelect = viewModel::setCity)
                    }
                    SegmentedTabs(
                        options = state.availabilities,
                        selectedIndex = state.availabilities.indexOf(state.availability).coerceAtLeast(0),
                        onSelect = { viewModel.setAvailability(state.availabilities[it]) },
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }

            when (val c = state.centers) {
                is LoadState.Loading -> item(key = "loading") { SkeletonList(count = 4) }
                is LoadState.Error -> item(key = "error") { ErrorState(title = "Centres didn't load", message = c.message, onRetry = viewModel::retry) }
                is LoadState.Ready -> {
                    if (c.data.isEmpty()) {
                        item(key = "empty") {
                            EmptyState(
                                title = "No centres listed yet",
                                message = "Accredited training centres will appear here once approved by the Society.",
                                icon = SsplIcons.Business
                            )
                        }
                    } else if (state.filtered.isEmpty()) {
                        item(key = "noMatch") {
                            EmptyState(
                                title = "No centres match your filters",
                                message = "Try another city or search term, or view all centres.",
                                icon = SsplIcons.SearchOff,
                                actionText = "Reset filters",
                                onActionClick = viewModel::resetFilters
                            )
                        }
                    } else {
                        item(key = "count") {
                            Text(
                                "Showing ${state.filtered.size} of ${c.data.size} accredited centres",
                                style = AppTheme.typography.labelMedium,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                        items(state.filtered, key = { it.id }) { center ->
                            CenterCard(
                                center = center,
                                onEmail = { center.email?.let { urlOpener.openUrl("mailto:$it") } },
                                onApply = {
                                    FellowshipNavArgs.programId = center.programId
                                    FellowshipNavArgs.centerId = center.id
                                    navController.navigate(Screen.ApplyAsFellowScreen.route) { launchSingleTop = true }
                                },
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                    }
                }
            }

            item(key = "accreditation") {
                SsplCard(Modifier.padding(horizontal = 16.dp).padding(top = 8.dp)) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("FOR INSTITUTIONS", style = AppTheme.typography.overline)
                        Text("Become an accredited centre", style = AppTheme.typography.titleMedium.copy(color = AppTheme.colors.heading))
                        Text(
                            "Surgical units with the required facilities and case volume can apply to train SSPL fellows.",
                            style = AppTheme.typography.bodyMedium
                        )
                        OutlineButton(
                            text = "Register a training centre",
                            onClick = {
                                navController.navigate(Screen.ApplyAsTrainingCentreScreen.route) { launchSingleTop = true }
                            }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CenterCard(center: DirectoryCenter, onEmail: () -> Unit, onApply: () -> Unit, modifier: Modifier = Modifier) {
    var expanded by rememberSaveable(center.id) { mutableStateOf(false) }
    val colors = AppTheme.colors
    SsplCard(modifier.animateContentSize()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                IconTile(SsplIcons.Business)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(center.institution, style = AppTheme.typography.titleSmall.copy(color = colors.heading), maxLines = 3, overflow = TextOverflow.Ellipsis)
                    center.unit?.let { Text(it, style = AppTheme.typography.bodySmall) }
                }
                Spacer(Modifier.width(8.dp))
                AvailabilityBadge(center.availability)
            }
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                MetaChip(center.city ?: "Pakistan", icon = SsplIcons.LocationOn)
                MetaChip("Seats ${center.seatsLabel}", icon = SsplIcons.Groups)
                center.programName?.let { MetaChip(it, icon = SsplIcons.WorkspacePremium) }
            }
            center.masterTrainer?.let { FactRow(SsplIcons.Person, "Master trainer", it) }

            Row(
                Modifier.fillMaxWidth().clip(AppTheme.shapes.small).clickable { expanded = !expanded }.heightIn(min = 44.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    if (expanded) "Hide details" else "Faculty, facilities & contact",
                    style = AppTheme.typography.labelMedium.copy(color = colors.primary, fontWeight = FontWeight.SemiBold),
                    modifier = Modifier.weight(1f)
                )
                Icon(if (expanded) SsplIcons.ExpandLess else SsplIcons.ExpandMore, contentDescription = null, tint = colors.primary)
            }

            AnimatedVisibility(expanded) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    HorizontalDivider(color = colors.divider)
                    center.address?.let { FactRow(SsplIcons.LocationOn, "Address", it) }
                    if (center.supervisors.isNotEmpty()) {
                        Text("Faculty & supervisors (${center.supervisors.size})", style = AppTheme.typography.titleSmall.copy(color = colors.heading))
                        center.supervisors.forEach { s ->
                            Column(Modifier.fillMaxWidth().clip(AppTheme.shapes.small).background(colors.boxColor).padding(10.dp)) {
                                Text(s.name, style = AppTheme.typography.bodyMedium.copy(color = colors.heading, fontWeight = FontWeight.SemiBold))
                                val meta = listOfNotNull(s.role, s.designation, s.qualifications, s.pmdc?.let { "PMDC $it" }).joinToString(" · ")
                                if (meta.isNotBlank()) Text(meta, style = AppTheme.typography.bodySmall)
                            }
                        }
                    }
                    if (center.facilities.isNotEmpty()) {
                        Text("Facilities", style = AppTheme.typography.titleSmall.copy(color = colors.heading))
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            center.facilities.forEach { MetaChip(it) }
                        }
                    }
                    center.email?.let { email ->
                        Row(
                            Modifier.fillMaxWidth().clip(AppTheme.shapes.small).clickable(onClickLabel = "Email centre", onClick = onEmail),
                        ) { FactRow(SsplIcons.Email, "Email", email) }
                    }
                }
            }

            PrimaryButton(
                text = if (center.isBooked) "Apply (alternative / waitlist)" else "Apply to this centre",
                onClick = onApply
            )
        }
    }
}
