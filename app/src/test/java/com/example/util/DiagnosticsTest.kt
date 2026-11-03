package com.example.util

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Mise à jour 089 — journal de bord : contient les informations techniques nécessaires à un
 * signalement de bug et AUCUNE donnée personnelle ni clé API.
 */
@RunWith(RobolectricTestRunner::class)
class DiagnosticsTest {

    private val context: Context = ApplicationProvider.getApplicationContext()

    @Test
    fun `le diagnostic contient version modèle API et réglages`() {
        val text = Diagnostics.build(
            context = context,
            styleName = "Verre",
            isDark = true,
            glassMaterial = "BALANCED",
            readableText = true,
            reduceAnimations = false
        )
        assertTrue(text.contains("MusicPro"))
        assertTrue(text.contains("Appareil"))
        assertTrue(text.contains("API ${android.os.Build.VERSION.SDK_INT}"))
        assertTrue(text.contains("Verre"))
        assertTrue(text.contains("BALANCED"))
        assertTrue(text.contains("texte plus lisible"))
    }

    @Test
    fun `le diagnostic ne contient aucune clé ni donnée personnelle`() {
        val text = Diagnostics.build(
            context = context,
            styleName = "Néon",
            isDark = false,
            glassMaterial = "FASTEST",
            readableText = false,
            reduceAnimations = true
        )
        // Aucun motif de clé API (sk-…, gsk_…, long hex), aucun identifiant d'appareil.
        assertFalse(text.contains("sk-"))
        assertFalse(text.contains("gsk_"))
        assertFalse(text.contains("Bearer"))
        // Pas de longues séquences hexadécimales potentiellement secrètes.
        val hexRun = Regex("[0-9a-fA-F]{32,}")
        assertFalse(hexRun.containsMatchIn(text))
    }

    @Test
    fun `la version est non vide`() {
        val version = Diagnostics.appVersion(context)
        assertTrue(version.isNotBlank())
    }
}
