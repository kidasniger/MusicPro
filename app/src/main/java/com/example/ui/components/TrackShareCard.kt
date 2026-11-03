package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.net.Uri
import android.text.TextPaint
import android.text.TextUtils
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.core.content.FileProvider
import com.example.R
import com.example.data.local.AudioTrackEntity
import java.io.File

/**
 * Mise à jour 070 — carte de morceau à partager (1080×1350) générée via
 * [Canvas]/[Bitmap] : pochette (optionnelle pour respecter les droits),
 * titre, artiste et accent du thème, puis envoi par la feuille de partage système.
 */
object TrackShareCard {

    private const val CARD_WIDTH = 1080
    private const val CARD_HEIGHT = 1350

    /** Génère la carte. [accentArgb] : couleur d'accent du thème courant. */
    fun buildBitmap(
        context: Context,
        track: AudioTrackEntity,
        coverUri: String?,
        includeCover: Boolean,
        accentArgb: Int
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(CARD_WIDTH, CARD_HEIGHT, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val accent = accentArgb

        // Fond : dégradé vertical sombre teinté par l'accent.
        val base = Color.rgb(
            (Color.red(accent) * 0.30f).toInt().coerceIn(10, 70),
            (Color.green(accent) * 0.30f).toInt().coerceIn(10, 70),
            (Color.blue(accent) * 0.30f).toInt().coerceIn(12, 80)
        )
        val deep = Color.argb(255, 14, 14, 22)
        val bg = Paint().apply {
            shader = LinearGradient(
                0f, 0f, 0f, CARD_HEIGHT.toFloat(), base, deep, Shader.TileMode.CLAMP
            )
        }
        canvas.drawRoundRect(0f, 0f, CARD_WIDTH.toFloat(), CARD_HEIGHT.toFloat(), 48f, 48f, bg)

        val white = Color.WHITE
        val muted = Color.argb(210, 255, 255, 255)

        // Pochette carrée centrée (ou médaillon « note » si absente/écartée).
        val coverSize = 720
        val coverLeft = ((CARD_WIDTH - coverSize) / 2).toFloat()
        val coverTop = 150f
        val coverPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        val frame = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(90, 255, 255, 255)
            style = Paint.Style.STROKE
            strokeWidth = 3f
        }
        if (includeCover) {
            val cover = decodeCover(context, coverUri)
            if (cover != null) {
                canvas.drawBitmap(
                    Bitmap.createScaledBitmap(cover, coverSize, coverSize, true),
                    coverLeft, coverTop, coverPaint
                )
            } else {
                drawCoverPlaceholder(canvas, coverLeft, coverTop, coverSize, accent)
            }
        } else {
            drawCoverPlaceholder(canvas, coverLeft, coverTop, coverSize, accent)
        }
        canvas.drawRoundRect(
            coverLeft, coverTop, coverLeft + coverSize, coverTop + coverSize, 36f, 36f, frame
        )

        // Titre (2 lignes maximum, ellipse).
        val titlePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = white
            textSize = 64f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val titleLines = TextUtils.ellipsize(track.title, titlePaint, 920f, TextUtils.TruncateAt.END)
            .toString()
        val titleY = coverTop + coverSize + 120f
        canvas.drawText(titleLines, 80f, titleY, titlePaint)

        // Artiste.
        val artistPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = muted
            textSize = 44f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        }
        val artist = TextUtils.ellipsize(
            track.artist.ifBlank { " " }, artistPaint, 920f, TextUtils.TruncateAt.END
        ).toString()
        canvas.drawText(artist, 80f, titleY + 78f, artistPaint)

        // Accent : petit trait de couleur + signature MusicPro.
        val bar = Paint().apply { color = accent }
        canvas.drawRoundRect(80f, titleY + 118f, 320f, titleY + 130f, 6f, 6f, bar)
        val brandPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = muted
            textSize = 34f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            letterSpacing = 0.12f
        }
        canvas.drawText("MusicPro", CARD_WIDTH - 80f - brandPaint.measureText("MusicPro"),
            CARD_HEIGHT - 90f, brandPaint)

        return bitmap
    }

    private fun drawCoverPlaceholder(
        canvas: Canvas, left: Float, top: Float, size: Int, accent: Int
    ) {
        val fill = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(60, 255, 255, 255)
        }
        canvas.drawRoundRect(left, top, left + size, top + size, 36f, 36f, fill)
        val note = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = accent
            textSize = 260f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("♪", left + size / 2f, top + size / 2f + 90f, note)
    }

    private fun decodeCover(context: Context, uriString: String?): Bitmap? {
        if (uriString.isNullOrBlank()) return null
        return runCatching {
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            context.contentResolver.openInputStream(Uri.parse(uriString))?.use {
                BitmapFactory.decodeStream(it, null, bounds)
            }
            val longest = maxOf(bounds.outWidth, bounds.outHeight)
            if (longest <= 0) return@runCatching null
            var sample = 1
            while (longest / sample > 720) sample *= 2
            val options = BitmapFactory.Options().apply { inSampleSize = sample }
            context.contentResolver.openInputStream(Uri.parse(uriString))?.use {
                BitmapFactory.decodeStream(it, null, options)
            }
        }.getOrNull()
    }

    /**
     * Écrit la carte dans le cache et ouvre la feuille de partage système.
     * Le chemin `cache/share/` est exposé par le FileProvider (file_paths.xml).
     */
    fun share(context: Context, track: AudioTrackEntity, coverUri: String?, includeCover: Boolean, accentArgb: Int) {
        runCatching {
            val bitmap = buildBitmap(context, track, coverUri, includeCover, accentArgb)
            val dir = File(context.cacheDir, "share").apply { mkdirs() }
            val file = File(dir, "musicpro_card_${track.id}_${System.currentTimeMillis()}.png")
            file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 95, it) }
            bitmap.recycle()
            val uri = FileProvider.getUriForFile(context, context.packageName + ".provider", file)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_TEXT, track.title + " — " + track.artist)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(
                Intent.createChooser(
                    intent,
                    context.getString(R.string.share_card_title)
                )
            )
        }
    }
}

/**
 * Mise à jour 070 — choix « avec / sans pochette » avant la génération de la carte,
 * pour respecter les droits d'usage des visuels.
 */
@Composable
fun TrackShareCardDialog(
    track: AudioTrackEntity,
    accentArgb: Int,
    onDismiss: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val scheme = MaterialTheme.colorScheme
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.share_card_title), color = scheme.onBackground) },
        text = {
            Text(stringResource(R.string.share_card_hint), color = scheme.onSurfaceVariant)
        },
        confirmButton = {
            TextButton(onClick = {
                onDismiss()
                TrackShareCard.share(context, track, track.albumArtUri, true, accentArgb)
            }) {
                Text(stringResource(R.string.share_card_with_cover))
            }
        },
        dismissButton = {
            TextButton(onClick = {
                onDismiss()
                TrackShareCard.share(context, track, track.albumArtUri, false, accentArgb)
            }) {
                Text(stringResource(R.string.share_card_no_cover))
            }
        },
        containerColor = scheme.surface
    )
}
