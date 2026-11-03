package com.example

import android.content.Context
import android.util.TypedValue
import androidx.appcompat.R as AppCompatR
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Test anti-régression du lancement (mise à jour 036).
 *
 * Historique : [MainActivity] est devenue une AppCompatActivity pour
 * `AppCompatDelegate.setApplicationLocales`, mais le thème restait un thème framework
 * (`android:Theme.DeviceDefault.NoActionBar`), ce qui provoquait un crash immédiat au
 * démarrage : « You need to use a Theme.AppCompat theme (or a descendant) with this activity ».
 *
 * Les thèmes AppCompat définissent l'attribut `colorPrimary` du namespace AppCompat ;
 * un thème framework ne le définit jamais. Si ce test échoue, AppCompat refusera
 * l'activité au lancement — corriger le thème avant de publier un APK.
 *
 * NB : ce test ne lance volontairement PAS l'activité (un lancement réel de MainActivity
 * laisse des coroutines/vues actives qui font échouer les tests Compose suivants dans la
 * même JVM avec AppNotIdleException).
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class MainActivitySmokeTest {

  @Test
  fun `le thème de l'application est un thème AppCompat`() {
    val base = ApplicationProvider.getApplicationContext<Context>()
    val themed = android.view.ContextThemeWrapper(base, R.style.Theme_MyApplication)
    val value = TypedValue()
    assertTrue(
      "Theme.MyApplication doit hériter d'un thème AppCompat (sinon crash au lancement)",
      themed.theme.resolveAttribute(AppCompatR.attr.colorPrimary, value, true)
    )
  }
}
