package com.example.ui.settings

import androidx.compose.runtime.Immutable
import com.example.data.preferences.DefaultGlassActiveTabOpacity
import com.example.data.preferences.DefaultGlassColorStrength
import com.example.data.preferences.DefaultGlassFilmOpacity
import com.example.data.preferences.DefaultGlassFrostDp
import com.example.data.preferences.GlassMaterial

/**
 * Réglages du verre affichés par l'aperçu en direct (mise à jour 010).
 *
 * Cet état est hissé dans [SettingsViewModel] ([SettingsViewModel.glassPreview]) : chaque curseur le
 * modifie immédiatement, avant même l'écriture dans le DataStore, pour que l'aperçu bouge sans délai.
 */
@Immutable
data class GlassPreviewState(
    val material: GlassMaterial = GlassMaterial.BALANCED,
    val colorStrength: Int = DefaultGlassColorStrength,
    val frostDp: Int = DefaultGlassFrostDp,
    val tintHue: Float? = null,
    val filmOpacity: Int = DefaultGlassFilmOpacity,
    val activeTabOpacity: Int = DefaultGlassActiveTabOpacity
)
