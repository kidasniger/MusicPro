package com.example.ui.settings

import android.app.TimePickerDialog
import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import com.example.R
import com.example.data.preferences.AccentChoice
import com.example.data.preferences.AppStyle
import com.example.data.preferences.AppearanceMode
import com.example.ui.components.MusicProCard
import com.example.ui.components.glass.rememberAnimationsEnabled
import com.example.ui.theme.LocalIsDarkTheme
import com.example.ui.theme.MusicProTokens
import com.example.ui.theme.accentSeedColor
import com.example.ui.theme.musicProBaseScheme

/**
 * Réglages d'apparence (mises à jour 013, 014, 017 et 018) : « Apparence » (Clair, Sombre, Auto,
 * Sombre le soir), « Style » (Minimal, Néon, Verre) avec trois cartes d'aperçu de 96 × 140 dp,
 * « Accent » et couleurs dynamiques. Tout changement s'applique tout de suite : le fondu de
 * 250 ms est fait par `MusicProTheme` (instantané avec « Réduire les animations »).
 */
@Composable
fun AppearanceSettingsCard(
    appearance: AppearanceMode,
    onAppearanceSelected: (AppearanceMode) -> Unit,
    style: AppStyle,
    onStyleSelected: (AppStyle) -> Unit,
    accent: AccentChoice,
    onAccentSelected: (AccentChoice) -> Unit,
    eveningStartMinutes: Int,
    eveningEndMinutes: Int,
    onEveningStartChange: (Int) -> Unit,
    onEveningEndChange: (Int) -> Unit,
    isDynamicColor: Boolean,
    onDynamicColorChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val dynamicSupported = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    val accentApplies = style != AppStyle.NEON && !(isDynamicColor && dynamicSupported)
    val isDark = LocalIsDarkTheme.current

    MusicProCard(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(MusicProTokens.Spacing.Lg),
            verticalArrangement = Arrangement.spacedBy(MusicProTokens.Spacing.Lg)
        ) {
            // --- Apparence ---------------------------------------------------------------------
            Column {
                SectionTitle(stringResource(R.string.appearance_title))
                AppearanceMode.values().forEach { mode ->
                    OptionRow(
                        label = appearanceLabel(mode),
                        selected = appearance == mode,
                        onClick = { onAppearanceSelected(mode) }
                    )
                }
                AnimatedVisibility(visible = appearance == AppearanceMode.DARK_EVENING) {
                    Column {
                        Text(
                            text = stringResource(R.string.appearance_evening_hint),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = MusicProTokens.Spacing.Sm)
                        )
                        TimeRow(
                            label = stringResource(R.string.appearance_evening_start),
                            minutes = eveningStartMinutes,
                            onChange = onEveningStartChange
                        )
                        TimeRow(
                            label = stringResource(R.string.appearance_evening_end),
                            minutes = eveningEndMinutes,
                            onChange = onEveningEndChange
                        )
                    }
                }
            }

            // --- Style -------------------------------------------------------------------------
            Column(verticalArrangement = Arrangement.spacedBy(MusicProTokens.Spacing.Md)) {
                SectionTitle(stringResource(R.string.style_title))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(MusicProTokens.Spacing.Md)
                ) {
                    AppStyle.values().forEach { option ->
                        StylePreviewCard(
                            styleOption = option,
                            selected = style == option,
                            isDark = isDark,
                            onClick = { onStyleSelected(option) }
                        )
                    }
                }
            }

            // --- Accent ------------------------------------------------------------------------
            Column(verticalArrangement = Arrangement.spacedBy(MusicProTokens.Spacing.Sm)) {
                SectionTitle(stringResource(R.string.accent_title))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(MusicProTokens.Spacing.Sm)
                ) {
                    AccentChoice.values().forEach { choice ->
                        FilterChip(
                            selected = accent == choice,
                            onClick = { onAccentSelected(choice) },
                            enabled = accentApplies,
                            label = { Text(accentLabel(choice)) },
                            leadingIcon = {
                                if (choice == AccentChoice.ARTWORK) {
                                    Icon(
                                        imageVector = Icons.Filled.AutoAwesome,
                                        contentDescription = null,
                                        modifier = Modifier.size(MusicProTokens.Size.IconSm)
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .size(MusicProTokens.Size.AccentDot)
                                            .clip(CircleShape)
                                            .background(accentSeedColor(choice, isDark))
                                    )
                                }
                            }
                        )
                    }
                }
                if (!accentApplies) {
                    Text(
                        text = stringResource(R.string.accent_unavailable),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // --- Couleurs dynamiques -----------------------------------------------------------
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = MusicProTokens.Size.Touch),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.dynamic_color_title),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = stringResource(R.string.dynamic_color_subtitle),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.width(MusicProTokens.Spacing.Md))
                Switch(
                    checked = isDynamicColor && dynamicSupported,
                    onCheckedChange = onDynamicColorChanged,
                    enabled = dynamicSupported
                )
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.padding(bottom = MusicProTokens.Spacing.Xs)
    )
}

@Composable
private fun OptionRow(label: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = MusicProTokens.Size.Touch)
            .selectable(selected = selected, onClick = onClick, role = Role.RadioButton),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = null)
        Spacer(modifier = Modifier.width(MusicProTokens.Spacing.Md))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun TimeRow(label: String, minutes: Int, onChange: (Int) -> Unit) {
    val context = LocalContext.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = MusicProTokens.Size.Touch),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        TextButton(
            onClick = {
                TimePickerDialog(
                    context,
                    { _, hour, minute -> onChange(hour * 60 + minute) },
                    minutes / 60,
                    minutes % 60,
                    true
                ).show()
            }
        ) {
            Text(text = stringResource(R.string.appearance_time_format, minutes / 60, minutes % 60))
        }
    }
}

/**
 * Carte d'aperçu d'un style (96 × 140 dp) : mini-écran dessiné avec les couleurs réelles du style
 * dans le mode affiché, coche sur le style actif (fondu de 250 ms).
 */
@Composable
private fun StylePreviewCard(
    styleOption: AppStyle,
    selected: Boolean,
    isDark: Boolean,
    onClick: () -> Unit
) {
    val label = styleLabel(styleOption)
    val animationsEnabled = rememberAnimationsEnabled()
    val previewShape = RoundedCornerShape(MusicProTokens.Radius.Md)
    val activeColor = MaterialTheme.colorScheme.primary
    val previewScheme = remember(styleOption, isDark) { musicProBaseScheme(styleOption, isDark) }

    Column(
        modifier = Modifier
            .width(MusicProTokens.Size.StylePreviewWidth)
            .selectable(selected = selected, onClick = onClick, role = Role.RadioButton),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(MusicProTokens.Spacing.Xs)
    ) {
        Box(
            modifier = Modifier
                .size(MusicProTokens.Size.StylePreviewWidth, MusicProTokens.Size.StylePreviewHeight)
                .clip(previewShape)
                .background(previewScheme.background)
                .border(
                    width = if (selected) MusicProTokens.Size.StrokeSelected else MusicProTokens.Size.Stroke,
                    color = if (selected) activeColor else previewScheme.outline,
                    shape = previewShape
                )
        ) {
            MiniScreen(styleOption = styleOption, scheme = previewScheme)
            Crossfade(
                targetState = selected,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(MusicProTokens.Spacing.Sm),
                animationSpec = tween(
                    durationMillis = if (animationsEnabled) MusicProTokens.Duration.ThemeFade else 0
                ),
                label = "style_check"
            ) { isSelected ->
                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .size(MusicProTokens.Size.Icon)
                            .clip(CircleShape)
                            .background(activeColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Check,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(MusicProTokens.Size.IconSm)
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.size(MusicProTokens.Size.Icon))
                }
            }
        }
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun MiniScreen(styleOption: AppStyle, scheme: androidx.compose.material3.ColorScheme) {
    val cardShape = RoundedCornerShape(MusicProTokens.Radius.Sm)
    val navShape = if (styleOption == AppStyle.GLASS) {
        MusicProTokens.Radius.PillShape
    } else {
        cardShape
    }
    val navColor = if (styleOption == AppStyle.GLASS) {
        scheme.primary.copy(alpha = 0.28f)
    } else {
        scheme.surface
    }
    val cardBorder = if (styleOption == AppStyle.NEON) {
        scheme.primary.copy(alpha = 0.35f)
    } else {
        scheme.outline
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(MusicProTokens.Spacing.Sm),
        verticalArrangement = Arrangement.spacedBy(MusicProTokens.Spacing.Sm)
    ) {
        Box(
            modifier = Modifier
                .width(40.dp)
                .height(MusicProTokens.Spacing.Sm)
                .clip(MusicProTokens.Radius.PillShape)
                .background(scheme.onBackground.copy(alpha = 0.85f))
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .clip(cardShape)
                .background(scheme.surface)
                .border(MusicProTokens.Size.Stroke, cardBorder, cardShape)
        ) {
            Column(
                modifier = Modifier.padding(MusicProTokens.Spacing.Sm),
                verticalArrangement = Arrangement.spacedBy(MusicProTokens.Spacing.Xs)
            ) {
                Box(
                    modifier = Modifier
                        .width(48.dp)
                        .height(MusicProTokens.Spacing.Xs)
                        .background(scheme.onSurface.copy(alpha = 0.7f))
                )
                Box(
                    modifier = Modifier
                        .width(32.dp)
                        .height(MusicProTokens.Spacing.Xs)
                        .background(scheme.onSurfaceVariant.copy(alpha = 0.6f))
                )
            }
        }
    }
    Box(modifier = Modifier.fillMaxWidth().height(MusicProTokens.Size.StylePreviewHeight)) {
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(MusicProTokens.Spacing.Sm)
                .fillMaxWidth()
                .height(MusicProTokens.Spacing.Xl)
                .clip(navShape)
                .background(navColor)
                .border(MusicProTokens.Size.Stroke, scheme.outline, navShape),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(MusicProTokens.Spacing.Sm)
                    .clip(CircleShape)
                    .background(scheme.primary)
            )
        }
    }
}

@Composable
private fun appearanceLabel(mode: AppearanceMode): String = stringResource(
    when (mode) {
        AppearanceMode.LIGHT -> R.string.appearance_light
        AppearanceMode.DARK -> R.string.appearance_dark
        AppearanceMode.AUTO -> R.string.appearance_auto
        AppearanceMode.DARK_EVENING -> R.string.appearance_dark_evening
    }
)

@Composable
private fun styleLabel(style: AppStyle): String = stringResource(
    when (style) {
        AppStyle.MINIMAL -> R.string.style_minimal
        AppStyle.NEON -> R.string.style_neon
        AppStyle.GLASS -> R.string.style_glass
    }
)

@Composable
private fun accentLabel(choice: AccentChoice): String = stringResource(
    when (choice) {
        AccentChoice.TERRACOTTA -> R.string.accent_terracotta
        AccentChoice.BLUE -> R.string.accent_blue
        AccentChoice.GREEN -> R.string.accent_green
        AccentChoice.VIOLET -> R.string.accent_violet
        AccentChoice.ARTWORK -> R.string.accent_artwork
    }
)
