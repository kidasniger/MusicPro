package com.example.ui.artist

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.preferences.UserPreferencesRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Artistes marqués d'une étoile (mise à jour 024), conservés dans DataStore. */
class ArtistFavoritesViewModel(application: Application) : AndroidViewModel(application) {

    private val preferences = UserPreferencesRepository(application)

    val favoriteArtists: StateFlow<Set<String>> = preferences.favoriteArtists
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptySet()
        )

    fun toggle(name: String) {
        viewModelScope.launch {
            val current = favoriteArtists.value
            preferences.setFavoriteArtists(if (name in current) current - name else current + name)
        }
    }
}
