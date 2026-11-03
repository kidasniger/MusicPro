package com.example.widget

import android.content.Context
import android.graphics.Bitmap
import androidx.compose.runtime.Composable
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
import android.content.res.Configuration
import com.example.data.preferences.AppearanceMode
import com.example.data.preferences.AppStyle
import com.example.data.preferences.UserPreferencesRepository
import kotlinx.coroutines.flow.first

/**
 * Widget d'écran d'accueil Glance MusicPro.
 * Prend en charge les formats Petit (compact) et Moyen (étendu).
 *
 * Mise à jour 080 — le widget suit le style choisi dans l'application (clair,
 * sombre, auto ou verre), avec des coins de 24 dp et des boutons
 * Précédent/Lecture/Suivant de 48 dp.
 */
class MusicGlanceWidget : GlanceAppWidget() {

    companion object {
        val SMALL_SIZE = DpSize(110.dp, 90.dp)
        val MEDIUM_SIZE = DpSize(240.dp, 90.dp)

        /** Rayon des coins du widget (mise à jour 080). */
        val CORNER_DP = 24.dp

        /** Taille des boutons Précédent / Lecture / Suivant (mise à jour 080). */
        val BUTTON_DP = 48.dp
    }

    override val sizeMode: SizeMode = SizeMode.Responsive(
        setOf(SMALL_SIZE, MEDIUM_SIZE)
    )

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        // Mise à jour 080 — le style du widget suit l'apparence et l'effet verre
        // choisis dans l'application (lecture directe des préférences DataStore).
        val isNight = (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
            Configuration.UI_MODE_NIGHT_YES
        val palette = runCatching {
            val repository = UserPreferencesRepository(context.applicationContext)
            val appearance = repository.appearanceMode.first()
            val appStyle = repository.appStyle.first()
            WidgetPalette.forAppearance(appearance, appStyle == AppStyle.GLASS, isNight)
        }.getOrDefault(if (isNight) WidgetPalette.DARK else WidgetPalette.LIGHT)

        provideContent {
            val size = LocalSize.current
            val state = MusicWidgetState.load(context)
            val artBitmap = MusicWidgetState.loadBitmap(context)
            val componentName = android.content.ComponentName(context, MainActivity::class.java)

            Box(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(palette.backgroundProvider())
                    .cornerRadius(CORNER_DP)
                    .padding(8.dp)
                    .clickable(actionStartActivity(componentName)),
                contentAlignment = Alignment.Center
            ) {
                if (size.width < 220.dp) {
                    SmallWidgetContent(
                        state = state,
                        artBitmap = artBitmap,
                        palette = palette
                    )
                } else {
                    MediumWidgetContent(
                        state = state,
                        artBitmap = artBitmap,
                        palette = palette
                    )
                }
            }
        }
    }
}

/** Fournisseurs de couleur Glance pour la palette résolue. */
private fun WidgetPalette.Colors.backgroundProvider(): ColorProvider = ColorProvider(background)

private fun WidgetPalette.Colors.cardProvider(): ColorProvider = ColorProvider(card)

private fun WidgetPalette.Colors.textProvider(): ColorProvider = ColorProvider(text)

/**
 * Disposition compacte (Petit widget : ~2x1 ou 2x2). Place disponible réduite :
 * Lecture + Suivant (48 dp).
 */
@Composable
fun SmallWidgetContent(
    state: MusicWidgetState,
    artBitmap: Bitmap?,
    palette: WidgetPalette.Colors
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
            WidgetAlbumArt(
                artBitmap = artBitmap,
                size = 36.dp
            )

            Spacer(modifier = GlanceModifier.width(8.dp))

            Column(modifier = GlanceModifier.defaultWeight()) {
                Text(
                    text = state.title,
                    style = TextStyle(
                        color = palette.textProvider(),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    maxLines = 1
                )
                Text(
                    text = state.artist,
                    style = TextStyle(
                        color = ColorProvider(palette.accent),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Normal
                    ),
                    maxLines = 1
                )
            }
        }

        Spacer(modifier = GlanceModifier.height(8.dp))

        // Contrôles 48 dp : Lecture / Suivant (Précédent sur le format moyen).
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalAlignment = Alignment.CenterVertically
        ) {
            WidgetControlButton(
                background = palette.accentProvider(),
                iconRes = if (state.isPlaying) R.drawable.ic_widget_pause else R.drawable.ic_widget_play,
                contentDescription = if (state.isPlaying) "Pause" else "Lecture",
                action = actionSendBroadcast(MusicWidgetReceiver.createPlayPauseIntent(LocalContext.current))
            )

            Spacer(modifier = GlanceModifier.width(14.dp))

            WidgetControlButton(
                background = palette.cardProvider(),
                iconRes = R.drawable.ic_widget_next,
                contentDescription = "Suivant",
                action = actionSendBroadcast(MusicWidgetReceiver.createNextIntent(LocalContext.current))
            )
        }
    }
}

/**
 * Disposition étendue (Moyen widget : ~4x1 ou 4x2) — Précédent / Lecture / Suivant 48 dp.
 */
@Composable
fun MediumWidgetContent(
    state: MusicWidgetState,
    artBitmap: Bitmap?,
    palette: WidgetPalette.Colors
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
                    color = palette.textProvider(),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                ),
                maxLines = 1
            )
            Spacer(modifier = GlanceModifier.height(2.dp))
            Text(
                text = state.artist,
                style = TextStyle(
                    color = ColorProvider(palette.accent),
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
                        color = ColorProvider(palette.accentAlt),
                        fontSize = 10.sp
                    ),
                    maxLines = 1
                )
            }
        }

        Spacer(modifier = GlanceModifier.width(8.dp))

        // Contrôles de lecture droits : Précédent, Play/Pause, Suivant — 48 dp (080).
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalAlignment = Alignment.End
        ) {
            WidgetControlButton(
                background = palette.cardProvider(),
                iconRes = R.drawable.ic_widget_prev,
                contentDescription = "Précédent",
                action = actionSendBroadcast(MusicWidgetReceiver.createPrevIntent(LocalContext.current))
            )

            Spacer(modifier = GlanceModifier.width(8.dp))

            WidgetControlButton(
                background = palette.accentProvider(),
                iconRes = if (state.isPlaying) R.drawable.ic_widget_pause else R.drawable.ic_widget_play,
                contentDescription = if (state.isPlaying) "Pause" else "Lecture",
                action = actionSendBroadcast(MusicWidgetReceiver.createPlayPauseIntent(LocalContext.current))
            )

            Spacer(modifier = GlanceModifier.width(8.dp))

            WidgetControlButton(
                background = palette.cardProvider(),
                iconRes = R.drawable.ic_widget_next,
                contentDescription = "Suivant",
                action = actionSendBroadcast(MusicWidgetReceiver.createNextIntent(LocalContext.current))
            )
        }
    }
}

/** Bouton circulaire 48 dp (coins 24 dp) avec action broadcast (mise à jour 080). */
@Composable
private fun WidgetControlButton(
    background: ColorProvider,
    iconRes: Int,
    contentDescription: String,
    action: androidx.glance.action.Action
) {
    Box(
        modifier = GlanceModifier
            .size(MusicGlanceWidget.BUTTON_DP)
            .background(background)
            .cornerRadius(MusicGlanceWidget.BUTTON_DP / 2)
            .clickable(action),
        contentAlignment = Alignment.Center
    ) {
        Image(
            provider = ImageProvider(iconRes),
            contentDescription = contentDescription,
            modifier = GlanceModifier.size(22.dp)
        )
    }
}

/**
 * Affichage de la pochette ou icône par défaut
 */
@Composable
private fun WidgetAlbumArt(
    artBitmap: Bitmap?,
    size: androidx.compose.ui.unit.Dp
) {
    Box(
        modifier = GlanceModifier
            .size(size)
            .cornerRadius(10.dp)
            .background(ColorProvider(WidgetPalette.DARK.card)),
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

private fun WidgetPalette.Colors.accentProvider(): ColorProvider = ColorProvider(accent)
