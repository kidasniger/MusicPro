package com.example.ui.radio

import com.example.data.local.AudioTrackEntity
import kotlin.random.Random

/** Les cinq mixes automatiques de l'écran Radio (mise à jour 022). */
enum class MixKind { DISCOVERY, ENERGY, CALM, PARTY, MEMORIES }

/** Un mix généré localement : son type et la liste ordonnée de ses morceaux. */
data class Mix(val kind: MixKind, val tracks: List<AudioTrackEntity>)

/**
 * Générateur de mixes 100 % local, sans réseau. La base ne contient ni tempo ni genre :
 * la durée du morceau sert de repère de rythme (court = plus nerveux, long = plus posé),
 * combinée à l'historique d'écoute (`lastPlayed`, `dateAdded`) et aux favoris.
 * Le tirage aléatoire est fixé par le jour : un mix reste stable pendant la journée.
 */
object MixEngine {

    const val MixSize = 30
    const val DayMs = 24L * 60L * 60L * 1000L
    const val EnergyMaxDurationMs = 210_000L
    const val CalmMinDurationMs = 270_000L
    const val PartyMinDurationMs = 150_000L
    const val PartyMaxDurationMs = 330_000L
    const val MemoriesAgeMs = 90L * DayMs

    fun generate(
        tracks: List<AudioTrackEntity>,
        favorites: Set<Long>,
        nowMs: Long
    ): List<Mix> {
        val seed = nowMs / DayMs
        val favoritesFirst = compareByDescending<AudioTrackEntity> { it.id in favorites }
            .thenByDescending { it.lastPlayed }

        val discovery = tracks
            .filter { it.lastPlayed == 0L }
            .sortedByDescending { it.dateAdded }
            .take(MixSize)

        val energy = tracks
            .filter { it.duration in 1..EnergyMaxDurationMs }
            .sortedWith(favoritesFirst)
            .take(MixSize)

        val calm = tracks
            .filter { it.duration >= CalmMinDurationMs }
            .sortedWith(favoritesFirst)
            .take(MixSize)

        val party = tracks
            .filter { (it.id in favorites || it.lastPlayed > 0L) && it.duration in PartyMinDurationMs..PartyMaxDurationMs }
            .shuffled(Random(seed))
            .take(MixSize)

        val memories = tracks
            .filter { it.lastPlayed in 1 until (nowMs - MemoriesAgeMs) }
            .sortedWith(compareByDescending<AudioTrackEntity> { it.id in favorites }.thenBy { it.lastPlayed })
            .take(MixSize)

        return listOf(
            Mix(MixKind.DISCOVERY, discovery),
            Mix(MixKind.ENERGY, energy),
            Mix(MixKind.CALM, calm),
            Mix(MixKind.PARTY, party),
            Mix(MixKind.MEMORIES, memories)
        )
    }
}
