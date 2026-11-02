package com.example.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
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

/**
 * Ancien réglage de thème unique. Conservé uniquement pour migrer la clé `app_theme_mode`
 * vers [AppearanceMode] et [AppStyle] (mise à jour 013) ; l'interface n'utilise plus que ces deux-là.
 */
enum class AppThemeMode(val displayName: String) {
    DARK("Sombre Néon"),
    LIGHT("Clair"),
    SYSTEM("Système")
}

class UserPreferencesRepository(private val context: Context) {

    companion object {
        val KEY_ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        /** Ancienne clé (Sombre Néon / Clair / Système) : lue pour la migration, plus écrite par l'interface. */
        val KEY_THEME_MODE = stringPreferencesKey("app_theme_mode")
        /** Apparence (Clair, Sombre, Auto, Sombre le soir). Absente : déduite de [KEY_THEME_MODE]. */
        val KEY_APPEARANCE_MODE = stringPreferencesKey("appearance_mode")
        /** Style (Minimal, Néon, Verre). Absente : déduite de [KEY_THEME_MODE]. */
        val KEY_APP_STYLE = stringPreferencesKey("app_style")
        /** Accent (Terracotta, Bleu, Vert, Violet, Selon la pochette). Absente : Terracotta. */
        val KEY_ACCENT_CHOICE = stringPreferencesKey("accent_choice")
        /** Début de « Sombre le soir », en minutes depuis minuit. Absente : 20:00. */
        val KEY_EVENING_START = intPreferencesKey("dark_evening_start_minutes")
        /** Fin de « Sombre le soir », en minutes depuis minuit. Absente : 07:00. */
        val KEY_EVENING_END = intPreferencesKey("dark_evening_end_minutes")
        val KEY_DYNAMIC_COLOR = booleanPreferencesKey("dynamic_color")
        val KEY_TEXT_SIZE = stringPreferencesKey("text_size_preference")
        val KEY_LIST_DENSITY = stringPreferencesKey("list_density_preference")
        val KEY_REDUCE_ANIMATIONS = booleanPreferencesKey("reduce_animations")
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
        /** Artistes favoris (étoile de l'écran Artiste, mise à jour 024). Absente : aucun. */
        val KEY_FAVORITE_ARTISTS = stringSetPreferencesKey("favorite_artists")
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

    private val themeData: Flow<Preferences> = context.musicProDataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
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


    val textSizePreference: Flow<TextSizePreference> = themeData.map { resolveTextSizePreference(it[KEY_TEXT_SIZE]) }

    val listDensityPreference: Flow<ListDensityPreference> = themeData.map { resolveListDensityPreference(it[KEY_LIST_DENSITY]) }

    val reduceAnimations: Flow<Boolean> = themeData.map { it[KEY_REDUCE_ANIMATIONS] ?: false }

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

    // --- Apparence et style (mises à jour 013, 017, 018) ------------------------------------------

    /** Apparence ; déduite de l'ancienne clé `app_theme_mode` tant qu'elle n'est pas écrite. */
    val appearanceMode: Flow<AppearanceMode> = themeData.map { preferences ->
        resolveAppearanceMode(preferences[KEY_APPEARANCE_MODE], preferences[KEY_THEME_MODE])
    }

    /** Style ; déduit de l'ancienne clé `app_theme_mode` tant qu'il n'est pas écrit. */
    val appStyle: Flow<AppStyle> = themeData.map { preferences ->
        resolveAppStyle(preferences[KEY_APP_STYLE], preferences[KEY_THEME_MODE])
    }

    /** Accent ; Terracotta par défaut. */
    val accentChoice: Flow<AccentChoice> = themeData.map { preferences ->
        resolveAccentChoice(preferences[KEY_ACCENT_CHOICE])
    }

    /** Début de « Sombre le soir » en minutes depuis minuit ; 20:00 par défaut. */
    val eveningStartMinutes: Flow<Int> = themeData.map { preferences ->
        resolveEveningMinutes(preferences[KEY_EVENING_START], DefaultEveningStartMinutes)
    }

    /** Fin de « Sombre le soir » en minutes depuis minuit ; 07:00 par défaut. */
    val eveningEndMinutes: Flow<Int> = themeData.map { preferences ->
        resolveEveningMinutes(preferences[KEY_EVENING_END], DefaultEveningEndMinutes)
    }

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

    /**
     * Fige la migration de `app_theme_mode` : écrit l'apparence et le style déduits s'ils sont absents,
     * pour que les deux réglages deviennent indépendants dès la première modification de l'un d'eux.
     */
    private fun migrateThemeKeys(preferences: MutablePreferences) {
        val legacy = preferences[KEY_THEME_MODE]
        if (preferences[KEY_APPEARANCE_MODE] == null) {
            preferences[KEY_APPEARANCE_MODE] = legacyThemeToAppearance(legacy).name
        }
        if (preferences[KEY_APP_STYLE] == null) {
            preferences[KEY_APP_STYLE] = legacyThemeToStyle(legacy).name
        }
    }

    suspend fun setAppearanceMode(mode: AppearanceMode) {
        context.musicProDataStore.edit { preferences ->
            migrateThemeKeys(preferences)
            preferences[KEY_APPEARANCE_MODE] = mode.name
        }
    }

    suspend fun setAppStyle(style: AppStyle) {
        context.musicProDataStore.edit { preferences ->
            migrateThemeKeys(preferences)
            preferences[KEY_APP_STYLE] = style.name
        }
    }

    suspend fun setAccentChoice(choice: AccentChoice) {
        context.musicProDataStore.edit { preferences ->
            preferences[KEY_ACCENT_CHOICE] = choice.name
        }
    }

    suspend fun setEveningStartMinutes(minutes: Int) {
        context.musicProDataStore.edit { preferences ->
            preferences[KEY_EVENING_START] = clampDayMinutes(minutes)
        }
    }

    suspend fun setEveningEndMinutes(minutes: Int) {
        context.musicProDataStore.edit { preferences ->
            preferences[KEY_EVENING_END] = clampDayMinutes(minutes)
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

    val favoriteArtists: Flow<Set<String>> = context.musicProDataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { preferences -> preferences[KEY_FAVORITE_ARTISTS].orEmpty() }

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

    suspend fun setReduceAnimations(enabled: Boolean) {
        context.musicProDataStore.edit { it[KEY_REDUCE_ANIMATIONS] = enabled }
    }

    suspend fun setTextSizePreference(value: TextSizePreference) {
        context.musicProDataStore.edit { it[KEY_TEXT_SIZE] = value.name }
    }

    suspend fun setListDensityPreference(value: ListDensityPreference) {
        context.musicProDataStore.edit { it[KEY_LIST_DENSITY] = value.name }
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

    suspend fun setFavoriteArtists(names: Set<String>) {
        context.musicProDataStore.edit { preferences ->
            preferences[KEY_FAVORITE_ARTISTS] = names
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
