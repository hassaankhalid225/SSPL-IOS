package com.sspl.ui.fellowship

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.sspl.designsystem.components.GlassPill
import com.sspl.designsystem.components.GradientHeroCard
import com.sspl.designsystem.components.IconTile
import com.sspl.designsystem.components.OutlineButton
import com.sspl.designsystem.components.PrimaryButton
import com.sspl.designsystem.components.SsplCard
import com.sspl.designsystem.components.SsplTextField
import com.sspl.designsystem.theme.AppTheme

// -------------------------------------------------------------------------------------
// Page intro
// -------------------------------------------------------------------------------------

@Composable
internal fun FellowshipHero(
    eyebrow: String,
    title: String,
    summary: String?,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    extra: @Composable ColumnScope.() -> Unit = {}
) {
    GradientHeroCard(modifier = modifier) {
        GlassPill(text = eyebrow.uppercase(), icon = icon)
        Spacer(Modifier.size(10.dp))
        Text(title, style = AppTheme.typography.headlineSmall.copy(color = Color.White, fontWeight = FontWeight.Bold))
        if (!summary.isNullOrBlank()) {
            Spacer(Modifier.size(4.dp))
            Text(summary, style = AppTheme.typography.bodyMedium.copy(color = Color.White.copy(alpha = 0.82f)))
        }
        extra()
    }
}

// -------------------------------------------------------------------------------------
// Form building blocks
// -------------------------------------------------------------------------------------

/** Card that groups one form section, with optional eyebrow + title + subtitle. */
@Composable
internal fun FormSection(
    title: String,
    modifier: Modifier = Modifier,
    eyebrow: String? = null,
    subtitle: String? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    SsplCard(modifier = modifier) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                if (eyebrow != null) Text(eyebrow.uppercase(), style = AppTheme.typography.overline)
                Text(title, style = AppTheme.typography.titleMedium.copy(color = AppTheme.colors.heading))
                if (!subtitle.isNullOrBlank()) Text(subtitle, style = AppTheme.typography.bodySmall)
            }
            content()
        }
    }
}

internal fun labelOf(label: String, required: Boolean) = if (required) "$label *" else label

/** Text input with "Next" focus movement and helper / error text. */
@Composable
internal fun FormField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    required: Boolean = false,
    help: String? = null,
    error: String? = null,
    placeholder: String? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    multiline: Boolean = false,
    enabled: Boolean = true
) {
    val focus = LocalFocusManager.current
    Column(modifier) {
        SsplTextField(
            value = value,
            onValueChange = onValueChange,
            label = labelOf(label, required),
            placeholder = placeholder,
            error = error,
            enabled = enabled,
            singleLine = !multiline,
            maxLines = if (multiline) 6 else 1,
            keyboardType = keyboardType,
            keyboardActions = KeyboardActions(
                onNext = { focus.moveFocus(FocusDirection.Down) },
                onDone = { focus.clearFocus() }
            )
        )
        if (help != null && error == null) {
            Text(help, style = AppTheme.typography.bodySmall, modifier = Modifier.padding(start = 4.dp, top = 4.dp))
        }
    }
}

@Composable
internal fun FieldLabel(label: String, required: Boolean = false, help: String? = null, error: String? = null) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            labelOf(label, required),
            style = AppTheme.typography.labelMedium.copy(
                color = if (error != null) AppTheme.colors.error else AppTheme.colors.heading,
                fontWeight = FontWeight.Medium
            )
        )
        if (help != null) Text(help, style = AppTheme.typography.bodySmall)
    }
}

@Composable
internal fun FieldError(error: String?) {
    AnimatedVisibility(visible = error != null) {
        Text(error.orEmpty(), style = AppTheme.typography.labelSmall.copy(color = AppTheme.colors.error), modifier = Modifier.padding(start = 4.dp))
    }
}

/** Single-choice list of tappable rows (radio semantics). */
@Composable
internal fun ChoiceGroup(
    options: List<String>,
    selected: String?,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    required: Boolean = false,
    help: String? = null,
    error: String? = null,
    enabled: Boolean = true,
    optionLabel: (String) -> String = { it }
) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (label != null) FieldLabel(label, required, help, error)
        options.forEach { opt ->
            OptionRow(text = optionLabel(opt), checked = selected == opt, radio = true, enabled = enabled) { onSelect(opt) }
        }
        FieldError(error)
    }
}

/** Compact single-choice pills (for short options such as Yes / No). */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun PillChoice(
    options: List<String>,
    selected: String?,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    required: Boolean = false,
    error: String? = null,
    enabled: Boolean = true
) {
    val colors = AppTheme.colors
    Column(modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (label != null) FieldLabel(label, required, null, error)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            options.forEach { opt ->
                val on = opt == selected
                Box(
                    modifier = Modifier
                        .heightIn(min = 44.dp)
                        .clip(RoundedCornerShape(50))
                        .background(if (on) colors.primary else colors.boxColor)
                        .border(1.dp, if (on) colors.primary else colors.border, RoundedCornerShape(50))
                        .clickable(enabled = enabled, role = Role.RadioButton) { onSelect(opt) }
                        .padding(horizontal = 18.dp, vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(opt, style = AppTheme.typography.bodyMedium.copy(color = if (on) Color.White else colors.body, fontWeight = FontWeight.Medium))
                }
            }
        }
        FieldError(error)
    }
}

/** Multi-choice list (checkbox semantics). */
@Composable
internal fun CheckGroup(
    options: List<String>,
    selected: Collection<String>,
    onToggle: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    required: Boolean = false,
    help: String? = null,
    error: String? = null,
    enabled: Boolean = true
) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (label != null) FieldLabel(label, required, help, error)
        options.forEach { opt ->
            OptionRow(text = opt, checked = opt in selected, radio = false, enabled = enabled) { onToggle(opt) }
        }
        FieldError(error)
    }
}

@Composable
internal fun OptionRow(text: String, checked: Boolean, radio: Boolean, enabled: Boolean = true, onClick: () -> Unit) {
    val colors = AppTheme.colors
    val shape = AppTheme.shapes.small
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .clip(shape)
            .background(if (checked) colors.surfaceTint else colors.surface)
            .border(1.dp, if (checked) colors.primary.copy(alpha = 0.6f) else colors.border, shape)
            .clickable(enabled = enabled, role = if (radio) Role.RadioButton else Role.Checkbox, onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val markShape = if (radio) CircleShape else RoundedCornerShape(6.dp)
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(markShape)
                .background(if (checked) colors.primary else Color.Transparent)
                .border(1.5.dp, if (checked) colors.primary else colors.muted.copy(alpha = 0.6f), markShape),
            contentAlignment = Alignment.Center
        ) {
            if (checked) Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(14.dp))
        }
        Spacer(Modifier.width(12.dp))
        Text(text, style = AppTheme.typography.bodyMedium.copy(color = if (enabled) colors.heading else colors.muted), modifier = Modifier.weight(1f))
    }
}

/** Dropdown select field. [options] are (value, label). */
@Composable
internal fun SelectField(
    label: String,
    options: List<Pair<String, String>>,
    selected: String?,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
    required: Boolean = false,
    placeholder: String = "Select",
    help: String? = null,
    error: String? = null,
    allowClear: Boolean = false,
    enabled: Boolean = true
) {
    var open by remember { mutableStateOf(false) }
    val colors = AppTheme.colors
    val shape = AppTheme.shapes.button
    val current = options.firstOrNull { it.first == selected }?.second
    Column(modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        FieldLabel(label, required, null, error)
        Box {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 52.dp)
                    .clip(shape)
                    .background(colors.surface)
                    .border(1.dp, if (error != null) colors.error else colors.border, shape)
                    .clickable(enabled = enabled) { open = true }
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    current ?: placeholder,
                    style = AppTheme.typography.bodyLarge.copy(color = if (current != null) colors.heading else colors.muted),
                    modifier = Modifier.weight(1f)
                )
                Icon(Icons.Default.ArrowDropDown, contentDescription = "Open options", tint = colors.muted)
            }
            DropdownMenu(expanded = open, onDismissRequest = { open = false }, containerColor = colors.surfaceElevated) {
                if (allowClear) {
                    DropdownMenuItem(text = { Text(placeholder, style = AppTheme.typography.bodyMedium.copy(color = colors.muted)) }, onClick = { onSelect(""); open = false })
                }
                options.forEach { (value, text) ->
                    DropdownMenuItem(
                        text = { Text(text, style = AppTheme.typography.bodyMedium.copy(color = colors.heading)) },
                        onClick = { onSelect(value); open = false }
                    )
                }
            }
        }
        if (help != null && error == null) Text(help, style = AppTheme.typography.bodySmall)
        FieldError(error)
    }
}

/** 1..N scale buttons (ratings / confidence). */
@Composable
internal fun ScaleButtons(
    value: Int?,
    onChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    max: Int = 5,
    enabled: Boolean = true,
    error: Boolean = false
) {
    val colors = AppTheme.colors
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        for (i in 1..max) {
            val on = value == i
            Box(
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 44.dp)
                    .clip(AppTheme.shapes.small)
                    .background(if (on) colors.primary else colors.boxColor)
                    .border(1.dp, when { on -> colors.primary; error -> colors.error; else -> colors.border }, AppTheme.shapes.small)
                    .clickable(enabled = enabled, role = Role.RadioButton) { onChange(i) },
                contentAlignment = Alignment.Center
            ) {
                Text("$i", style = AppTheme.typography.titleSmall.copy(color = if (on) Color.White else colors.heading))
            }
        }
    }
}

/** Inline note / alert box. */
@Composable
internal fun InlineNote(
    text: String,
    modifier: Modifier = Modifier,
    tone: NoteTone = NoteTone.Info,
    title: String? = null
) {
    val colors = AppTheme.colors
    val (bg, fg, icon) = when (tone) {
        NoteTone.Info -> Triple(colors.infoContainer, colors.info, Icons.Default.Info)
        NoteTone.Warning -> Triple(colors.warningContainer, colors.warning, Icons.Default.Info)
        NoteTone.Error -> Triple(colors.errorContainer, colors.error, Icons.Default.ErrorOutline)
        NoteTone.Success -> Triple(colors.successContainer, colors.success, Icons.Default.CheckCircle)
    }
    Row(
        modifier = modifier.fillMaxWidth().clip(AppTheme.shapes.small).background(bg).padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(icon, null, tint = fg, modifier = Modifier.size(18.dp))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            if (title != null) Text(title, style = AppTheme.typography.titleSmall.copy(color = fg))
            Text(text, style = AppTheme.typography.bodyMedium.copy(color = if (title != null) colors.body else fg))
        }
    }
}

enum class NoteTone { Info, Warning, Error, Success }

/** Sticky Back / Next bar for wizards. Owns the nav-bar inset. */
@Composable
internal fun WizardBar(
    isFirst: Boolean,
    isLast: Boolean,
    onBack: () -> Unit,
    onNext: () -> Unit,
    loading: Boolean,
    nextText: String = "Continue",
    submitText: String = "Submit application",
    enabled: Boolean = true
) {
    Surface(color = AppTheme.colors.surface, shadowElevation = 16.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (!isFirst) {
                OutlineButton(text = "Back", onClick = onBack, icon = Icons.AutoMirrored.Filled.ArrowBack, enabled = !loading, modifier = Modifier.weight(1f))
            }
            PrimaryButton(
                text = if (isLast) submitText else nextText,
                onClick = onNext,
                loading = loading,
                enabled = enabled && !loading,
                trailingIcon = if (isLast) null else Icons.AutoMirrored.Filled.ArrowForward,
                modifier = Modifier.weight(if (isFirst) 1f else 1.4f)
            )
        }
    }
}

/** Small "label: value" chip used inside cards. */
@Composable
internal fun MetaChip(text: String, modifier: Modifier = Modifier, icon: ImageVector? = null) {
    val colors = AppTheme.colors
    Row(
        modifier = modifier.clip(RoundedCornerShape(50)).background(colors.boxColor).padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        if (icon != null) Icon(icon, null, tint = colors.primary, modifier = Modifier.size(13.dp))
        Text(text, style = AppTheme.typography.labelMedium.copy(color = colors.body))
    }
}

/** Requirement checklist row (✓ / ○). */
@Composable
internal fun RequirementRow(label: String, detail: String?, met: Boolean) {
    val colors = AppTheme.colors
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Box(
            Modifier.size(24.dp).clip(CircleShape).background(if (met) colors.successContainer else colors.boxColor),
            contentAlignment = Alignment.Center
        ) {
            if (met) Icon(Icons.Default.Check, contentDescription = "Met", tint = colors.success, modifier = Modifier.size(15.dp))
            else Box(Modifier.size(8.dp).clip(CircleShape).background(colors.muted.copy(alpha = 0.5f)))
        }
        Spacer(Modifier.width(10.dp))
        Text(label, style = AppTheme.typography.bodyMedium.copy(color = colors.heading), modifier = Modifier.weight(1f))
        if (detail != null) Text(detail, style = AppTheme.typography.labelMedium.copy(color = if (met) colors.success else colors.muted))
    }
}

/** Receipt shown after a successful application. */
@Composable
internal fun ReceiptCard(
    eyebrow: String,
    title: String,
    rows: List<Pair<String, String>>,
    note: String,
    modifier: Modifier = Modifier,
    primaryText: String,
    onPrimary: () -> Unit,
    secondaryText: String? = null,
    onSecondary: (() -> Unit)? = null
) {
    SsplCard(modifier) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            IconTile(Icons.Default.CheckCircle, tint = AppTheme.colors.success, background = AppTheme.colors.successContainer, size = 52.dp, iconSize = 26.dp)
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(eyebrow.uppercase(), style = AppTheme.typography.overline)
                Text(title, style = AppTheme.typography.headlineSmall.copy(color = AppTheme.colors.heading))
            }
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                rows.forEach { (k, v) ->
                    Column {
                        Text(k, style = AppTheme.typography.labelMedium)
                        Text(v, style = AppTheme.typography.bodyLarge.copy(color = AppTheme.colors.heading, fontWeight = FontWeight.SemiBold))
                    }
                }
            }
            Text(note, style = AppTheme.typography.bodyMedium)
            PrimaryButton(text = primaryText, onClick = onPrimary)
            if (secondaryText != null && onSecondary != null) OutlineButton(text = secondaryText, onClick = onSecondary)
        }
    }
}
