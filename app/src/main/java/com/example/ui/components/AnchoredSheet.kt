package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FloatSpringSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.ui.components.glass.rememberAnimationsEnabled
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * Mise à jour 074 — feuille du bas à deux ancrages : mi-hauteur et plein écran.
 *
 * - poignée de 36×4 dp dans une zone de glissement de 48 dp de haut ;
 * - fermeture par glissement vers le bas (voire depuis le plein écran) ;
 * - fond voilé à 40 % : le contenu derrière reste visible, un tap ferme la feuille ;
 * - le contenu ne défile que lorsque la feuille est pleine ([isFull]), pour éviter
 *   les conflits de gestes entre le déplacement de la feuille et la liste.
 *
 * L'ouverture est animée depuis le bas ; [onDismissed] est appelé une fois la
 * fermeture terminée (glissement complet, tap sur le voile).
 */
@Composable
fun AnchoredSheet(
    onDismissed: () -> Unit,
    modifier: Modifier = Modifier,
    startFull: Boolean = false,
    scrimColor: Color = Color.Black.copy(alpha = 0.4f),
    content: @Composable ColumnScope.(isFull: Boolean) -> Unit
) {
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    val animationsEnabled = rememberAnimationsEnabled()

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val fullHeightPx = with(density) { maxHeight.toPx() }
        val halfFraction = AnchoredSheetSpec.HALF_FRACTION
        val initial = if (startFull) 1f else halfFraction

        var heightPx by remember { mutableFloatStateOf(0f) }
        val fraction = remember { Animatable(0f) }
        var entered by remember { mutableStateOf(false) }

        fun settle(velocityPxPerS: Float) {
            scope.launch {
                val velocityFraction =
                    velocityPxPerS / heightPx.coerceAtLeast(1f)
                val target = AnchoredSheetSpec.nearestAnchor(fraction.value, velocityFraction)
                fraction.animateTo(
                    target,
                    FloatSpringSpec(dampingRatio = 0.85f, stiffness = Spring.StiffnessMediumLow)
                )
                if (fraction.value <= AnchoredSheetSpec.CLOSED_EPSILON) {
                    onDismissed()
                }
            }
        }

        // Entrée : depuis le bas jusqu'à l'ancrage de départ.
        LaunchedEffect(heightPx, entered) {
            if (heightPx <= 0f || entered) return@LaunchedEffect
            entered = true
            fraction.animateTo(
                initial,
                if (animationsEnabled) {
                    tween(260)
                } else {
                    FloatSpringSpec(stiffness = Spring.StiffnessHigh)
                }
            )
        }

        // Fond voilé à 40 % : un tap ferme la feuille.
        val scrimAlpha = (fraction.value / halfFraction).coerceIn(0f, 1f)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .alpha(scrimAlpha)
                .background(scrimColor)
                .pointerInput(Unit) {
                    detectTapGestures { settle(0f) }
                }
        )

        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .fillMaxHeight()
                .onSizeChanged { heightPx = it.height.toFloat() }
                .offset {
                    IntOffset(0, ((1f - fraction.value) * heightPx).roundToInt())
                },
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Poignée 36×4 dp + zone de glissement 48 dp.
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(AnchoredSheetSpec.HANDLE_ZONE_DP.dp)
                        .draggable(
                            orientation = Orientation.Vertical,
                            state = rememberDraggableState { delta ->
                                scope.launch {
                                    fraction.snapTo(
                                        (fraction.value - delta / heightPx.coerceAtLeast(1f))
                                            .coerceIn(0f, 1f)
                                    )
                                }
                            },
                            onDragStopped = { velocity -> settle(velocity) }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(
                                width = AnchoredSheetSpec.HANDLE_WIDTH_DP.dp,
                                height = AnchoredSheetSpec.HANDLE_HEIGHT_DP.dp
                            )
                            .background(
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                RoundedCornerShape(2.dp)
                            )
                    )
                }

                val isFull = fraction.value >= AnchoredSheetSpec.FULL_THRESHOLD
                content(isFull)
            }
        }
    }
}

/** Constantes partagées de la feuille à ancrages, exposées pour les tests. */
object AnchoredSheetSpec {
    /** Ancrage « mi-hauteur », en fraction de la hauteur disponible. */
    const val HALF_FRACTION = 0.55f

    /** Au-delà de cette fraction, la feuille est considérée pleine. */
    const val FULL_THRESHOLD = 0.9f

    /** En dessous de cette fraction, la feuille est considérée fermée. */
    const val CLOSED_EPSILON = 0.02f

    /** Seuil de vitesse (fraction/s) qui force l'ancrage suivant dans la direction du geste. */
    const val VELOCITY_THRESHOLD = 1.2f

    /** Poignée : largeur 36 dp, hauteur 4 dp, zone tactile 48 dp. */
    const val HANDLE_WIDTH_DP = 36
    const val HANDLE_HEIGHT_DP = 4
    const val HANDLE_ZONE_DP = 48

    /**
     * Ancrage cible après un relâchement : l'ancrage le plus proche, sauf si la
     * vitesse dépasse [VELOCITY_THRESHOLD] — dans ce cas l'ancrage extrême dans la
     * direction du geste. Renvoie une fraction d'ancrage (0, [HALF_FRACTION] ou 1).
     */
    fun nearestAnchor(current: Float, velocityFraction: Float): Float {
        val anchors = floatArrayOf(0f, HALF_FRACTION, 1f)
        if (abs(velocityFraction) >= VELOCITY_THRESHOLD) {
            return if (velocityFraction > 0f) 1f else 0f
        }
        val projected = current + velocityFraction * 0.12f
        return anchors.minByOrNull { abs(it - projected) } ?: 0f
    }
}
