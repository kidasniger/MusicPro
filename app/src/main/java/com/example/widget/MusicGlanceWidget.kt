package com.example.widget

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.LocalSize
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.actionSendBroadcast
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxHeight
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.example.MainActivity
import com.example.R
import com.example.playback.MusicPlaybackService

/**
 * Widget d'écran d'accueil Glance MusicPro.
 * Prend en charge les formats Petit (compact) et Moyen (étendu).
 */
class MusicGlanceWidget : GlanceAppWidget() {

    companion object {
        val SMALL_SIZE = DpSize(110.dp, 90.dp)
        val MEDIUM_SIZE = DpSize(240.dp, 90.dp)

        val BackgroundDark = Color(0xFF130924)
        val SurfaceCard = Color(0xFF1E1338)
        val CyanNeon = Color(0xFF00D4FF)
        val VioletPrimary = Color(0xFF8A2BE2)
        val TextPrimary = Color(0xFFFFFFFF)
        val TextSecondary = Color(0xFFB0A8C4)
    }

    override val sizeMode: SizeMode = SizeMode.Responsive(
        setOf(SMALL_SIZE, MEDIUM_SIZE)
    )

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val size = LocalSize.current
            val state = MusicWidgetState.load(context)
            val artBitmap = MusicWidgetState.loadBitmap(context)

            val componentName = android.content.ComponentName(context, MainActivity::class.java)

            Box(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(ColorProvider(BackgroundDark))
                    .cornerRadius(18.dp)
                    .padding(8.dp)
                    .clickable(actionStartActivity(componentName)),
                contentAlignment = Alignment.Center
            ) {
                if (size.width < 220.dp) {
                    SmallWidgetContent(
                        context = context,
                        state = state,
                        artBitmap = artBitmap
                    )
                } else {
                    MediumWidgetContent(
                        context = context,
                        state = state,
                        artBitmap = artBitmap
                    )
                }
            }
        }
    }
}

/**
 * Disposition compacte (Petit widget : ~2x1 ou 2x2)
 */
@androidx.compose.runtime.Composable
fun SmallWidgetContent(
    context: Context,
    state: MusicWidgetState,
    artBitmap: Bitmap?
) {
    Column(
        modifier = GlanceModifier.fillMaxSize().padding(horizontal = 4.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // En-tête : Pochette + Titre/Artiste
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Pochette
            WidgetAlbumArt(
                artBitmap = artBitmap,
                size = 36.dp
            )

            Spacer(modifier = GlanceModifier.width(8.dp))

            Column(modifier = GlanceModifier.defaultWeight()) {
                Text(
                    text = state.title,
                    style = TextStyle(
                        color = ColorProvider(MusicGlanceWidget.TextPrimary),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    maxLines = 1
                )
                Text(
                    text = state.artist,
                    style = TextStyle(
                        color = ColorProvider(MusicGlanceWidget.CyanNeon),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Normal
                    ),
                    maxLines = 1
                )
            }
        }

        Spacer(modifier = GlanceModifier.height(8.dp))

        // Contrôles Play/Pause et Suivant
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Bouton Play/Pause
            Box(
                modifier = GlanceModifier
                    .size(34.dp)
                    .background(ColorProvider(if (state.isPlaying) MusicGlanceWidget.CyanNeon else MusicGlanceWidget.VioletPrimary))
                    .cornerRadius(17.dp)
                    .clickable(actionSendBroadcast(MusicWidgetReceiver.createPlayPauseIntent(context))),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    provider = ImageProvider(
                        if (state.isPlaying) R.drawable.ic_widget_pause else R.drawable.ic_widget_play
                    ),
                    contentDescription = if (state.isPlaying) "Pause" else "Lecture",
                    modifier = GlanceModifier.size(18.dp)
                )
            }

            Spacer(modifier = GlanceModifier.width(14.dp))

            // Bouton Suivant
            Box(
                modifier = GlanceModifier
                    .size(32.dp)
                    .background(ColorProvider(MusicGlanceWidget.SurfaceCard))
                    .cornerRadius(16.dp)
                    .clickable(actionSendBroadcast(MusicWidgetReceiver.createNextIntent(context))),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    provider = ImageProvider(R.drawable.ic_widget_next),
                    contentDescription = "Suivant",
                    modifier = GlanceModifier.size(16.dp)
                )
            }
        }
    }
}

/**
 * Disposition étendue (Moyen widget : ~4x1 ou 4x2)
 */
@androidx.compose.runtime.Composable
fun MediumWidgetContent(
    context: Context,
    state: MusicWidgetState,
    artBitmap: Bitmap?
) {
    Row(
        modifier = GlanceModifier
            .fillMaxSize()
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Pochette gauche avec coins arrondis
        WidgetAlbumArt(
            artBitmap = artBitmap,
            size = 54.dp
        )

        Spacer(modifier = GlanceModifier.width(12.dp))

        // Métadonnées centrales
        Column(
            modifier = GlanceModifier.defaultWeight(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = state.title,
                style = TextStyle(
                    color = ColorProvider(MusicGlanceWidget.TextPrimary),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                ),
                maxLines = 1
            )
            Spacer(modifier = GlanceModifier.height(2.dp))
            Text(
                text = state.artist,
                style = TextStyle(
                    color = ColorProvider(MusicGlanceWidget.CyanNeon),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                ),
                maxLines = 1
            )
            if (state.album.isNotBlank()) {
                Spacer(modifier = GlanceModifier.height(2.dp))
                Text(
                    text = state.album,
                    style = TextStyle(
                        color = ColorProvider(MusicGlanceWidget.TextSecondary),
                        fontSize = 10.sp
                    ),
                    maxLines = 1
                )
            }
        }

        Spacer(modifier = GlanceModifier.width(8.dp))

        // Contrôles de lecture droits : Précédent, Play/Pause, Suivant
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalAlignment = Alignment.End
        ) {
            // Précédent
            Box(
                modifier = GlanceModifier
                    .size(34.dp)
                    .background(ColorProvider(MusicGlanceWidget.SurfaceCard))
                    .cornerRadius(17.dp)
                    .clickable(actionSendBroadcast(MusicWidgetReceiver.createPrevIntent(context))),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    provider = ImageProvider(R.drawable.ic_widget_prev),
                    contentDescription = "Précédent",
                    modifier = GlanceModifier.size(18.dp)
                )
            }

            Spacer(modifier = GlanceModifier.width(10.dp))

            // Play / Pause principal
            Box(
                modifier = GlanceModifier
                    .size(42.dp)
                    .background(
                        ColorProvider(
                            if (state.isPlaying) MusicGlanceWidget.CyanNeon else MusicGlanceWidget.VioletPrimary
                        )
                    )
                    .cornerRadius(21.dp)
                    .clickable(actionSendBroadcast(MusicWidgetReceiver.createPlayPauseIntent(context))),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    provider = ImageProvider(
                        if (state.isPlaying) R.drawable.ic_widget_pause else R.drawable.ic_widget_play
                    ),
                    contentDescription = if (state.isPlaying) "Pause" else "Lecture",
                    modifier = GlanceModifier.size(22.dp)
                )
            }

            Spacer(modifier = GlanceModifier.width(10.dp))

            // Suivant
            Box(
                modifier = GlanceModifier
                    .size(34.dp)
                    .background(ColorProvider(MusicGlanceWidget.SurfaceCard))
                    .cornerRadius(17.dp)
                    .clickable(actionSendBroadcast(MusicWidgetReceiver.createNextIntent(context))),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    provider = ImageProvider(R.drawable.ic_widget_next),
                    contentDescription = "Suivant",
                    modifier = GlanceModifier.size(18.dp)
                )
            }
        }
    }
}

/**
 * Affichage de la pochette ou icône par défaut
 */
@androidx.compose.runtime.Composable
private fun WidgetAlbumArt(
    artBitmap: Bitmap?,
    size: androidx.compose.ui.unit.Dp
) {
    Box(
        modifier = GlanceModifier
            .size(size)
            .cornerRadius(10.dp)
            .background(ColorProvider(MusicGlanceWidget.SurfaceCard)),
        contentAlignment = Alignment.Center
    ) {
        if (artBitmap != null) {
            Image(
                provider = ImageProvider(artBitmap),
                contentDescription = "Pochette",
                modifier = GlanceModifier.size(size).cornerRadius(10.dp)
            )
        } else {
            Image(
                provider = ImageProvider(R.drawable.ic_widget_music_art),
                contentDescription = "Musique",
                modifier = GlanceModifier.size(size).cornerRadius(10.dp)
            )
        }
    }
}
