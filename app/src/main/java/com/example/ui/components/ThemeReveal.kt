package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.Path
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.unit.IntSize
import com.example.ui.components.glass.rememberAnimationsEnabled
import kotlin.math.hypot

/**
 * Mise à jour 078 — transition de thème sans flash.
 *
 * Encapsule un écran dont le thème peut changer à la volée. [beginReveal] mémorise
 * le point de contact du bouton et capture l'image de l'interface actuelle ; une
 * fois la capture prête (deux frames plus tard), le rappel fourni change réellement
 * le thème. L'ancienne interface reste alors affichée dans un calque statique tandis
 * que le nouveau thème est révélé par un cercle qui s'étend depuis le point de
 * contact (clip circulaire de 400 ms).
 *
 * Repli : fondu de 250 ms lorsque les animations sont réduites.
 */
class ThemeRevealState internal constructor() {
    internal var pendingOrigin by mutableStateOf<Offset?>(null)
    internal var pendingCallback by mutableStateOf<(() -> Unit)?>(null)
    internal var screenOffset by mutableStateOf(Offset.Zero)
    internal var screenSize by mutableStateOf(IntSize.Zero)
    internal var revealActive by mutableStateOf(false)
    internal var fadeFallback by mutableStateOf(false)
}

@Composable
fun rememberThemeRevealState(): ThemeRevealState = remember { ThemeRevealState() }

/**
 * Demande une révélation depuis [originInRoot] (coordonnées racine, p. ex. le centre
 * du bouton touché). Dès que l'image de l'interface actuelle est capturée,
 * [onCaptureReady] est invoqué — c'est là qu'il faut changer le thème.
 */
fun ThemeRevealState.beginReveal(originInRoot: Offset, onCaptureReady: () -> Unit) {
    pendingOrigin = originInRoot
    pendingCallback = onCaptureReady
}

/** Constantes partagées de la transition de thème, exposées pour les tests. */
object ThemeRevealSpec {
    /** Durée du clip circulaire, en millisecondes. */
    const val CIRCLE_MS = 400

    /** Durée du repli en fondu, en millisecondes. */
    const val FADE_MS = 250
}

/**
 * Hôte de la révélation : enregistre le contenu courant dans un calque graphique,
 * puis anime un cercle de révélation (ou un fondu de repli) par-dessus l'ancienne
 * image figée pendant que le contenu réel affiche déjà le nouveau thème.
 */
@Composable
fun ThemeRevealHost(
    state: ThemeRevealState,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val layer = rememberGraphicsLayer()
    val animationsEnabled = rememberAnimationsEnabled()
    val progress = remember { Animatable(1f) }
    var radiusPx by remember { mutableStateOf(0f) }

    // Capture de l'interface actuelle tant qu'une demande est en attente.
    val capturing = state.pendingOrigin != null

    LaunchedEffect(state.pendingOrigin) {
        val origin = state.pendingOrigin ?: return@LaunchedEffect
        val callback = state.pendingCallback
        // Deux frames : le dessin avec l'ancien thème est enregistré dans le calque.
        withFrameNanos { }
        withFrameNanos { }

        // Arrête la capture AVANT le changement de thème : le calque garde l'ancienne image.
        state.pendingOrigin = null
        state.pendingCallback = null
        state.revealActive = true
        state.fadeFallback = !animationsEnabled

        val w = state.screenSize.width.toFloat()
        val h = state.screenSize.height.toFloat()
        val local = origin - state.screenOffset
        radiusPx = maxOf(
            hypot(local.x, local.y),
            hypot(w - local.x, local.y),
            hypot(local.x, h - local.y),
            hypot(w - local.x, h - local.y)
        )

        // Le thème change maintenant : le contenu réel passe au nouveau thème sous le
        // calque figé, et le cercle le révèle depuis le point de contact.
        callback?.invoke()

        if (animationsEnabled) {
            progress.snapTo(0f)
            progress.animateTo(1f, tween(durationMillis = ThemeRevealSpec.CIRCLE_MS, easing = FastOutSlowInEasing))
        } else {
            // Repli : fondu de 250 ms.
            progress.snapTo(0f)
            progress.animateTo(1f, tween(durationMillis = ThemeRevealSpec.FADE_MS, easing = FastOutSlowInEasing))
        }
        state.revealActive = false
        callback?.invoke()
    }

    Box(modifier = modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawWithContent {
                    if (capturing) {
                        layer.record { this@drawWithContent.drawContent() }
                    }
                    drawContent()
                }
        ) {
            content()
        }

        if (state.revealActive) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .drawWithContent {
                        if (state.fadeFallback) {
                            // Repli : l'ancienne interface s'efface en fondu.
                            layer.alpha = 1f - progress.value
                            drawLayer(layer)
                        } else {
                            // L'ancienne interface, visible partout sauf dans le cercle
                            // qui révèle le nouveau thème.
                            val local = (state.pendingOrigin ?: Offset.Zero) - state.screenOffset
                            val circle = Path().apply {
                                addOval(Rect(center = local, radius = progress.value * radiusPx))
                            }
                            val hole = Path().apply {
                                addRect(Rect(0f, 0f, size.width, size.height))
                                op(this@apply, circle, PathOperation.Difference)
                            }
                            clipPath(hole) {
                                drawLayer(layer)
                            }
                        }
                    }
            )
        }
    }
}
