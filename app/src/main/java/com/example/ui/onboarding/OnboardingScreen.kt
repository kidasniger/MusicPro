package com.example.ui.onboarding

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.MusicProBackground
import com.example.ui.theme.MusicProCyanLight
import com.example.ui.theme.MusicProCyanNeon
import com.example.ui.theme.MusicProCyanVibrant
import com.example.ui.theme.MusicProPrimaryGradient
import com.example.ui.theme.MusicProSuccess
import com.example.ui.theme.MusicProSurface
import com.example.ui.theme.MusicProSurfaceElevated
import com.example.ui.theme.MusicProSurfaceVariant
import com.example.ui.theme.MusicProTextMuted
import com.example.ui.theme.MusicProTextPrimary
import com.example.ui.theme.MusicProTextSecondary
import com.example.ui.theme.MusicProVioletGlow
import com.example.ui.theme.MusicProVioletLight
import com.example.ui.theme.MusicProVioletPastel
import com.example.ui.theme.MusicProVioletPrimary
import com.example.ui.theme.MusicProVioletVibrant
import kotlinx.coroutines.launch

data class OnboardingPageData(
    val title: String,
    val description: String,
    val badgeText: String,
    val icon: ImageVector,
    val accentColor: Color,
    val glowColor: Color
)

@Composable
fun OnboardingScreen(
    onFinishOnboarding: () -> Unit,
    modifier: Modifier = Modifier
) {
    val pages = listOf(
        OnboardingPageData(
            title = "100% Hors ligne",
            description = "Toute votre bibliothèque scannée localement. Aucun compte, aucun cloud. Vos fichiers audio restent sur votre appareil.",
            badgeText = "STOCKAGE LOCAL UNIQUEMENT",
            icon = Icons.Default.CloudOff,
            accentColor = MusicProVioletPrimary,
            glowColor = MusicProVioletGlow
        ),
        OnboardingPageData(
            title = "Paroles synchronisées",
            description = "Karaoké précis en direct, défilement avec surbrillance néon et ajustement du décalage. Import de fichiers .LRC ou génération intégrée.",
            badgeText = "FICHIERS .LRC & KARAOKÉ",
            icon = Icons.Default.Subtitles,
            accentColor = MusicProCyanNeon,
            glowColor = Color(0x6600D4FF)
        ),
        OnboardingPageData(
            title = "Playlists & Contrôles",
            description = "Créez, réordonnez et classez vos morceaux hors ligne. Profitez de la notification multimédia native et d'une ergonomie moderne.",
            badgeText = "LECTEUR PERSISTANT & PLAYLISTS",
            icon = Icons.AutoMirrored.Filled.QueueMusic,
            accentColor = MusicProVioletVibrant,
            glowColor = MusicProVioletGlow
        )
    )

    val pagerState = rememberPagerState(pageCount = { pages.size })
    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MusicProBackground)
            .testTag("onboarding_screen")
    ) {
        // Ambient background blur
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 100.dp)
                .size(300.dp)
                .blur(90.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            pages[pagerState.currentPage].accentColor.copy(alpha = 0.25f),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Bar: Logo on left, "Passer" (Skip) button on right
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .shadow(8.dp, RoundedCornerShape(12.dp), spotColor = MusicProVioletGlow)
                            .background(MusicProSurface, RoundedCornerShape(12.dp))
                            .border(1.dp, Color(0x26FFFFFF), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.musicpro_logo_cutout),
                            contentDescription = "Logo",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "MusicPro",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = MusicProTextPrimary
                    )
                }

                // Skip button
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0x1AFFFFFF),
                    border = BorderStroke(1.dp, Color(0x26FFFFFF)),
                    modifier = Modifier
                        .clickable { onFinishOnboarding() }
                        .testTag("onboarding_skip_button")
                ) {
                    Text(
                        text = "Passer",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MusicProTextSecondary,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                    )
                }
            }

            // Pager content
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) { pageIndex ->
                val page = pages[pageIndex]
                OnboardingPageView(page = page, pageIndex = pageIndex)
            }

            // Bottom Section: Page Indicators + Next/Start Button
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Page Indicator Dots
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 24.dp)
                ) {
                    pages.indices.forEach { index ->
                        val isSelected = pagerState.currentPage == index
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .height(8.dp)
                                .width(if (isSelected) 28.dp else 8.dp)
                                .background(
                                    if (isSelected) {
                                        MusicProPrimaryGradient
                                    } else {
                                        Brush.linearGradient(listOf(Color(0x33FFFFFF), Color(0x33FFFFFF)))
                                    },
                                    shape = RoundedCornerShape(4.dp)
                                )
                        )
                    }
                }

                val isLastPage = pagerState.currentPage == pages.size - 1

                // Primary Next / Commencer Button
                Button(
                    onClick = {
                        if (isLastPage) {
                            onFinishOnboarding()
                        } else {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    contentPadding = PaddingValues(),
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .shadow(16.dp, RoundedCornerShape(18.dp), spotColor = MusicProVioletGlow)
                        .background(MusicProPrimaryGradient, RoundedCornerShape(18.dp))
                        .testTag("onboarding_next_button")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = if (isLastPage) "Commencer" else "Suivant",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OnboardingPageView(
    page: OnboardingPageData,
    pageIndex: Int
) {
    val infiniteTransition = rememberInfiniteTransition(label = "iconFloat")
    val floatOffset by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "floatOffset"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Feature Hero Illustration Card
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .padding(bottom = 32.dp)
                .size(130.dp)
        ) {
            // Outer glowing background
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .blur(26.dp)
                    .background(page.glowColor, RoundedCornerShape(32.dp))
            )

            // Inner Glass Card with icon
            Box(
                modifier = Modifier
                    .size(116.dp)
                    .shadow(20.dp, RoundedCornerShape(32.dp), spotColor = page.glowColor)
                    .background(
                        Brush.linearGradient(
                            listOf(MusicProSurfaceVariant, MusicProSurface)
                        ),
                        RoundedCornerShape(32.dp)
                    )
                    .border(
                        1.5.dp,
                        Brush.linearGradient(
                            listOf(page.accentColor.copy(alpha = 0.8f), Color(0x26FFFFFF))
                        ),
                        RoundedCornerShape(32.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = page.icon,
                    contentDescription = null,
                    tint = page.accentColor,
                    modifier = Modifier.size(54.dp)
                )
            }
        }

        // Title
        Text(
            text = page.title,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MusicProTextPrimary,
            textAlign = TextAlign.Center,
            letterSpacing = (-0.5).sp
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Description
        Text(
            text = page.description,
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal,
            color = MusicProTextSecondary,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp,
            modifier = Modifier.padding(horizontal = 12.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Slide Specific Demo Element
        when (pageIndex) {
            0 -> {
                // Slide 0: Offline storage badge
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0x2610B981),
                    border = BorderStroke(1.dp, Color(0x4D10B981))
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .background(MusicProSuccess, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = page.badgeText,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MusicProSuccess,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }
            1 -> {
                // Slide 1: Karaoke LRC live preview mockup
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MusicProSurface),
                    border = BorderStroke(1.dp, Color(0x3322D3EE)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "♪ I look around and Sin City's cold and empty",
                            fontSize = 11.sp,
                            color = MusicProTextMuted,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0x338A2BE2),
                            border = BorderStroke(1.dp, MusicProVioletPrimary.copy(alpha = 0.5f))
                        ) {
                            Text(
                                text = "I said, ooh, I'm blinded by the lights ♪",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MusicProCyanNeon,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "No, I can't sleep until I feel your touch...",
                            fontSize = 11.sp,
                            color = MusicProTextMuted,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
            2 -> {
                // Slide 2: Playlist & offline controller preview mockup
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MusicProSurface),
                    border = BorderStroke(1.dp, Color(0x338A2BE2)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    Brush.linearGradient(
                                        listOf(MusicProVioletPrimary, MusicProCyanVibrant)
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.MusicNote,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Playlist: Nuit Néon",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MusicProTextPrimary
                            )
                            Text(
                                text = "12 morceaux • Widget d'accueil actif",
                                fontSize = 11.sp,
                                color = MusicProCyanLight
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.GraphicEq,
                            contentDescription = null,
                            tint = MusicProVioletPastel,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}
