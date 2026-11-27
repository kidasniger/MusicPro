package com.example.ui.artwork

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.ui.theme.MusicProTokens

/** Résolution de chargement d'une pochette via les requêtes de [ArtworkRequests]. */
enum class ArtworkSize { GRID, PLAYER }

/**
 * Composant pochette unique de la refonte minimaliste.
 *
 * Un seul point de code pour la chaîne d'affichage des pochettes : image si disponible,
 * repli icône sur fond de surface si le fichier est absent ou le décodage échoue. Les trois
 * états sont garantis sur tous les écrans (bibliothèque, lecteur, mini-lecteur, détails),
 * ce qui corrige structurellement les régressions du type « pochettes qui ne s'affichent plus ».
 *
 * Le [modifier] fourni (taille, clip, bordure) s'applique aux deux branches, image et repli :
 * le gabarit de l'écran ne dépend jamais de l'état du chargement.
 */
@Composable
fun MusicProArtwork(
    model: Any?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    size: ArtworkSize = ArtworkSize.GRID,
    fallbackIcon: ImageVector = Icons.Filled.MusicNote,
    fallbackIconSize: Dp = MusicProTokens.Size.CoverIcon
) {
    var failed by remember(model) { mutableStateOf(false) }
    val context = LocalContext.current

    if (model != null && !failed) {
        val request = remember(model, size, context) {
            when (size) {
                ArtworkSize.GRID -> context.artworkGridRequest(model)
                ArtworkSize.PLAYER -> context.artworkPlayerRequest(model)
            }
        }
        AsyncImage(
            model = request,
            contentDescription = contentDescription,
            contentScale = ContentScale.Crop,
            onError = { failed = true },
            modifier = modifier
        )
    } else {
        Box(
            modifier = modifier.background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = fallbackIcon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(fallbackIconSize)
            )
        }
    }
}
