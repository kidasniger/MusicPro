package com.example.ui.components.glass

import android.graphics.Bitmap
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.ImageShader
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.asImageBitmap
import kotlin.random.Random

/**
 * Tuile de bruit en niveaux de gris (64 x 64 px, graine fixe), répétée sur la surface.
 * Créée une seule fois, à la première utilisation, puis dessinée à [GlassNoiseOpacity].
 */
internal object GlassNoise {
    private const val TILE_SIZE = 64

    val brush: Brush by lazy {
        val random = Random(7)
        val pixels = IntArray(TILE_SIZE * TILE_SIZE) {
            val v = random.nextInt(256)
            (0xFF shl 24) or (v shl 16) or (v shl 8) or v
        }
        val bitmap = Bitmap.createBitmap(pixels, TILE_SIZE, TILE_SIZE, Bitmap.Config.ARGB_8888)
        ShaderBrush(ImageShader(bitmap.asImageBitmap(), TileMode.Repeated, TileMode.Repeated))
    }
}
