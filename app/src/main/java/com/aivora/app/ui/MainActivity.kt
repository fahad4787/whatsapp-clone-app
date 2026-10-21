package com.aivora.app.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.aivora.app.ui.navigation.AivoraNavHost
import com.aivora.app.ui.navigation.Routes
import com.aivora.app.ui.theme.AivoraTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AivoraAppRoot()
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
