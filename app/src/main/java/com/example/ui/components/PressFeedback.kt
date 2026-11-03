package com.example.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import com.example.ui.components.glass.rememberAnimationsEnabled

/**
 * Mise à jour 075 — micro-interaction de pression.
 *
 * Tout élément tactile branché avec [pressFeedback] se réduit à 97 % de son échelle
 * au toucher (80 ms) puis revient avec un ressort doux
 * (`spring(dampingRatio = 0.7f)`). Le voile discret de 8 % de la couleur de texte
 * remplace le ripple Material trop marqué (l'appelant passe `indication = null`).
 *
 * Sans animations (réglage application ou système), l'échelle reste à 1 : seul le
 * voile reste actif, comme retour visuel minimal.
 */
@Composable
fun Modifier.pressFeedback(
    interactionSource: InteractionSource,
    scaleOnPress: Boolean = true,
    veilOnPress: Boolean = true
): Modifier {
    val pressed by interactionSource.collectIsPressedAsState()
    val animationsEnabled = rememberAnimationsEnabled()
    val scale by animateFloatAsState(
        targetValue = if (pressed && scaleOnPress && animationsEnabled) 0.97f else 1f,
        animationSpec = if (pressed) {
            tween(durationMillis = 80)
        } else {
            spring(dampingRatio = 0.7f, stiffness = Spring.StiffnessMediumLow)
        },
        label = "press_scale"
    )
    // Voile de 8 % de la couleur de texte (Mise à jour 075) — nettement plus discret
    // que le ripple Material par défaut.
    val veil = if (veilOnPress) {
        Color.Black.copy(alpha = 0.08f)
    } else {
        Color.Transparent
    }
    return this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .drawWithContent {
            drawContent()
            if (pressed && veilOnPress) {
                drawRect(veil)
            }
        }
}

/**
 * Constantes partagées de la micro-interaction, exposées pour les tests.
 */
object PressFeedbackSpec {
    /** Échelle appliquée pendant la pression. */
    const val PRESSED_SCALE = 0.97f

    /** Durée d'entrée en pression, en millisecondes. */
    const val PRESS_IN_MS = 80

    /** Rapport d'amortissement du ressort de retour. */
    const val RETURN_DAMPING = 0.7f

    /** Opacité du voile de pression. */
    const val VEIL_ALPHA = 0.08f
}
