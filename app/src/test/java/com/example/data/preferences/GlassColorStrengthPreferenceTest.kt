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
class GlassColorStrengthPreferenceTest {

    /** Une seule méthode : le défaut est lu avant toute écriture, puis les valeurs sont relues et bornées. */
    @Test
    fun glassColorStrengthDefaultsToSeventyAndPersists() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repo = UserPreferencesRepository(context)

        assertEquals(70, repo.glassColorStrength.first())

        repo.setGlassColorStrength(0)
        assertEquals(0, repo.glassColorStrength.first())

        repo.setGlassColorStrength(100)
        assertEquals(100, repo.glassColorStrength.first())

        repo.setGlassColorStrength(35)
        assertEquals(35, repo.glassColorStrength.first())

        // Une valeur hors limites est bornée à l'écriture.
        repo.setGlassColorStrength(180)
        assertEquals(100, repo.glassColorStrength.first())
    }
}
