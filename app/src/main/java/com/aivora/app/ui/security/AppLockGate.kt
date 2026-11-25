package com.aivora.app.ui.security

import android.os.Build
import androidx.biometric.BiometricManager
import androidx.activity.compose.BackHandler
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ProcessLifecycleOwner
import com.aivora.app.data.local.AppLockPrefs
import com.aivora.app.data.local.SessionStore
import com.aivora.app.ui.components.PrimaryButton
import com.aivora.app.ui.components.blockTouchesBehind
import com.aivora.app.ui.theme.AivoraColors

/** Observes app backgrounding and prompts biometric unlock when enabled. */
@Composable
fun AppLockHost(content: @Composable () -> Unit) {
    val context = LocalContext.current
    // Cold start: lock if enabled. Background return uses needsUnlock flag.
    var locked by remember {
        mutableStateOf(
            SessionStore.hasSession(context) && AppLockPrefs.enabled(context),
        )
    }
    var error by remember { mutableStateOf<String?>(null) }

    DisposableEffect(Unit) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_STOP -> {
                    if (SessionStore.hasSession(context) && AppLockPrefs.enabled(context)) {
                        AppLockPrefs.setNeedsUnlock(context, true)
                    }
                }
                Lifecycle.Event.ON_START -> {
                    if (
                        SessionStore.hasSession(context) &&
                        AppLockPrefs.enabled(context) &&
                        AppLockPrefs.needsUnlock(context)
                    ) {
                        locked = true
                    }
                }
                else -> Unit
            }
        }
        val owner = ProcessLifecycleOwner.get()
        owner.lifecycle.addObserver(observer)
        onDispose { owner.lifecycle.removeObserver(observer) }
    }

    Box(Modifier.fillMaxSize()) {
        content()
        if (locked) {
            AppLockOverlay(
                error = error,
                onUnlockRequest = {
                    error = null
                    promptUnlock(
                        activity = context as? FragmentActivity,
                        onSuccess = {
                            AppLockPrefs.setNeedsUnlock(context, false)
                            locked = false
                        },
                        onError = { error = it },
                    )
                },
            )
            LaunchedEffect(Unit) {
                promptUnlock(
                    activity = context as? FragmentActivity,
                    onSuccess = {
                        AppLockPrefs.setNeedsUnlock(context, false)
                        locked = false
                    },
                    onError = { error = it },
                )
            }
        }
    }
}

@Composable
private fun AppLockOverlay(
    error: String?,
    onUnlockRequest: () -> Unit,
) {
    // Nothing behind the lock may react: Back leaves the app, and the keyboard is closed
    // so it can't type into the chat underneath.
    val context = LocalContext.current
    BackHandler { (context as? FragmentActivity)?.moveTaskToBack(true) }
    val keyboard = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    LaunchedEffect(Unit) {
        focusManager.clearFocus(force = true)
        keyboard?.hide()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AivoraColors.LightBackground)
            .blockTouchesBehind(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(32.dp),
        ) {
            Icon(
                Icons.Filled.Lock,
                contentDescription = null,
                tint = AivoraColors.Primary,
                modifier = Modifier.size(48.dp),
            )
            Spacer(Modifier.height(16.dp))
            Text(
                "Aivora is locked",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = AivoraColors.Ink,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Unlock with your fingerprint, face, or device PIN.",
                style = MaterialTheme.typography.bodyMedium,
                color = AivoraColors.TextSecondary,
                textAlign = TextAlign.Center,
            )
            if (error != null) {
                Spacer(Modifier.height(12.dp))
                Text(error, color = AivoraColors.Danger, textAlign = TextAlign.Center)
            }
            Spacer(Modifier.height(24.dp))
            PrimaryButton(text = "Unlock", onClick = onUnlockRequest)
        }
    }
}

fun canUseAppLock(context: android.content.Context): Boolean {
    val mgr = BiometricManager.from(context)
    val authenticators =
        BiometricManager.Authenticators.BIOMETRIC_STRONG or
            BiometricManager.Authenticators.DEVICE_CREDENTIAL
    return when (mgr.canAuthenticate(authenticators)) {
        BiometricManager.BIOMETRIC_SUCCESS -> true
        BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> true // still show; prompt may enroll
        else -> Build.VERSION.SDK_INT >= 30 // device credential often available
    }
}

fun promptUnlock(
    activity: FragmentActivity?,
    onSuccess: () -> Unit,
    onError: (String) -> Unit,
) {
    if (activity == null) {
        onError("Unlock unavailable on this screen")
        return
    }
    val authenticators =
        BiometricManager.Authenticators.BIOMETRIC_STRONG or
            BiometricManager.Authenticators.DEVICE_CREDENTIAL
    val mgr = BiometricManager.from(activity)
    if (mgr.canAuthenticate(authenticators) == BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE) {
        onError("No biometric or screen lock available")
        return
    }
    val executor = ContextCompat.getMainExecutor(activity)
    val prompt = BiometricPrompt(
        activity,
        executor,
        object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                onSuccess()
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                if (errorCode != BiometricPrompt.ERROR_USER_CANCELED &&
                    errorCode != BiometricPrompt.ERROR_NEGATIVE_BUTTON
                ) {
                    onError(errString.toString())
                }
            }
        },
    )
    val info = BiometricPrompt.PromptInfo.Builder()
        .setTitle("Unlock Aivora")
        .setSubtitle("Confirm it’s you")
        .setAllowedAuthenticators(authenticators)
        .build()
    try {
        prompt.authenticate(info)
    } catch (e: Exception) {
        onError(e.message ?: "Could not show unlock prompt")
    }
}
