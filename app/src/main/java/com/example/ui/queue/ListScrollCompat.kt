package com.example.ui.queue

import androidx.compose.foundation.lazy.LazyListState

/** Défile la liste de [value] pixels et renvoie la distance réellement consommée (auto-défilement du glisser-déposer). */
internal fun LazyListState.scrollBy(value: Float): Float = dispatchRawDelta(value)
