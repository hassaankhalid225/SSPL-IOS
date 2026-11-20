package com.sspl.ui.council

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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.sspl.designsystem.components.AvatarImage
import com.sspl.designsystem.components.FactRow
import com.sspl.designsystem.components.ListRowCard
import com.sspl.designsystem.components.SectionHeader
import com.sspl.designsystem.components.SsplImage
import com.sspl.designsystem.theme.AppTheme
import com.sspl.ui.components.PageIntroHero

/** Web: CabinetWelcomeSection / LeadershipSection (data/homepage/cabinet2026.ts). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExecutiveCouncilScreen(navController: NavController) {
    val bearers = remember { CABINET_AND_EXECUTIVES.filter { it.role in OFFICE_BEARER_ROLES } }
    val executives = remember { CABINET_AND_EXECUTIVES.filter { it.role == "Executive Member" } }
    val exOfficio = remember { CABINET_AND_EXECUTIVES.filter { it.role == "Ex-Officio Member" } }
    var selected by remember { mutableStateOf<CouncilMember?>(null) }

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(AppTheme.colors.background),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item(key = "hero") {
            PageIntroHero(
                eyebrow = "Term $CABINET_TERM_LABEL",
                eyebrowIcon = Icons.Default.Groups,
                title = "Cabinet and executive council",
                summary = "The surgeons guiding SSP Lahore's academic and clinical programme.",
                stats = listOf(
                    "${CABINET_AND_EXECUTIVES.size}" to "Members",
                    "${bearers.size}" to "Office bearers",
                    "${executives.size}" to "Executives"
                )
            )
            Spacer(Modifier.height(12.dp))
        }

        item(key = "bearersHeader") { SectionHeader(title = "Office bearers") }
        items(bearers.chunked(2), key = { row -> "b_" + row.first().name }) { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                row.forEach { m ->
                    BearerCard(m, onClick = { selected = m }, modifier = Modifier.weight(1f))
                }
                if (row.size == 1) Spacer(Modifier.weight(1f))
            }
        }

        item(key = "execHeader") {
            Spacer(Modifier.height(8.dp))
            SectionHeader(title = "Executive members · ${executives.size}")
        }
        items(executives, key = { "e_" + it.name }) { m -> MemberRow(m) { selected = m } }

        if (exOfficio.isNotEmpty()) {
            item(key = "exHeader") {
                Spacer(Modifier.height(8.dp))
                SectionHeader(title = "Ex-officio members")
            }
            items(exOfficio, key = { "x_" + it.name }) { m -> MemberRow(m) { selected = m } }
        }
    }

    selected?.let { member ->
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { selected = null },
            sheetState = sheetState,
            containerColor = AppTheme.colors.surfaceElevated,
            shape = AppTheme.shapes.sheet
        ) {
            MemberSheet(member)
        }
    }
}

@Composable
private fun BearerCard(member: CouncilMember, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = AppTheme.colors
    Column(
        modifier = modifier
            .clip(AppTheme.shapes.card)
            .background(colors.surface)
            .border(1.dp, colors.border.copy(alpha = 0.7f), AppTheme.shapes.card)
            .clickable(onClick = onClick)
    ) {
        Box(Modifier.fillMaxWidth().aspectRatio(0.95f)) {
            SsplImage(
                source = member.imageUrl,
                contentDescription = member.name,
                modifier = Modifier.fillMaxSize(),
                fallbackIcon = Icons.Default.Person,
                fallbackIconSize = 40.dp
            )
            // Role pill over the photo (image surface: white text allowed).
            Box(
                Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .background(Brush.verticalGradient(listOf(Color.Transparent, colors.gradientStart.copy(alpha = 0.75f))))
                    .padding(start = 10.dp, end = 10.dp, top = 22.dp, bottom = 8.dp)
            ) {
                Text(
                    member.role.uppercase(),
                    style = AppTheme.typography.labelSmall.copy(color = Color.White),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        Text(
            member.name,
            style = AppTheme.typography.titleSmall,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(12.dp)
        )
    }
}

@Composable
private fun MemberRow(member: CouncilMember, onClick: () -> Unit) {
    ListRowCard(
        title = member.name,
        subtitle = member.role,
        leading = { AvatarImage(url = member.imageUrl, name = member.name, size = 46.dp) },
        trailing = {
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = AppTheme.colors.muted
            )
        },
        onClick = onClick
    )
}

@Composable
private fun MemberSheet(member: CouncilMember) {
    Column(
        Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(start = 20.dp, end = 20.dp, bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            Modifier
                .size(120.dp)
                .clip(CircleShape)
                .border(3.dp, AppTheme.colors.primaryContainer, CircleShape)
        ) {
            AvatarImage(url = member.imageUrl, name = member.name, size = 120.dp)
        }
        Spacer(Modifier.height(14.dp))
        Text(member.name, style = AppTheme.typography.headlineSmall.copy(color = AppTheme.colors.heading), textAlign = TextAlign.Center)
        Text(member.role, style = AppTheme.typography.labelMedium.copy(color = AppTheme.colors.accent))
        Spacer(Modifier.height(18.dp))
        Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            FactRow(Icons.Default.Badge, "Position", member.role)
            FactRow(Icons.Default.CalendarMonth, "Term", CABINET_TERM_LABEL)
        }
        Spacer(Modifier.height(16.dp))
        Text(
            roleDescription(member.role),
            style = AppTheme.typography.bodyMedium.copy(color = AppTheme.colors.body),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.width(1.dp))
    }
}
