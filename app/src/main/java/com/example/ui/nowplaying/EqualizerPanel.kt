package com.example.ui.nowplaying

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.MusicProCyanNeon
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * Mise à jour 045 — Onglet « Réglages audio » du lecteur : version compacte de l'égaliseur
 * (mise à jour 037) sans feuille du bas : interrupteur, préréglages en chips, bandes −/+ et
 * bouton « Comparer » A/B de 3 s.
 */
@Composable
fun EqualizerPanel(
    enabled: Boolean,
    preset: String,
    levels: List<Int>,
    onEnabledChange: (Boolean) -> Unit,
    onPresetChange: (String) -> Unit,
    onBandChange: (Int, Int) -> Unit,
    onReset: () -> Unit,
    modifier: Modifier = Modifier
) {
    val presetEntries = listOf(
        "Flat" to R.string.eq_preset_normal,
        "Bass Boost" to R.string.eq_preset_bass,
        "Vocal" to R.string.eq_preset_vocal,
        "Classical" to R.string.eq_preset_classical,
        "Pop" to R.string.eq_preset_pop,
        "Rock" to R.string.eq_preset_rock
    )
    val bandLabels = listOf("60 Hz", "230 Hz", "910 Hz", "3.6 kHz", "14 kHz")
    val scope = rememberCoroutineScope()
    var compareJob by remember { mutableStateOf<kotlinx.coroutines.Job?>(null) }
    val isComparing = compareJob?.isActive == true

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Equalizer,
                    contentDescription = null,
                    tint = MusicProCyanNeon,
                    modifier = Modifier.size(20.dp)
                )
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 10.dp)
                ) {
                    Text(
                        stringResource(R.string.eq_title),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        stringResource(if (enabled) R.string.eq_effect_on else R.string.eq_effect_off),
                        fontSize = 11.sp,
                        color = if (enabled) MusicProCyanNeon else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(checked = enabled, onCheckedChange = onEnabledChange)
            }

            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                presetEntries.forEach { (key, labelRes) ->
                    FilterChip(
                        selected = preset == key,
                        onClick = { onPresetChange(key) },
                        label = { Text(stringResource(labelRes), fontSize = 9.sp) }
                    )
                }
            }

            bandLabels.forEachIndexed { index, label ->
                val level = levels.getOrElse(index) { 0 }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(label, color = MaterialTheme.colorScheme.onBackground, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        Text(
                            (level / 100f).roundToInt().toString() + " dB",
                            color = MusicProCyanNeon,
                            fontSize = 11.sp
                        )
                    }
                    IconButton(
                        onClick = { onBandChange(index, (level - 100).coerceIn(-1500, 1500)) },
                        modifier = Modifier.size(40.dp)
                    ) { Text("−", color = MaterialTheme.colorScheme.onBackground, fontSize = 20.sp) }
                    IconButton(
                        onClick = { onBandChange(index, (level + 100).coerceIn(-1500, 1500)) },
                        modifier = Modifier.size(40.dp)
                    ) { Text("+", color = MaterialTheme.colorScheme.onBackground, fontSize = 18.sp) }
                }
            }

            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        compareJob?.cancel()
                        compareJob = scope.launch {
                            val initial = enabled
                            try {
                                repeat(3) {
                                    onEnabledChange(if (it % 2 == 0) !initial else initial)
                                    delay(1000)
                                }
                            } finally {
                                onEnabledChange(initial)
                            }
                        }
                    },
                    enabled = !isComparing,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Equalizer, null, Modifier.size(14.dp))
                    Text(
                        stringResource(if (isComparing) R.string.eq_comparing else R.string.eq_compare),
                        fontSize = 11.sp,
                        modifier = Modifier.padding(start = 6.dp)
                    )
                }
                OutlinedButton(
                    onClick = onReset,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Refresh, null, Modifier.size(14.dp))
                    Text(
                        stringResource(R.string.eq_reset_all),
                        fontSize = 11.sp,
                        modifier = Modifier.padding(start = 6.dp)
                    )
                }
            }
        }
    }
}
