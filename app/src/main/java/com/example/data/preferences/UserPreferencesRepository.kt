package com.example.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

data class SavedPlaybackState(
    val trackId: Long,
    val positionMs: Long,
    val wasPlaying: Boolean,
    val shuffleEnabled: Boolean
)

val Context.musicProDataStore: DataStore<Preferences> by preferencesDataStore(name = "musicpro_preferences")

enum class AppThemeMode(val displayName: String) {
    DARK("Sombre Néon"),
    LIGHT("Clair"),
    SYSTEM("Système")
}

class UserPreferencesRepository(private val context: Context) {

    companion object {
        val KEY_ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val KEY_THEME_MODE = stringPreferencesKey("app_theme_mode")
        val KEY_DYNAMIC_COLOR = booleanPreferencesKey("dynamic_color")
        val KEY_GLASS_MATERIAL = stringPreferencesKey("glass_material")
        /** Force de couleur du verre, en % (0-100). Absente : 70 %. */
        val KEY_GLASS_COLOR_STRENGTH = intPreferencesKey("glass_color_strength")
        /** Givre du verre (rayon du flou), en dp (0-24). Absente : 6 dp. */
        val KEY_GLASS_FROST_DP = intPreferencesKey("glass_frost_dp")
        /** Teinte du verre, en degrés (0-360 exclu). Absente : aucune teinte. */
        val KEY_GLASS_TINT_HUE = floatPreferencesKey("glass_tint_hue")
        /** Film du verre (opacité du voile de couleur), en % (0-100). Absente : 50 %. */
        val KEY_GLASS_FILM_OPACITY = intPreferencesKey("glass_film_opacity")
        /** Opacité de la capsule de l'onglet actif, en % (0-100). Absente : 26 %. */
        val KEY_GLASS_ACTIVE_TAB_OPACITY = intPreferencesKey("glass_active_tab_opacity")
        /** Ancienne clé (mise à jour 004, valeurs BEAUTIFUL / FAST) : migrée à la lecture. */
        val KEY_GLASS_QUALITY_LEGACY = stringPreferencesKey("glass_quality")
        val KEY_FAVORITE_TRACK_IDS = stringSetPreferencesKey("favorite_track_ids")
        val KEY_SEARCH_HISTORY = stringPreferencesKey("search_history")
        val KEY_AUTO_PLAY = booleanPreferencesKey("auto_play")
        val KEY_GAPLESS = booleanPreferencesKey("gapless_playback")
        val KEY_RESUME_PLAYBACK = booleanPreferencesKey("resume_playback")
        val KEY_KARAOKE_FONT_SIZE = stringPreferencesKey("karaoke_font_size")
        val KEY_KARAOKE_ACTIVE_COLOR = stringPreferencesKey("karaoke_active_color")
        val KEY_KARAOKE_OFFSET = stringPreferencesKey("karaoke_offset")
        val KEY_LAST_PLAYED_TRACK_ID = longPreferencesKey("last_played_track_id")
        val KEY_LAST_PLAYED_POSITION_MS = longPreferencesKey("last_played_position_ms")
        val KEY_LAST_PLAYED_WAS_PLAYING = booleanPreferencesKey("last_played_was_playing")
        val KEY_SHUFFLE_ENABLED = booleanPreferencesKey("shuffle_enabled")
    }

    val isOnboardingCompleted: Flow<Boolean> = context.musicProDataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[KEY_ONBOARDING_COMPLETED] ?: false
        }

    val themeMode: Flow<AppThemeMode> = context.musicProDataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val modeString = preferences[KEY_THEME_MODE] ?: AppThemeMode.DARK.name
            try {
                AppThemeMode.valueOf(modeString)
            } catch (_: Exception) {
                AppThemeMode.DARK
            }
        }

    val isDynamicColorEnabled: Flow<Boolean> = context.musicProDataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[KEY_DYNAMIC_COLOR] ?: false
        }

    /** Niveau de l'effet verre ; BALANCED par défaut, ancienne clé migrée (voir [resolveGlassMaterial]). */
    val glassMaterial: Flow<GlassMaterial> = context.musicProDataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { preferences ->
            resolveGlassMaterial(
                stored = preferences[KEY_GLASS_MATERIAL],
                legacy = preferences[KEY_GLASS_QUALITY_LEGACY]
            )
        }

    /** Force de couleur du verre en % (0-100) ; 70 % par défaut (voir [resolveGlassColorStrength]). */
    val glassColorStrength: Flow<Int> = context.musicProDataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { preferences -> resolveGlassColorStrength(preferences[KEY_GLASS_COLOR_STRENGTH]) }

    /** Givre du verre en dp (0-24) ; 6 dp par défaut (voir [resolveGlassFrostDp]). */
    val glassFrostDp: Flow<Int> = context.musicProDataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { preferences -> resolveGlassFrostDp(preferences[KEY_GLASS_FROST_DP]) }

    /** Teinte du verre en degrés ; `null` = Aucune, par défaut (voir [resolveGlassTintHue]). */
    val glassTintHue: Flow<Float?> = context.musicProDataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { preferences -> resolveGlassTintHue(preferences[KEY_GLASS_TINT_HUE]) }

    /** Film du verre en % (0-100) ; 50 % par défaut (voir [resolveGlassFilmOpacity]). */
    val glassFilmOpacity: Flow<Int> = context.musicProDataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { preferences -> resolveGlassFilmOpacity(preferences[KEY_GLASS_FILM_OPACITY]) }

    /** Opacité de la capsule de l'onglet actif en % (0-100) ; 26 % par défaut. */
    val glassActiveTabOpacity: Flow<Int> = context.musicProDataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { preferences -> resolveGlassActiveTabOpacity(preferences[KEY_GLASS_ACTIVE_TAB_OPACITY]) }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.musicProDataStore.edit { preferences ->
            preferences[KEY_ONBOARDING_COMPLETED] = completed
        }
    }

    suspend fun setThemeMode(mode: AppThemeMode) {
        context.musicProDataStore.edit { preferences ->
            preferences[KEY_THEME_MODE] = mode.name
        }
    }

    suspend fun setGlassMaterial(material: GlassMaterial) {
        context.musicProDataStore.edit { preferences ->
            preferences[KEY_GLASS_MATERIAL] = material.name
            preferences.remove(KEY_GLASS_QUALITY_LEGACY)
        }
    }

    suspend fun setGlassColorStrength(percent: Int) {
        context.musicProDataStore.edit { preferences ->
            preferences[KEY_GLASS_COLOR_STRENGTH] = clampGlassColorStrength(percent)
        }
    }

    suspend fun setGlassFrostDp(dp: Int) {
        context.musicProDataStore.edit { preferences ->
            preferences[KEY_GLASS_FROST_DP] = clampGlassFrostDp(dp)
        }
    }

    /** Enregistre la teinte du verre ; `null` (Aucune) supprime la clé. */
    suspend fun setGlassTintHue(hue: Float?) {
        context.musicProDataStore.edit { preferences ->
            val resolved = resolveGlassTintHue(hue)
            if (resolved == null) {
                preferences.remove(KEY_GLASS_TINT_HUE)
            } else {
                preferences[KEY_GLASS_TINT_HUE] = resolved
            }
        }
    }

    suspend fun setGlassFilmOpacity(percent: Int) {
        context.musicProDataStore.edit { preferences ->
            preferences[KEY_GLASS_FILM_OPACITY] = clampGlassOpacity(percent)
        }
    }

    suspend fun setGlassActiveTabOpacity(percent: Int) {
        context.musicProDataStore.edit { preferences ->
            preferences[KEY_GLASS_ACTIVE_TAB_OPACITY] = clampGlassOpacity(percent)
        }
    }

    val favoriteTrackIds: Flow<Set<Long>> = context.musicProDataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { preferences ->
            preferences[KEY_FAVORITE_TRACK_IDS].orEmpty()
                .mapNotNull { it.toLongOrNull() }
                .toSet()
        }

    val searchHistory: Flow<List<String>> = context.musicProDataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { preferences ->
            preferences[KEY_SEARCH_HISTORY]
                .orEmpty()
                .split("\n")
                .map(String::trim)
                .filter(String::isNotBlank)
                .take(12)
        }

    val autoPlay: Flow<Boolean> = context.musicProDataStore.data
        .catch { exception -> if (exception is IOException) emit(emptyPreferences()) else throw exception }
        .map { it[KEY_AUTO_PLAY] ?: true }

    val gaplessPlayback: Flow<Boolean> = context.musicProDataStore.data
        .catch { exception -> if (exception is IOException) emit(emptyPreferences()) else throw exception }
        .map { it[KEY_GAPLESS] ?: true }

    val resumePlayback: Flow<Boolean> = context.musicProDataStore.data
        .catch { exception -> if (exception is IOException) emit(emptyPreferences()) else throw exception }
        .map { it[KEY_RESUME_PLAYBACK] ?: true }

    val karaokeFontSize: Flow<Float> = context.musicProDataStore.data
        .catch { exception -> if (exception is IOException) emit(emptyPreferences()) else throw exception }
        .map { it[KEY_KARAOKE_FONT_SIZE]?.toFloatOrNull() ?: 18f }

    val karaokeActiveColor: Flow<String> = context.musicProDataStore.data
        .catch { exception -> if (exception is IOException) emit(emptyPreferences()) else throw exception }
        .map { it[KEY_KARAOKE_ACTIVE_COLOR] ?: "cyan" }

    val karaokeOffsetMs: Flow<Long> = context.musicProDataStore.data
        .catch { exception -> if (exception is IOException) emit(emptyPreferences()) else throw exception }
        .map { it[KEY_KARAOKE_OFFSET]?.toLongOrNull() ?: 0L }

    val savedPlaybackState: Flow<SavedPlaybackState?> = context.musicProDataStore.data
        .catch { exception -> if (exception is IOException) emit(emptyPreferences()) else throw exception }
        .map { preferences ->
            val trackId = preferences[KEY_LAST_PLAYED_TRACK_ID] ?: return@map null
            SavedPlaybackState(
                trackId = trackId,
                positionMs = (preferences[KEY_LAST_PLAYED_POSITION_MS] ?: 0L).coerceAtLeast(0L),
                wasPlaying = preferences[KEY_LAST_PLAYED_WAS_PLAYING] ?: false,
                shuffleEnabled = preferences[KEY_SHUFFLE_ENABLED] ?: false
            )
        }

    suspend fun setDynamicColor(enabled: Boolean) {
        context.musicProDataStore.edit { preferences ->
            preferences[KEY_DYNAMIC_COLOR] = enabled
        }
    }

    suspend fun setFavoriteTrackIds(ids: Set<Long>) {
        context.musicProDataStore.edit { preferences ->
            preferences[KEY_FAVORITE_TRACK_IDS] = ids.map(Long::toString).toSet()
        }
    }

    suspend fun setSearchHistory(values: List<String>) {
        context.musicProDataStore.edit { preferences ->
            preferences[KEY_SEARCH_HISTORY] = values
                .map(String::trim)
                .filter(String::isNotBlank)
                .distinct()
                .take(12)
                .joinToString("\n")
        }
    }

    suspend fun setAutoPlay(enabled: Boolean) {
        context.musicProDataStore.edit { preferences -> preferences[KEY_AUTO_PLAY] = enabled }
    }

    suspend fun setGaplessPlayback(enabled: Boolean) {
        context.musicProDataStore.edit { preferences -> preferences[KEY_GAPLESS] = enabled }
    }

    suspend fun setResumePlayback(enabled: Boolean) {
        context.musicProDataStore.edit { preferences -> preferences[KEY_RESUME_PLAYBACK] = enabled }
    }

    suspend fun setKaraokeFontSize(size: Float) {
        context.musicProDataStore.edit { preferences -> preferences[KEY_KARAOKE_FONT_SIZE] = size.toString() }
    }

    suspend fun setKaraokeActiveColor(value: String) {
        context.musicProDataStore.edit { preferences -> preferences[KEY_KARAOKE_ACTIVE_COLOR] = value }
    }

    suspend fun setKaraokeOffsetMs(value: Long) {
        context.musicProDataStore.edit { preferences -> preferences[KEY_KARAOKE_OFFSET] = value.toString() }
    }

    suspend fun setSavedPlaybackState(
        trackId: Long,
        positionMs: Long,
        wasPlaying: Boolean,
        shuffleEnabled: Boolean
    ) {
        context.musicProDataStore.edit { preferences ->
            preferences[KEY_LAST_PLAYED_TRACK_ID] = trackId
            preferences[KEY_LAST_PLAYED_POSITION_MS] = positionMs.coerceAtLeast(0L)
            preferences[KEY_LAST_PLAYED_WAS_PLAYING] = wasPlaying
            preferences[KEY_SHUFFLE_ENABLED] = shuffleEnabled
        }
    }

    suspend fun setShuffleEnabled(enabled: Boolean) {
        context.musicProDataStore.edit { preferences ->
            preferences[KEY_SHUFFLE_ENABLED] = enabled
        }
    }
}
