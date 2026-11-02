package com.example.ui.settings

import android.os.Build
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.R
import com.example.data.preferences.DefaultGlassActiveTabOpacity
import com.example.data.preferences.DefaultGlassColorStrength
import com.example.data.preferences.DefaultGlassFilmOpacity
import com.example.data.preferences.DefaultGlassFrostDp
import com.example.data.preferences.GlassMaterial
import com.example.ui.theme.MusicProTokens

/**
 * Écran « Réglages › Glass » (mise à jour 025), route `settings/glass`.
 *
 * De haut en bas : titre « Glass » avec bouton retour rond, aperçu en direct, section MATÉRIAU
 * (trois niveaux avec coche, curseurs Couleur et Givre), section COULEUR (Teinte, Film, Onglet actif)
 * et un bouton « Réinitialiser » discret en bas de page.
 *
 * Les cartes réutilisées gèrent déjà les replis : avant Android 12 le niveau Qualité est grisé et le
 * flou est remplacé par un fond translucide ; aucune animation n'est ajoutée ici, donc « Réduire les
 * animations » n'a rien à désactiver.
 */
@Composable
fun GlassSettingsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    settingsViewModel: SettingsViewModel = viewModel()
) {
    val material by settingsViewModel.glassMaterial.collectAsStateWithLifecycle()
    val colorStrength by settingsViewModel.glassColorStrength.collectAsStateWithLifecycle()
    val frostDp by settingsViewModel.glassFrostDp.collectAsStateWithLifecycle()
    val tintHue by settingsViewModel.glassTintHue.collectAsStateWithLifecycle()
    val filmOpacity by settingsViewModel.glassFilmOpacity.collectAsStateWithLifecycle()
    val activeTabOpacity by settingsViewModel.glassActiveTabOpacity.collectAsStateWithLifecycle()
    val preview by settingsViewModel.glassPreview.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("glass_settings_screen")
    ) {
        GlassSettingsTopBar(onBack = onBack)

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = MusicProTokens.Spacing.Lg),
            verticalArrangement = Arrangement.spacedBy(MusicProTokens.Spacing.Lg)
        ) {
            GlassPreviewCard(state = preview)

            GlassSectionLabel(text = stringResource(R.string.glass_section_material))
            GlassQualityCard(
                material = material,
                blurSupported = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S,
                onMaterialSelected = { settingsViewModel.setGlassMaterial(it) },
                colorStrength = colorStrength,
                onColorStrengthChange = { settingsViewModel.setGlassColorStrength(it) },
                frostDp = frostDp,
                onFrostDpChange = { settingsViewModel.setGlassFrostDp(it) }
            )

            GlassSectionLabel(text = stringResource(R.string.glass_section_color))
            GlassTintCard(
                hue = tintHue,
                onHueChange = { settingsViewModel.setGlassTintHue(it) }
            )
            GlassFilmCard(
                filmOpacity = filmOpacity,
                onFilmOpacityChange = { settingsViewModel.setGlassFilmOpacity(it) },
                activeTabOpacity = activeTabOpacity,
                onActiveTabOpacityChange = { settingsViewModel.setGlassActiveTabOpacity(it) }
            )

            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                TextButton(
                    onClick = { resetGlassSettings(settingsViewModel) },
                    modifier = Modifier
                        .heightIn(min = MusicProTokens.Size.Touch)
                        .testTag("glass_reset_button")
                ) {
                    Text(
                        text = stringResource(R.string.glass_reset),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.size(MusicProTokens.Spacing.Sm))
        }
    }
}

/**
 * Remet les six réglages du verre à leurs valeurs par défaut (Équilibré, couleur 70 %, givre 6 dp,
 * aucune teinte, film 50 %, onglet actif 26 %). Chaque setter met l'aperçu à jour tout de suite
 * puis écrit dans le DataStore.
 */
private fun resetGlassSettings(viewModel: SettingsViewModel) {
    viewModel.setGlassMaterial(GlassMaterial.BALANCED)
    viewModel.setGlassColorStrength(DefaultGlassColorStrength)
    viewModel.setGlassFrostDp(DefaultGlassFrostDp)
    viewModel.setGlassTintHue(null)
    viewModel.setGlassFilmOpacity(DefaultGlassFilmOpacity)
    viewModel.setGlassActiveTabOpacity(DefaultGlassActiveTabOpacity)
}

/** Titre « Glass » avec bouton retour rond (zone tactile 48 dp). */
@Composable
private fun GlassSettingsTopBar(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = MusicProTokens.Spacing.Lg,
                vertical = MusicProTokens.Spacing.Md
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .size(MusicProTokens.Size.Touch)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .testTag("glass_back_button")
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.glass_screen_back),
                tint = MaterialTheme.colorScheme.onBackground
            )
        }

        Spacer(modifier = Modifier.width(MusicProTokens.Spacing.Md))

        Text(
            text = stringResource(R.string.glass_screen_title),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .weight(1f)
                .semantics { heading() }
        )
    }
}

/** Étiquette de section en capitales (MATÉRIAU, COULEUR). */
@Composable
private fun GlassSectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 1.sp),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier
            .padding(start = MusicProTokens.Spacing.Xs, top = MusicProTokens.Spacing.Sm)
            .semantics { heading() }
    )
}

/**
 * Ligne d'entrée de la page Paramètres : remplace les quatre cartes du verre et ouvre
 * `settings/glass`.
 */
@Composable
fun GlassSettingsEntry(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(MusicProTokens.Radius.Lg)
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .clickable(role = Role.Button, onClick = onClick)
            .testTag("glass_settings_entry"),
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(
            MusicProTokens.Size.Stroke,
            MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = MusicProTokens.Size.Touch)
                .padding(MusicProTokens.Spacing.Lg),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(MusicProTokens.Size.Touch)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .border(MusicProTokens.Size.Stroke, MaterialTheme.colorScheme.outlineVariant, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.AutoAwesome,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(MusicProTokens.Size.Icon)
                )
            }

            Spacer(modifier = Modifier.width(MusicProTokens.Spacing.Md))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.glass_entry_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = stringResource(R.string.glass_entry_subtitle),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
