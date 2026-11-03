package com.example.ui.nowplaying

import android.media.AudioDeviceInfo
import android.media.AudioManager
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeDown
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Speaker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.MusicProCyanNeon

/** Types de sorties proposés dans la feuille (mise à jour 049). */
enum class AudioOutputType { SPEAKER, WIRED, BLUETOOTH, CAST }

/**
 * Recalcule la liste des sorties quand [refreshKey] change — il est incrémenté à chaque
 * ajout/retrait d'appareil audio par le rappel [android.media.AudioDeviceCallback].
 */
@Composable
internal fun rememberAudioOutputs(audioManager: AudioManager?, refreshKey: Int = 0): List<AudioOutputDevice> =
    androidx.compose.runtime.remember(refreshKey) { audioOutputDevices(audioManager) }

/** Une sortie audio affichée : type, libellé et état actif. */
data class AudioOutputDevice(
    val type: AudioOutputType,
    val icon: ImageVector,
    val nameRes: Int,
    val isActive: Boolean
)

/**
 * Mise à jour 049 — Feuille « Sortie audio » : haut-parleur, casque filaire, Bluetooth et Cast
 * (si une route distante existe), l'appareil actif coché, plus les boutons −/+ de volume.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudioOutputSheet(
    audioManager: AudioManager?,
    devicesVersion: Int = 0,
    volumePercent: Int,
    onVolumeUp: () -> Unit,
    onVolumeDown: () -> Unit,
    onDismiss: () -> Unit
) {
    val devices = rememberAudioOutputs(audioManager, devicesVersion)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp)
                .padding(bottom = 12.dp)
        ) {
            Text(
                text = stringResource(R.string.np_output_title),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = stringResource(R.string.np_output_subtitle),
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp)
            )

            devices.forEach { device ->
                OutputRow(device)
            }

            // Commandes de volume − / +
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                IconButton(onClick = onVolumeDown, modifier = Modifier.size(52.dp)) {
                    Icon(
                        Icons.AutoMirrored.Filled.VolumeDown,
                        contentDescription = stringResource(R.string.np_volume_down),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Text(
                    text = "$volumePercent %",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MusicProCyanNeon,
                    modifier = Modifier.padding(horizontal = 18.dp)
                )
                IconButton(onClick = onVolumeUp, modifier = Modifier.size(52.dp)) {
                    Icon(
                        Icons.AutoMirrored.Filled.VolumeUp,
                        contentDescription = stringResource(R.string.np_volume_up),
                        tint = MusicProCyanNeon,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun OutputRow(device: AudioOutputDevice) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        shape = RoundedCornerShape(14.dp),
        color = if (device.isActive) MusicProCyanNeon.copy(alpha = 0.10f)
        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = device.icon,
                contentDescription = null,
                tint = if (device.isActive) MusicProCyanNeon else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(22.dp)
            )
            Text(
                text = stringResource(device.nameRes),
                fontSize = 14.sp,
                fontWeight = if (device.isActive) FontWeight.SemiBold else FontWeight.Medium,
                color = if (device.isActive) MusicProCyanNeon else MaterialTheme.colorScheme.onBackground,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp)
            )
            if (device.isActive) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = stringResource(R.string.np_output_active),
                    tint = MusicProCyanNeon,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

/**
 * Liste les sorties audio disponibles. Le haut-parleur est toujours là ; casque filaire et
 * Bluetooth selon les appareils connectés ; Cast reste masqué tant qu'aucune intégration
 * dédiée n'est branchée (voir [castDeviceOrNull]).
 */
internal fun audioOutputDevices(audioManager: AudioManager?): List<AudioOutputDevice> {
    val outputs = audioManager?.getDevices(AudioManager.GET_DEVICES_OUTPUTS).orEmpty()
    val hasWired = outputs.any {
        it.type == AudioDeviceInfo.TYPE_WIRED_HEADPHONES ||
            it.type == AudioDeviceInfo.TYPE_WIRED_HEADSET ||
            it.type == AudioDeviceInfo.TYPE_USB_HEADSET
    }
    val hasBluetooth = outputs.any {
        it.type == AudioDeviceInfo.TYPE_BLUETOOTH_A2DP || it.type == AudioDeviceInfo.TYPE_BLE_HEADSET
    }
    val active = when {
        hasBluetooth -> AudioOutputType.BLUETOOTH
        hasWired -> AudioOutputType.WIRED
        else -> AudioOutputType.SPEAKER
    }
    return listOf(
        AudioOutputDevice(AudioOutputType.SPEAKER, Icons.Default.Speaker, R.string.np_device_speaker, active == AudioOutputType.SPEAKER),
        AudioOutputDevice(AudioOutputType.WIRED, Icons.Default.Headphones, R.string.np_device_wired, active == AudioOutputType.WIRED).takeIf { hasWired },
        AudioOutputDevice(AudioOutputType.BLUETOOTH, Icons.Default.Bluetooth, R.string.np_device_bluetooth, active == AudioOutputType.BLUETOOTH).takeIf { hasBluetooth }
    ).filterNotNull()
}

/**
 * Route Cast éventuelle. Sans le SDK Cast de Google (non embarqué), aucune route distante n'est
 * découverte sur la majorité des appareils : la ligne Cast reste simplement masquée. La fonction
 * reste le point d'extension pour une future intégration.
 */
private fun castDeviceOrNull(): AudioOutputDevice? = null
