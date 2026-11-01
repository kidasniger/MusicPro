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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.preferences.GlassQuality

/**
 * Réglage « Effet verre » : Plus beau (flou en temps réel) ou Plus rapide (aplat translucide).
 * Avant Android 12, seul « Plus rapide » est possible et l'autre choix est désactivé.
 */
@Composable
fun GlassQualityCard(
    quality: GlassQuality,
    blurSupported: Boolean,
    onQualitySelected: (GlassQuality) -> Unit,
    modifier: Modifier = Modifier
) {
    val effective = if (blurSupported) quality else GlassQuality.FAST

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
                GlassQualityOption(
                    title = stringResource(R.string.glass_quality_beautiful),
                    description = stringResource(R.string.glass_quality_beautiful_desc),
                    selected = effective == GlassQuality.BEAUTIFUL,
                    enabled = blurSupported,
                    onClick = { onQualitySelected(GlassQuality.BEAUTIFUL) },
                    tag = "glass_quality_beautiful"
                )
                GlassQualityOption(
                    title = stringResource(R.string.glass_quality_fast),
                    description = stringResource(R.string.glass_quality_fast_desc),
                    selected = effective == GlassQuality.FAST,
                    enabled = true,
                    onClick = { onQualitySelected(GlassQuality.FAST) },
                    tag = "glass_quality_fast"
                )
            }

            if (!blurSupported) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.glass_quality_unsupported),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun GlassQualityOption(
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
