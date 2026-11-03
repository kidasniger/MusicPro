package com.example

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache

/**
 * Configuration du chargement d'images (mise à jour 087) : un seul [ImageLoader] pour toute
 * l'application, fourni à Coil via [ImageLoaderFactory].
 *
 *  - Cache mémoire : 25 % de la RAM disponible ;
 *  - Cache disque : 100 Mo dans le dossier cache de l'application ;
 *  - Fondu enchaîné de 200 ms à l'apparition des pochettes ;
 *  - Miniatures : 300 px pour les grilles, 1080 px pour le lecteur (voir ArtworkRequests).
 */
class MusicProApp : Application(), ImageLoaderFactory {

    override fun onCreate() {
        super.onCreate()
    }

    /** Cache disque : 100 Mo (constante testée dans MusicProAppTest). */
    fun diskCacheMaxBytes(): Long = 100L * 1024 * 1024

    /** Part de la RAM dédiée au cache mémoire (25 %, constante testée). */
    fun memoryCachePercent(): Double = 0.25

    override fun newImageLoader(): ImageLoader =
        ImageLoader.Builder(this)
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(memoryCachePercent())
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("image_cache"))
                    .maxSizeBytes(diskCacheMaxBytes())
                    .build()
            }
            .crossfade(200)
            .build()
}
