package com.example.playback

import android.os.Bundle
import androidx.media3.session.SessionCommand

object AudioEffectCommands {
    const val ACTION_SET_ENABLED = "com.example.musicpro.audiofx.SET_ENABLED"
    const val ACTION_SET_PRESET = "com.example.musicpro.audiofx.SET_PRESET"
    const val ACTION_SET_BAND = "com.example.musicpro.audiofx.SET_BAND"
    const val ACTION_RESET = "com.example.musicpro.audiofx.RESET"

    const val KEY_ENABLED = "enabled"
    const val KEY_PRESET = "preset"
    const val KEY_BAND = "band"
    const val KEY_LEVEL = "level"

    val SET_ENABLED = SessionCommand(ACTION_SET_ENABLED, Bundle.EMPTY)
    val SET_PRESET = SessionCommand(ACTION_SET_PRESET, Bundle.EMPTY)
    val SET_BAND = SessionCommand(ACTION_SET_BAND, Bundle.EMPTY)
    val RESET = SessionCommand(ACTION_RESET, Bundle.EMPTY)
}
