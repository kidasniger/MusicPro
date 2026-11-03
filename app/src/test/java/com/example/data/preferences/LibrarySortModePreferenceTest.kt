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
 * Mise à jour 055 — le dernier tri choisi de la bibliothèque est mémorisé
 * dans DataStore ; une valeur inconnue retombe sur A–Z.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class LibrarySortModePreferenceTest {

  @Test
  fun `par défaut le tri est A–Z`() = runTest {
    val repository = UserPreferencesRepository(ApplicationProvider.getApplicationContext())
    assertEquals(LibrarySortMode.TITLE_AZ, repository.librarySort.first())
  }

  @Test
  fun `le choix de tri est persisté`() = runTest {
    val repository = UserPreferencesRepository(ApplicationProvider.getApplicationContext())

    repository.setLibrarySort(LibrarySortMode.RECENT)
    assertEquals(LibrarySortMode.RECENT, repository.librarySort.first())

    repository.setLibrarySort(LibrarySortMode.DURATION)
    assertEquals(LibrarySortMode.DURATION, repository.librarySort.first())

    repository.setLibrarySort(LibrarySortMode.MOST_PLAYED)
    assertEquals(LibrarySortMode.MOST_PLAYED, repository.librarySort.first())

    repository.setLibrarySort(LibrarySortMode.TITLE_AZ)
    assertEquals(LibrarySortMode.TITLE_AZ, repository.librarySort.first())
  }

  @Test
  fun `une valeur stockée inconnue retombe sur A–Z`() {
    assertEquals(LibrarySortMode.TITLE_AZ, resolveLibrarySortMode(null))
    assertEquals(LibrarySortMode.TITLE_AZ, resolveLibrarySortMode("nimporte quoi"))
    assertEquals(LibrarySortMode.RECENT, resolveLibrarySortMode("RECENT"))
    assertEquals(LibrarySortMode.MOST_PLAYED, resolveLibrarySortMode("MOST_PLAYED"))
  }

  @Test
  fun `l'historique de recherche accepte 20 entrées`() = runTest {
    val repository = UserPreferencesRepository(ApplicationProvider.getApplicationContext())

    // L'appelant fournit l'historique déjà trié « plus récent en tête »
    // (cf. SearchUtils.mergeSearchHistory) : « requête 1 » est la plus récente.
    val twenty = (1..20).map { "requête $it" }
    repository.setSearchHistory(twenty)
    assertEquals(20, repository.searchHistory.first().size)
    assertEquals("requête 1", repository.searchHistory.first().first())
  }
}
