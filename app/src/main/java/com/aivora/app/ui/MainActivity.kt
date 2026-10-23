package com.aivora.app.ui

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.aivora.app.data.SessionRestorer
import com.aivora.app.data.local.SessionStore
import com.aivora.app.data.push.MessageNotifier
import com.aivora.app.data.push.PushRegistrar
import com.aivora.app.ui.navigation.AivoraNavHost
import com.aivora.app.ui.navigation.Routes
import com.aivora.app.ui.theme.AivoraTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val requestNotifications = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { /* user may deny; badge still works */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // Defer channel + permission so first frame isn't blocked.
        lifecycleScope.launch {
            delay(400)
            MessageNotifier.ensureChannel(this@MainActivity)
            if (Build.VERSION.SDK_INT >= 33) {
                requestNotifications.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
            if (SessionStore.hasSession(this@MainActivity)) {
                SessionRestorer.ensureFresh(applicationContext)
                PushRegistrar.refresh(applicationContext)
            }
        }
        setContent {
            val start = remember {
                if (SessionStore.hasSession(this@MainActivity)) Routes.Home else Routes.Login
            }
            AivoraAppRoot(startDestination = start)
        }
    }
}

@Composable
fun AivoraAppRoot(startDestination: String = Routes.Login) {
    AivoraTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            val navController = rememberNavController()
            AivoraNavHost(
                navController = navController,
                startDestination = startDestination,
            )
        }
    }
}
