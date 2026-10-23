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
import com.aivora.app.ui.navigation.AivoraNavHost
import com.aivora.app.ui.navigation.Routes
import com.aivora.app.ui.theme.AivoraTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val requestNotifications = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { /* user may deny; badge still works */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        MessageNotifier.ensureChannel(this)
        if (Build.VERSION.SDK_INT >= 33) {
            requestNotifications.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
        // Refresh in background so access tokens older than ~15m still work after rebuild.
        if (SessionStore.hasSession(this)) {
            lifecycleScope.launch { SessionRestorer.ensureFresh(applicationContext) }
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
