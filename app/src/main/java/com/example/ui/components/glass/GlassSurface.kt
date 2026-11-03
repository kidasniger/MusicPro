package com.example.ui.components.glass

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.example.data.preferences.GlassMaterial
import com.example.data.preferences.clampGlassFrostDp
import com.example.data.preferences.glassBlurActive
import com.example.data.preferences.glassSaturation
import com.example.data.preferences.glassScaledFilmAlpha
import com.example.data.preferences.glassTintAlpha
import com.example.ui.theme.GlassMaxReadableFilmAlpha
import com.example.ui.theme.readableGlassFilmAlpha
import kotlin.math.roundToInt

/**
 * Surface « verre liquide » unique de l'application. Le rendu dépend du niveau [GlassMaterial]
 * choisi dans les réglages ([LocalGlassMaterial]) :
 *
 *  - FASTEST  : aplat translucide ([GlassFlatFallbackAlpha]), fine bordure, aucun flou.
 *  - BALANCED : flou du rayon « Givre », film plus dense, un seul reflet plat, fine bordure.
 *  - QUALITY  : flou du rayon « Givre », reflet dégradé, bordure lumineuse en dégradé,
 *               bruit à [GlassNoiseOpacity] et ombre portée douce.
 *
 * Le rayon du flou est celui du curseur « Givre » ([LocalGlassFrostDp], 0 à 24 dp, 6 dp par défaut).
 * Sous 2 dp, le flou est désactivé : la surface se replie sur l'aplat translucide et le fond
 * n'est pas enregistré (voir [glassBlurActive]), ce qui économise le GPU.
 *
 * Quand le flou est actif, le fond qui transparaît passe aussi par un filtre de saturation réglé
 * par le curseur « Couleur du verre » ([LocalGlassColorStrength], voir [glassSaturation]) :
 * 0,6 à 0 % (neutre, désaturé) jusqu'à 1,4 à 100 % (couleurs de la pochette amplifiées).
 * Le film, les reflets, la bordure et le contenu ne sont pas affectés.
 *
 * Film : l'opacité du film de la recette du niveau est multipliée par le curseur « Film »
 * ([LocalGlassStyle], voir [glassScaledFilmAlpha]) : 50 % (défaut) la laisse inchangée, 0 % la
 * supprime, 100 % la double (bornée à 1).
 *
 * Mise à jour 084 — lisibilité : l'opacité finale remonte automatiquement par pas de 5 % (jusqu'à
 * [GlassMaxReadableFilmAlpha]) si le pire cas derrière le verre (blanc ou noir pur) tombait sous
 * 4,5:1 de contraste avec le contenu. L'interrupteur « Texte plus lisible » ([GlassStyle.readableText])
 * force directement un film quasi opaque, et « Réduire la transparence » suit le réglage système :
 * verre en aplat dense, sans flou temps réel.
 *
 * Mise à jour 086 — performance : le flou est un calque partagé, limité par un budget d'écran
 * ([LocalGlassBlurBudget]) — jamais un flou par ligne. [blurPriority] décide qui garde le flou
 * quand le budget est plein (la barre flottante prime : [GlassBlurPriorityFloatingBar]). De plus,
 * la version floutée du fond n'est ré-enregistrée que si le fond ([GlassBackdropState.generation]),
 * la position ou la taille ont changé : quand rien ne bouge, la surface réutilise son calque.
 *
 * Teinte : si une teinte est choisie ([LocalGlassTintHue], `null` = Aucune), un voile de cette
 * couleur est posé juste après le film, avec une opacité de film x 0,5 (voir [glassTintAlpha]),
 * le film étant celui obtenu après réglage du curseur. Il s'applique à tous les niveaux, y compris
 * « Le plus rapide » (aplat), sans coût GPU supplémentaire : c'est un simple aplat.
 *
 * Sans [GlassBackdropHost] parent ou avant Android 12, tous les niveaux se replient sur l'aplat.
 * En mode aplat, rien n'est enregistré ni mesuré.
 */
@Composable
fun GlassSurface(
    modifier: Modifier = Modifier,
    shape: Shape = CircleShape,
    material: GlassSpec = GlassMaterials.regular(),
    blurPriority: Int = GlassBlurPriorityDefault,
    content: @Composable BoxScope.() -> Unit
) {
    val backdrop = LocalGlassBackdrop.current
    val level = LocalGlassMaterial.current
    val colorStrength = LocalGlassColorStrength.current
    val frostDp = clampGlassFrostDp(LocalGlassFrostDp.current)
    val tintHue = LocalGlassTintHue.current
    val style = LocalGlassStyle.current
    val blurLayer = rememberGraphicsLayer()
    var origin by remember { mutableStateOf(Offset.Zero) }
    // Mise à jour 086 — empreinte du dernier enregistrement du calque flouté :
    // [0] = génération du fond, [1] = position relative + taille.
    val recordStamp = remember { longArrayOf(-1L, 0L) }

    // Mise à jour 084 — « Réduire la transparence » suit le réglage d'accessibilité du système.
    val context = LocalContext.current
    val reduceTransparency = style.followReduceTransparency &&
        remember(context) { isSystemReduceTransparencyEnabled(context) }

    // Envie de flou avant arbitration du budget (mise à jour 086).
    val wantsBlur = !LocalReduceAnimations.current && !reduceTransparency && backdrop != null &&
        glassBlurActive(level, glassBlurSupported, frostDp) &&
        material.blurRadius > 0.dp

    // Mise à jour 086 — budget de flou partagé : un seul calque flouté par écran au maximum.
    // La demande passe par un SideEffect après chaque recomposition : `request` est idempotent
    // (aucune mutation si l'état ne change pas), donc aucune boucle de recomposition. La lecture
    // de `version` en composition réveille la surface quand un slot se libère.
    val blurBudget = LocalGlassBlurBudget.current
    val blurClaimId = remember(blurBudget) { blurBudget.newClaimId() }
    if (wantsBlur) {
        // Lecture observée : notifie cette surface des attributions/vols/libérations.
        blurBudget.version
        SideEffect { blurBudget.request(blurClaimId, blurPriority) }
    }
    DisposableEffect(blurBudget, blurClaimId) {
        onDispose { blurBudget.release(blurClaimId) }
    }
    val blurGranted = wantsBlur && blurBudget.isGranted(blurClaimId)

    val blurOn = wantsBlur && blurGranted
    val quality = blurOn && level == GlassMaterial.QUALITY

    val radiusPx = with(LocalDensity.current) { frostDp.dp.toPx() }
    // Saturation du fond : recalculée seulement quand le curseur bouge.
    val saturationFilter = remember(colorStrength) {
        ColorFilter.colorMatrix(
            ColorMatrix().apply { setToSaturation(glassSaturation(colorStrength)) }
        )
    }
    // Couleur du voile teinté : recalculée seulement quand la teinte change (null = Aucune).
    val tintColor = remember(tintHue) { tintHue?.let(::glassTintColor) }
    SideEffect {
        blurLayer.renderEffect =
            if (blurOn) BlurEffect(radiusPx, radiusPx, TileMode.Clamp) else null
        blurLayer.colorFilter = if (blurOn) saturationFilter else null
    }

    // Film de la recette selon le niveau, puis curseur « Film » (50 % = inchangé).
    val baseFilmAlpha = when {
        !blurOn -> material.fallbackFilmAlpha
        level == GlassMaterial.BALANCED -> maxOf(material.filmAlpha, GlassBalancedMinFilmAlpha)
        else -> material.filmAlpha
    }
    val scaledFilmAlpha = glassScaledFilmAlpha(baseFilmAlpha, style.filmOpacity)
    // Mise à jour 084 — filet de sécurité lisibilité : le pire cas derrière le verre (blanc ou
    // noir pur) doit rester contrasté à 4,5:1 ; l'opacité remonte par pas de 5 % jusqu'à 85 %.
    // « Texte plus lisible » force directement le film quasi opaque.
    val filmAlpha = if (style.readableText) {
        maxOf(scaledFilmAlpha, GlassMaxReadableFilmAlpha)
    } else {
        readableGlassFilmAlpha(material.filmColor, scaledFilmAlpha, material.contentColor)
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
                // (1) copie du fond, floutée et resaturée par les effets du calque.
                // Mise à jour 086 — on ne ré-enregistre que si le fond a changé (génération),
                // si la position relative ou si la taille ont changé : quand rien ne bouge,
                // le calque déjà flouté est réutilisé tel quel.
                if (blurOn && backdrop != null) {
                    val dx = backdrop.origin.x - origin.x
                    val dy = backdrop.origin.y - origin.y
                    val stamp = (dx.toRawBits().toLong() shl 32) xor dy.toRawBits().toLong() xor
                        (size.width.roundToInt().toLong() * 31) xor
                        (size.height.roundToInt().toLong() * 61)
                    if (backdrop.generation != recordStamp[0] || stamp != recordStamp[1]) {
                        blurLayer.record(
                            density = this,
                            layoutDirection = layoutDirection,
                            size = IntSize(size.width.roundToInt(), size.height.roundToInt())
                        ) {
                            translate(left = dx, top = dy) { drawLayer(backdrop.layer) }
                        }
                        recordStamp[0] = backdrop.generation
                        recordStamp[1] = stamp
                    }
                    drawLayer(blurLayer)
                }
                // (2) film (ou aplat translucide quand il n'y a pas de flou)
                drawRect(material.filmColor.copy(alpha = filmAlpha))
                // (2 bis) voile teinté : alpha = film x 0,5 (rien si Aucune teinte)
                if (tintColor != null) {
                    drawRect(tintColor.copy(alpha = glassTintAlpha(filmAlpha)))
                }
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
