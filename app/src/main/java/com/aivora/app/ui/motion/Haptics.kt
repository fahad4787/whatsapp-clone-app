package com.aivora.app.ui.motion

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback

class AivoraHaptics(private val haptic: HapticFeedback) {
    fun tick() = haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
    fun confirm() = haptic.performHapticFeedback(HapticFeedbackType.LongPress)
    fun reject() = haptic.performHapticFeedback(HapticFeedbackType.LongPress)
}

@Composable
fun rememberAivoraHaptics(): AivoraHaptics {
    val haptic = LocalHapticFeedback.current
    return remember(haptic) { AivoraHaptics(haptic) }
}
