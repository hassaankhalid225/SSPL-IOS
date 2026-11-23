package com.sspl.ui.fellowship

import com.sspl.designsystem.icons.SsplIcons

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Feedback
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.SupervisorAccount
import androidx.compose.material.icons.filled.SupportAgent
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.sspl.Screen
import com.sspl.core.models.FellowshipStats
import com.sspl.designsystem.components.ErrorState
import com.sspl.designsystem.components.EmptyState
import com.sspl.designsystem.components.HeroStat
import com.sspl.designsystem.components.IconTile
import com.sspl.designsystem.components.ListRowCard
import com.sspl.designsystem.components.LoadState
import com.sspl.designsystem.components.LocalShellActions
import com.sspl.designsystem.components.NavTile
import com.sspl.designsystem.components.OutlineButton
import com.sspl.designsystem.components.PrimaryButton
import com.sspl.designsystem.components.SectionHeader
import com.sspl.designsystem.components.SecondaryButton
import com.sspl.designsystem.components.SkeletonBox
import com.sspl.designsystem.components.SkeletonListItem
import com.sspl.designsystem.components.SkeletonMediaCard
import com.sspl.designsystem.components.SsplCard
import com.sspl.designsystem.components.SsplImage
import com.sspl.designsystem.components.SsplRefreshBox
import com.sspl.designsystem.components.StatusBadge
import com.sspl.designsystem.components.BadgeVariant
import com.sspl.designsystem.components.ChipRow
import com.sspl.designsystem.theme.AppTheme
import com.sspl.session.UserSession
import com.sspl.core.repositories.FellowshipRepository
import org.koin.compose.koinInject

/**
 * E1: Fellowship hub — intro + live stats, open programmes (with full programme outline),
 * how it works, centres preview, guidelines and a role-aware dashboard shortcut.
 */
@Composable
fun FellowshipHubScreen(
    navController: NavController,
    repository: FellowshipRepository = koinInject(),
    userSession: UserSession = koinInject()
) {
    val viewModel: FellowshipHubViewModel = viewModel { FellowshipHubViewModel(repository, userSession) }
    val state by viewModel.state.collectAsStateWithLifecycle()

    fun open(route: String) = navController.navigate(route) { launchSingleTop = true }
    fun applyFellow(programId: Long?) {
        FellowshipNavArgs.programId = programId
        FellowshipNavArgs.centerId = null
        open(Screen.ApplyAsFellowScreen.route)
    }
    fun applyCenter(programId: Long?) {
        FellowshipNavArgs.programId = programId
        open(Screen.ApplyAsTrainingCentreScreen.route)
    }

    SsplRefreshBox(isRefreshing = state.isRefreshing, onRefresh = viewModel::refresh, modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize().background(AppTheme.colors.background),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item(key = "hero") { HubHero(state.stats) }

            if (state.isFellow || state.isTrainer) {
                item(key = "role") {
                    RoleShortcut(
                        isTrainer = state.isTrainer,
                        onDashboard = { open(if (state.isTrainer) Screen.TrainerDashboardScreen.route else Screen.FellowDashboardScreen.route) },
                        onFeedback = { open(Screen.FellowshipFeedbackScreen.route) }
                    )
                }
            }

            item(key = "nav") {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        NavTile(SsplIcons.Business, "Training centres", { open(Screen.TrainingCentresScreen.route) }, Modifier.weight(1f),
                            subtitle = if (state.totalCenters > 0) "${state.totalCenters} accredited" else "Directory")
                        NavTile(SsplIcons.Description, "Guidelines", { open(Screen.FellowshipGuidelinesScreen.route) }, Modifier.weight(1f),
                            subtitle = "SOPs & requirements")
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        NavTile(SsplIcons.School, "Apply as fellow", { applyFellow(null) }, Modifier.weight(1f), subtitle = "Candidate form")
                        NavTile(SsplIcons.Apartment, "Register centre", { applyCenter(null) }, Modifier.weight(1f), subtitle = "Accreditation")
                    }
                }
            }

            item(key = "programsHeader") {
                SectionHeader(title = "Open programmes", modifier = Modifier.padding(top = 10.dp))
            }
            when (val p = state.programs) {
                is LoadState.Loading -> item(key = "programsLoading") { SkeletonMediaCard(Modifier.fillMaxWidth(), imageAspect = 2f) }
                is LoadState.Error -> item(key = "programsError") {
                    ErrorState(title = "Programmes didn't load", message = p.message, onRetry = viewModel::retryPrograms)
                }
                is LoadState.Ready -> if (p.data.isEmpty()) {
                    // Web: nothing open → the default Basic Laparoscopy outline + "Preview the application".
                    item(key = "programsClosed") {
                        ProgramCard(
                            program = defaultLaparoscopyProgram(),
                            initiallyExpanded = false,
                            closed = true,
                            onApply = { applyFellow(null) },
                            onRegisterCenter = { applyCenter(null) }
                        )
                    }
                } else {
                    items(p.data, key = { "prog_" + (it.id ?: it.name.hashCode().toLong()) }) { prog ->
                        ProgramCard(
                            program = prog,
                            initiallyExpanded = false,
                            onApply = { applyFellow(prog.id) },
                            onRegisterCenter = { applyCenter(prog.id) }
                        )
                    }
                }
            }

            item(key = "how") { HowItWorks(Modifier.padding(top = 10.dp)) }

            item(key = "centersHeader") {
                SectionHeader(
                    title = "Accredited centres",
                    actionText = "View all",
                    onActionClick = { open(Screen.TrainingCentresScreen.route) },
                    modifier = Modifier.padding(top = 10.dp)
                )
            }
            // More than one open programme → let the user filter the centre list by programme (web selectedId).
            val openPrograms = (state.programs as? LoadState.Ready)?.data.orEmpty().filter { it.id != null }
            if (openPrograms.size > 1) {
                item(key = "centersFilter") {
                    val all = "All programmes"
                    val labelOf = { pr: Program -> pr.shortName.ifBlank { pr.name } }
                    ChipRow(
                        options = listOf(all) + openPrograms.map(labelOf),
                        selected = openPrograms.firstOrNull { it.id == state.selectedProgramId }?.let(labelOf) ?: all,
                        onSelect = { label -> viewModel.selectCentreProgram(openPrograms.firstOrNull { labelOf(it) == label }?.id) },
                        horizontalPadding = 0.dp
                    )
                }
            }
            when (val c = state.centers) {
                is LoadState.Loading -> items(3, key = { "cs_$it" }) { SkeletonListItem() }
                is LoadState.Error -> item(key = "centersError") {
                    ErrorState(title = "Centres didn't load", message = c.message, onRetry = viewModel::retryPrograms)
                }
                is LoadState.Ready -> if (c.data.isEmpty()) {
                    item(key = "centersEmpty") {
                        EmptyState(title = "No centres listed yet", message = "Accredited training centres will appear here once approved.", icon = SsplIcons.Business)
                    }
                } else {
                    items(c.data, key = { "c_" + it.id }) { center ->
                        ListRowCard(
                            title = center.institution,
                            subtitle = listOfNotNull(center.city, center.masterTrainer).joinToString(" · "),
                            meta = "Seats ${center.seatsLabel}",
                            leading = { IconTile(SsplIcons.Business) },
                            trailing = { AvailabilityBadge(center.availability) },
                            onClick = { open(Screen.TrainingCentresScreen.route) }
                        )
                    }
                }
            }

            item(key = "guidelines") {
                GuidelinesPromo(onOpen = { open(Screen.FellowshipGuidelinesScreen.route) }, modifier = Modifier.padding(top = 10.dp))
            }

            item(key = "contact") {
                ListRowCard(
                    title = "Fellowship secretariat",
                    subtitle = "Questions on eligibility, logbooks or centre placement?",
                    leading = { IconTile(SsplIcons.SupportAgent) },
                    trailing = { Text("Contact", style = AppTheme.typography.labelMedium.copy(color = AppTheme.colors.primary, fontWeight = FontWeight.SemiBold)) },
                    onClick = { open(Screen.ContactUsScreen.route) }
                )
            }
        }
    }
}

@Composable
private fun HubHero(stats: LoadState<FellowshipStats>) {
    FellowshipHero(
        eyebrow = "Education & training",
        title = "Mini fellowships",
        summary = "Structured surgical training pathways offered by the Society of Surgeons of Pakistan — Lahore Chapter.",
        icon = SsplIcons.WorkspacePremium
    ) {
        Spacer(Modifier.height(16.dp))
        when (stats) {
            is LoadState.Loading -> Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    repeat(2) { SkeletonBox(Modifier.weight(1f).height(38.dp)) }
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    repeat(2) { SkeletonBox(Modifier.weight(1f).height(38.dp)) }
                }
            }
            // Web DynamicCounter order + labels; failures fall back to the web's base counts.
            else -> {
                val d = (stats as? LoadState.Ready)?.data ?: FellowshipStats.WebDefaults
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        HeroStat("${d.graduatedFellows}+", "Completed", Modifier.weight(1f))
                        HeroStat("${d.activeFellows}", "Currently enrolled", Modifier.weight(1f))
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        HeroStat("${d.totalFellows}+", "Total fellows", Modifier.weight(1f))
                        HeroStat("${d.trainingCenters}", "Accredited centres", Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun RoleShortcut(isTrainer: Boolean, onDashboard: () -> Unit, onFeedback: () -> Unit) {
    SsplCard(onClick = onDashboard) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            IconTile(if (isTrainer) SsplIcons.SupervisorAccount else SsplIcons.MenuBook, size = 46.dp)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(if (isTrainer) "Trainer desk" else "Your fellowship", style = AppTheme.typography.overline)
                Text(
                    if (isTrainer) "Review cases, attendance & research" else "Open logbook & progress",
                    style = AppTheme.typography.titleSmall.copy(color = AppTheme.colors.heading)
                )
            }
            if (!isTrainer) {
                IconTile(
                    SsplIcons.Feedback,
                    modifier = Modifier.clip(AppTheme.shapes.small).clickable(onClickLabel = "Open feedback", onClick = onFeedback),
                    size = 44.dp
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ProgramCard(
    program: Program,
    initiallyExpanded: Boolean,
    onApply: () -> Unit,
    onRegisterCenter: () -> Unit,
    closed: Boolean = false
) {
    var expanded by rememberSaveable(program.id) { mutableStateOf(initiallyExpanded) }
    val colors = AppTheme.colors
    SsplCard(Modifier.animateContentSize()) {
        program.posters.firstOrNull()?.let { poster ->
            SsplImage(
                source = poster,
                contentDescription = "${program.name} poster",
                modifier = Modifier.fillMaxWidth().aspectRatio(16f / 9f)
            )
        }
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Column(Modifier.weight(1f)) {
                    if (program.shortName.isNotBlank()) Text(program.shortName.uppercase(), style = AppTheme.typography.overline)
                    Text(program.name, style = AppTheme.typography.titleMedium.copy(color = colors.heading))
                }
                Spacer(Modifier.width(8.dp))
                if (closed) StatusBadge("Closed", variant = BadgeVariant.NEUTRAL)
                else StatusBadge("Open", variant = BadgeVariant.SUCCESS)
            }
            if (program.description.isNotBlank()) Text(program.description, style = AppTheme.typography.bodyMedium)
            if (closed) {
                InlineNote(
                    "Applications are not open right now. The outline below is the Basic Laparoscopy program.",
                    tone = NoteTone.Warning
                )
            }
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                program.specialty?.let { MetaChip(it, icon = SsplIcons.Badge) }
                program.duration?.let { MetaChip(it, icon = SsplIcons.Schedule) }
                program.location?.let { MetaChip(it, icon = SsplIcons.LocationOn) }
                program.programType?.let { MetaChip(it, icon = SsplIcons.School) }
            }

            Row(
                Modifier.fillMaxWidth().clip(AppTheme.shapes.small).clickable { expanded = !expanded }.heightIn(min = 44.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    if (expanded) "Hide programme outline" else "View programme outline",
                    style = AppTheme.typography.labelMedium.copy(color = colors.primary, fontWeight = FontWeight.SemiBold),
                    modifier = Modifier.weight(1f)
                )
                Icon(if (expanded) SsplIcons.ExpandLess else SsplIcons.ExpandMore, contentDescription = null, tint = colors.primary)
            }
            AnimatedVisibility(expanded) { ProgramOutline(program) }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                SecondaryButton(text = "Register centre", onClick = onRegisterCenter, modifier = Modifier.weight(1f))
                if (closed) OutlineButton(text = "Preview the application", onClick = onApply, modifier = Modifier.weight(1f))
                else PrimaryButton(text = "Apply now", onClick = onApply, modifier = Modifier.weight(1f))
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ProgramOutline(program: Program) {
    val colors = AppTheme.colors
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        HorizontalDivider(color = colors.divider)
        if (program.categories.isNotEmpty()) {
            OutlineBlock("Eligibility") {
                program.categories.forEach { cat ->
                    Column(
                        Modifier.fillMaxWidth().clip(AppTheme.shapes.small).background(colors.boxColor).padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(cat.name, style = AppTheme.typography.titleSmall.copy(color = colors.heading))
                        Text(cat.description.ifBlank { cat.shortLabel }, style = AppTheme.typography.bodySmall)
                        cat.minimumExperience?.let {
                            Text("Experience requirement: $it", style = AppTheme.typography.labelMedium.copy(color = colors.body))
                        }
                    }
                }
            }
        }
        OutlineBlock("Training") {
            Text(
                program.facilities.joinToString(", ").ifBlank { "Facilities are confirmed with the training center." },
                style = AppTheme.typography.bodyMedium
            )
            if (program.procedures.isNotEmpty()) {
                Text("Procedures: " + program.procedures.joinToString(", "), style = AppTheme.typography.bodyMedium.copy(color = colors.heading))
            }
            program.caseVolume.forEach { r ->
                Text(
                    "${r.procedure}: minimum ${r.minimum ?: "—"}, target ${r.target ?: "—"}" + (r.maximum?.let { ", maximum $it" } ?: ""),
                    style = AppTheme.typography.bodySmall.copy(color = colors.heading)
                )
            }
        }
        if (program.objectives.isNotEmpty()) {
            OutlineBlock("What you will learn") {
                program.objectives.forEach { o ->
                    Text(
                        o.title + (o.description?.let { ". $it" } ?: ""),
                        style = AppTheme.typography.bodyMedium.copy(color = colors.heading)
                    )
                }
            }
        }
        if (program.documents.isNotEmpty()) {
            OutlineBlock("Documents to prepare") {
                program.documents.forEach { d ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(d.name, style = AppTheme.typography.bodyMedium.copy(color = colors.heading), modifier = Modifier.weight(1f))
                        Text(d.fileTypes.joinToString(", "), style = AppTheme.typography.labelMedium)
                        Spacer(Modifier.width(8.dp))
                        StatusBadge(if (d.required) "Required" else "Optional", variant = if (d.required) BadgeVariant.PRIMARY else BadgeVariant.NEUTRAL)
                    }
                }
            }
        }
    }
}

@Composable
private fun OutlineBlock(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(title, style = AppTheme.typography.titleSmall.copy(color = AppTheme.colors.heading))
        content()
    }
}

private val HOW_STEPS = listOf(
    "Hold SSPL life membership" to "Life membership is compulsory. Existing life members only need to quote their LM number.",
    "Apply for a programme" to "Choose an open fellowship, your experience category and up to three preferred centres.",
    "Complete SAGES online modules" to "Finish the Safe Cholecystectomy modules within 7–10 days and send proof to the secretariat.",
    "Train 4–6 weeks at your centre" to "Work under your master trainer and log every case in the digital logbook (codes A, B, C).",
    "Get verified and certified" to "After trainer sign-off and feedback, download your certificate and be honoured at SURGICON."
)

@Composable
private fun HowItWorks(modifier: Modifier = Modifier) {
    val colors = AppTheme.colors
    Column(modifier) {
        SectionHeader(title = "How it works")
        Spacer(Modifier.height(6.dp))
        SsplCard {
            Column(Modifier.padding(16.dp)) {
                HOW_STEPS.forEachIndexed { i, (title, body) ->
                    Row {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                Modifier.size(28.dp).clip(CircleShape).background(colors.primaryGradient),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("${i + 1}", style = AppTheme.typography.labelSmall.copy(color = Color.White, fontWeight = FontWeight.Bold))
                            }
                            if (i < HOW_STEPS.lastIndex) {
                                Box(Modifier.width(2.dp).height(44.dp).background(colors.border))
                            }
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f).padding(bottom = if (i < HOW_STEPS.lastIndex) 10.dp else 0.dp)) {
                            Text(title, style = AppTheme.typography.titleSmall.copy(color = colors.heading))
                            Text(body, style = AppTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GuidelinesPromo(onOpen: () -> Unit, modifier: Modifier = Modifier) {
    SsplCard(modifier, onClick = onOpen) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("MANDATORY SOPS", style = AppTheme.typography.overline)
            Text("Programme guidelines & requirements", style = AppTheme.typography.titleMedium.copy(color = AppTheme.colors.heading))
            Text(
                "SSPL life membership, SAGES online learning, digital logbook codes (A, B, C) and SURGICON 2027 certification.",
                style = AppTheme.typography.bodyMedium
            )
            OutlineButton(text = "View full guidelines", onClick = onOpen, icon = SsplIcons.Description)
        }
    }
}

@Composable
internal fun AvailabilityBadge(status: String) {
    when (status) {
        "BOOKED", "FULL" -> StatusBadge("Fully booked", variant = BadgeVariant.ERROR)
        "LIMITED" -> StatusBadge("Limited seats", variant = BadgeVariant.WARNING)
        "CLOSED" -> StatusBadge("Closed", variant = BadgeVariant.NEUTRAL)
        else -> StatusBadge("Open", variant = BadgeVariant.SUCCESS)
    }
}
