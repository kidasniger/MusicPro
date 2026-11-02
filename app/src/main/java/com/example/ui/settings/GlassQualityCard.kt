package com.example.ui.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.preferences.GlassMaterial
import com.example.data.preferences.effective
import kotlin.math.roundToInt

/**
 * Réglage « Effet verre » : trois niveaux (Le plus rapide, Équilibré, Qualité) et le curseur
 * « Couleur du verre » (0 à 100 %).
 * Avant Android 12, le niveau Qualité n'est pas proposé (option désactivée) et le flou est
 * remplacé par un fond translucide quel que soit le niveau.
 * Le curseur n'a d'effet que si un flou est dessiné : il est désactivé avec « Le plus rapide »
 * et avant Android 12.
 */
@Composable
fun GlassQualityCard(
    material: GlassMaterial,
    blurSupported: Boolean,
    onMaterialSelected: (GlassMaterial) -> Unit,
    colorStrength: Int,
    onColorStrengthChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val effective = material.effective(blurSupported)
    val colorEnabled = blurSupported && effective != GlassMaterial.FASTEST

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

            GlassColorStrengthSlider(
                colorStrength = colorStrength,
                enabled = colorEnabled,
                onColorStrengthChange = onColorStrengthChange
            )
        }
    }
}

/** Curseur « Couleur du verre » : de 0 à 100 % par pas de 5 %. */
@Composable
private fun GlassColorStrengthSlider(
    colorStrength: Int,
    enabled: Boolean,
    onColorStrengthChange: (Int) -> Unit
) {
    // Valeur locale pendant le glissement ; resynchronisée quand la préférence change.
    var sliderValue by remember(colorStrength) { mutableFloatStateOf(colorStrength / 100f) }
    val percent = (sliderValue * 100f).roundToInt()
    val valueText = stringResource(R.string.glass_color_strength_value, percent)
    val title = stringResource(R.string.glass_color_strength_title)

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
            modifier = Modifier.testTag("glass_color_strength_value")
        )
    }
    Text(
        text = stringResource(R.string.glass_color_strength_desc),
        style = MaterialTheme.typography.bodySmall,
        color = secondaryColor
    )
    Slider(
        value = sliderValue,
        onValueChange = {
            sliderValue = it
            onColorStrengthChange((it * 100f).roundToInt())
        },
        enabled = enabled,
        valueRange = 0f..1f,
        steps = 19,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .semantics {
                contentDescription = title
                stateDescription = valueText
            }
            .testTag("glass_color_strength_slider")
    )
    if (!enabled) {
        Text(
            text = stringResource(R.string.glass_color_strength_unavailable),
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
