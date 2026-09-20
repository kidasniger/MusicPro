package com.example.lyrics

import java.io.ByteArrayOutputStream
import java.nio.charset.StandardCharsets

/**
 * Encode les événements SYLT suivant l'encodage Unicode déclaré par le frame.
 *
 * ID3 SYLT utilise un indicateur d'encodage distinct du format des timestamps.
 * L'encodage UTF-16 est représenté par la valeur 1 et les chaînes Unicode sont
 * terminées par deux octets nuls.
 */
internal object SyltCodec {
    const val TEXT_ENCODING_UTF16 = 1

    fun serialize(lines: List<LyricLine>): ByteArray {
        val stream = ByteArrayOutputStream()

        for (line in lines) {
            val textBytes = line.text.toByteArray(StandardCharsets.UTF_16BE)
            stream.write(textBytes)
            stream.write(0)
            stream.write(0)

            val timestamp = line.timeMs.coerceAtLeast(0L)
            stream.write(((timestamp ushr 24) and 0xFF).toInt())
            stream.write(((timestamp ushr 16) and 0xFF).toInt())
            stream.write(((timestamp ushr 8) and 0xFF).toInt())
            stream.write((timestamp and 0xFF).toInt())
        }

        return stream.toByteArray()
    }
}
