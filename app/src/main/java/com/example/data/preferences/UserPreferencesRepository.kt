package com.example.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
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
    val wasPlaying: Boolean
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
                wasPlaying = preferences[KEY_LAST_PLAYED_WAS_PLAYING] ?: false
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

    suspend fun setSavedPlaybackState(trackId: Long, positionMs: Long, wasPlaying: Boolean) {
        context.musicProDataStore.edit { preferences ->
            preferences[KEY_LAST_PLAYED_TRACK_ID] = trackId
            preferences[KEY_LAST_PLAYED_POSITION_MS] = positionMs.coerceAtLeast(0L)
            preferences[KEY_LAST_PLAYED_WAS_PLAYING] = wasPlaying
        }
    }
}
