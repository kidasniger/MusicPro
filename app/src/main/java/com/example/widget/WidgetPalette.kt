package com.example.widget

import androidx.compose.ui.graphics.Color
import com.example.data.preferences.AppearanceMode
import com.example.data.preferences.AppStyle

/**
 * Mise à jour 080 — palette du widget Glance alignée sur le style choisi dans
 * l'application : clair, sombre (ou sombre le soir), auto (suit le système au moment
 * du rafraîchissement) ou verre (style « Effet verre », translucide).
 * Objet pur : testable sans Robolectric.
 */
object WidgetPalette {

    /** Couleurs du widget pour un rendu donné. */
    data class Colors(
        val background: Color,
        val card: Color,
        val text: Color,
        val accent: Color,
        val accentAlt: Color
    )

    val LIGHT = Colors(
        background = Color(0xFFF4F5F8),
        card = Color(0xFFFFFFFF),
        text = Color(0xFF252936),
        accent = Color(0xFF6478D3),
        accentAlt = Color(0xFF7C8498)
    )

    val DARK = Colors(
        background = Color(0xFF130924),
        card = Color(0xFF1E1338),
        text = Color(0xFFFFFFFF),
        accent = Color(0xFF00D4FF),
        accentAlt = Color(0xFF8A2BE2)
    )

    /** Style « verre » : fond translucide, texte clair, accent néon. */
    val GLASS = Colors(
        background = Color(0xB31E1338),
        card = Color(0x80302157),
        text = Color(0xFFFFFFFF),
        accent = Color(0xFF00D4FF),
        accentAlt = Color(0xFF8A2BE2)
    )

    /** Palette pour le style choisi ([isNight] sert au mode Auto). */
    fun forAppearance(mode: AppearanceMode, isGlass: Boolean, isNight: Boolean): Colors = when {
        isGlass -> GLASS
        mode == AppearanceMode.LIGHT -> LIGHT
        mode == AppearanceMode.AUTO -> if (isNight) DARK else LIGHT
        else -> DARK
    }
}
