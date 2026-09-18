package com.example.ui.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
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
        // Neon ambient glow in the background
        Box(
            modifier = Modifier
                .size(280.dp)
                .blur(70.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            MusicProVioletPrimary.copy(alpha = 0.35f),
                            MusicProCyanVibrant.copy(alpha = 0.15f),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                )
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 24.dp)
        ) {
            // Animated Logo Card
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .scale(scale.value * pulseScale)
                    .alpha(alpha.value)
                    .testTag("splash_logo")
            ) {
                // Outer Glow halo
                Box(
                    modifier = Modifier
                        .size(136.dp)
                        .blur(30.dp)
                        .background(
                            Brush.linearGradient(
                                listOf(MusicProVioletPrimary, MusicProCyanNeon)
                            ),
                            shape = RoundedCornerShape(36.dp)
                        )
                )

                // Glass container with border
                Box(
                    modifier = Modifier
                        .size(112.dp)
                        .shadow(24.dp, RoundedCornerShape(32.dp), spotColor = MusicProVioletGlow)
                        .background(MusicProSurface, RoundedCornerShape(32.dp))
                        .border(
                            width = 1.5.dp,
                            brush = Brush.linearGradient(
                                listOf(MusicProVioletPrimary, MusicProCyanNeon)
                            ),
                            shape = RoundedCornerShape(32.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.musicpro_logo_cutout),
                        contentDescription = "Logo MusicPro",
                        modifier = Modifier.size(76.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // App Name with Neon Gradient Appearance
            Text(
                text = "MusicPro",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = MusicProTextPrimary,
                letterSpacing = (-0.5).sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Offline Status Badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .shadow(6.dp, CircleShape, spotColor = MusicProSuccess)
                        .background(MusicProSuccess, CircleShape)
                )

                Spacer(modifier = Modifier.width(6.dp))

                Text(
                    text = "100% HORS LIGNE",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MusicProSuccess,
                    letterSpacing = 1.5.sp
                )
            }

            Spacer(modifier = Modifier.height(36.dp))

            // Shimmer loading bar
            Box(
                modifier = Modifier
                    .width(120.dp)
                    .height(3.dp)
                    .background(Color(0x26FFFFFF), RoundedCornerShape(2.dp)),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .offset(x = shimmerOffset.dp)
                        .width(48.dp)
                        .height(3.dp)
                        .background(MusicProPrimaryGradient, RoundedCornerShape(2.dp))
                )
            }
        }

        // Bottom version text
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "VERSION 1.0.0 • OFFLINE FIRST",
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = MusicProTextMuted.copy(alpha = 0.6f),
                letterSpacing = 1.2.sp
            )
        }
    }
}
