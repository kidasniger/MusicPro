package com.example.ui.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.preferences.AccentChoice
import com.example.data.preferences.AppStyle
import com.example.data.preferences.AppThemeMode
import com.example.data.preferences.AppearanceMode
import com.example.data.preferences.DefaultEveningEndMinutes
import com.example.data.preferences.DefaultEveningStartMinutes
import com.example.data.preferences.DefaultGlassActiveTabOpacity
import com.example.data.preferences.DefaultGlassColorStrength
import com.example.data.preferences.DefaultGlassFilmOpacity
import com.example.data.preferences.DefaultGlassFrostDp
import com.example.data.preferences.GlassMaterial
import com.example.data.preferences.PlayerPresentation
import com.example.data.preferences.TextSizePreference
import com.example.data.preferences.ListDensityPreference
import com.example.data.preferences.UserPreferencesRepository
import com.example.data.preferences.isInDarkWindow
import com.example.data.backup.BackupFormatException
import com.example.data.backup.SettingsBackup
import com.example.data.backup.SettingsBackupManager
import com.example.data.repository.AudioRepository
import com.example.updater.AppUpdateManager
import com.example.updater.DownloadState
import com.example.updater.UpdateCheckState
import com.example.util.CacheManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val preferencesRepository = UserPreferencesRepository(application)
    private val audioRepository = AudioRepository.getInstance(application)
    val appUpdateManager = AppUpdateManager(application, preferencesRepository)

    val updateCheckState: StateFlow<UpdateCheckState> = appUpdateManager.updateCheckState
    val downloadState: StateFlow<DownloadState> = appUpdateManager.downloadState

    val currentVersionName: String = appUpdateManager.getCurrentVersionName()

    // --- Apparence et style (mises à jour 013, 017, 018) ------------------------------------------

    /** Apparence (Clair, Sombre, Auto, Sombre le soir) ; déduite de l'ancienne clé tant qu'elle n'est pas écrite. */
    val appearanceMode: StateFlow<AppearanceMode> = preferencesRepository.appearanceMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppearanceMode.DARK)

    /** Style (Minimal, Néon, Verre) ; déduit de l'ancienne clé tant qu'il n'est pas écrit. */
    val appStyle: StateFlow<AppStyle> = preferencesRepository.appStyle
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppStyle.NEON)

    /** Accent ; Terracotta par défaut. */
    val accentChoice: StateFlow<AccentChoice> = preferencesRepository.accentChoice
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AccentChoice.TERRACOTTA)

    /** Début de « Sombre le soir », en minutes depuis minuit (20:00 par défaut). */
    val eveningStartMinutes: StateFlow<Int> = preferencesRepository.eveningStartMinutes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DefaultEveningStartMinutes)

    /** Fin de « Sombre le soir », en minutes depuis minuit (07:00 par défaut). */
    val eveningEndMinutes: StateFlow<Int> = preferencesRepository.eveningEndMinutes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DefaultEveningEndMinutes)

    /** Minute courante de la journée, rafraîchie toutes les 30 s tant que l'écran observe le thème. */
    private val minuteOfDayTicker: Flow<Int> = flow {
        while (true) {
            emit(currentMinuteOfDay())
            delay(EveningTickMillis)
        }
    }

    /**
     * Vrai quand l'heure courante tombe dans la plage « Sombre le soir ». Observé par
     * `MusicProTheme` : le thème bascule sans redémarrer l'activité.
     */
    val eveningDarkActive: StateFlow<Boolean> = combine(
        eveningStartMinutes,
        eveningEndMinutes,
        minuteOfDayTicker
    ) { start, end, now -> isInDarkWindow(now, start, end) }
        .distinctUntilChanged()
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            isInDarkWindow(currentMinuteOfDay(), DefaultEveningStartMinutes, DefaultEveningEndMinutes)
        )

    /**
     * Ancien sélecteur à trois valeurs, dérivé de l'apparence. Conservé tant que l'ancien écran de
     * réglages l'utilise ; il pilote désormais les deux nouveaux réglages (voir [setThemeMode]).
     */
    val themeMode: StateFlow<AppThemeMode> = preferencesRepository.appearanceMode
        .map { appearance ->
            when (appearance) {
                AppearanceMode.LIGHT -> AppThemeMode.LIGHT
                AppearanceMode.AUTO -> AppThemeMode.SYSTEM
                AppearanceMode.DARK, AppearanceMode.DARK_EVENING -> AppThemeMode.DARK
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppThemeMode.DARK)

    val isDynamicColor: StateFlow<Boolean> = preferencesRepository.isDynamicColorEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val glassMaterial: StateFlow<GlassMaterial> = preferencesRepository.glassMaterial
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), GlassMaterial.BALANCED)

    /** Force de couleur du verre en % (0-100), 70 % par défaut. */
    val glassColorStrength: StateFlow<Int> = preferencesRepository.glassColorStrength
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DefaultGlassColorStrength)

    /** Givre du verre (rayon du flou) en dp (0-24), 6 dp par défaut. */
    val glassFrostDp: StateFlow<Int> = preferencesRepository.glassFrostDp
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DefaultGlassFrostDp)

    /** Teinte du verre en degrés (0-360) ; `null` = Aucune, par défaut. */
    val glassTintHue: StateFlow<Float?> = preferencesRepository.glassTintHue
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    /** Film du verre en % (0-100), 50 % par défaut. */
    val glassFilmOpacity: StateFlow<Int> = preferencesRepository.glassFilmOpacity
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DefaultGlassFilmOpacity)

    /** Opacité de la capsule de l'onglet actif en % (0-100), 26 % par défaut. */
    val glassActiveTabOpacity: StateFlow<Int> = preferencesRepository.glassActiveTabOpacity
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DefaultGlassActiveTabOpacity)

    /** Mise à jour 084 — « Texte plus lisible » : film du verre forcé quasi opaque. */
    val glassReadableText: StateFlow<Boolean> = preferencesRepository.glassReadableText
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    /** Mise à jour 084 — « Réduire la transparence » suit le réglage système (par défaut). */
    val glassFollowReduceTransparency: StateFlow<Boolean> =
        preferencesRepository.glassFollowReduceTransparency
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val textSizePreference: StateFlow<TextSizePreference> = preferencesRepository.textSizePreference
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), TextSizePreference.NORMAL)

    val listDensityPreference: StateFlow<ListDensityPreference> = preferencesRepository.listDensityPreference
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ListDensityPreference.COMFORTABLE)

    val reduceAnimations: StateFlow<Boolean> = preferencesRepository.reduceAnimations
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val autoPlay: StateFlow<Boolean> = preferencesRepository.autoPlay
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val gaplessPlayback: StateFlow<Boolean> = preferencesRepository.gaplessPlayback
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val resumePlayback: StateFlow<Boolean> = preferencesRepository.resumePlayback
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    /** Présentation du lecteur (mise à jour 048) : pochette, vinyle ou minimal. */
    val playerPresentation: StateFlow<PlayerPresentation> = preferencesRepository.playerPresentation
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PlayerPresentation.POCHETTE)

    // --- Aperçu en direct du verre (mise à jour 010) ---------------------------------------------

    private val _glassPreview = MutableStateFlow(GlassPreviewState())

    /**
     * État hissé de l'aperçu : les six réglages du verre réunis. Chaque setter ci-dessous le met à
     * jour tout de suite, avant l'écriture dans le DataStore, pour que l'aperçu suive le curseur
     * sans délai.
     */
    val glassPreview: StateFlow<GlassPreviewState> = _glassPreview.asStateFlow()

    /** Écritures du verre en cours : tant qu'il y en a, on ignore les émissions (périmées) du DataStore. */
    private var glassWritesInFlight = 0

    private val glassSettingsFromStore = combine(
        combine(
            preferencesRepository.glassMaterial,
            preferencesRepository.glassColorStrength,
            preferencesRepository.glassFrostDp
        ) { material, strength, frost -> Triple(material, strength, frost) },
        combine(
            preferencesRepository.glassTintHue,
            preferencesRepository.glassFilmOpacity,
            preferencesRepository.glassActiveTabOpacity
        ) { hue, film, tab -> Triple(hue, film, tab) }
    ) { first, second ->
        GlassPreviewState(
            material = first.first,
            colorStrength = first.second,
            frostDp = first.third,
            tintHue = second.first,
            filmOpacity = second.second,
            activeTabOpacity = second.third
        )
    }

    private val _cacheSize = MutableStateFlow("Calcul...")
    val cacheSize: StateFlow<String> = _cacheSize.asStateFlow()

    private val _isClearingCache = MutableStateFlow(false)
    val isClearingCache: StateFlow<Boolean> = _isClearingCache.asStateFlow()

    private val _isCleaningLibrary = MutableStateFlow(false)
    val isCleaningLibrary: StateFlow<Boolean> = _isCleaningLibrary.asStateFlow()

    private val _cacheClearMessage = MutableStateFlow<String?>(null)
    val cacheClearMessage: StateFlow<String?> = _cacheClearMessage.asStateFlow()

    init {
        refreshCacheSize()
        // Synchronise l'aperçu avec le DataStore (valeurs enregistrées, changements externes).
        viewModelScope.launch {
            glassSettingsFromStore.collect { stored ->
                if (glassWritesInFlight == 0) {
                    _glassPreview.value = stored
                }
            }
        }
    }

    /** Met à jour l'aperçu tout de suite, puis enregistre dans le DataStore. */
    private fun updateGlass(
        preview: (GlassPreviewState) -> GlassPreviewState,
        persist: suspend () -> Unit
    ) {
        _glassPreview.update(preview)
        glassWritesInFlight++
        viewModelScope.launch {
            try {
                persist()
            } finally {
                glassWritesInFlight--
            }
        }
    }

    /**
     * Ancien sélecteur : applique la table de migration de `app_theme_mode`
     * (DARK → Sombre + Néon, LIGHT → Clair + Minimal, SYSTEM → Auto + Minimal).
     */
    fun setThemeMode(mode: AppThemeMode) {
        viewModelScope.launch {
            when (mode) {
                AppThemeMode.DARK -> {
                    preferencesRepository.setAppearanceMode(AppearanceMode.DARK)
                    preferencesRepository.setAppStyle(AppStyle.NEON)
                }
                AppThemeMode.LIGHT -> {
                    preferencesRepository.setAppearanceMode(AppearanceMode.LIGHT)
                    preferencesRepository.setAppStyle(AppStyle.MINIMAL)
                }
                AppThemeMode.SYSTEM -> {
                    preferencesRepository.setAppearanceMode(AppearanceMode.AUTO)
                    preferencesRepository.setAppStyle(AppStyle.MINIMAL)
                }
            }
        }
    }

    fun setAppearanceMode(mode: AppearanceMode) {
        viewModelScope.launch { preferencesRepository.setAppearanceMode(mode) }
    }

    fun setAppStyle(style: AppStyle) {
        viewModelScope.launch { preferencesRepository.setAppStyle(style) }
    }

    fun setAccentChoice(choice: AccentChoice) {
        viewModelScope.launch { preferencesRepository.setAccentChoice(choice) }
    }

    fun setEveningStartMinutes(minutes: Int) {
        viewModelScope.launch { preferencesRepository.setEveningStartMinutes(minutes) }
    }

    fun setEveningEndMinutes(minutes: Int) {
        viewModelScope.launch { preferencesRepository.setEveningEndMinutes(minutes) }
    }

    fun setDynamicColor(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setDynamicColor(enabled)
        }
    }

    fun setGlassMaterial(material: GlassMaterial) {
        updateGlass(
            preview = { it.copy(material = material) },
            persist = { preferencesRepository.setGlassMaterial(material) }
        )
    }

    fun setGlassColorStrength(percent: Int) {
        updateGlass(
            preview = { it.copy(colorStrength = percent) },
            persist = { preferencesRepository.setGlassColorStrength(percent) }
        )
    }

    fun setGlassFrostDp(dp: Int) {
        updateGlass(
            preview = { it.copy(frostDp = dp) },
            persist = { preferencesRepository.setGlassFrostDp(dp) }
        )
    }

    /** Choisit la teinte du verre ; `null` = Aucune. */
    fun setGlassTintHue(hue: Float?) {
        updateGlass(
            preview = { it.copy(tintHue = hue) },
            persist = { preferencesRepository.setGlassTintHue(hue) }
        )
    }

    fun setGlassFilmOpacity(percent: Int) {
        updateGlass(
            preview = { it.copy(filmOpacity = percent) },
            persist = { preferencesRepository.setGlassFilmOpacity(percent) }
        )
    }

    fun setGlassActiveTabOpacity(percent: Int) {
        updateGlass(
            preview = { it.copy(activeTabOpacity = percent) },
            persist = { preferencesRepository.setGlassActiveTabOpacity(percent) }
        )
    }

    /** Mise à jour 084 — interrupteur « Texte plus lisible » du verre. */
    fun setGlassReadableText(enabled: Boolean) {
        viewModelScope.launch { preferencesRepository.setGlassReadableText(enabled) }
    }

    /** Mise à jour 084 — interrupteur « Réduire la transparence » du verre. */
    fun setGlassFollowReduceTransparency(enabled: Boolean) {
        viewModelScope.launch { preferencesRepository.setGlassFollowReduceTransparency(enabled) }
    }

    fun setReduceAnimations(enabled: Boolean) {
        viewModelScope.launch { preferencesRepository.setReduceAnimations(enabled) }
    }

    fun setTextSizePreference(value: TextSizePreference) {
        viewModelScope.launch { preferencesRepository.setTextSizePreference(value) }
    }

    fun setListDensityPreference(value: ListDensityPreference) {
        viewModelScope.launch { preferencesRepository.setListDensityPreference(value) }
    }

    fun setAutoPlay(enabled: Boolean) {
        viewModelScope.launch { preferencesRepository.setAutoPlay(enabled) }
    }

    fun setGaplessPlayback(enabled: Boolean) {
        viewModelScope.launch { preferencesRepository.setGaplessPlayback(enabled) }
    }

    fun setResumePlayback(enabled: Boolean) {
        viewModelScope.launch { preferencesRepository.setResumePlayback(enabled) }
    }

    /** Choisit la présentation du lecteur (mise à jour 048). */
    fun setPlayerPresentation(value: PlayerPresentation) {
        viewModelScope.launch { preferencesRepository.setPlayerPresentation(value) }
    }

    fun refreshCacheSize() {
        viewModelScope.launch(Dispatchers.IO) {
            val sizeStr = CacheManager.getFormattedCacheSize(getApplication())
            _cacheSize.value = sizeStr
        }
    }

    fun clearCache() {
        viewModelScope.launch {
            _isClearingCache.value = true
            val success = withContext(Dispatchers.IO) {
                CacheManager.clearCache(getApplication())
            }
            refreshCacheSize()
            _isClearingCache.value = false
            _cacheClearMessage.value = if (success) {
                "Cache effacé avec succès !"
            } else {
                "Erreur lors de l'effacement du cache."
            }
        }
    }

    /**
     * Rescanne le stockage, supprime tous les fichiers fantômes ou supprimés
     * et synchronise la base de données Room locale pour libérer la mémoire.
     */
    fun cleanAndRescanLibrary() {
        viewModelScope.launch {
            _isCleaningLibrary.value = true
            try {
                val validCount = audioRepository.refreshMediaStoreScan()
                _cacheClearMessage.value = "Bibliothèque nettoyée : $validCount morceau(x) actif(s) sur le téléphone"
            } catch (e: Exception) {
                _cacheClearMessage.value = "Erreur nettoyage bibliothèque : ${e.message}"
            } finally {
                _isCleaningLibrary.value = false
            }
        }
    }

    fun dismissCacheMessage() {
        _cacheClearMessage.value = null
    }

    fun checkForUpdates() {
        viewModelScope.launch {
            appUpdateManager.checkForUpdates()
        }
    }

    fun downloadAndInstallUpdate(downloadUrl: String) {
        viewModelScope.launch {
            appUpdateManager.downloadAndInstallApk(downloadUrl)
        }
    }

    fun installExistingApk() {
        val existing = appUpdateManager.getExistingDownloadedApk(0)
        if (existing != null) {
            appUpdateManager.installApk(existing)
        }
    }

    fun resetUpdateState() {
        appUpdateManager.resetState()
    }

    // --- Mise à jour 038 — Sauvegarde et export des réglages -------------------------------------

    /** Sérialise les réglages courants en JSON (thème, style, verre, karaoké, favoris). */
    suspend fun exportSettingsJson(): String = preferencesRepository.backupSnapshot().toJson()

    /**
     * Analyse et valide un JSON de sauvegarde sans l'appliquer. Retourne la sauvegarde pour
     * l'aperçu de confirmation, ou lève [BackupFormatException] si le fichier est invalide.
     */
    fun parseImportedSettings(json: String): SettingsBackup = SettingsBackupManager.fromJson(json)

    /** Applique une sauvegarde préalablement validée et confirmée par l'utilisateur. */
    suspend fun applyImportedSettings(backup: SettingsBackup) {
        preferencesRepository.applyBackup(backup)
    }

    // --- Mise à jour 040 — Réinitialisation par section -------------------------------------------

    /** Remet la section Apparence par défaut. */
    fun resetAppearanceSection() {
        viewModelScope.launch { preferencesRepository.resetAppearanceSection() }
    }

    /** Remet la section Lecture par défaut. */
    fun resetPlaybackSection() {
        viewModelScope.launch { preferencesRepository.resetPlaybackSection() }
    }

    /** Remet la section Paroles (karaoké) par défaut. */
    fun resetKaraokeSection() {
        viewModelScope.launch { preferencesRepository.resetKaraokeSection() }
    }

    private companion object {
        /** Intervalle de rafraîchissement de l'heure pour « Sombre le soir ». */
        const val EveningTickMillis = 30_000L

        fun currentMinuteOfDay(): Int {
            val now = Calendar.getInstance()
            return now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)
        }
    }
}
