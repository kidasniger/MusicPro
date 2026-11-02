package com.example.ui.radio

import com.example.data.local.AudioTrackEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MixEngineTest {

    private val now = 1_000L * MixEngine.DayMs

    private fun track(
        id: Long,
        duration: Long = 200_000L,
        lastPlayed: Long = 0L,
        dateAdded: Long = id
    ) = AudioTrackEntity(
        id = id,
        title = "t$id",
        artist = "a",
        album = "b",
        duration = duration,
        contentUri = "content://$id",
        dateAdded = dateAdded,
        lastPlayed = lastPlayed
    )

    private fun mix(mixes: List<Mix>, kind: MixKind) = mixes.first { it.kind == kind }.tracks

    @Test
    fun discoveryContainsOnlyUnplayedTracksNewestFirst() {
        val tracks = listOf(track(1), track(2), track(3, lastPlayed = now - 1))
        val discovery = mix(MixEngine.generate(tracks, emptySet(), now), MixKind.DISCOVERY)
        assertEquals(listOf(2L, 1L), discovery.map { it.id })
    }

    @Test
    fun energyKeepsShortTracksWithFavoritesFirst() {
        val tracks = listOf(track(1, duration = 180_000L), track(2, duration = 190_000L), track(3, duration = 400_000L))
        val energy = mix(MixEngine.generate(tracks, setOf(2L), now), MixKind.ENERGY)
        assertEquals(listOf(2L, 1L), energy.map { it.id })
    }

    @Test
    fun calmKeepsOnlyLongTracks() {
        val tracks = listOf(track(1, duration = 180_000L), track(2, duration = 400_000L))
        val calm = mix(MixEngine.generate(tracks, emptySet(), now), MixKind.CALM)
        assertEquals(listOf(2L), calm.map { it.id })
    }

    @Test
    fun memoriesKeepsOnlyTracksNotPlayedForNinetyDays() {
        val old = now - MixEngine.MemoriesAgeMs - 1
        val tracks = listOf(track(1, lastPlayed = old), track(2, lastPlayed = now - 1), track(3))
        val memories = mix(MixEngine.generate(tracks, emptySet(), now), MixKind.MEMORIES)
        assertEquals(listOf(1L), memories.map { it.id })
    }

    @Test
    fun partyIsStableWithinTheSameDayAndCappedAtMixSize() {
        val tracks = (1L..60L).map { track(it, lastPlayed = now - it) }
        val first = mix(MixEngine.generate(tracks, emptySet(), now), MixKind.PARTY)
        val second = mix(MixEngine.generate(tracks, emptySet(), now + 1_000L), MixKind.PARTY)
        assertEquals(first.map { it.id }, second.map { it.id })
        assertTrue(first.size <= MixEngine.MixSize)
    }
}
