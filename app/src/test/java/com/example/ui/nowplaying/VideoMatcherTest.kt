package com.example.ui.nowplaying

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Mise à jour 046 — détection du fichier vidéo associé à un morceau audio.
 */
class VideoMatcherTest {

  @Test
  fun `reconnait les extensions video courantes`() {
    assertTrue(VideoMatcher.isVideoFile("Concert.mp4"))
    assertTrue(VideoMatcher.isVideoFile("clip.MKV"))
    assertTrue(VideoMatcher.isVideoFile("tout.WEBM"))
    assertTrue(VideoMatcher.isVideoFile("v.MOV"))
    assertFalse(VideoMatcher.isVideoFile("chanson.mp3"))
    assertFalse(VideoMatcher.isVideoFile("pochette.jpg"))
    assertFalse(VideoMatcher.isVideoFile("lyrics.lrc"))
  }

  @Test
  fun `privilégie le fichier video du meme nom de base`() {
    val siblings = listOf("cover.jpg", "Live.mp4", "Live (clip).mkv")
    assertEquals(
      "Live.mp4",
      VideoMatcher.findVideoFile("/storage/music/Live.flac", siblings)
    )
  }

  @Test
  fun `retombe sur nimporte quelle video du dossier`() {
    val siblings = listOf("cover.jpg", "clip officiel.mp4")
    assertEquals(
      "clip officiel.mp4",
      VideoMatcher.findVideoFile("/storage/music/Live.flac", siblings)
    )
  }

  @Test
  fun `renvoie null sans video ni chemin`() {
    assertNull(VideoMatcher.findVideoFile("/storage/music/Live.flac", listOf("cover.jpg")))
    assertNull(VideoMatcher.findVideoFile(null, listOf("clip.mp4")))
    assertNull(VideoMatcher.findVideoFile("", listOf("clip.mp4")))
    assertNull(VideoMatcher.findVideoFile("/storage/music/Live.flac", emptyList()))
  }

  @Test
  fun `la comparaison du nom de base ignore la casse`() {
    val siblings = listOf("LIVE.MP4")
    assertEquals("LIVE.MP4", VideoMatcher.findVideoFile("/m/live.flac", siblings))
  }
}
