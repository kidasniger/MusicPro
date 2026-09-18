package com.example.ui.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.MusicProBackground
import com.example.ui.theme.MusicProCyanNeon
import com.example.ui.theme.MusicProCyanVibrant
import com.example.ui.theme.MusicProPrimaryGradient
import com.example.ui.theme.MusicProSuccess
import com.example.ui.theme.MusicProSurface
import com.example.ui.theme.MusicProTextMuted
import com.example.ui.theme.MusicProTextPrimary
import com.example.ui.theme.MusicProVioletGlow
import com.example.ui.theme.MusicProVioletPastel
import com.example.ui.theme.MusicProVioletPrimary
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first

@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit,
    modifier: Modifier = Modifier,
    isReadyToNavigate: Boolean = true,
    splashDurationMillis: Long = 1500L
) {
    val currentOnSplashFinished by rememberUpdatedState(onSplashFinished)
    val currentIsReady by rememberUpdatedState(isReadyToNavigate)

    // Scale and Alpha animation for logo entry
    val scale = remember { Animatable(0.6f) }
    val alpha = remember { Animatable(0f) }

    // Pulsing glow animation
    val infiniteTransition = rememberInfiniteTransition(label = "pulseGlow")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    val shimmerOffset by infiniteTransition.animateFloat(
        initialValue = -60f,
        targetValue = 60f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerOffset"
    )

    LaunchedEffect(Unit) {
        // Run entry animation and wait for exact splash duration (1.5s)
        scale.animateTo(
            targetValue = 1.0f,
            animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing)
        )
        alpha.animateTo(
            targetValue = 1.0f,
            animationSpec = tween(durationMillis = 400, easing = LinearEasing)
        )
        delay(splashDurationMillis - 600L) // Remaining time to reach 1.5s exactly

        // Ensure preferences and readiness check are completed before navigating
        snapshotFlow { currentIsReady }.first { it }

        currentOnSplashFinished()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MusicProBackground)
            .testTag("splash_screen"),
        contentAlignment = Alignment.Center
    ) {
        // Neon ambient glow rendered via Canvas to avoid edge clipping
        Canvas(modifier = Modifier.fillMaxSize()) {
            val centerOffset = Offset(size.width / 2f, size.height / 2f)
            val glowRadius = size.minDimension * 0.55f * pulseScale
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        MusicProVioletPrimary.copy(alpha = 0.40f),
                        MusicProCyanVibrant.copy(alpha = 0.20f),
                        Color.Transparent
                    ),
                    center = centerOffset,
                    radius = glowRadius
                ),
                center = centerOffset,
                radius = glowRadius
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 24.dp)
        ) {
            // Animated Logo with smooth radial glow and crisp clean icon
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .scale(scale.value * pulseScale)
                    .alpha(alpha.value)
                    .testTag("splash_logo")
            ) {
                // Soft radial glow behind the logo with vibrant neon diffusion
                Canvas(modifier = Modifier.size(190.dp)) {
                    val centerOffset = Offset(size.width / 2f, size.height / 2f)
                    val glowRadius = size.width / 2f
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                MusicProCyanNeon.copy(alpha = 0.65f),
                                MusicProVioletPrimary.copy(alpha = 0.45f),
                                Color.Transparent
                            ),
                            center = centerOffset,
                            radius = glowRadius
                        ),
                        center = centerOffset,
                        radius = glowRadius
                    )
                }

                Image(
                    painter = painterResource(id = R.drawable.musicpro_logo_boosted),
                    contentDescription = "Logo MusicPro",
                    modifier = Modifier.size(142.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // App Name with high-contrast typography
            Text(
                text = "MusicPro",
                fontSize = 34.sp,
                fontWeight = FontWeight.Bold,
                color = MusicProTextPrimary,
                letterSpacing = (-0.5).sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Shimmer loading bar
            Box(
                modifier = Modifier
                    .width(130.dp)
                    .height(3.dp)
                    .background(Color(0x26FFFFFF), RoundedCornerShape(2.dp)),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .offset(x = shimmerOffset.dp)
                        .width(50.dp)
                        .height(3.dp)
                        .background(MusicProPrimaryGradient, RoundedCornerShape(2.dp))
                )
            }
        }
    }
}
