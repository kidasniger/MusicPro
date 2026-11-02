package com.example.ui.settings

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.core.os.LocaleListCompat
import com.example.R
import com.example.ui.components.MusicProCard
import com.example.ui.theme.MusicProTokens

private data class LanguageOption(val tag: String, val label: Int, val id: String)
private val languageOptions = listOf(
    LanguageOption("", R.string.language_auto, "auto"),
    LanguageOption("fr", R.string.language_french, "fr"),
    LanguageOption("en", R.string.language_english, "en"),
    LanguageOption("ar", R.string.language_arabic, "ar"),
    LanguageOption("ha", R.string.language_hausa, "ha"),
    LanguageOption("dje", R.string.language_zarma, "dje")
)

@Composable
fun LanguageSettingsCard() {
    val selected = AppCompatDelegate.getApplicationLocales().toLanguageTags().substringBefore(',').ifBlank { "" }
    MusicProCard(modifier = Modifier.fillMaxWidth().testTag("language_settings_card")) {
        Column(
            modifier = Modifier.padding(MusicProTokens.Spacing.Lg),
            verticalArrangement = Arrangement.spacedBy(MusicProTokens.Spacing.Sm)
        ) {
            Text(stringResource(R.string.language_title), style = MaterialTheme.typography.titleMedium)
            languageOptions.chunked(3).forEach { group ->
                Row(horizontalArrangement = Arrangement.spacedBy(MusicProTokens.Spacing.Xs)) {
                    group.forEach { option ->
                        FilterChip(
                            selected = selected == option.tag,
                            onClick = { AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(option.tag)) },
                            label = { Text(stringResource(option.label)) },
                            modifier = Modifier.testTag("language_${option.id}")
                        )
                    }
                }
            }
            Text(
                stringResource(R.string.language_restart_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
