package com.example.ui.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.preferences.GlassFrostBlurThresholdDp
import com.example.data.preferences.GlassMaterial
import com.example.data.preferences.MaxGlassFrostDp
import com.example.data.preferences.MinGlassFrostDp
import com.example.data.preferences.effective
import com.example.data.preferences.glassBlurActive
import com.example.data.preferences.rendersBlur
import com.example.ui.components.glass.GlassBackdropHost
import com.example.ui.components.glass.GlassSurface
import kotlin.math.roundToInt

/**
 * Réglage « Effet verre » : trois niveaux (Le plus rapide, Équilibré, Qualité), un aperçu en direct,
 * le curseur « Givre » (rayon du flou, 0 à 24 dp) et le curseur « Couleur du verre » (0 à 100 %).
 *
 * Avant Android 12, le niveau Qualité n'est pas proposé (option désactivée) et le flou est
 * remplacé par un fond translucide quel que soit le niveau.
 * Le givre n'a d'effet qu'avec un niveau qui flou (pas « Le plus rapide »), sur Android 12 ou plus ;
 * sous 2 dp le flou est coupé, et le curseur « Couleur » (qui agit sur le fond flouté) est grisé.
 */
@Composable
fun GlassQualityCard(
    material: GlassMaterial,
    blurSupported: Boolean,
    onMaterialSelected: (GlassMaterial) -> Unit,
    colorStrength: Int,
    onColorStrengthChange: (Int) -> Unit,
    frostDp: Int,
    onFrostDpChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val effective = material.effective(blurSupported)
    val levelBlurs = effective.rendersBlur(blurSupported)

    // Valeurs locales pendant le glissement ; resynchronisées quand la préférence change.
    var frostValue by remember(frostDp) { mutableFloatStateOf(frostDp.toFloat()) }
    val shownFrost = frostValue.roundToInt()
    val blurActive = glassBlurActive(effective, blurSupported, shownFrost)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("glass_quality_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.glass_quality_title),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = stringResource(R.string.glass_quality_subtitle),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))

            Column(modifier = Modifier.selectableGroup()) {
                GlassMaterialOption(
                    title = stringResource(R.string.glass_material_fastest),
                    description = stringResource(R.string.glass_material_fastest_desc),
                    selected = effective == GlassMaterial.FASTEST,
                    enabled = true,
                    onClick = { onMaterialSelected(GlassMaterial.FASTEST) },
                    tag = "glass_material_fastest"
                )
                GlassMaterialOption(
                    title = stringResource(R.string.glass_material_balanced),
                    description = stringResource(R.string.glass_material_balanced_desc),
                    selected = effective == GlassMaterial.BALANCED,
                    enabled = true,
                    onClick = { onMaterialSelected(GlassMaterial.BALANCED) },
                    tag = "glass_material_balanced"
                )
                GlassMaterialOption(
                    title = stringResource(R.string.glass_material_quality),
                    description = stringResource(R.string.glass_material_quality_desc),
                    selected = effective == GlassMaterial.QUALITY,
                    enabled = blurSupported,
                    onClick = { onMaterialSelected(GlassMaterial.QUALITY) },
                    tag = "glass_material_quality"
                )
            }

            if (!blurSupported) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.glass_material_unsupported),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(12.dp))

            GlassPreview()

            Spacer(modifier = Modifier.height(12.dp))

            GlassSliderSetting(
                title = stringResource(R.string.glass_frost_title),
                description = stringResource(R.string.glass_frost_desc),
                valueText = stringResource(R.string.glass_frost_value, shownFrost),
                value = frostValue,
                valueRange = MinGlassFrostDp.toFloat()..MaxGlassFrostDp.toFloat(),
                // Un cran par dp : 24 intervalles, donc 23 crans intermédiaires.
                steps = MaxGlassFrostDp - MinGlassFrostDp - 1,
                enabled = levelBlurs,
                note = when {
                    !levelBlurs -> stringResource(R.string.glass_frost_unavailable)
                    shownFrost < GlassFrostBlurThresholdDp -> stringResource(R.string.glass_frost_off)
                    else -> null
                },
                onValueChange = {
                    frostValue = it
                    onFrostDpChange(it.roundToInt())
                },
                sliderTag = "glass_frost_slider",
                valueTag = "glass_frost_value"
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Valeur locale du curseur « Couleur » ; resynchronisée quand la préférence change.
            var colorValue by remember(colorStrength) { mutableFloatStateOf(colorStrength / 100f) }
            GlassSliderSetting(
                title = stringResource(R.string.glass_color_strength_title),
                description = stringResource(R.string.glass_color_strength_desc),
                valueText = stringResource(R.string.glass_color_strength_value, (colorValue * 100f).roundToInt()),
                value = colorValue,
                valueRange = 0f..1f,
                // Pas de 5 % : 20 intervalles, donc 19 crans intermédiaires.
                steps = 19,
                enabled = blurActive,
                note = when {
                    !levelBlurs -> stringResource(R.string.glass_color_strength_unavailable)
                    !blurActive -> stringResource(R.string.glass_color_strength_blur_off)
                    else -> null
                },
                onValueChange = {
                    colorValue = it
                    onColorStrengthChange((it * 100f).roundToInt())
                },
                sliderTag = "glass_color_strength_slider",
                valueTag = "glass_color_strength_value"
            )
        }
    }
}

/**
 * Aperçu en direct : un fond coloré et une pilule de verre, rendus avec les réglages courants
 * (niveau, givre, couleur). Il se met à jour dès qu'un curseur bouge.
 */
@Composable
private fun GlassPreview(modifier: Modifier = Modifier) {
    GlassBackdropHost(
        modifier = modifier
            .fillMaxWidth()
            .height(96.dp)
            .clip(RoundedCornerShape(12.dp))
            .testTag("glass_preview"),
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
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(5) { index ->
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(if (index % 2 == 0) Color.White else Color.Black)
                    )
                }
            }
        },
        foreground = {
            GlassSurface(
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth(0.7f)
                    .height(44.dp)
            ) {
                Text(
                    text = stringResource(R.string.glass_preview_label),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    )
}

/** Un réglage à curseur : titre, valeur affichée en direct, description, curseur et note facultative. */
@Composable
private fun GlassSliderSetting(
    title: String,
    description: String,
    valueText: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int,
    enabled: Boolean,
    note: String?,
    onValueChange: (Float) -> Unit,
    sliderTag: String,
    valueTag: String
) {
    val titleColor = if (enabled) {
        MaterialTheme.colorScheme.onSurface
    } else {
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.60f)
    }
    val secondaryColor = if (enabled) {
        MaterialTheme.colorScheme.onSurfaceVariant
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.60f)
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            color = titleColor,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = valueText,
            style = MaterialTheme.typography.bodyMedium,
            color = secondaryColor,
            modifier = Modifier.testTag(valueTag)
        )
    }
    Text(
        text = description,
        style = MaterialTheme.typography.bodySmall,
        color = secondaryColor
    )
    Slider(
        value = value,
        onValueChange = onValueChange,
        enabled = enabled,
        valueRange = valueRange,
        steps = steps,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .semantics {
                contentDescription = title
                stateDescription = valueText
            }
            .testTag(sliderTag)
    )
    if (note != null) {
        Text(
            text = note,
            style = MaterialTheme.typography.bodySmall,
            color = secondaryColor
        )
    }
}

@Composable
private fun GlassMaterialOption(
    title: String,
    description: String,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    tag: String
) {
    val titleColor = if (enabled) {
        MaterialTheme.colorScheme.onSurface
    } else {
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.60f)
    }
    val descriptionColor = if (enabled) {
        MaterialTheme.colorScheme.onSurfaceVariant
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.60f)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .clip(RoundedCornerShape(12.dp))
            .selectable(
                selected = selected,
                enabled = enabled,
                role = Role.RadioButton,
                onClick = onClick
            )
            .padding(horizontal = 4.dp, vertical = 8.dp)
            .testTag(tag),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = null, enabled = enabled)
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = titleColor
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = descriptionColor
            )
        }
    }
}
