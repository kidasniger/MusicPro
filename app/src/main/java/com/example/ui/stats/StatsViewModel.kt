package com.example.ui.stats

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.ListeningStatEntity
import com.example.data.local.MusicProDatabase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class StatsViewModel(application: Application) : AndroidViewModel(application) {
    val stats: StateFlow<List<ListeningStatEntity>> =
        MusicProDatabase.getInstance(application).listeningStatsDao().observeAll()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}
