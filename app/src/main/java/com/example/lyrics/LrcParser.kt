package com.example.lyrics

import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets
import java.util.regex.Pattern

object LrcParser {

    private val TIME_TAG_PATTERN = Pattern.compile("\\[(\\d{1,2}):(\\d{2})(?:[.:](\\d{1,3}))?\\]")
    private val METADATA_TAG_PATTERN = Pattern.compile("^\\[([a-zA-Z]+):(.*)\\]$")

    /**
     * Parse une chaîne de caractères au format LRC.
     */
    fun parse(lrcContent: String): LyricsData {
        val lines = lrcContent.lines()
        return parseLines(lines)
    }

    /**
     * Parse un flux d'entrée au format LRC.
     */
    fun parse(inputStream: InputStream): LyricsData {
        val reader = BufferedReader(InputStreamReader(inputStream, StandardCharsets.UTF_8))
        val lines = reader.readLines()
        return parseLines(lines)
    }

    private fun parseLines(rawLines: List<String>): LyricsData {
        var title: String? = null
        var artist: String? = null
        var album: String? = null
        var offsetMs = 0L
        val lyricLines = mutableListOf<LyricLine>()

        for (rawLine in rawLines) {
            val line = rawLine.trim()
            if (line.isBlank()) continue

            // 1. Vérification des métadonnées comme [ti:Song], [ar:Artist], [offset:100]
            val metaMatcher = METADATA_TAG_PATTERN.matcher(line)
            if (metaMatcher.matches()) {
                val tag = metaMatcher.group(1)?.lowercase()
                val value = metaMatcher.group(2)?.trim() ?: ""
                when (tag) {
                    "ti" -> title = value
                    "ar" -> artist = value
                    "al" -> album = value
                    "offset" -> {
                        offsetMs = value.toLongOrNull() ?: 0L
                    }
                }
                // Si la ligne ne contient qu'une balise de métadonnée, passer à la suivante
                if (!TIME_TAG_PATTERN.matcher(line).find()) {
                    continue
                }
            }

            // 2. Extraction des timestamps et du texte
            val timeMatcher = TIME_TAG_PATTERN.matcher(line)
            val timestamps = mutableListOf<Long>()
            var lastMatchEnd = 0

            while (timeMatcher.find()) {
                val minutesStr = timeMatcher.group(1) ?: "0"
                val secondsStr = timeMatcher.group(2) ?: "0"
                val fractionStr = timeMatcher.group(3)

                val minutes = minutesStr.toLongOrNull() ?: 0L
                val seconds = secondsStr.toLongOrNull() ?: 0L
                val fractionMs = parseFractionToMs(fractionStr)

                val timeMs = (minutes * 60_000L) + (seconds * 1_000L) + fractionMs
                timestamps.add(timeMs)
                lastMatchEnd = timeMatcher.end()
            }

            if (timestamps.isNotEmpty()) {
                // Le texte des paroles est tout ce qui suit le dernier timestamp
                val text = if (lastMatchEnd < line.length) {
                    line.substring(lastMatchEnd).trim()
                } else {
                    ""
                }

                // Pour chaque timestamp associé à cette ligne (support des timestamps multiples)
                for (t in timestamps) {
                    lyricLines.add(LyricLine(timeMs = t, text = text))
                }
            }
        }

        // Tri par ordre chronologique
        val sortedLines = lyricLines.sortedBy { it.timeMs }

        return LyricsData(
            title = title,
            artist = artist,
            album = album,
            offsetMs = offsetMs,
            lines = sortedLines,
            source = LyricsSource.LRC_FILE
        )
    }

    /**
     * Convertit la fraction de seconde (1 à 3 chiffres) en millisecondes.
     * Exemple : "5" -> 500ms, "50" -> 500ms, "05" -> 50ms, "123" -> 123ms
     */
    private fun parseFractionToMs(fractionStr: String?): Long {
        if (fractionStr.isNullOrEmpty()) return 0L
        return when (fractionStr.length) {
            1 -> (fractionStr.toLongOrNull() ?: 0L) * 100L
            2 -> (fractionStr.toLongOrNull() ?: 0L) * 10L
            3 -> fractionStr.toLongOrNull() ?: 0L
            else -> fractionStr.take(3).toLongOrNull() ?: 0L
        }
    }

    /**
     * Convertit une instance de LyricsData en texte au format standard LRC.
     */
    fun toLrcString(lyricsData: LyricsData): String {
        val sb = StringBuilder()
        lyricsData.title?.let { if (it.isNotBlank()) sb.append("[ti:").append(it).append("]\n") }
        lyricsData.artist?.let { if (it.isNotBlank()) sb.append("[ar:").append(it).append("]\n") }
        lyricsData.album?.let { if (it.isNotBlank()) sb.append("[al:").append(it).append("]\n") }

        for (line in lyricsData.lines) {
            val totalSec = line.timeMs / 1000
            val min = totalSec / 60
            val sec = totalSec % 60
            val hundredths = (line.timeMs % 1000) / 10
            sb.append(String.format(java.util.Locale.US, "[%02d:%02d.%02d]%s\n", min, sec, hundredths, line.text))
        }
        return sb.toString()
    }
}
