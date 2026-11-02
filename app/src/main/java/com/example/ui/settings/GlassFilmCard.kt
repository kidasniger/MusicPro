package com.example.ui.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.preferences.GlassFilmLowContrastPercent
import com.example.data.preferences.MaxGlassOpacity
import com.example.data.preferences.MinGlassOpacity
import com.example.ui.components.glass.GlassBackdropHost
import com.example.ui.components.glass.GlassSurface
import com.example.ui.components.glass.glassActiveCapsuleColor
import com.example.ui.components.glass.glassActiveTabContentColor
import kotlin.math.roundToInt

/**
 * Réglages « Film » (opacité du voile de couleur du verre) et « Onglet actif » (capsule de l'onglet
 * sélectionné). Chaque curseur va de 0 à 100 %, affiche sa valeur en direct et met l'aperçu à jour
 * immédiatement : l'aperçu relit les mêmes réglages que la barre de navigation (LocalGlassStyle).
 */
@Composable
fun GlassFilmCard(
    filmOpacity: Int,
    onFilmOpacityChange: (Int) -> Unit,
    activeTabOpacity: Int,
    onActiveTabOpacityChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    // Valeurs locales pendant le glissement ; resynchronisées quand la préférence change.
    var filmValue by remember(filmOpacity) { mutableFloatStateOf(filmOpacity.toFloat()) }
    var tabValue by remember(activeTabOpacity) { mutableFloatStateOf(activeTabOpacity.toFloat()) }
    val shownFilm = filmValue.roundToInt()
    val shownTab = tabValue.roundToInt()

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("glass_film_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.glass_film_card_title),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = stringResource(R.string.glass_film_card_subtitle),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))

            GlassStylePreview()

            Spacer(modifier = Modifier.height(12.dp))

            PercentSliderSetting(
                title = stringResource(R.string.glass_film_title),
                description = stringResource(R.string.glass_film_desc),
                valueText = stringResource(R.string.glass_film_value, shownFilm),
                value = filmValue,
                note = if (shownFilm < GlassFilmLowContrastPercent) {
                    stringResource(R.string.glass_film_low_contrast)
                } else {
                    null
                },
                onValueChange = {
                    filmValue = it
                    val rounded = it.roundToInt()
                    if (rounded != filmOpacity) onFilmOpacityChange(rounded)
                },
                sliderTag = "glass_film_slider",
                valueTag = "glass_film_value"
            )

            Spacer(modifier = Modifier.height(4.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(12.dp))

            PercentSliderSetting(
                title = stringResource(R.string.glass_active_tab_title),
                description = stringResource(R.string.glass_active_tab_desc),
                valueText = stringResource(R.string.glass_active_tab_value, shownTab),
                value = tabValue,
                note = null,
                onValueChange = {
                    tabValue = it
                    val rounded = it.roundToInt()
                    if (rounded != activeTabOpacity) onActiveTabOpacityChange(rounded)
                },
                sliderTag = "glass_active_tab_slider",
                valueTag = "glass_active_tab_value"
            )
        }
    }
}

/**
 * Aperçu en direct : une mini barre de navigation en verre avec un onglet actif, rendue avec les
 * réglages courants (film, teinte, givre, couleur, onglet actif).
 */
@Composable
private fun GlassStylePreview(modifier: Modifier = Modifier) {
    val tabShape = RoundedCornerShape(24.dp)
    GlassBackdropHost(
        modifier = modifier
            .fillMaxWidth()
            .height(88.dp)
            .clip(RoundedCornerShape(12.dp))
            .testTag("glass_film_preview"),
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
                            .size(26.dp)
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
                    .fillMaxWidth(0.9f)
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp)
            ) {
                val capsule = glassActiveCapsuleColor()
                val baseContent = LocalContentColor.current
                val selectedContent = glassActiveTabContentColor(capsule, baseContent)
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    listOf(
                        R.string.nav_home,
                        R.string.nav_search,
                        R.string.nav_library
                    ).forEachIndexed { index, labelRes ->
                        val selected = index == 0
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(tabShape)
                                .then(
                                    if (selected) {
                                        Modifier
                                            .background(capsule)
                                            .border(1.dp, Color.White.copy(alpha = 0.25f), tabShape)
                                    } else {
                                        Modifier
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = stringResource(labelRes),
                                color = if (selected) selectedContent else baseContent.copy(alpha = 0.70f),
                                fontSize = 11.sp,
                                maxLines = 1,
                                softWrap = false,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    )
}

/** Un réglage à curseur en pourcentage (0 à 100 %) : titre, valeur en direct, description, curseur et note. */
@Composable
private fun PercentSliderSetting(
    title: String,
    description: String,
    valueText: String,
    value: Float,
    note: String?,
    onValueChange: (Float) -> Unit,
    sliderTag: String,
    valueTag: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = valueText,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.testTag(valueTag)
        )
    }
    Text(
        text = description,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Slider(
        value = value,
        onValueChange = onValueChange,
        valueRange = MinGlassOpacity.toFloat()..MaxGlassOpacity.toFloat(),
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
            color = MaterialTheme.colorScheme.error
        )
    }
}
