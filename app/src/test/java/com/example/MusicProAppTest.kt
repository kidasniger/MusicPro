package com.example

import androidx.test.core.app.ApplicationProvider
import com.example.ui.artwork.ArtworkCrossfadeMs
import com.example.ui.artwork.ArtworkGridSizePx
import com.example.ui.artwork.ArtworkPlayerSizePx
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Mise à jour 087 — configuration de chargement d'images : cache mémoire 25 % de la RAM,
 * cache disque 100 Mo, miniatures 300 px (grilles) / 1080 px (lecteur), fondu 200 ms.
 */
@RunWith(RobolectricTestRunner::class)
class MusicProAppTest {

    private val app: MusicProApp
        get() = ApplicationProvider.getApplicationContext()

    @Test
    fun `le cache disque est limité à 100 Mo`() {
        assertEquals(100L * 1024 * 1024, app.diskCacheMaxBytes())
    }

    @Test
    fun `le cache mémoire prend 25 pour cent de la RAM`() {
        assertEquals(0.25, app.memoryCachePercent(), 1e-9)
    }

    @Test
    fun `le chargeur d'images de l'application est bien celui configuré`() {
        val loader = app.newImageLoader()
        assertEquals(true, loader.memoryCache != null)
        assertEquals(true, loader.diskCache != null)
    }

    @Test
    fun `les tailles de miniatures et le fondu suivent la spécification`() {
        assertEquals(300, ArtworkGridSizePx)
        assertEquals(1080, ArtworkPlayerSizePx)
        assertEquals(200, ArtworkCrossfadeMs)
    }
}
