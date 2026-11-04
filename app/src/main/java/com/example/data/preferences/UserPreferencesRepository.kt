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
import com.example.data.backup.BACKUP_SCHEMA_VERSION
import com.example.data.backup.FavoritesBackup
import com.example.data.backup.GlassBackup
import com.example.data.backup.KaraokeBackup
import com.example.data.backup.SettingsBackup
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
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

class UserPreferencesRepository(
    private val context: Context,
    /** Injectable pour les tests (fichier et scope isolés) ; par défaut le DataStore unique de l'appli. */
    private val dataStore: DataStore<Preferences> = context.musicProDataStore
) {

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
        /** Mise à jour 100 — consentement au contexte musical transmis à l'IA du chat. */
        val KEY_AI_CONTEXT_CONSENT = booleanPreferencesKey("ai_context_consent")
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
        /** Mise à jour 084 — « Texte plus lisible » : film du verre forcé quasi opaque. Absente : faux. */
        val KEY_GLASS_READABLE_TEXT = booleanPreferencesKey("glass_readable_text")
        /** Mise à jour 084 — « Réduire la transparence » suit le réglage système. Absente : vrai. */
        val KEY_GLASS_FOLLOW_REDUCE_TRANSPARENCY = booleanPreferencesKey("glass_follow_reduce_transparency")
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
        /** Présentation du lecteur (mise à jour 048). Absente : pochette carrée. */
        val KEY_PLAYER_PRESENTATION = stringPreferencesKey("player_presentation")
        /** Tri de la bibliothèque (mise à jour 055). Absent : A–Z. */
        val KEY_LIBRARY_SORT = stringPreferencesKey("library_sort_mode")
    }

    val isOnboardingCompleted: Flow<Boolean> = dataStore.data
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

    val themeMode: Flow<AppThemeMode> = dataStore.data
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

    private val themeData: Flow<Preferences> = dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }

    /** Niveau de l'effet verre ; BALANCED par défaut, ancienne clé migrée (voir [resolveGlassMaterial]). */
    val glassMaterial: Flow<GlassMaterial> = dataStore.data
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

    val isDynamicColorEnabled: Flow<Boolean> = dataStore.data
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
    val glassColorStrength: Flow<Int> = dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { preferences -> resolveGlassColorStrength(preferences[KEY_GLASS_COLOR_STRENGTH]) }

    /** Givre du verre en dp (0-24) ; 6 dp par défaut (voir [resolveGlassFrostDp]). */
    val glassFrostDp: Flow<Int> = dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { preferences -> resolveGlassFrostDp(preferences[KEY_GLASS_FROST_DP]) }

    /** Teinte du verre en degrés ; `null` = Aucune, par défaut (voir [resolveGlassTintHue]). */
    val glassTintHue: Flow<Float?> = dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { preferences -> resolveGlassTintHue(preferences[KEY_GLASS_TINT_HUE]) }

    /** Film du verre en % (0-100) ; 50 % par défaut (voir [resolveGlassFilmOpacity]). */
    val glassFilmOpacity: Flow<Int> = dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { preferences -> resolveGlassFilmOpacity(preferences[KEY_GLASS_FILM_OPACITY]) }

    /** Opacité de la capsule de l'onglet actif en % (0-100) ; 26 % par défaut. */
    val glassActiveTabOpacity: Flow<Int> = dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { preferences -> resolveGlassActiveTabOpacity(preferences[KEY_GLASS_ACTIVE_TAB_OPACITY]) }

    /**
     * Mise à jour 084 — « Texte plus lisible » : force l'opacité du film du verre à 85 % pour un
     * contraste maximal. Désactivé par défaut (le filet de sécurité automatique suffit).
     */
    val glassReadableText: Flow<Boolean> = dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { preferences -> preferences[KEY_GLASS_READABLE_TEXT] ?: false }

    /**
     * Mise à jour 084 — « Réduire la transparence » suit le réglage d'accessibilité du système
     * (verre plus opaque, sans flou temps réel). Activé par défaut : le système décide.
     */
    val glassFollowReduceTransparency: Flow<Boolean> = dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { preferences -> preferences[KEY_GLASS_FOLLOW_REDUCE_TRANSPARENCY] ?: true }

    /**
     * Mise à jour 100 — consentement explicite à l'envoi du contexte musical à l'IA du chat.
     * Désactivé par défaut : seuls titre, artiste, album et genre sont transmis, jamais
     * un chemin de fichier ni une donnée personnelle.
     */
    val aiContextConsent: Flow<Boolean> = dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { preferences -> preferences[KEY_AI_CONTEXT_CONSENT] ?: false }

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
        dataStore.edit { preferences ->
            preferences[KEY_ONBOARDING_COMPLETED] = completed
        }
    }

    suspend fun setThemeMode(mode: AppThemeMode) {
        dataStore.edit { preferences ->
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
        dataStore.edit { preferences ->
            migrateThemeKeys(preferences)
            preferences[KEY_APPEARANCE_MODE] = mode.name
        }
    }

    suspend fun setAppStyle(style: AppStyle) {
        dataStore.edit { preferences ->
            migrateThemeKeys(preferences)
            preferences[KEY_APP_STYLE] = style.name
        }
    }

    suspend fun setAccentChoice(choice: AccentChoice) {
        dataStore.edit { preferences ->
            preferences[KEY_ACCENT_CHOICE] = choice.name
        }
    }

    suspend fun setEveningStartMinutes(minutes: Int) {
        dataStore.edit { preferences ->
            preferences[KEY_EVENING_START] = clampDayMinutes(minutes)
        }
    }

    suspend fun setEveningEndMinutes(minutes: Int) {
        dataStore.edit { preferences ->
            preferences[KEY_EVENING_END] = clampDayMinutes(minutes)
        }
    }

    suspend fun setGlassMaterial(material: GlassMaterial) {
        dataStore.edit { preferences ->
            preferences[KEY_GLASS_MATERIAL] = material.name
            preferences.remove(KEY_GLASS_QUALITY_LEGACY)
        }
    }

    suspend fun setGlassColorStrength(percent: Int) {
        dataStore.edit { preferences ->
            preferences[KEY_GLASS_COLOR_STRENGTH] = clampGlassColorStrength(percent)
        }
    }

    suspend fun setGlassFrostDp(dp: Int) {
        dataStore.edit { preferences ->
            preferences[KEY_GLASS_FROST_DP] = clampGlassFrostDp(dp)
        }
    }

    /** Enregistre la teinte du verre ; `null` (Aucune) supprime la clé. */
    suspend fun setGlassTintHue(hue: Float?) {
        dataStore.edit { preferences ->
            val resolved = resolveGlassTintHue(hue)
            if (resolved == null) {
                preferences.remove(KEY_GLASS_TINT_HUE)
            } else {
                preferences[KEY_GLASS_TINT_HUE] = resolved
            }
        }
    }

    suspend fun setGlassFilmOpacity(percent: Int) {
        dataStore.edit { preferences ->
            preferences[KEY_GLASS_FILM_OPACITY] = clampGlassOpacity(percent)
        }
    }

    suspend fun setGlassActiveTabOpacity(percent: Int) {
        dataStore.edit { preferences ->
            preferences[KEY_GLASS_ACTIVE_TAB_OPACITY] = clampGlassOpacity(percent)
        }
    }

    /** Mise à jour 084 — force le film du verre à 85 % (« Texte plus lisible »). */
    suspend fun setGlassReadableText(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[KEY_GLASS_READABLE_TEXT] = enabled
        }
    }

    /** Mise à jour 084 — active/désactive le suivi du réglage système « Réduire la transparence ». */
    suspend fun setGlassFollowReduceTransparency(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[KEY_GLASS_FOLLOW_REDUCE_TRANSPARENCY] = enabled
        }
    }

    val favoriteTrackIds: Flow<Set<Long>> = dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { preferences ->
            preferences[KEY_FAVORITE_TRACK_IDS].orEmpty()
                .mapNotNull { it.toLongOrNull() }
                .toSet()
        }

    val favoriteArtists: Flow<Set<String>> = dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { preferences -> preferences[KEY_FAVORITE_ARTISTS].orEmpty() }

    val searchHistory: Flow<List<String>> = dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { preferences ->
            preferences[KEY_SEARCH_HISTORY]
                .orEmpty()
                .split("\n")
                .map(String::trim)
                .filter(String::isNotBlank)
                .take(20)
        }

    /** Mise à jour 055 — mode de tri mémorisé de la bibliothèque. */
    val librarySort: Flow<LibrarySortMode> = dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { preferences -> resolveLibrarySortMode(preferences[KEY_LIBRARY_SORT]) }

    val autoPlay: Flow<Boolean> = dataStore.data
        .catch { exception -> if (exception is IOException) emit(emptyPreferences()) else throw exception }
        .map { it[KEY_AUTO_PLAY] ?: true }

    val gaplessPlayback: Flow<Boolean> = dataStore.data
        .catch { exception -> if (exception is IOException) emit(emptyPreferences()) else throw exception }
        .map { it[KEY_GAPLESS] ?: true }

    val resumePlayback: Flow<Boolean> = dataStore.data
        .catch { exception -> if (exception is IOException) emit(emptyPreferences()) else throw exception }
        .map { it[KEY_RESUME_PLAYBACK] ?: true }

    /** Présentation du lecteur : pochette, vinyle ou minimal (mise à jour 048). */
    val playerPresentation: Flow<PlayerPresentation> = themeData.map { preferences ->
        resolvePlayerPresentation(preferences[KEY_PLAYER_PRESENTATION])
    }

    val karaokeFontSize: Flow<Float> = dataStore.data
        .catch { exception -> if (exception is IOException) emit(emptyPreferences()) else throw exception }
        .map { it[KEY_KARAOKE_FONT_SIZE]?.toFloatOrNull() ?: 18f }

    val karaokeActiveColor: Flow<String> = dataStore.data
        .catch { exception -> if (exception is IOException) emit(emptyPreferences()) else throw exception }
        .map { it[KEY_KARAOKE_ACTIVE_COLOR] ?: "cyan" }

    val karaokeOffsetMs: Flow<Long> = dataStore.data
        .catch { exception -> if (exception is IOException) emit(emptyPreferences()) else throw exception }
        .map { it[KEY_KARAOKE_OFFSET]?.toLongOrNull() ?: 0L }

    val savedPlaybackState: Flow<SavedPlaybackState?> = dataStore.data
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
        dataStore.edit { it[KEY_REDUCE_ANIMATIONS] = enabled }
    }

    /** Mise à jour 100 — consentement au contexte musical pour l'IA du chat. */
    suspend fun setAiContextConsent(enabled: Boolean) {
        dataStore.edit { it[KEY_AI_CONTEXT_CONSENT] = enabled }
    }

    suspend fun setTextSizePreference(value: TextSizePreference) {
        dataStore.edit { it[KEY_TEXT_SIZE] = value.name }
    }

    suspend fun setListDensityPreference(value: ListDensityPreference) {
        dataStore.edit { it[KEY_LIST_DENSITY] = value.name }
    }

    suspend fun setDynamicColor(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[KEY_DYNAMIC_COLOR] = enabled
        }
    }

    suspend fun setFavoriteTrackIds(ids: Set<Long>) {
        dataStore.edit { preferences ->
            preferences[KEY_FAVORITE_TRACK_IDS] = ids.map(Long::toString).toSet()
        }
    }

    suspend fun setFavoriteArtists(names: Set<String>) {
        dataStore.edit { preferences ->
            preferences[KEY_FAVORITE_ARTISTS] = names
        }
    }

    suspend fun setSearchHistory(values: List<String>) {
        dataStore.edit { preferences ->
            preferences[KEY_SEARCH_HISTORY] = values
                .map(String::trim)
                .filter(String::isNotBlank)
                .take(20)
                .joinToString("\n")
        }
    }

    /** Mise à jour 055 — mémorise le dernier tri choisi pour la bibliothèque. */
    suspend fun setLibrarySort(mode: LibrarySortMode) {
        dataStore.edit { preferences -> preferences[KEY_LIBRARY_SORT] = mode.name }
    }

    suspend fun setAutoPlay(enabled: Boolean) {
        dataStore.edit { preferences -> preferences[KEY_AUTO_PLAY] = enabled }
    }

    suspend fun setGaplessPlayback(enabled: Boolean) {
        dataStore.edit { preferences -> preferences[KEY_GAPLESS] = enabled }
    }

    suspend fun setResumePlayback(enabled: Boolean) {
        dataStore.edit { preferences -> preferences[KEY_RESUME_PLAYBACK] = enabled }
    }

    /** Enregistre la présentation du lecteur (mise à jour 048). */
    suspend fun setPlayerPresentation(value: PlayerPresentation) {
        dataStore.edit { preferences -> preferences[KEY_PLAYER_PRESENTATION] = value.name }
    }

    suspend fun setKaraokeFontSize(size: Float) {
        dataStore.edit { preferences -> preferences[KEY_KARAOKE_FONT_SIZE] = size.toString() }
    }

    suspend fun setKaraokeActiveColor(value: String) {
        dataStore.edit { preferences -> preferences[KEY_KARAOKE_ACTIVE_COLOR] = value }
    }

    suspend fun setKaraokeOffsetMs(value: Long) {
        dataStore.edit { preferences -> preferences[KEY_KARAOKE_OFFSET] = value.toString() }
    }

    suspend fun setSavedPlaybackState(
        trackId: Long,
        positionMs: Long,
        wasPlaying: Boolean,
        shuffleEnabled: Boolean
    ) {
        dataStore.edit { preferences ->
            preferences[KEY_LAST_PLAYED_TRACK_ID] = trackId
            preferences[KEY_LAST_PLAYED_POSITION_MS] = positionMs.coerceAtLeast(0L)
            preferences[KEY_LAST_PLAYED_WAS_PLAYING] = wasPlaying
            preferences[KEY_SHUFFLE_ENABLED] = shuffleEnabled
        }
    }

    suspend fun setShuffleEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[KEY_SHUFFLE_ENABLED] = enabled
        }
    }

    // --- Mise à jour 038 — Sauvegarde et export des réglages -------------------------------------

    /** Instantané complet des réglages sauvegardables, prêt à être sérialisé en JSON. */
    suspend fun backupSnapshot(): SettingsBackup {
        val preferences = dataStore.data.first()
        return SettingsBackup(
            schemaVersion = BACKUP_SCHEMA_VERSION,
            appearance = resolveAppearanceMode(preferences[KEY_APPEARANCE_MODE], preferences[KEY_THEME_MODE]),
            appStyle = resolveAppStyle(preferences[KEY_APP_STYLE], preferences[KEY_THEME_MODE]),
            accentChoice = resolveAccentChoice(preferences[KEY_ACCENT_CHOICE]),
            eveningStartMinutes = resolveEveningMinutes(preferences[KEY_EVENING_START], DefaultEveningStartMinutes),
            eveningEndMinutes = resolveEveningMinutes(preferences[KEY_EVENING_END], DefaultEveningEndMinutes),
            dynamicColor = preferences[KEY_DYNAMIC_COLOR] ?: false,
            textSize = resolveTextSizePreference(preferences[KEY_TEXT_SIZE]),
            listDensity = resolveListDensityPreference(preferences[KEY_LIST_DENSITY]),
            reduceAnimations = preferences[KEY_REDUCE_ANIMATIONS] ?: false,
            glass = GlassBackup(
                material = resolveGlassMaterial(
                    stored = preferences[KEY_GLASS_MATERIAL],
                    legacy = preferences[KEY_GLASS_QUALITY_LEGACY]
                ),
                colorStrength = resolveGlassColorStrength(preferences[KEY_GLASS_COLOR_STRENGTH]),
                frostDp = resolveGlassFrostDp(preferences[KEY_GLASS_FROST_DP]),
                tintHue = resolveGlassTintHue(preferences[KEY_GLASS_TINT_HUE]),
                filmOpacity = resolveGlassFilmOpacity(preferences[KEY_GLASS_FILM_OPACITY]),
                activeTabOpacity = resolveGlassActiveTabOpacity(preferences[KEY_GLASS_ACTIVE_TAB_OPACITY])
            ),
            karaoke = KaraokeBackup(
                fontSize = preferences[KEY_KARAOKE_FONT_SIZE]?.toFloatOrNull() ?: 18f,
                activeColor = preferences[KEY_KARAOKE_ACTIVE_COLOR] ?: "cyan",
                offsetMs = preferences[KEY_KARAOKE_OFFSET]?.toLongOrNull() ?: 0L
            ),
            favorites = FavoritesBackup(
                trackIds = preferences[KEY_FAVORITE_TRACK_IDS].orEmpty().mapNotNull(String::toLongOrNull),
                artists = preferences[KEY_FAVORITE_ARTISTS].orEmpty().toList()
            )
        )
    }

    /** Applique une sauvegarde validée ([SettingsBackupManager.fromJson]) aux réglages. */
    suspend fun applyBackup(backup: SettingsBackup) {
        dataStore.edit { preferences ->
            migrateThemeKeys(preferences)
            preferences[KEY_APPEARANCE_MODE] = backup.appearance.name
            preferences[KEY_APP_STYLE] = backup.appStyle.name
            preferences[KEY_ACCENT_CHOICE] = backup.accentChoice.name
            preferences[KEY_EVENING_START] = clampDayMinutes(backup.eveningStartMinutes)
            preferences[KEY_EVENING_END] = clampDayMinutes(backup.eveningEndMinutes)
            preferences[KEY_DYNAMIC_COLOR] = backup.dynamicColor
            preferences[KEY_TEXT_SIZE] = backup.textSize.name
            preferences[KEY_LIST_DENSITY] = backup.listDensity.name
            preferences[KEY_REDUCE_ANIMATIONS] = backup.reduceAnimations
            preferences[KEY_GLASS_MATERIAL] = backup.glass.material.name
            preferences.remove(KEY_GLASS_QUALITY_LEGACY)
            preferences[KEY_GLASS_COLOR_STRENGTH] = clampGlassColorStrength(backup.glass.colorStrength)
            preferences[KEY_GLASS_FROST_DP] = clampGlassFrostDp(backup.glass.frostDp)
            val tint = resolveGlassTintHue(backup.glass.tintHue)
            if (tint == null) preferences.remove(KEY_GLASS_TINT_HUE) else preferences[KEY_GLASS_TINT_HUE] = tint
            preferences[KEY_GLASS_FILM_OPACITY] = clampGlassOpacity(backup.glass.filmOpacity)
            preferences[KEY_GLASS_ACTIVE_TAB_OPACITY] = clampGlassOpacity(backup.glass.activeTabOpacity)
            preferences[KEY_KARAOKE_FONT_SIZE] = backup.karaoke.fontSize.toString()
            preferences[KEY_KARAOKE_ACTIVE_COLOR] = backup.karaoke.activeColor
            preferences[KEY_KARAOKE_OFFSET] = backup.karaoke.offsetMs.toString()
            preferences[KEY_FAVORITE_TRACK_IDS] = backup.favorites.trackIds.map(Long::toString).toSet()
            preferences[KEY_FAVORITE_ARTISTS] = backup.favorites.artists.toSet()
        }
    }

    // --- Mise à jour 040 — Réinitialisation par section -------------------------------------------

    /** Remet la section Apparence (thème, style, accent, verre, taille, densité, animations) par défaut. */
    suspend fun resetAppearanceSection() {
        dataStore.edit { preferences ->
            preferences[KEY_APPEARANCE_MODE] = AppearanceMode.DARK.name
            preferences[KEY_APP_STYLE] = AppStyle.NEON.name
            preferences[KEY_ACCENT_CHOICE] = AccentChoice.TERRACOTTA.name
            preferences[KEY_EVENING_START] = DefaultEveningStartMinutes
            preferences[KEY_EVENING_END] = DefaultEveningEndMinutes
            preferences[KEY_DYNAMIC_COLOR] = false
            preferences[KEY_TEXT_SIZE] = TextSizePreference.NORMAL.name
            preferences[KEY_LIST_DENSITY] = ListDensityPreference.COMFORTABLE.name
            preferences[KEY_REDUCE_ANIMATIONS] = false
            preferences[KEY_GLASS_MATERIAL] = GlassMaterial.BALANCED.name
            preferences[KEY_GLASS_COLOR_STRENGTH] = DefaultGlassColorStrength
            preferences[KEY_GLASS_FROST_DP] = DefaultGlassFrostDp
            preferences.remove(KEY_GLASS_TINT_HUE)
            preferences[KEY_GLASS_FILM_OPACITY] = DefaultGlassFilmOpacity
            preferences[KEY_GLASS_ACTIVE_TAB_OPACITY] = DefaultGlassActiveTabOpacity
            preferences.remove(KEY_GLASS_QUALITY_LEGACY)
        }
    }

    /** Remet la section Lecture (lecture auto, enchaînement, reprise, présentation) par défaut. */
    suspend fun resetPlaybackSection() {
        dataStore.edit { preferences ->
            preferences[KEY_AUTO_PLAY] = true
            preferences[KEY_GAPLESS] = true
            preferences[KEY_RESUME_PLAYBACK] = true
            preferences[KEY_PLAYER_PRESENTATION] = PlayerPresentation.POCHETTE.name
        }
    }

    /** Remet la section Paroles (karaoké : taille, couleur, décalage) par défaut. */
    suspend fun resetKaraokeSection() {
        dataStore.edit { preferences ->
            preferences[KEY_KARAOKE_FONT_SIZE] = 18f.toString()
            preferences[KEY_KARAOKE_ACTIVE_COLOR] = "cyan"
            preferences[KEY_KARAOKE_OFFSET] = 0L.toString()
        }
    }
}
