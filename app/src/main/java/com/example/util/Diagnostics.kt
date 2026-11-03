package com.example.util

import android.content.Context
import android.os.Build

/**
 * Journal de bord anonyme (mise à jour 089) : un texte de diagnostic à copier-coller dans un
 * signalement de bug. Aucune donnée personnelle, aucune clé API, aucun identifiant — uniquement
 * la version de l'application, le modèle d'appareil, le niveau d'Android et les réglages
 * d'apparence actifs.
 */
object Diagnostics {

    /** Version affichée de l'application (ex. « 1.0-b502 »), sans identifiant d'installation. */
    fun appVersion(context: Context): String = try {
        val info = context.packageManager.getPackageInfo(context.packageName, 0)
        info.versionName ?: "?"
    } catch (_: Exception) {
        "?"
    }

    /**
     * Construit le diagnostic complet.
     *
     * @param styleName style actif (Minimal, Néon, Verre)
     * @param isDark thème sombre actif ?
     * @param glassMaterial niveau du verre (FASTEST / BALANCED / QUALITY)
     * @param readableText interrupteur « Texte plus lisible »
     * @param reduceAnimations interrupteur « Réduire les animations »
     */
    fun build(
        context: Context,
        styleName: String,
        isDark: Boolean,
        glassMaterial: String,
        readableText: Boolean,
        reduceAnimations: Boolean
    ): String = buildString {
        appendLine("MusicPro ${appVersion(context)}")
        appendLine("Appareil : ${Build.MANUFACTURER} ${Build.MODEL}")
        appendLine("Android : API ${Build.VERSION.SDK_INT}")
        appendLine("Style : $styleName (${if (isDark) "sombre" else "clair"})")
        appendLine("Verre : $glassMaterial${if (readableText) " + texte plus lisible" else ""}")
        appendLine("Animations réduites : ${if (reduceAnimations) "oui" else "non"}")
        appendLine("Batterie : ${if (isBatterySaver(context)) "économiseur actif" else "normal"}")
    }.trimEnd()

    /** Niveau d'économie de batterie (PowerManager), information utile aux rapports de lenteur. */
    private fun isBatterySaver(context: Context): Boolean = try {
        val pm = context.getSystemService(Context.POWER_SERVICE) as android.os.PowerManager
        pm.isPowerSaveMode
    } catch (_: Exception) {
        false
    }
}
