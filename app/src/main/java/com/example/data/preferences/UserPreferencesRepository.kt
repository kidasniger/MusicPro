package com.example.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

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

    suspend fun setDynamicColor(enabled: Boolean) {
        context.musicProDataStore.edit { preferences ->
            preferences[KEY_DYNAMIC_COLOR] = enabled
        }
    }
}
