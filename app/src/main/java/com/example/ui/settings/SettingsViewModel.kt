package com.example.ui.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.preferences.AppThemeMode
import com.example.data.preferences.UserPreferencesRepository
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
