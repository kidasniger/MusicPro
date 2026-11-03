package com.example.ui.navigation

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.filter

/**
 * Mise à jour 071 — bus de retour en haut de liste.
 *
 * Un second tap sur l'onglet actif publie la clé de la section ; chaque écran
 * branché avec [ScrollToTopEffect] fait défiler sa liste principale vers le
 * haut avec une animation.
 */
object TabScrollToTopBus {

    private val _events = MutableSharedFlow<String>(extraBufferCapacity = 8)

    val events: SharedFlow<String> = _events.asSharedFlow()

    /** Demande le retour en haut pour la section donnée (nom de [com.example.ui.home.NavigationSection]). */
    fun request(sectionKey: String) {
        _events.tryEmit(sectionKey)
    }
}

/**
 * Branche un état de liste au bus : quand un événement correspondant à [sectionKey]
 * arrive et que la liste n'est pas déjà en haut, elle y remonte avec défilement animé.
 */
@Composable
fun ScrollToTopEffect(sectionKey: String, listState: LazyListState) {
    LaunchedEffect(listState, sectionKey) {
        TabScrollToTopBus.events.filter { it == sectionKey }.collect {
            if (listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 0) {
                listState.animateScrollToItem(0)
            }
        }
    }
}

/** Variante pour les grilles ([LazyVerticalGrid]). */
@Composable
fun ScrollToTopEffect(sectionKey: String, gridState: LazyGridState) {
    LaunchedEffect(gridState, sectionKey) {
        TabScrollToTopBus.events.filter { it == sectionKey }.collect {
            if (gridState.firstVisibleItemIndex > 0 || gridState.firstVisibleItemScrollOffset > 0) {
                gridState.animateScrollToItem(0)
            }
        }
    }
}
