package com.example.ui.components.glass

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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlurEffect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.example.data.preferences.GlassMaterial
import com.example.data.preferences.rendersBlur
import kotlin.math.roundToInt

/**
 * Surface « verre liquide » unique de l'application. Le rendu dépend du niveau [GlassMaterial]
 * choisi dans les réglages ([LocalGlassMaterial]) :
 *
 *  - FASTEST  : aplat translucide ([GlassFlatFallbackAlpha]), fine bordure, aucun flou.
 *  - BALANCED : flou de [GlassBalancedBlurRadius], film plus dense, un seul reflet plat, fine bordure.
 *  - QUALITY  : flou réel (rayon de la recette), reflet dégradé, bordure lumineuse en dégradé,
 *               bruit à [GlassNoiseOpacity] et ombre portée douce.
 *
 * Sans [GlassBackdropHost] parent ou avant Android 12, tous les niveaux se replient sur l'aplat.
 * En mode aplat, rien n'est enregistré ni mesuré.
 */
@Composable
fun GlassSurface(
    modifier: Modifier = Modifier,
    shape: Shape = CircleShape,
    material: GlassSpec = GlassMaterials.regular(),
    content: @Composable BoxScope.() -> Unit
) {
    val backdrop = LocalGlassBackdrop.current
    val level = LocalGlassMaterial.current
    val blurLayer = rememberGraphicsLayer()
    var origin by remember { mutableStateOf(Offset.Zero) }

    val blurOn = backdrop != null &&
        level.rendersBlur(glassBlurSupported) &&
        material.blurRadius > 0.dp
    val quality = blurOn && level == GlassMaterial.QUALITY

    val blurRadius = if (level == GlassMaterial.BALANCED) GlassBalancedBlurRadius else material.blurRadius
    val radiusPx = with(LocalDensity.current) { blurRadius.toPx() }
    SideEffect {
        blurLayer.renderEffect =
            if (blurOn) BlurEffect(radiusPx, radiusPx, TileMode.Clamp) else null
    }

    val filmAlpha = when {
        !blurOn -> material.fallbackFilmAlpha
        level == GlassMaterial.BALANCED -> maxOf(material.filmAlpha, GlassBalancedMinFilmAlpha)
        else -> material.filmAlpha
    }

    val highlightBrush = remember(material.highlightTopAlpha, material.highlightBottomAlpha) {
        Brush.verticalGradient(
            listOf(
                Color.White.copy(alpha = material.highlightTopAlpha),
                Color.White.copy(alpha = material.highlightBottomAlpha)
            )
        )
    }
    val borderBrush: Brush = remember(quality, material.borderStartAlpha, material.borderEndAlpha) {
        if (quality) {
            // Bordure lumineuse : plus claire que la bordure standard, en dégradé.
            Brush.linearGradient(
                listOf(
                    Color.White.copy(alpha = (material.borderStartAlpha + 0.20f).coerceAtMost(1f)),
                    Color.White.copy(alpha = (material.borderEndAlpha + 0.04f).coerceAtMost(1f))
                )
            )
        } else {
            SolidColor(Color.White.copy(alpha = 0.22f))
        }
    }

    Box(
        modifier = modifier
            // Ombre portée douce : QUALITY uniquement, dessinée avant le clip.
            .then(
                if (quality) {
                    Modifier.shadow(
                        elevation = 12.dp,
                        shape = shape,
                        clip = false,
                        ambientColor = Color.Black.copy(alpha = 0.18f),
                        spotColor = Color.Black.copy(alpha = 0.22f)
                    )
                } else {
                    Modifier
                }
            )
            // La position n'est utile qu'au flou : pas de suivi en mode aplat.
            .then(
                if (blurOn) {
                    Modifier.onGloballyPositioned { origin = it.positionInWindow() }
                } else {
                    Modifier
                }
            )
            .clip(shape)
            .drawWithContent {
                // (1) copie du fond, floutée par le RenderEffect du calque
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
                // (2) film (ou aplat translucide quand il n'y a pas de flou)
                drawRect(material.filmColor.copy(alpha = filmAlpha))
                // (3) reflets et grain
                when {
                    quality -> {
                        drawRect(brush = highlightBrush)
                        drawRect(brush = GlassNoise.brush, alpha = GlassNoiseOpacity)
                    }
                    blurOn -> {
                        // BALANCED : un seul reflet plat sur la moitié haute.
                        drawRect(
                            color = Color.White.copy(alpha = material.highlightTopAlpha * 0.5f),
                            size = Size(size.width, size.height / 2f)
                        )
                    }
                }
                drawContent()
            }
            // (4) bordure
            .border(material.borderWidth, borderBrush, shape)
    ) {
        val boxScope = this
        CompositionLocalProvider(LocalContentColor provides material.contentColor) {
            boxScope.content()
        }
    }
}
