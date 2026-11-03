package com.example.ui.components.glass

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.unit.IntSize
import com.example.data.preferences.glassBlurActive
import kotlin.math.roundToInt

/** État partagé : calque contenant le rendu du fond + sa position à l'écran. */
@Stable
class GlassBackdropState internal constructor(internal val layer: GraphicsLayer) {
    internal var origin by mutableStateOf(Offset.Zero)

    /**
     * Mise à jour 086 — compteur (champ simple, hors snapshot) incrémenté à chaque fois que le
     * fond se redessine. Les [GlassSurface] le comparent à leur dernier enregistrement : si le
     * fond n'a pas changé, elles réutilisent leur calque flouté sans le re-enregistrer, ce qui
     * évite de re-flouter à chaque frame quand « rien ne bouge ».
     */
    internal var generation: Long = 0
}

/** Fourni par [GlassBackdropHost] ; `null` => les GlassSurface passent en repli sans flou. */
val LocalGlassBackdrop = staticCompositionLocalOf<GlassBackdropState?> { null }

@Composable
fun rememberGlassBackdropState(): GlassBackdropState {
    val layer = rememberGraphicsLayer()
    return remember(layer) { GlassBackdropState(layer) }
}

/**
 * Enregistre le contenu de ce Modifier dans le calque du backdrop, puis le dessine normalement.
 * Ne jamais y placer de GlassSurface (récursion).
 *
 * Avec [enabled] = false (API < 31, niveau FASTEST ou givre sous 2 dp), rien n'est enregistré :
 * le contenu n'est dessiné qu'une seule fois, ce qui garde l'application fluide.
 */
fun Modifier.glassBackdropSource(state: GlassBackdropState, enabled: Boolean = true): Modifier =
    if (!enabled) {
        this
    } else {
        this
            .onGloballyPositioned { state.origin = it.positionInWindow() }
            .drawWithContent {
                state.layer.record(
                    density = this,
                    layoutDirection = layoutDirection,
                    size = IntSize(size.width.roundToInt(), size.height.roundToInt())
                ) {
                    this@drawWithContent.drawContent()
                }
                // Mise à jour 086 — le contenu du calque vient de changer : on prévient les
                // surfaces pour qu'elles ré-enregistrent leur version floutée.
                state.generation++
                drawLayer(state.layer)
            }
    }

/**
 * Conteneur à deux plans :
 *  - [background] : ce qui sera visible (et flouté) derrière le verre ;
 *  - [foreground] : barres, mini-lecteur, boutons… qui utilisent [GlassSurface].
 *
 * Avec le niveau FASTEST, avant Android 12 ou avec un givre sous 2 dp, le fond n'est pas
 * enregistré et les [GlassSurface] reçoivent `null` : elles affichent l'aplat translucide.
 */
@Composable
fun GlassBackdropHost(
    modifier: Modifier = Modifier,
    state: GlassBackdropState = rememberGlassBackdropState(),
    background: @Composable BoxScope.() -> Unit,
    foreground: @Composable BoxScope.() -> Unit
) {
    val recordBackdrop = !LocalReduceAnimations.current && glassBlurActive(
        material = LocalGlassMaterial.current,
        blurSupported = glassBlurSupported,
        frostDp = LocalGlassFrostDp.current
    )
    Box(modifier) {
        val boxScope = this
        // matchParentSize est un membre de BoxScope (pas d'import nécessaire).
        Box(
            modifier = Modifier
                .matchParentSize()
                .glassBackdropSource(state, enabled = recordBackdrop),
            content = background
        )
        CompositionLocalProvider(
            LocalGlassBackdrop provides (if (recordBackdrop) state else null),
            // Mise à jour 086 — un budget de flou par écran : au plus une surface floutée à la fois.
            LocalGlassBlurBudget provides remember { GlassBlurBudget(maxBlurSurfaces = 1) }
        ) {
            boxScope.foreground()
        }
    }
}
