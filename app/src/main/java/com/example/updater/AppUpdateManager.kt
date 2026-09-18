package com.example.updater

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.FileProvider
import com.example.data.preferences.UserPreferencesRepository
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit

class AppUpdateManager(
    private val context: Context,
    private val preferencesRepository: UserPreferencesRepository
) {
    companion object {
        const val GITHUB_OWNER = "kidasniger"
        const val GITHUB_REPO = "MusicPro"
        private const val API_URL = "https://api.github.com/repos/$GITHUB_OWNER/$GITHUB_REPO/releases/latest"
    }

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val _updateCheckState = MutableStateFlow<UpdateCheckState>(UpdateCheckState.Idle)
    val updateCheckState: StateFlow<UpdateCheckState> = _updateCheckState.asStateFlow()

    private val _downloadState = MutableStateFlow<DownloadState>(DownloadState.Idle)
    val downloadState: StateFlow<DownloadState> = _downloadState.asStateFlow()

    init {
        // Nettoyage uniquement des fichiers corrompus ou trop anciens (> 24h), en préservant l'APK téléchargé
        cleanupObsoleteApks(force = false)
    }

    /**
     * Supprime les APK temporaires du cache.
     * Si force = false, ne supprime que les fichiers non valides ou datant de plus de 24 heures.
     * Si force = true, supprime tous les APK temporaires (par exemple lorsque l'application a été mise à jour avec succès).
     */
    fun cleanupObsoleteApks(force: Boolean = false) {
        try {
            val updatesDir = File(context.cacheDir, "updates")
            if (updatesDir.exists() && updatesDir.isDirectory) {
                val now = System.currentTimeMillis()
                val oneDayAgo = now - 24 * 60 * 60 * 1000L

                updatesDir.listFiles()?.forEach { file ->
                    if (file.name.endsWith(".apk", ignoreCase = true)) {
                        if (force || file.length() == 0L || file.lastModified() < oneDayAgo) {
                            file.delete()
                        }
                    }
                }
            }
        } catch (_: Exception) {}
    }

    /**
     * Vérifie si l'APK de mise à jour correspondant est déjà téléchargé et valide.
     */
    fun getExistingDownloadedApk(expectedSize: Long): File? {
        val updatesDir = File(context.cacheDir, "updates")
        val destinationFile = File(updatesDir, "MusicPro_update.apk")
        if (destinationFile.exists() && destinationFile.isFile && destinationFile.length() > 0) {
            // Si la taille est précisée et correspond (ou à 98%+), l'APK est déjà complet
            if (expectedSize <= 0 || destinationFile.length() >= expectedSize) {
                return destinationFile
            }
        }
        return null
    }

    /**
     * Nettoie et formate les notes de mise à jour pour enlever le markdown brut (**, `, hashs, etc.)
     * et ne garder que les points de nouveautés et corrections compréhensibles.
     */
    fun formatReleaseNotes(rawBody: String?, latestVersion: String): String {
        if (rawBody.isNullOrBlank()) {
            return "• Optimisations des performances audio et stabilité générale\n• Corrections de bugs et améliorations de l'interface"
        }

        val lines = rawBody.lines()
        val cleanedLines = mutableListOf<String>()

        for (line in lines) {
            var trimmed = line.trim()
            if (trimmed.isBlank()) continue

            // Ignorer les en-têtes markdown et métadonnées techniques
            if (trimmed.startsWith("#") ||
                trimmed.contains("Commit", ignoreCase = true) ||
                trimmed.contains("Version Code", ignoreCase = true) ||
                trimmed.contains("Version Name", ignoreCase = true) ||
                trimmed.contains("Date", ignoreCase = true)
            ) {
                continue
            }

            // Nettoyage complet du Markdown : gras **, code `, italique *, puces
            trimmed = trimmed.replace("**", "")
            trimmed = trimmed.replace("`", "")
            trimmed = trimmed.replace(Regex("""^\s*[-*•]\s*"""), "")
            trimmed = trimmed.replace("*", "")
            trimmed = trimmed.trim()

            if (trimmed.isNotBlank()) {
                cleanedLines.add("• $trimmed")
            }
        }

        return if (cleanedLines.isNotEmpty()) {
            cleanedLines.joinToString("\n")
        } else {
            "• Version $latestVersion prête à l'installation\n• Optimisations et corrections de stabilité"
        }
    }

    fun getCurrentVersionName(): String {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            packageInfo.versionName ?: "1.0"
        } catch (_: Exception) {
            "1.0"
        }
    }

    /**
     * Vérifie auprès de l'API GitHub si une release plus récente existe.
     */
    suspend fun checkForUpdates(): UpdateCheckState = withContext(Dispatchers.IO) {
        _updateCheckState.value = UpdateCheckState.Checking
        try {
            val request = Request.Builder()
                .url(API_URL)
                .header("Accept", "application/vnd.github.v3+json")
                .header("User-Agent", "MusicPro-Android")
                .get()
                .build()

            val response = httpClient.newCall(request).execute()
            if (!response.isSuccessful) {
                val err = if (response.code == 404) {
                    "Aucune release trouvée sur GitHub pour le moment."
                } else {
                    "Erreur GitHub HTTP ${response.code}"
                }
                val errorState = UpdateCheckState.Error(err)
                _updateCheckState.value = errorState
                return@withContext errorState
            }

            val responseBody = response.body?.string() ?: ""
            val adapter = moshi.adapter(GitHubRelease::class.java)
            val release = adapter.fromJson(responseBody)
                ?: return@withContext UpdateCheckState.Error("Données de release invalides").also {
                    _updateCheckState.value = it
                }

            // Recherche de l'asset APK dans les fichiers attachés à la release
            val apkAsset = release.assets.firstOrNull { it.name.endsWith(".apk", ignoreCase = true) }
            if (apkAsset == null) {
                val state = UpdateCheckState.Error("La release ${release.tagName} n'a pas de fichier APK attaché.")
                _updateCheckState.value = state
                return@withContext state
            }

            val currentVersion = getCurrentVersionName()
            val latestVersionTag = release.tagName.removePrefix("v").trim()

            // Comparaison des versions
            val isNewer = isVersionNewer(latestVersionTag, currentVersion)

            val finalState = if (isNewer) {
                val formattedNotes = formatReleaseNotes(release.body, latestVersionTag)
                
                // Si l'APK est déjà présent en cache et complet, initialiser l'état sur Downloaded
                val existingApk = getExistingDownloadedApk(apkAsset.size)
                if (existingApk != null) {
                    _downloadState.value = DownloadState.Downloaded(existingApk.absolutePath)
                }

                UpdateCheckState.UpdateAvailable(
                    latestVersion = latestVersionTag,
                    currentVersion = currentVersion,
                    releaseNotes = formattedNotes,
                    downloadUrl = apkAsset.downloadUrl,
                    apkSize = apkAsset.size
                )
            } else {
                // Si l'application est déjà à jour, s'assurer que les anciens APK temporaires sont supprimés
                cleanupObsoleteApks(force = true)
                UpdateCheckState.UpToDate(currentVersion)
            }

            _updateCheckState.value = finalState
            finalState
        } catch (e: Exception) {
            val errState = UpdateCheckState.Error("Impossible de vérifier les mises à jour : ${e.localizedMessage ?: "Vérifiez votre connexion internet"}")
            _updateCheckState.value = errState
            errState
        }
    }

    /**
     * Télécharge l'APK depuis GitHub avec suivi de la progression.
     */
    suspend fun downloadAndInstallApk(downloadUrl: String) = withContext(Dispatchers.IO) {
        _downloadState.value = DownloadState.Downloading(progressPercent = 0, downloadedBytes = 0, totalBytes = 0)
        try {
            val request = Request.Builder()
                .url(downloadUrl)
                .header("User-Agent", "MusicPro-Android")
                .get()
                .build()

            val response = httpClient.newCall(request).execute()
            if (!response.isSuccessful) {
                _downloadState.value = DownloadState.Error("Échec du téléchargement (HTTP ${response.code})")
                return@withContext
            }

            val body = response.body ?: run {
                _downloadState.value = DownloadState.Error("Réponse de téléchargement vide")
                return@withContext
            }

            val totalBytes = body.contentLength()
            val updatesDir = File(context.cacheDir, "updates")
            if (!updatesDir.exists()) {
                updatesDir.mkdirs()
            }

            // Nettoyage des anciens APK en cache
            updatesDir.listFiles()?.forEach { it.delete() }

            val destinationFile = File(updatesDir, "MusicPro_update.apk")
            val inputStream = body.byteStream()
            val outputStream = FileOutputStream(destinationFile)

            val buffer = ByteArray(8 * 1024)
            var bytesCopied: Long = 0
            var read: Int

            while (inputStream.read(buffer).also { read = it } != -1) {
                outputStream.write(buffer, 0, read)
                bytesCopied += read
                if (totalBytes > 0) {
                    val progress = ((bytesCopied * 100) / totalBytes).toInt()
                    _downloadState.value = DownloadState.Downloading(
                        progressPercent = progress,
                        downloadedBytes = bytesCopied,
                        totalBytes = totalBytes
                    )
                }
            }

            outputStream.flush()
            outputStream.close()
            inputStream.close()

            _downloadState.value = DownloadState.Downloaded(destinationFile.absolutePath)

            // Déclenche l'installation
            withContext(Dispatchers.Main) {
                installApk(destinationFile)
            }
        } catch (e: Exception) {
            _downloadState.value = DownloadState.Error("Erreur lors du téléchargement : ${e.localizedMessage}")
        }
    }

    /**
     * Vérifie si l'application a la permission d'installer des paquets et lance l'intent d'installation.
     */
    fun installApk(apkFile: File) {
        if (!apkFile.exists()) return

        // Sur Android 8.0+, vérifier si l'autorisation d'installer des paquets est accordée
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (!context.packageManager.canRequestPackageInstalls()) {
                val permissionIntent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                    data = Uri.parse("package:${context.packageName}")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(permissionIntent)
                return
            }
        }

        val apkUri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            apkFile
        )

        val installIntent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(apkUri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        context.startActivity(installIntent)
    }

    fun resetState() {
        _updateCheckState.value = UpdateCheckState.Idle
        _downloadState.value = DownloadState.Idle
    }

    /**
     * Compare deux numéros de versions type "1.2.0" ou "1.0-b2"
     */
    private fun isVersionNewer(latest: String, current: String): Boolean {
        try {
            // Nettoyage de tags type "1.0-b2" -> "1.0.2" pour la comparaison
            val cleanLatest = latest.replace("-b", ".").split(".")
            val cleanCurrent = current.replace("-b", ".").split(".")

            val maxLength = maxOf(cleanLatest.size, cleanCurrent.size)
            for (i in 0 until maxLength) {
                val latestPart = cleanLatest.getOrNull(i)?.filter { it.isDigit() }?.toIntOrNull() ?: 0
                val currentPart = cleanCurrent.getOrNull(i)?.filter { it.isDigit() }?.toIntOrNull() ?: 0
                if (latestPart > currentPart) return true
                if (latestPart < currentPart) return false
            }
            return false
        } catch (_: Exception) {
            return latest != current
        }
    }
}
