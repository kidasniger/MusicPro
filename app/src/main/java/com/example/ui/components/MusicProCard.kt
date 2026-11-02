package com.example.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import com.example.data.preferences.AppStyle
import com.example.ui.components.glass.rememberAnimationsEnabled
import com.example.ui.theme.LocalAppStyle
import com.example.ui.theme.MusicProTokens
import com.example.ui.theme.MusicProVioletGlow
import com.example.ui.theme.MusicProVioletPrimary

/**
 * Carte de référence de MusicPro (mise à jour 019).
 *
 *  - Minimal : contour 1 dp (`outline`), aucune ombre au repos. La carte ne s'élève que lorsqu'on la
 *    touche (0 → 3 dp en 120 ms, instantané avec « Réduire les animations »), en clair comme en sombre.
 *  - Verre : contour fin, fond légèrement translucide.
 *  - Néon : look historique (lueur violette et contour violet).
 *
 * Le toucher est observé sans être consommé : les boutons, curseurs et défilements à l'intérieur de la
 * carte continuent de fonctionner normalement.
 */
@Composable
fun MusicProCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(MusicProTokens.Radius.Lg),
    content: @Composable ColumnScope.() -> Unit
) {
    val style = LocalAppStyle.current
    val scheme = MaterialTheme.colorScheme
    val animationsEnabled = rememberAnimationsEnabled()
    var pressed by remember { mutableStateOf(false) }

    val liftTarget = if (style == AppStyle.MINIMAL && pressed) {
        MusicProTokens.Elevation.Raised
    } else {
        MusicProTokens.Elevation.None
    }
    val elevation by animateDpAsState(
        targetValue = liftTarget,
        animationSpec = tween(durationMillis = if (animationsEnabled) MusicProTokens.Duration.Fast else 0),
        label = "card_lift"
    )

    val borderColor = when (style) {
        AppStyle.NEON -> MusicProVioletPrimary.copy(alpha = 0.35f)
        else -> scheme.outline
    }
    val containerColor = when (style) {
        AppStyle.GLASS -> scheme.surface.copy(alpha = 0.92f)
        else -> scheme.surface
    }
    val neonGlow = if (style == AppStyle.NEON) {
        Modifier.shadow(MusicProTokens.Elevation.NeonGlow, shape, spotColor = MusicProVioletGlow)
    } else {
        Modifier
    }

    Surface(
        modifier = modifier
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent(PointerEventPass.Initial)
                        pressed = event.changes.any { it.pressed }
                    }
                }
            }
            .then(neonGlow),
        shape = shape,
        color = containerColor,
        shadowElevation = elevation,
        border = BorderStroke(MusicProTokens.Size.Stroke, borderColor)
    ) {
        Column(content = content)
    }
}
