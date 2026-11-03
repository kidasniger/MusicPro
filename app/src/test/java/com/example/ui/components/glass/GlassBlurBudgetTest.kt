package com.example.ui.components.glass

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Mise à jour 086 — budget de flou partagé : un seul calque flouté par écran ; les priorités
 * décident qui garde le slot ; la libération réveille les surfaces en attente.
 */
class GlassBlurBudgetTest {

    @Test
    fun `le premier demandeur obtient le slot`() {
        val budget = GlassBlurBudget(maxBlurSurfaces = 1)
        val bar = Any()
        assertTrue(budget.request(bar, GlassBlurPriorityFloatingBar))
        assertEquals(1, budget.grantedCount())
        budget.release(bar)
        assertEquals(0, budget.grantedCount())
    }

    @Test
    fun `le second demandeur est débouté sans priorité`() {
        val budget = GlassBlurBudget(maxBlurSurfaces = 1)
        val first = Any()
        val second = Any()
        assertTrue(budget.request(first, GlassBlurPriorityFloatingBar))
        assertFalse(budget.request(second, GlassBlurPriorityDefault))
        assertEquals(1, budget.grantedCount())
    }

    @Test
    fun `la priorité la plus haute vole le slot`() {
        val budget = GlassBlurBudget(maxBlurSurfaces = 1)
        val navBar = Any()
        val floatingBar = Any()
        assertTrue(budget.request(navBar, GlassBlurPriorityNavBar))
        // La barre flottante (priorité 100) vole le flou à la barre d'onglets (50).
        assertTrue(budget.request(floatingBar, GlassBlurPriorityFloatingBar))
        assertEquals(1, budget.grantedCount())
        // Une surface secondaire (0) ne peut pas voler à son tour.
        assertFalse(budget.request(Any(), GlassBlurPriorityDefault))
    }

    @Test
    fun `un vol ne passe jamais au détriment d une priorité supérieure`() {
        val budget = GlassBlurBudget(maxBlurSurfaces = 1)
        val floatingBar = Any()
        assertTrue(budget.request(floatingBar, GlassBlurPriorityFloatingBar))
        // La barre d'onglets (50) ne vole pas la barre flottante (100).
        assertFalse(budget.request(Any(), GlassBlurPriorityNavBar))
    }

    @Test
    fun `la libération permet une nouvelle attribution`() {
        val budget = GlassBlurBudget(maxBlurSurfaces = 1)
        val first = Any()
        val second = Any()
        budget.request(first, GlassBlurPriorityDefault)
        budget.release(first)
        assertTrue(budget.request(second, GlassBlurPriorityDefault))
    }

    @Test
    fun `la version change à chaque événement pour réveiller les surfaces`() {
        val budget = GlassBlurBudget(maxBlurSurfaces = 1)
        val before = budget.version
        val id = Any()
        budget.request(id, GlassBlurPriorityDefault)
        val afterGrant = budget.version
        assertTrue(afterGrant > before)
        budget.release(id)
        assertTrue(budget.version > afterGrant)
    }

    @Test
    fun `deux slots pour un budget de deux`() {
        val budget = GlassBlurBudget(maxBlurSurfaces = 2)
        assertTrue(budget.request(Any(), GlassBlurPriorityDefault))
        assertTrue(budget.request(Any(), GlassBlurPriorityDefault))
        assertFalse(budget.request(Any(), GlassBlurPriorityDefault))
        assertEquals(2, budget.grantedCount())
    }
}
