package com.example.ui.settings

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.LibraryMusic
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.components.glass.GlassBackdropHost
import com.example.ui.components.glass.GlassStyle
import com.example.ui.components.glass.GlassSurface
import com.example.ui.components.glass.LocalGlassColorStrength
import com.example.ui.components.glass.LocalGlassFrostDp
import com.example.ui.components.glass.LocalGlassMaterial
import com.example.ui.components.glass.LocalGlassStyle
import com.example.ui.components.glass.LocalGlassTintHue
import com.example.ui.components.glass.glassActiveCapsuleColor
import com.example.ui.components.glass.glassActiveTabContentColor
import com.example.ui.theme.MusicProCyanNeon
import com.example.ui.theme.MusicProVioletPrimary

/** Hauteur de l'aperçu (mise à jour 010). */
private val PreviewHeight = 120.dp

/**
 * Aperçu en direct du verre liquide : fond dégradé orange→turquoise rayé, surmonté d'un faux
 * mini-lecteur et d'une fausse barre de navigation, tous deux de vraies [GlassSurface].
 *
 * Les réglages sont fournis par [state] (état hissé dans le ViewModel) via leurs propres
 * CompositionLocal : l'aperçu suit chaque curseur sans attendre le DataStore, et le reste de
 * l'application n'est pas affecté. Le repli sans flou (API < 31, niveau « Le plus rapide »,
 * givre sous 2 dp) est celui de [GlassSurface].
 */
@Composable
fun GlassPreviewCard(
    state: GlassPreviewState,
    modifier: Modifier = Modifier
) {
    val style = remember(state.filmOpacity, state.activeTabOpacity) {
        GlassStyle(
            filmOpacity = state.filmOpacity,
            activeTabOpacity = state.activeTabOpacity
        )
    }
    val shape = RoundedCornerShape(20.dp)
    val description = stringResource(R.string.glass_preview_description)

    CompositionLocalProvider(
        LocalGlassMaterial provides state.material,
        LocalGlassColorStrength provides state.colorStrength,
        LocalGlassFrostDp provides state.frostDp,
        LocalGlassTintHue provides state.tintHue,
        LocalGlassStyle provides style
    ) {
        GlassBackdropHost(
            modifier = modifier
                .fillMaxWidth()
                .height(PreviewHeight)
                .clip(shape)
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, shape)
                .clearAndSetSemantics { contentDescription = description }
                .testTag("glass_preview"),
            background = { PreviewBackdrop() },
            foreground = {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    PreviewMiniPlayer(Modifier.fillMaxWidth().height(44.dp))
                    PreviewNavBar(Modifier.fillMaxWidth().height(50.dp))
                }
            }
        )
    }
}

/** Dégradé orange→turquoise barré de rayures diagonales : le flou et la teinte y sont bien visibles. */
@Composable
private fun PreviewBackdrop() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        drawRect(
            brush = Brush.horizontalGradient(
                listOf(Color(0xFFFF8A1F), Color(0xFF14C8BE))
            )
        )
        val stripeWidth = 7.dp.toPx()
        val stripeGap = 22.dp.toPx()
        val h = size.height
        var x = -h
        while (x < size.width) {
            drawLine(
                color = Color.White.copy(alpha = 0.28f),
                start = Offset(x, h),
                end = Offset(x + h, 0f),
                strokeWidth = stripeWidth
            )
            x += stripeGap
        }
    }
}

@Composable
private fun PreviewMiniPlayer(modifier: Modifier = Modifier) {
    GlassSurface(
        modifier = modifier,
        shape = RoundedCornerShape(22.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        Brush.linearGradient(listOf(MusicProVioletPrimary, MusicProCyanNeon))
                    )
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.glass_preview_track_title),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = stringResource(R.string.glass_preview_track_artist),
                    fontSize = 10.sp,
                    color = LocalContentColor.current.copy(alpha = 0.7f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Icon(
                imageVector = Icons.Filled.PlayArrow,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun PreviewNavBar(modifier: Modifier = Modifier) {
    val activeCapsule = glassActiveCapsuleColor()
    GlassSurface(
        modifier = modifier,
        shape = RoundedCornerShape(25.dp)
    ) {
        val baseContent = LocalContentColor.current
        val selectedContent = glassActiveTabContentColor(activeCapsule, baseContent)
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(3.dp)
        ) {
            PreviewNavTab(
                icon = Icons.Filled.Home,
                labelRes = R.string.nav_home,
                selected = true,
                activeCapsule = activeCapsule,
                tint = selectedContent,
                modifier = Modifier.weight(1f)
            )
            PreviewNavTab(
                icon = Icons.Outlined.LibraryMusic,
                labelRes = R.string.nav_library,
                selected = false,
                activeCapsule = activeCapsule,
                tint = baseContent.copy(alpha = 0.70f),
                modifier = Modifier.weight(1f)
            )
            PreviewNavTab(
                icon = Icons.Outlined.Search,
                labelRes = R.string.nav_search,
                selected = false,
                activeCapsule = activeCapsule,
                tint = baseContent.copy(alpha = 0.70f),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun PreviewNavTab(
    icon: ImageVector,
    labelRes: Int,
    selected: Boolean,
    activeCapsule: Color,
    tint: Color,
    modifier: Modifier = Modifier
) {
    val capsuleShape = RoundedCornerShape(22.dp)
    Box(
        modifier = modifier
            .fillMaxHeight()
            .clip(capsuleShape)
            .then(
                if (selected) {
                    Modifier
                        .background(activeCapsule)
                        .border(1.dp, Color.White.copy(alpha = 0.25f), capsuleShape)
                } else {
                    Modifier
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(1.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = stringResource(labelRes),
                color = tint,
                fontSize = 9.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
