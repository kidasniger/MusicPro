package com.example.ui.components.glass

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf

/** Priorité de flou de la barre d'onglets (barre structurelle). */
const val GlassBlurPriorityNavBar = 50

/** Priorité de flou de la barre flottante (mini-lecteur) : elle prime sur tout le reste. */
const val GlassBlurPriorityFloatingBar = 100

/** Priorité de flou par défaut des surfaces secondaires (feuilles, éléments de liste). */
const val GlassBlurPriorityDefault = 0

/**
 * Budget de flou partagé (mise à jour 086) : au plus [maxBlurSurfaces] surfaces d'un même écran
 * floutent réellement en même temps — un seul calque partagé en pratique — ; les suivantes se
 * replient sur l'aplat translucide ([GlassFlatFallbackAlpha]), jamais un flou par ligne.
 *
 * Chaque surface demande un slot avec une priorité ; si le budget est plein, la demande est
 * accordée seulement si sa priorité dépasse celle du slot le plus faible, qu'elle lui vole alors
 * (la barre flottante reprend le flou à un élément secondaire, jamais l'inverse). Les surfaces
 * déboutées observent [version] : quand un slot se libère, elles retentent leur chance.
 */
@Stable
class GlassBlurBudget internal constructor(val maxBlurSurfaces: Int = 1) {

    internal data class Claim(val id: Any, val priority: Int)

    private val claims = mutableStateListOf<Claim>()

    /** Compteur d'événements (attribution, vol, libération) ; lu par les surfaces pour retenter. */
    var version: Int by mutableIntStateOf(0)
        private set

    private var nextId = 0L

    /** Identifiant stable pour une surface (composé une fois par surface). */
    fun newClaimId(): Long = nextId++

    /**
     * Demande un slot de flou pour [id] avec [priority]. Appelé depuis un effet (jamais pendant
     * la composition). Renvoie vrai si le flou est accordé. [version] ne change que si l'état
     * effectif change — sinon les effets se relanceraient en boucle.
     */
    fun request(id: Any, priority: Int): Boolean {
        val existing = claims.firstOrNull { it.id == id }
        if (existing != null && existing.priority == priority) return true
        var changed = false
        if (existing != null) {
            claims.remove(existing)
            changed = true
        }
        val granted = if (claims.size < maxBlurSurfaces) {
            true
        } else {
            val weakest = claims.minByOrNull { it.priority }
            weakest == null || weakest.priority < priority
        }
        if (granted) {
            while (claims.size >= maxBlurSurfaces) {
                val weakest = claims.minByOrNull { it.priority } ?: break
                if (weakest.priority >= priority) break
                claims.remove(weakest)
                changed = true
            }
            claims.add(Claim(id, priority))
            changed = true
        }
        if (changed) version++
        return granted
    }

    /** Libère le slot de [id] (disposition de la surface). */
    fun release(id: Any) {
        if (claims.removeAll { it.id == id }) version++
    }

    /** Vrai si [id] détient actuellement un slot (lecture observée par la composition). */
    fun isGranted(id: Any): Boolean = claims.any { it.id == id }

    /** Nombre de slots actuellement attribués (tests et diagnostic). */
    fun grantedCount(): Int = claims.size
}

/** Budget de flou de l'écran courant, fourni par [GlassBackdropHost]. */
val LocalGlassBlurBudget = staticCompositionLocalOf { GlassBlurBudget() }
