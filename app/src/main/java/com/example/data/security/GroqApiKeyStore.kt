package com.example.data.security

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.security.KeyStore

/**
 * Stockage local de la clé API Groq uniquement via EncryptedSharedPreferences.
 *
 * Aucune clé de build n'est lue et aucun repli vers SharedPreferences standard n'existe.
 * Cela évite qu'un secret injecté par Gradle puisse être embarqué dans l'APK.
 *
 * Sur certains appareils (Samsung notamment), le Keystore Android peut devenir
 * inutilisable après un changement de code de verrouillage, une mise à jour
 * système ou une restauration Smart Switch / sauvegarde Google : le fichier
 * chiffré est transféré mais la clé maître est liée au matériel et ne voyage
 * pas. Le stockage tente alors de se réparer seul en supprimant ce qui est
 * illisible (fichier de préférences, puis clé maître) avant de retenter —
 * les données perdues étaient de toute façon indéchiffrables, seule la clé
 * API doit être ressaisie.
 */
class GroqApiKeyStore(context: Context) {

    private val appContext = context.applicationContext

    private val prefs: SharedPreferences? by lazy { openSecurePrefsWithRecovery() }

    /**
     * Indique si le stockage chiffré est utilisable. Quand ce n'est pas le cas,
     * setApiKey échoue et l'UI doit inviter à effacer les données de l'application.
     */
    fun isStorageAvailable(): Boolean = prefs != null

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

    private fun createSecurePrefs(): SharedPreferences {
        val masterKey = MasterKey.Builder(appContext)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        return EncryptedSharedPreferences.create(
            appContext,
            ENCRYPTED_PREFS_FILE,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    /**
     * Ouvre le stockage chiffré et tente une auto-réparation limitée en cas de
     * corruption, au lieu de rester bloqué sur un échec silencieux permanent :
     * 1. suppression du fichier de préférences illisible puis nouvelle tentative ;
     * 2. suppression de la clé maître du Keystore puis nouvelle tentative
     *    (c'est le seul usage du Keystore dans l'application).
     */
    private fun openSecurePrefsWithRecovery(): SharedPreferences? {
        try {
            return createSecurePrefs()
        } catch (e: Exception) {
            Log.e(TAG, "Initialisation du stockage Groq chiffré impossible ; tentative de réparation.", e)
        }

        deleteCorruptedPrefsFile()

        try {
            Log.w(TAG, "Nouvelle tentative après suppression du fichier chiffré.")
            return createSecurePrefs()
        } catch (e: Exception) {
            Log.e(TAG, "Le stockage reste indisponible ; réinitialisation de la clé maître du Keystore.", e)
        }

        resetMasterKey()

        return try {
            Log.w(TAG, "Dernière tentative après réinitialisation du Keystore.")
            createSecurePrefs()
        } catch (e: Exception) {
            Log.e(TAG, "Stockage Groq chiffré définitivement indisponible.", e)
            null
        }
    }

    private fun deleteCorruptedPrefsFile() {
        try {
            appContext.deleteSharedPreferences(ENCRYPTED_PREFS_FILE)
        } catch (e: Exception) {
            Log.e(TAG, "Impossible de supprimer le fichier chiffré corrompu.", e)
        }
    }

    private fun resetMasterKey() {
        try {
            val keyStore = KeyStore.getInstance("AndroidKeyStore")
            keyStore.load(null)
            keyStore.deleteEntry(MasterKey.DEFAULT_MASTER_KEY_ALIAS)
        } catch (e: Exception) {
            Log.e(TAG, "Impossible de réinitialiser la clé maître du Keystore.", e)
        }
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
