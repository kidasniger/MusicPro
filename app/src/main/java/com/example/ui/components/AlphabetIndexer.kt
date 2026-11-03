package com.example.ui.components

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.MusicProCyanNeon
import com.example.ui.theme.MusicProVioletGlow
import kotlin.math.floor

/**
 * Mise à jour 056 — index A–Z glissable affiché à droite des longues listes.
 * Une bulle de lettre flottante suit le doigt, la liste défile instantanément
 * vers la première entrée de la lettre, et une vibration très légère est
 * déclenchée à chaque changement de lettre. L'index est masqué si la liste
 * compte moins de [minItemsForIndex] éléments.
 */
const val minItemsForIndex = 40

/** Vibration très légère (12 ms) — tolérée même si le service est indisponible. */
internal fun lightHapticTick(context: Context) {
    runCatching {
        val vibrator: Vibrator? =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                (context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager)
                    ?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
        if (vibrator?.hasVibrator() == true) {
            vibrator.vibrate(VibrationEffect.createOneShot(12L, VibrationEffect.DEFAULT_AMPLITUDE))
        }
    }
}

@Composable
fun AlphabetIndexer(
    letters: List<Char>,
    currentLetter: () -> Char?,
    onLetterSelected: (Char) -> Unit,
    onDragEnded: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    var columnHeightPx by remember { mutableIntStateOf(0) }
    var isActive by remember { mutableStateOf(false) }

    fun letterAt(yPx: Float): Char? {
        if (letters.isEmpty() || columnHeightPx <= 0) return null
        val slot = columnHeightPx.toFloat() / letters.size
        val index = floor(yPx / slot).toInt().coerceIn(0, letters.size - 1)
        return letters[index]
    }

    Box(
        modifier = modifier
            .onSizeChanged { columnHeightPx = it.height }
            .pointerInput(letters) {
                awaitEachGesture {
                    awaitFirstDown(requireUnconsumed = false)
                    isActive = true
                    var lastLetter: Char? = null
                    while (true) {
                        val event = awaitPointerEvent()
                        val change = event.changes.firstOrNull() ?: break
                        if (!change.pressed) break
                        val letter = letterAt(change.position.y)
                        if (letter != null && letter != lastLetter) {
                            lastLetter = letter
                            lightHapticTick(context)
                            onLetterSelected(letter)
                        }
                        change.consume()
                    }
                    isActive = false
                    onDragEnded()
                }
            }
            .semantics { contentDescription = "Index alphabétique" },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            letters.forEach { letter ->
                val isCurrent = letter == currentLetter()
                Text(
                    text = letter.toString(),
                    fontSize = 10.sp,
                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                    color = if (isCurrent) MusicProCyanNeon
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 1.dp)
                )
            }
        }

        // Bulle de lettre flottante pendant le glissement.
        AnimatedVisibility(
            visible = isActive,
            enter = scaleIn() + fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .padding(end = 30.dp)
                    .clip(CircleShape)
                    .background(MusicProVioletGlow)
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = (currentLetter() ?: letters.firstOrNull() ?: 'A').toString(),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}
