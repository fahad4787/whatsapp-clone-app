package com.aivora.app.ui.components

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput

/**
 * For full-screen layers drawn over another screen. A background alone is see-through to touch,
 * so taps would press buttons nobody can see. Controls inside the layer still work.
 */
fun Modifier.blockTouchesBehind(): Modifier = pointerInput(Unit) { detectTapGestures { } }
