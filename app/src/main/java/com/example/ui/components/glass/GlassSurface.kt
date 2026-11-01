package com.example.ui.components.glass

import android.os.Build
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlurEffect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

/**
 * Surface « verre liquide » unique de l'application.
 * Empile : (1) fond flouté, (2) film, (3) reflet haut→bas, (4) bordure 1 dp en dégradé.
 *
 * Sans [GlassBackdropHost] parent, ou sous API 31, le flou est remplacé par un film plus opaque
 * (lisibilité et contraste conservés).
 */
@Composable
fun GlassSurface(
    modifier: Modifier = Modifier,
    shape: Shape = CircleShape,
    material: GlassMaterial = GlassMaterials.regular(),
    content: @Composable BoxScope.() -> Unit
) {
    val backdrop = LocalGlassBackdrop.current
    val blurLayer = rememberGraphicsLayer()
    var origin by remember { mutableStateOf(Offset.Zero) }

    val blurOn = backdrop != null &&
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
        material.blurRadius > 0.dp

    val radiusPx = with(LocalDensity.current) { material.blurRadius.toPx() }
    SideEffect {
        blurLayer.renderEffect =
            if (blurOn) BlurEffect(radiusPx, radiusPx, TileMode.Clamp) else null
    }

    val highlightBrush = remember(material.highlightTopAlpha, material.highlightBottomAlpha) {
        Brush.verticalGradient(
            listOf(
                Color.White.copy(alpha = material.highlightTopAlpha),
                Color.White.copy(alpha = material.highlightBottomAlpha)
            )
        )
    }
    val borderBrush = remember(material.borderStartAlpha, material.borderEndAlpha) {
        Brush.linearGradient(
            listOf(
                Color.White.copy(alpha = material.borderStartAlpha),
                Color.White.copy(alpha = material.borderEndAlpha)
            )
        )
    }

    Box(
        modifier = modifier
            .onGloballyPositioned { origin = it.positionInWindow() }
            .clip(shape)
            .drawWithContent {
                // (1) fond flouté
                if (blurOn && backdrop != null) {
                    val dx = backdrop.origin.x - origin.x
                    val dy = backdrop.origin.y - origin.y
                    blurLayer.record(
                        density = this,
                        layoutDirection = layoutDirection,
                        size = IntSize(size.width.roundToInt(), size.height.roundToInt())
                    ) {
                        translate(left = dx, top = dy) { drawLayer(backdrop.layer) }
                    }
                    drawLayer(blurLayer)
                }
                // (2) film
                drawRect(
                    material.filmColor.copy(
                        alpha = if (blurOn) material.filmAlpha else material.fallbackFilmAlpha
                    )
                )
                // (3) reflet haut → bas
                drawRect(brush = highlightBrush)
                drawContent()
            }
            // (4) bordure en dégradé
            .border(material.borderWidth, borderBrush, shape)
    ) {
        val boxScope = this
        CompositionLocalProvider(LocalContentColor provides material.contentColor) {
            boxScope.content()
        }
    }
}
