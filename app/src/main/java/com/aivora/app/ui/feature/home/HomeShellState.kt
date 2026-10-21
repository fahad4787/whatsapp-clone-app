package com.aivora.app.ui.feature.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue

/** Survives leaving Home (status viewer, etc.) so back restores the same tab. */
object HomeShellState {
    var selectedTab by mutableIntStateOf(0)
}
