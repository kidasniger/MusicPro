package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.MusicProCyanNeon

/**
 * Mise à jour 057 — squelettes de chargement (shimmer 1,2 s) reprenant la forme
 * des lignes de morceaux et des pochettes, en remplacement des cercles de
 * progression. Le paramètre [reduceAnimations] (Réglages › Apparence) fige le
 * dégradé pour les utilisateurs sensibles aux animations.
 */

/** Dégradé animé de gauche à droite, cycle complet de 1 200 ms. */
fun Modifier.shimmerPlaceholder(reduceAnimations: Boolean): Modifier = composed {
    val base = MaterialTheme.colorScheme.surfaceVariant
    if (reduceAnimations) {
        this.background(base.copy(alpha = 0.7f))
    } else {
        val transition = rememberInfiniteTransition(label = "skeleton_shimmer")
        val progress by transition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1200, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "shimmer_progress"
        )
        val shimmerWidth = 600f
        this.background(
            Brush.linearGradient(
                colors = listOf(
                    base.copy(alpha = 0.45f),
                    base.copy(alpha = 0.95f),
                    base.copy(alpha = 0.45f)
                ),
                start = Offset(progress * 3f * shimmerWidth - shimmerWidth, 0f),
                end = Offset(progress * 3f * shimmerWidth, 0f)
            )
        )
    }
}

/** Ligne de morceau squelettique : pochette carrée + deux barres de texte. */
@Composable
fun SkeletonTrackRow(reduceAnimations: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(RoundedCornerShape(10.dp))
                .shimmerPlaceholder(reduceAnimations)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.62f)
                    .height(13.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .shimmerPlaceholder(reduceAnimations)
            )
            Spacer(modifier = Modifier.height(7.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.38f)
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp))
                    .shimmerPlaceholder(reduceAnimations)
            )
        }
    }
}

/** Colonne de [rows] lignes de morceaux squelettiques. */
@Composable
fun SkeletonTracksList(rows: Int = 8, reduceAnimations: Boolean = false) {
    Column {
        repeat(rows) { SkeletonTrackRow(reduceAnimations = reduceAnimations) }
    }
}

/** Pochette d'album squelettique (carrée, coins 12 dp — cf. mise à jour 052). */
@Composable
fun SkeletonAlbumCard(reduceAnimations: Boolean) {
    Column(modifier = Modifier.padding(2.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .clip(RoundedCornerShape(12.dp))
                .shimmerPlaceholder(reduceAnimations)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(11.dp)
                .clip(RoundedCornerShape(5.dp))
                .shimmerPlaceholder(reduceAnimations)
        )
        Spacer(modifier = Modifier.height(5.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth(0.5f)
                .height(9.dp)
                .clip(RoundedCornerShape(4.dp))
                .shimmerPlaceholder(reduceAnimations)
        )
    }
}

/**
 * Bandeau « 128 morceaux trouvés… » pendant le scan initial : compteur animé
 * (chaque changement de valeur glisse verticalement), navigation possible
 * pendant l'analyse.
 */
@Composable
fun ScanProgressBanner(
    foundCount: Int,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.GraphicEq,
                contentDescription = null,
                tint = MusicProCyanNeon,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            AnimatedContent(
                targetState = foundCount,
                transitionSpec = {
                    (slideInVertically(animationSpec = tween(250)) { it / 2 } + fadeIn())
                        .togetherWith(fadeOut(animationSpec = tween(150)))
                },
                label = "scan_counter"
            ) { count ->
                Text(
                    text = stringResource(R.string.scan_banner_tracks_found, count),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }
    }
}
