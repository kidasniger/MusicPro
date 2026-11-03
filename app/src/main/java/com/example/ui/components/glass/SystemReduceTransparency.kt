package com.example.ui.components.glass

import android.content.Context
import android.view.accessibility.AccessibilityManager

/**
 * Réduction de la transparence du système (mise à jour 084) : Android propose un réglage
 * d'accessibilité qui demande aux applications de limiter les surfaces translucides. L'API
 * [AccessibilityManager.isReduceTransparencyEnabled] n'est disponible que sur les versions
 * récentes ; on l'interroge par réflexion protégée pour rester compatible partout et renvoyer
 * `false` (comportement neutre) quand elle n'existe pas.
 */
fun isSystemReduceTransparencyEnabled(context: Context): Boolean {
    val manager = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as? AccessibilityManager
        ?: return false
    return try {
        val method = manager.javaClass.methods.firstOrNull { it.name == "isReduceTransparencyEnabled" }
        method?.invoke(manager) as? Boolean ?: false
    } catch (_: Throwable) {
        false
    }
}
