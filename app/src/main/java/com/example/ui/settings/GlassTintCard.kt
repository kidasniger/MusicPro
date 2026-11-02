package com.example.ui.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.setProgress
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.preferences.GlassTintPreset
import com.example.data.preferences.clampGlassTintHue
import com.example.data.preferences.glassTintPresetFor
import com.example.ui.components.glass.GlassBackdropHost
import com.example.ui.components.glass.GlassSurface
import com.example.ui.components.glass.glassTintColor
import kotlin.math.roundToInt

/**
 * Réglage « Teinte du verre » : 9 pastilles (Aucune, Violet, Rouge, Orange, Jaune, Vert, Cyan, Bleu,
 * Magenta) et une barre arc-en-ciel pour choisir une teinte libre. `hue == null` signifie « Aucune ».
 *
 * Le voile teinté s'applique à tout le verre de l'application ; l'aperçu de cette carte se met à
 * jour en direct, comme celui de la carte « Effet verre ».
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GlassTintCard(
    hue: Float?,
    onHueChange: (Float?) -> Unit,
    modifier: Modifier = Modifier
) {
    val presets = listOf(
        GlassTintPreset.VIOLET to stringResource(R.string.glass_tint_violet),
        GlassTintPreset.RED to stringResource(R.string.glass_tint_red),
        GlassTintPreset.ORANGE to stringResource(R.string.glass_tint_orange),
        GlassTintPreset.YELLOW to stringResource(R.string.glass_tint_yellow),
        GlassTintPreset.GREEN to stringResource(R.string.glass_tint_green),
        GlassTintPreset.CYAN to stringResource(R.string.glass_tint_cyan),
        GlassTintPreset.BLUE to stringResource(R.string.glass_tint_blue),
        GlassTintPreset.MAGENTA to stringResource(R.string.glass_tint_magenta)
    )
    val noneLabel = stringResource(R.string.glass_tint_none)
    val selectedPreset = glassTintPresetFor(hue)
    val valueText = when {
        hue == null -> stringResource(R.string.glass_tint_value_none)
        selectedPreset != null -> presets.first { it.first == selectedPreset }.second
        else -> stringResource(R.string.glass_tint_value_free, hue.roundToInt())
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("glass_tint_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.glass_tint_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = valueText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.testTag("glass_tint_value")
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = stringResource(R.string.glass_tint_desc),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))

            GlassTintPreview()

            Spacer(modifier = Modifier.height(8.dp))

            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectableGroup(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                TintSwatch(
                    color = null,
                    label = noneLabel,
                    selected = hue == null,
                    onClick = { onHueChange(null) },
                    tag = "glass_tint_none"
                )
                presets.forEach { (preset, label) ->
                    TintSwatch(
                        color = glassTintColor(preset.hue),
                        label = label,
                        selected = selectedPreset == preset,
                        onClick = { onHueChange(preset.hue) },
                        tag = "glass_tint_${preset.name.lowercase()}"
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = stringResource(R.string.glass_tint_free_title),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = stringResource(R.string.glass_tint_free_desc),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            HueBar(
                hue = hue,
                onHueChange = { onHueChange(it) },
                description = stringResource(R.string.glass_tint_free_title),
                valueText = valueText
            )
        }
    }
}

/** Aperçu en direct : fond coloré et pilote de verre, teintés par le réglage courant. */
@Composable
private fun GlassTintPreview(modifier: Modifier = Modifier) {
    GlassBackdropHost(
        modifier = modifier
            .fillMaxWidth()
            .height(72.dp)
            .clip(RoundedCornerShape(12.dp))
            .testTag("glass_tint_preview"),
        background = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFF7C4DFF), Color(0xFF00E5FF), Color(0xFFFF4081))
                        )
                    )
            )
        },
        foreground = {
            GlassSurface(
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth(0.7f)
                    .height(40.dp)
            ) {
                Text(
                    text = stringResource(R.string.glass_tint_preview_label),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    )
}

/**
 * Pastille de teinte (zone tactile de 48 dp). `color == null` dessine la pastille « Aucune ».
 * L'état sélectionné ne repose pas que sur la couleur : bordure épaisse et coche.
 */
@Composable
private fun TintSwatch(
    color: Color?,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    tag: String
) {
    val outline = MaterialTheme.colorScheme.outline
    val onSurface = MaterialTheme.colorScheme.onSurface
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .semantics { contentDescription = label }
            .selectable(selected = selected, role = Role.RadioButton, onClick = onClick)
            .testTag(tag),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(color ?: Color.Transparent)
                .border(
                    width = if (selected) 3.dp else 1.dp,
                    color = if (selected) onSurface else outline,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            if (color == null) {
                Icon(
                    imageVector = Icons.Filled.Block,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            } else if (selected) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = null,
                    tint = if (color.luminance() > 0.5f) Color.Black else Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

/**
 * Barre arc-en-ciel : toucher ou glisser choisit une teinte libre (0 à 360°). Le spectre garde
 * toujours le rouge à gauche, même en écriture de droite à gauche. Accessible via les actions
 * de progression (TalkBack).
 */
@Composable
private fun HueBar(
    hue: Float?,
    onHueChange: (Float) -> Unit,
    description: String,
    valueText: String
) {
    val currentOnHueChange by rememberUpdatedState(onHueChange)
    var widthPx by remember { mutableIntStateOf(0) }
    val thumbSize = 28.dp
    val thumbPx = with(LocalDensity.current) { thumbSize.toPx() }
    val outline = MaterialTheme.colorScheme.outline
    val rainbow = remember {
        Brush.horizontalGradient(
            listOf(0f, 60f, 120f, 180f, 240f, 300f, 359.9f).map { glassTintColor(it) }
        )
    }

    fun hueAt(x: Float): Float =
        if (widthPx <= 0) 0f else (x / widthPx * 360f).coerceIn(0f, 359.9f)

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp)
                .onSizeChanged { widthPx = it.width }
                .pointerInput(Unit) {
                    detectTapGestures { offset -> currentOnHueChange(hueAt(offset.x)) }
                }
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragStart = { offset -> currentOnHueChange(hueAt(offset.x)) }
                    ) { change, _ ->
                        change.consume()
                        currentOnHueChange(hueAt(change.position.x))
                    }
                }
                .semantics {
                    contentDescription = description
                    stateDescription = valueText
                    progressBarRangeInfo = ProgressBarRangeInfo(hue ?: 0f, 0f..360f)
                    setProgress { target ->
                        currentOnHueChange(clampGlassTintHue(target))
                        true
                    }
                }
                .testTag("glass_tint_hue_bar"),
            contentAlignment = Alignment.CenterStart
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(28.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(rainbow)
                    .border(1.dp, outline, RoundedCornerShape(14.dp))
            )
            if (hue != null && widthPx > 0) {
                val centerPx = clampGlassTintHue(hue) / 360f * widthPx
                Box(
                    modifier = Modifier
                        .absoluteOffset { IntOffset((centerPx - thumbPx / 2f).roundToInt(), 0) }
                        .size(thumbSize)
                        .shadow(2.dp, CircleShape)
                        .clip(CircleShape)
                        .background(glassTintColor(hue))
                        .border(3.dp, Color.White, CircleShape)
                )
            }
        }
    }
}
