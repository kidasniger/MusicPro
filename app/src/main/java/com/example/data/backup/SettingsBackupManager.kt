package com.example.data.backup

import com.example.data.preferences.AccentChoice
import com.example.data.preferences.AppearanceMode
import com.example.data.preferences.AppStyle
import com.example.data.preferences.GlassMaterial
import com.example.data.preferences.ListDensityPreference
import com.example.data.preferences.TextSizePreference
import org.json.JSONArray
import org.json.JSONObject

/**
 * Mise à jour 038 — Sauvegarde et export des réglages.
 *
 * Sérialise les réglages de l'application (thème, style, verre, karaoké, favoris) en JSON
 * versionné. L'import valide la version du schéma avant application : un fichier d'un schéma
 * plus récent est refusé avec [BackupFormatException].
 */
const val BACKUP_SCHEMA_VERSION = 1

data class GlassBackup(
    val material: GlassMaterial,
    val colorStrength: Int,
    val frostDp: Int,
    val tintHue: Float?,
    val filmOpacity: Int,
    val activeTabOpacity: Int
)

data class KaraokeBackup(
    val fontSize: Float,
    val activeColor: String,
    val offsetMs: Long
)

data class FavoritesBackup(
    val trackIds: List<Long>,
    val artists: List<String>
)

data class SettingsBackup(
    val schemaVersion: Int,
    val appearance: AppearanceMode,
    val appStyle: AppStyle,
    val accentChoice: AccentChoice,
    val eveningStartMinutes: Int,
    val eveningEndMinutes: Int,
    val dynamicColor: Boolean,
    val textSize: TextSizePreference,
    val listDensity: ListDensityPreference,
    val reduceAnimations: Boolean,
    val glass: GlassBackup,
    val karaoke: KaraokeBackup,
    val favorites: FavoritesBackup
) {
    fun toJson(): String {
        val glass = JSONObject()
            .put("material", glassJsonMaterial())
            .put("colorStrength", glass.colorStrength)
            .put("frostDp", glass.frostDp)
            .put("tintHue", glass.tintHue ?: JSONObject.NULL)
            .put("filmOpacity", glass.filmOpacity)
            .put("activeTabOpacity", glass.activeTabOpacity)
        val karaoke = JSONObject()
            .put("fontSize", karaoke.fontSize.toDouble())
            .put("activeColor", karaoke.activeColor)
            .put("offsetMs", karaoke.offsetMs)
        val trackIds = JSONArray()
        favorites.trackIds.forEach { trackIds.put(it) }
        val artists = JSONArray()
        favorites.artists.forEach { artists.put(it) }
        return JSONObject()
            .put("schemaVersion", schemaVersion)
            .put("app", "MusicPro")
            .put("appearance", appearance.name)
            .put("appStyle", appStyle.name)
            .put("accentChoice", accentChoice.name)
            .put("eveningStartMinutes", eveningStartMinutes)
            .put("eveningEndMinutes", eveningEndMinutes)
            .put("dynamicColor", dynamicColor)
            .put("textSize", textSize.name)
            .put("listDensity", listDensity.name)
            .put("reduceAnimations", reduceAnimations)
            .put("glass", glass)
            .put("karaoke", karaoke)
            .put("favorites", JSONObject().put("trackIds", trackIds).put("artists", artists))
            .toString(2)
    }

    private fun glassJsonMaterial(): String = glass.material.name
}

/** Levé quand le fichier importé n'est pas un JSON de sauvegarde MusicPro valide. */
class BackupFormatException(message: String) : Exception(message)

object SettingsBackupManager {

    /** Reconstruit une sauvegarde depuis un texte JSON ; lève [BackupFormatException] si invalide. */
    fun fromJson(text: String): SettingsBackup {
        val root = try {
            JSONObject(text)
        } catch (e: Exception) {
            throw BackupFormatException("JSON invalide: ${e.message}")
        }
        if (root.optString("app") != "MusicPro") {
            throw BackupFormatException("Ce fichier n'est pas une sauvegarde MusicPro.")
        }
        val version = root.optInt("schemaVersion", -1)
        if (version < 1) {
            throw BackupFormatException("Version de schéma absente ou invalide.")
        }
        if (version > BACKUP_SCHEMA_VERSION) {
            throw BackupFormatException(
                "Schéma v$version plus récent que cette version de l'application (v$BACKUP_SCHEMA_VERSION)."
            )
        }
        val favorites = root.optJSONObject("favorites") ?: JSONObject()
        val glass = root.optJSONObject("glass") ?: JSONObject()
        val karaoke = root.optJSONObject("karaoke") ?: JSONObject()
        return SettingsBackup(
            schemaVersion = version,
            appearance = resolveEnum(root.optString("appearance"), AppearanceMode.DARK),
            appStyle = resolveEnum(root.optString("appStyle"), AppStyle.NEON),
            accentChoice = resolveEnum(root.optString("accentChoice"), AccentChoice.TERRACOTTA),
            eveningStartMinutes = root.optInt("eveningStartMinutes", 20 * 60).coerceIn(0, 24 * 60 - 1),
            eveningEndMinutes = root.optInt("eveningEndMinutes", 7 * 60).coerceIn(0, 24 * 60 - 1),
            dynamicColor = root.optBoolean("dynamicColor", false),
            textSize = resolveEnum(root.optString("textSize"), TextSizePreference.NORMAL),
            listDensity = resolveEnum(root.optString("listDensity"), ListDensityPreference.COMFORTABLE),
            reduceAnimations = root.optBoolean("reduceAnimations", false),
            glass = GlassBackup(
                material = resolveEnum(glass.optString("material"), GlassMaterial.BALANCED),
                colorStrength = glass.optInt("colorStrength", 70).coerceIn(0, 100),
                frostDp = glass.optInt("frostDp", 6).coerceIn(0, 24),
                tintHue = if (glass.isNull("tintHue")) null
                else glass.optDouble("tintHue", 0.0).toFloat().mod(360f),
                filmOpacity = glass.optInt("filmOpacity", 50).coerceIn(0, 100),
                activeTabOpacity = glass.optInt("activeTabOpacity", 26).coerceIn(0, 100)
            ),
            karaoke = KaraokeBackup(
                fontSize = karaoke.optDouble("fontSize", 18.0).toFloat().coerceIn(12f, 32f),
                activeColor = karaoke.optString("activeColor", "cyan"),
                offsetMs = karaoke.optLong("offsetMs", 0L).coerceIn(-5000L, 5000L)
            ),
            favorites = FavoritesBackup(
                trackIds = favorites.optJSONArray("trackIds").orEmpty().asLongList().filter { it > 0 },
                artists = favorites.optJSONArray("artists").orEmpty().asStringList().filter(String::isNotBlank)
            )
        )
    }

    private fun org.json.JSONArray?.orEmpty(): org.json.JSONArray = this ?: org.json.JSONArray()

    private fun org.json.JSONArray.asLongList(): List<Long> = List(length()) { optLong(it) }

    private fun org.json.JSONArray.asStringList(): List<String> = List(length()) { optString(it) }

    private inline fun <reified T : Enum<T>> resolveEnum(name: String, fallback: T): T =
        T::class.java.enumConstants.firstOrNull { it.name == name } ?: fallback
}
