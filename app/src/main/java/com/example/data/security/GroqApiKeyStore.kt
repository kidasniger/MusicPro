package com.example.data.security

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * Stockage sécurisé de la clé API Groq via EncryptedSharedPreferences (AES-256 GCM).
 * Inclut un mécanisme de repli résilient en cas d'indisponibilité du Keystore matériel.
 */
class GroqApiKeyStore(context: Context) {

    private val appContext = context.applicationContext

    private val prefs: SharedPreferences by lazy {
        initEncryptedSharedPreferences()
    }

    private fun initEncryptedSharedPreferences(): SharedPreferences {
        return try {
            val masterKey = MasterKey.Builder(appContext)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            EncryptedSharedPreferences.create(
                appContext,
                ENCRYPTED_PREFS_FILE,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Throwable) {
            Log.w(TAG, "Échec de l'initialisation de EncryptedSharedPreferences: ${e.message}, repli vers SharedPreferences standard.", e)
            appContext.getSharedPreferences(FALLBACK_PREFS_FILE, Context.MODE_PRIVATE)
        }
    }

    /**
     * Récupère la clé API Groq stockée.
     */
    fun getApiKey(): String {
        val stored = prefs.getString(KEY_GROQ_API_KEY, null)?.trim()
        if (stored != null) {
            return stored
        }
        val buildConfigKey = com.example.BuildConfig.GROQ_API_KEY.trim()
        if (buildConfigKey.isNotBlank() && buildConfigKey != "your_groq_api_key_here") {
            return buildConfigKey
        }
        return ""
    }

    /**
     * Enregistre la clé API Groq de manière chiffrée.
     */
    fun setApiKey(apiKey: String) {
        prefs.edit().putString(KEY_GROQ_API_KEY, apiKey.trim()).apply()
    }

    /**
     * Supprime la clé API Groq enregistrée.
     */
    fun clearApiKey() {
        prefs.edit().putString(KEY_GROQ_API_KEY, "").apply()
    }

    /**
     * Vérifie si une clé API non vide est configurée.
     */
    fun hasApiKey(): Boolean {
        return getApiKey().isNotBlank()
    }

    /**
     * Retourne une version masquée de la clé (ex: "gsk_••••••••••••ab12") pour l'affichage UI.
     */
    fun getMaskedApiKey(): String {
        val key = getApiKey()
        if (key.isBlank()) return ""
        if (key.length <= 8) return "••••••••"
        val prefix = key.take(4)
        val suffix = key.takeLast(4)
        return "$prefix••••••••$suffix"
    }

    companion object {
        private const val TAG = "GroqApiKeyStore"
        private const val ENCRYPTED_PREFS_FILE = "musicpro_groq_encrypted_prefs"
        private const val FALLBACK_PREFS_FILE = "musicpro_groq_fallback_prefs"
        private const val KEY_GROQ_API_KEY = "key_groq_whisper_api"

        @Volatile
        private var INSTANCE: GroqApiKeyStore? = null

        fun getInstance(context: Context): GroqApiKeyStore {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: GroqApiKeyStore(context).also { INSTANCE = it }
            }
        }
    }
}
