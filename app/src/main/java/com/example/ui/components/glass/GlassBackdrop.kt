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
import kotlin.math.roundToInt

/** État partagé : calque contenant le rendu du fond + sa position à l'écran. */
@Stable
class GlassBackdropState internal constructor(internal val layer: GraphicsLayer) {
    internal var origin by mutableStateOf(Offset.Zero)
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
 * Avec [enabled] = false (API < 31 ou option « Plus rapide »), rien n'est enregistré :
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
                drawLayer(state.layer)
            }
    }

/**
 * Conteneur à deux plans :
 *  - [background] : ce qui sera visible (et flouté) derrière le verre ;
 *  - [foreground] : barres, mini-lecteur, boutons… qui utilisent [GlassSurface].
 *
 * Quand le flou est indisponible ou désactivé, le fond n'est pas enregistré et les
 * [GlassSurface] reçoivent `null` : elles affichent l'aplat translucide.
 */
@Composable
fun GlassBackdropHost(
    modifier: Modifier = Modifier,
    state: GlassBackdropState = rememberGlassBackdropState(),
    background: @Composable BoxScope.() -> Unit,
    foreground: @Composable BoxScope.() -> Unit
) {
    val recordBackdrop = LocalGlassBlurAllowed.current && glassBlurSupported
    Box(modifier) {
        val boxScope = this
        // matchParentSize est un membre de BoxScope (pas d'import nécessaire).
        Box(
            modifier = Modifier
                .matchParentSize()
                .glassBackdropSource(state, enabled = recordBackdrop),
            content = background
        )
        CompositionLocalProvider(LocalGlassBackdrop provides (if (recordBackdrop) state else null)) {
            boxScope.foreground()
        }
    }
}
