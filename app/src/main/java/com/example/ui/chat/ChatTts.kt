package com.example.ui.chat

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale

/**
 * Mise à jour 098 — « Lire à voix haute » sur appui long d'une réponse :
 * encapsule [TextToSpeech], ne parle que du texte débarrassé de la syntaxe
 * Markdown ([markdownToPlainText]). Une seule instance, réutilisée.
 */
object ChatTts {

    private var tts: TextToSpeech? = null
    private var ready = false

    fun speak(context: Context, text: String) {
        val clean = markdownToPlainText(text).trim()
        if (clean.isEmpty()) return
        if (tts == null) {
            tts = TextToSpeech(context.applicationContext) { status ->
                ready = status == TextToSpeech.SUCCESS
                if (ready) {
                    tts?.language = Locale.getDefault()
                }
            }
        }
        // L'initialisation est asynchrone : on tente la lecture, retentée à l'init suivante.
        val engine = tts ?: return
        if (ready) engine.speak(clean, TextToSpeech.QUEUE_FLUSH, null, "chat_message")
    }

    fun stop() {
        tts?.let { engine ->
            if (ready) engine.stop()
        }
    }

    fun shutdown() {
        tts?.shutdown()
        tts = null
        ready = false
    }
}
