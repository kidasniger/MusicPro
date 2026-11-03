package com.example

import android.content.Context
import android.util.TypedValue
import androidx.appcompat.R as AppCompatR
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
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
 * Ces tests lancent réellement l'activité (onCreate → onStart → onResume) et vérifient le
 * thème : toute régression du thème, du manifest ou du chemin de création (ViewModels,
 * DataStore, repositories) fait échouer le build avant la publication d'un APK.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class MainActivitySmokeTest {

  @Test
  fun `MainActivity se lance sans crash`() {
    val controller = Robolectric.buildActivity(MainActivity::class.java)
    controller.setup() // onCreate → onStart → onResume
    val activity = controller.get()
    assertFalse("L'activité ne doit pas se terminer au lancement", activity.isFinishing)
    controller.destroy()
  }

  @Test
  fun `le thème de l'application est un thème AppCompat`() {
    // Les thèmes AppCompat définissent l'attribut colorPrimary du namespace AppCompat ;
    // un thème framework (DeviceDefault, Material…) ne le définit pas, et AppCompat
    // refuse alors l'activité au lancement.
    val base = ApplicationProvider.getApplicationContext<Context>()
    val themed = android.view.ContextThemeWrapper(base, R.style.Theme_MyApplication)
    val value = TypedValue()
    assertTrue(
      "Theme.MyApplication doit hériter d'un thème AppCompat (sinon crash au lancement)",
      themed.theme.resolveAttribute(AppCompatR.attr.colorPrimary, value, true)
    )
  }
}
