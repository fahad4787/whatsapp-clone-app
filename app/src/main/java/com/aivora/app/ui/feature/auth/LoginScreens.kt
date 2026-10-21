package com.aivora.app.ui.feature.auth

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.aivora.app.ui.components.AivoraLogo
import com.aivora.app.ui.components.LinkButton
import com.aivora.app.ui.components.PrimaryButton
import com.aivora.app.ui.navigation.Routes
import com.aivora.app.ui.theme.AivoraColors

@Composable
fun LoginScreen(
    onNavigate: (String) -> Unit = {},
    onVerified: () -> Unit = {},
    onSkipHome: () -> Unit = {},
    onBack: (() -> Unit)? = null,
) {
    var step by remember { mutableIntStateOf(0) }
    var country by remember { mutableStateOf("Pakistan") }
    var dial by remember { mutableStateOf("+92") }
    var phone by remember { mutableStateOf("") }
    var otp by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        AivoraColors.Primary.copy(alpha = 0.12f),
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.background,
                    ),
                ),
            ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            AivoraLogo(size = 96.dp, animated = true)
            Spacer(modifier = Modifier.height(18.dp))
            Text(
                "Aivora",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = AivoraColors.Primary,
            )
            Text(
                "Chat · Call · Create",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(20.dp))

            AnimatedContent(
                targetState = step,
                transitionSpec = {
                    if (targetState > initialState) {
                        (slideInHorizontally { it / 4 } + fadeIn()) togetherWith
                            (slideOutHorizontally { -it / 4 } + fadeOut())
                    } else {
                        (slideInHorizontally { -it / 4 } + fadeIn()) togetherWith
                            (slideOutHorizontally { it / 4 } + fadeOut())
                    }
                },
                label = "loginStep",
            ) { current ->
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    if (current == 0) {
                        Text(
                            "Enter your phone number to get started.",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        OutlinedTextField(
                            value = country,
                            onValueChange = { country = it; error = null },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Country") },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = dial,
                                onValueChange = { dial = it; error = null },
                                modifier = Modifier.width(96.dp),
                                label = { Text("Code") },
                                singleLine = true,
                                shape = RoundedCornerShape(14.dp),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            OutlinedTextField(
                                value = phone,
                                onValueChange = {
                                    phone = it.filter { ch -> ch.isDigit() }.take(12)
                                    error = null
                                },
                                modifier = Modifier.weight(1f),
                                label = { Text("Phone number") },
                                singleLine = true,
                                shape = RoundedCornerShape(14.dp),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            )
                        }
                        if (error != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(error!!, color = AivoraColors.Danger, style = MaterialTheme.typography.bodyMedium)
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        PrimaryButton(
                            "Send code",
                            onClick = {
                                if (phone.length < 7) {
                                    error = "Enter a valid phone number"
                                } else {
                                    error = null
                                    step = 1
                                    otp = ""
                                }
                            },
                        )
                        LinkButton("Sign in with a passkey") { onNavigate(Routes.SettingsAccountPasskeys) }
                        LinkButton("Email sign-in") { onNavigate(Routes.SettingsAccountEmail) }
                    } else {
                        Text(
                            "Enter the 6-digit code sent to $dial $phone",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            "Dev mock: use any 6 digits (e.g. 123456)",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 8.dp),
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        OutlinedTextField(
                            value = otp,
                            onValueChange = {
                                otp = it.filter { ch -> ch.isDigit() }.take(6)
                                error = null
                            },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Verification code") },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        )
                        if (error != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(error!!, color = AivoraColors.Danger, style = MaterialTheme.typography.bodyMedium)
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        PrimaryButton(
                            "Verify",
                            enabled = otp.length == 6,
                            onClick = {
                                if (otp.length != 6) {
                                    error = "Enter the 6-digit code"
                                } else {
                                    onVerified()
                                }
                            },
                        )
                        LinkButton("Resend code") { otp = ""; error = null }
                        LinkButton("Wrong number?") { step = 0; error = null }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            LinkButton("Skip to home (dev)") { onSkipHome() }
        }
    }
}
