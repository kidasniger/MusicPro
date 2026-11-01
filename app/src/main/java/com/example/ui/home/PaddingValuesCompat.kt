package com.example.ui.home

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection

/**
 * Équivalents start/end (compatibles RTL) de calculateLeftPadding / calculateRightPadding.
 * Évite la dépendance à un import d'extension non résolu à la compilation.
 */
internal fun PaddingValues.calculateStartPadding(layoutDirection: LayoutDirection): Dp =
    if (layoutDirection == LayoutDirection.Ltr) {
        calculateLeftPadding(layoutDirection)
    } else {
        calculateRightPadding(layoutDirection)
    }

internal fun PaddingValues.calculateEndPadding(layoutDirection: LayoutDirection): Dp =
    if (layoutDirection == LayoutDirection.Ltr) {
        calculateRightPadding(layoutDirection)
    } else {
        calculateLeftPadding(layoutDirection)
    }
