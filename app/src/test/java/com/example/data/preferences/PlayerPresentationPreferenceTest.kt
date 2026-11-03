package com.example.data.preferences

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Mise à jour 048 — préférence « Présentation du lecteur » (pochette / vinyle / minimal) :
 * aller-retour DataStore, valeurs inconnues et remise à zéro de la section Lecture.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class PlayerPresentationPreferenceTest {

  @Test
  fun `par défaut la présentation est la pochette carrée`() = runTest {
    val repository = UserPreferencesRepository(ApplicationProvider.getApplicationContext())
    assertEquals(PlayerPresentation.POCHETTE, repository.playerPresentation.first())
  }

  @Test
  fun `le choix de présentation est persisté`() = runTest {
    val repository = UserPreferencesRepository(ApplicationProvider.getApplicationContext())

    repository.setPlayerPresentation(PlayerPresentation.VINYLE)
    assertEquals(PlayerPresentation.VINYLE, repository.playerPresentation.first())

    repository.setPlayerPresentation(PlayerPresentation.MINIMAL)
    assertEquals(PlayerPresentation.MINIMAL, repository.playerPresentation.first())

    repository.setPlayerPresentation(PlayerPresentation.POCHETTE)
    assertEquals(PlayerPresentation.POCHETTE, repository.playerPresentation.first())
  }

  @Test
  fun `une valeur stockée inconnue retombe sur la pochette`() {
    assertEquals(PlayerPresentation.POCHETTE, resolvePlayerPresentation(null))
    assertEquals(PlayerPresentation.POCHETTE, resolvePlayerPresentation("nimporte quoi"))
    assertEquals(PlayerPresentation.VINYLE, resolvePlayerPresentation("VINYLE"))
    assertEquals(PlayerPresentation.MINIMAL, resolvePlayerPresentation("MINIMAL"))
  }

  @Test
  fun `réinitialiser la section Lecture remet la pochette carrée`() = runTest {
    val repository = UserPreferencesRepository(ApplicationProvider.getApplicationContext())

    repository.setPlayerPresentation(PlayerPresentation.MINIMAL)
    assertEquals(PlayerPresentation.MINIMAL, repository.playerPresentation.first())

    repository.resetPlaybackSection()
    assertEquals(PlayerPresentation.POCHETTE, repository.playerPresentation.first())
  }
}
