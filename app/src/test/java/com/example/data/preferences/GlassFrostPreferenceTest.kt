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

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class GlassFrostPreferenceTest {

    /** Une seule méthode : le défaut est lu avant toute écriture, puis les valeurs sont relues et bornées. */
    @Test
    fun glassFrostDefaultsToSixDpAndPersists() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repo = UserPreferencesRepository(context)

        assertEquals(6, repo.glassFrostDp.first())

        repo.setGlassFrostDp(0)
        assertEquals(0, repo.glassFrostDp.first())

        repo.setGlassFrostDp(24)
        assertEquals(24, repo.glassFrostDp.first())

        repo.setGlassFrostDp(13)
        assertEquals(13, repo.glassFrostDp.first())

        // Une valeur hors limites est bornée à l'écriture.
        repo.setGlassFrostDp(99)
        assertEquals(24, repo.glassFrostDp.first())
    }
}
