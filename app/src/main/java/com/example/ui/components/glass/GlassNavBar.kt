package com.example.ui.components.glass

import android.provider.Settings
import androidx.annotation.StringRes
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Un onglet de la barre de navigation en verre. */
@Immutable
data class GlassNavItem(
    val key: String,
    @StringRes val labelRes: Int,
    /** Icône de l'onglet inactif (contour). */
    val icon: ImageVector,
    /** Icône de l'onglet actif (plein, donc visuellement plus grasse). */
    val selectedIcon: ImageVector,
    val testTag: String
)

/** Faux si l'utilisateur a désactivé les animations système (« Réduire les animations »). */
@Composable
fun rememberAnimationsEnabled(): Boolean {
    val context = LocalContext.current
    return remember(context) {
        Settings.Global.getFloat(
            context.contentResolver,
            Settings.Global.ANIMATOR_DURATION_SCALE,
            1f
        ) != 0f
    }
}

/**
 * Barre de navigation flottante en pilule de verre.
 * Marges : 16 dp latérales, 12 dp au-dessus de la barre système. Hauteur : 64 dp.
 */
@Composable
fun GlassNavBar(
    items: List<GlassNavItem>,
    selectedKey: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val animationsEnabled = rememberAnimationsEnabled()
    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    // Capsule « Active tab » : plus claire que le verre environnant.
    val activeCapsule = if (isDark) Color.White.copy(alpha = 0.18f) else Color.White.copy(alpha = 0.80f)

    GlassSurface(
        modifier = modifier
            .navigationBarsPadding()
            .padding(start = 16.dp, end = 16.dp, bottom = 12.dp)
            .fillMaxWidth()
            .height(64.dp),
        shape = RoundedCornerShape(32.dp),
        material = GlassMaterials.regular()
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(4.dp)
                .selectableGroup(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { item ->
                GlassNavTab(
                    item = item,
                    selected = item.key == selectedKey,
                    animationsEnabled = animationsEnabled,
                    activeCapsule = activeCapsule,
                    onClick = { onSelect(item.key) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun GlassNavTab(
    item: GlassNavItem,
    selected: Boolean,
    animationsEnabled: Boolean,
    activeCapsule: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val capsuleShape = RoundedCornerShape(28.dp)
    val capsuleAlpha by animateFloatAsState(
        targetValue = if (selected) 1f else 0f,
        animationSpec = tween(durationMillis = if (animationsEnabled) 220 else 0),
        label = "glass_nav_capsule"
    )
    val baseColor = LocalContentColor.current
    // Onglet actif : pleine opacité ; autres onglets : 70 %.
    val tint = if (selected) baseColor else baseColor.copy(alpha = 0.70f)

    Box(
        modifier = modifier
            .fillMaxHeight()
            .clip(capsuleShape)
            .selectable(selected = selected, onClick = onClick, role = Role.Tab)
            .testTag(item.testTag),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(capsuleShape)
                .background(activeCapsule.copy(alpha = activeCapsule.alpha * capsuleAlpha))
                .border(
                    width = 1.dp,
                    color = Color.White.copy(alpha = 0.25f * capsuleAlpha),
                    shape = capsuleShape
                )
        )
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Icon(
                imageVector = if (selected) item.selectedIcon else item.icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(22.dp)
            )
            Text(
                text = stringResource(item.labelRes),
                color = tint,
                fontSize = 10.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
