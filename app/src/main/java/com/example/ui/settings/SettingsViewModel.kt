package com.example.ui.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.preferences.AppThemeMode
import com.example.data.preferences.UserPreferencesRepository
import com.example.data.repository.AudioRepository
import com.example.updater.AppUpdateManager
import com.example.updater.DownloadState
import com.example.updater.UpdateCheckState
import com.example.util.CacheManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val preferencesRepository = UserPreferencesRepository(application)
    private val audioRepository = AudioRepository.getInstance(application)
    val appUpdateManager = AppUpdateManager(application, preferencesRepository)

    val updateCheckState: StateFlow<UpdateCheckState> = appUpdateManager.updateCheckState
    val downloadState: StateFlow<DownloadState> = appUpdateManager.downloadState

    val currentVersionName: String = appUpdateManager.getCurrentVersionName()

    val themeMode: StateFlow<AppThemeMode> = preferencesRepository.themeMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppThemeMode.DARK)

    val isDynamicColor: StateFlow<Boolean> = preferencesRepository.isDynamicColorEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

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
    }

    fun setThemeMode(mode: AppThemeMode) {
        viewModelScope.launch {
            preferencesRepository.setThemeMode(mode)
        }
    }

    fun setDynamicColor(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setDynamicColor(enabled)
        }
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
                _cacheClearMessage.value = "✓ Bibliothèque nettoyée : $validCount morceau(x) actif(s) sur le téléphone"
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
}
