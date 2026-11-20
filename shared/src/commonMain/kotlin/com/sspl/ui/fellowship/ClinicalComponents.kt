package com.sspl.ui.fellowship

import com.sspl.designsystem.icons.SsplIcons

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sspl.designsystem.components.BadgeVariant
import com.sspl.designsystem.components.IconTile
import com.sspl.designsystem.components.ListRowCard
import com.sspl.designsystem.components.StatusBadge
import com.sspl.designsystem.theme.AppTheme

internal fun statusVariant(status: String): BadgeVariant = when (status.uppercase()) {
    "ACCEPTED", "APPROVED", "VERIFIED", "COMPLETED" -> BadgeVariant.SUCCESS
    "PENDING", "SUBMITTED" -> BadgeVariant.WARNING
    "REJECTED" -> BadgeVariant.ERROR
    else -> BadgeVariant.NEUTRAL
}

@Composable
internal fun ClinicalStatus(status: String) = StatusBadge(status, variant = statusVariant(status))

@Composable
internal fun CaseRow(c: CaseItem, showFellow: Boolean, footer: (@Composable ColumnScope.() -> Unit)? = null) {
    val role = CASE_ROLES.firstOrNull { it.first == c.role }?.second ?: c.role?.humanize()
    ListRowCard(
        title = c.procedure,
        subtitle = listOfNotNull(prettyDate(c.date), role?.let { "Role: $it" }, if (showFellow) c.fellowName else null).joinToString(" · "),
        meta = listOfNotNull(c.diagnosis?.let { "Dx: $it" }, c.patientRef?.let { "Ref: $it" }, c.feedback?.let { "Feedback: $it" })
            .joinToString("\n").ifBlank { null },
        leading = { IconTile(SsplIcons.MedicalServices) },
        trailing = { ClinicalStatus(c.status) },
        footer = footer
    )
}

@Composable
internal fun AttendanceRow(a: AttendanceItem, showFellow: Boolean, footer: (@Composable ColumnScope.() -> Unit)? = null) {
    val type = ATTENDANCE_TYPES.firstOrNull { it.first == a.type }?.second ?: a.type.humanize()
    ListRowCard(
        title = "$type — ${a.hours ?: "0"}h",
        subtitle = listOfNotNull(prettyDate(a.date), if (showFellow) a.fellowName else null).joinToString(" · "),
        leading = { IconTile(SsplIcons.EventAvailable) },
        trailing = { ClinicalStatus(a.status) },
        footer = footer
    )
}

@Composable
internal fun AssessmentRow(a: AssessmentItem) {
    ListRowCard(
        title = a.title,
        subtitle = listOfNotNull(a.type?.humanize(), "Score: ${a.score?.let { (kotlin.math.round(it * 10) / 10).toString() } ?: "—"}", prettyDate(a.date)).joinToString(" · "),
        meta = a.strengths,
        leading = { IconTile(SsplIcons.Assessment) }
    )
}

@Composable
internal fun ResearchRow(r: ResearchItem, onOpenFile: ((String) -> Unit)?, footer: (@Composable ColumnScope.() -> Unit)? = null) {
    val type = PUBLICATION_TYPES.firstOrNull { it.first == r.type }?.second ?: r.type?.humanize()
    ListRowCard(
        title = r.title,
        subtitle = listOfNotNull(r.authors?.let { "By $it" }, type, r.venue, r.year?.let { "($it)" }).joinToString(" · "),
        meta = listOfNotNull(r.abstract, "Submitted: ${prettyDate(r.createdAt)}", r.feedback?.let { "Feedback: \"$it\"" }).joinToString("\n"),
        leading = { IconTile(SsplIcons.Article) },
        trailing = { ClinicalStatus(r.status) },
        onClick = r.fileUrl?.let { url -> onOpenFile?.let { { it(url) } } },
        footer = footer
    )
}

/** Bottom-sheet form shell with IME + nav-bar handling. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun FormSheet(title: String, subtitle: String?, onDismiss: () -> Unit, content: @Composable ColumnScope.() -> Unit) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = AppTheme.colors.surfaceElevated,
        shape = AppTheme.shapes.sheet
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(title, style = AppTheme.typography.titleLarge.copy(color = AppTheme.colors.heading))
                if (subtitle != null) Text(subtitle, style = AppTheme.typography.bodySmall)
            }
            content()
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
internal fun SectionEmpty(text: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 18.dp)) {
        Text(text, style = AppTheme.typography.bodyMedium.copy(color = AppTheme.colors.muted))
    }
}
