package com.example.permissions

import android.content.Context
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class PermissionUiState(
    val isAudioGranted: Boolean = false,
    val isNotificationGranted: Boolean = false,
    val hasAttemptedRequest: Boolean = false,
    val isDeniedExplanationNeeded: Boolean = false
) {
    val canProceedToApp: Boolean
        get() = isAudioGranted
}

class PermissionViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(PermissionUiState())
    val uiState: StateFlow<PermissionUiState> = _uiState.asStateFlow()

    fun checkPermissions(context: Context) {
        val audioGranted = PermissionUtils.isAudioPermissionGranted(context)
        val notifGranted = PermissionUtils.isNotificationPermissionGranted(context)

        _uiState.update { current ->
            current.copy(
                isAudioGranted = audioGranted,
                isNotificationGranted = notifGranted,
                isDeniedExplanationNeeded = current.hasAttemptedRequest && !audioGranted
            )
        }
    }

    fun onPermissionsResult(
        context: Context,
        result: Map<String, Boolean>
    ) {
        val audioPerm = PermissionUtils.getAudioPermission()
        val audioGranted = result[audioPerm] ?: PermissionUtils.isAudioPermissionGranted(context)
        val notifGranted = PermissionUtils.isNotificationPermissionGranted(context)

        _uiState.update {
            it.copy(
                isAudioGranted = audioGranted,
                isNotificationGranted = notifGranted,
                hasAttemptedRequest = true,
                isDeniedExplanationNeeded = !audioGranted
            )
        }
    }

    fun markRequestAttempted() {
        _uiState.update {
            it.copy(hasAttemptedRequest = true)
        }
    }
}
