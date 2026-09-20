package com.example.data.security

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * Stockage local de la clé API Groq uniquement via EncryptedSharedPreferences.
 *
 * Aucune clé de build n'est lue et aucun repli vers SharedPreferences standard n'existe.
 * Cela évite qu'un secret injecté par Gradle puisse être embarqué dans l'APK.
 */
class GroqApiKeyStore(context: Context) {

    private val appContext = context.applicationContext

    private val prefs: SharedPreferences? by lazy {
        try {
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
            Log.e(TAG, "Impossible d'initialiser le stockage Groq chiffré.", e)
            null
        }
    }

    /** Récupère la clé API Groq saisie à l'exécution. */
    fun getApiKey(): String {
        return prefs?.getString(KEY_GROQ_API_KEY, null)?.trim().orEmpty()
    }

    /** Enregistre la clé uniquement dans EncryptedSharedPreferences. */
    fun setApiKey(apiKey: String): Boolean {
        val securePrefs = prefs ?: return false
        securePrefs.edit().putString(KEY_GROQ_API_KEY, apiKey.trim()).apply()
        return true
    }

    /** Supprime la clé API Groq enregistrée. */
    fun clearApiKey() {
        prefs?.edit()?.remove(KEY_GROQ_API_KEY)?.apply()
    }

    /** Vérifie si une clé API non vide est configurée. */
    fun hasApiKey(): Boolean = getApiKey().isNotBlank()

    /** Retourne une version masquée de la clé pour l'affichage UI. */
    fun getMaskedApiKey(): String {
        val key = getApiKey()
        if (key.isBlank()) return ""
        if (key.length <= 8) return "••••••••"
        return "${key.take(4)}••••••••${key.takeLast(4)}"
    }

    companion object {
        private const val TAG = "GroqApiKeyStore"
        private const val ENCRYPTED_PREFS_FILE = "musicpro_groq_encrypted_prefs"
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
