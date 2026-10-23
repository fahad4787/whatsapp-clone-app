package com.aivora.app.ui.feature.auth

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aivora.app.BuildConfig
import com.aivora.app.data.api.NetworkModule
import com.aivora.app.data.local.SessionStore
import com.aivora.app.data.model.DeviceInfo
import com.aivora.app.data.model.OtpRequestBody
import com.aivora.app.data.model.OtpVerifyBody
import com.aivora.app.ui.components.AivoraLogo
import com.aivora.app.ui.components.PrimaryButton
import com.aivora.app.ui.navigation.Routes
import com.aivora.app.ui.theme.AivoraColors
import com.aivora.app.ui.theme.AivoraDimens
import kotlinx.coroutines.launch

private val FieldShape = RoundedCornerShape(AivoraDimens.RadiusMd)

@Composable
fun LoginScreen(
    onNavigate: (String) -> Unit = {},
    onVerified: () -> Unit = {},
    onSkipHome: () -> Unit = {},
    onBack: (() -> Unit)? = null,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val api = remember {
        NetworkModule.api(NetworkModule.okHttp { SessionStore.accessToken(context) })
    }
    var step by remember { mutableIntStateOf(0) }
    var country by remember { mutableStateOf("Pakistan") }
    var dial by remember { mutableStateOf("+92") }
    var phone by remember { mutableStateOf("") }
    var e164Phone by remember { mutableStateOf<String?>(null) }
    var otp by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var busy by remember { mutableStateOf(false) }

    suspend fun completeVerify(targetPhone: String, code: String): String? {
        val tokens = api.postAuthOtpVerify(
            OtpVerifyBody(
                phone = targetPhone,
                code = code,
                device = DeviceInfo(
                    platform = "android",
                    deviceName = android.os.Build.MODEL,
                    appVersion = BuildConfig.VERSION_NAME,
                ),
            ),
        )
        val access = tokens.access
        val refresh = tokens.refresh
        if (access.isNullOrBlank() || refresh.isNullOrBlank()) {
            return "Invalid server response"
        }
        SessionStore.save(context, access, refresh, targetPhone)
        try {
            val me = api.getMe()
            me.id?.let { SessionStore.saveUserId(context, it) }
        } catch (_: Exception) { /* optional */ }
        return null
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AivoraColors.LightBackground),
    ) {
        // Soft brand atmosphere behind the hero — not a flat wash.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(340.dp)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            AivoraColors.SoftMint,
                            AivoraColors.PrimarySoft.copy(alpha = 0.55f),
                            Color.Transparent,
                        ),
                    ),
                ),
        )
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 72.dp)
                .size(220.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            AivoraColors.Primary.copy(alpha = 0.16f),
                            Color.Transparent,
                        ),
                    ),
                ),
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(56.dp))
            AivoraLogo(size = 88.dp, animated = true)
            Spacer(Modifier.height(20.dp))
            Text(
                "Aivora",
                fontSize = 34.sp,
                fontWeight = FontWeight.Bold,
                color = AivoraColors.InkMuted,
                letterSpacing = (-0.5).sp,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                "Messages, calls, and moments — in one place.",
                fontSize = 15.sp,
                color = AivoraColors.TextSecondary,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp,
                modifier = Modifier.padding(horizontal = 12.dp),
            )

            Spacer(Modifier.height(40.dp))

            AnimatedContent(
                targetState = step,
                transitionSpec = {
                    (fadeIn(tween(160)) + slideInHorizontally(tween(180)) { it / 10 }) togetherWith
                        (fadeOut(tween(120)) + slideOutHorizontally(tween(160)) { -it / 10 })
                },
                label = "loginStep",
            ) { current ->
                Column(modifier = Modifier.fillMaxWidth()) {
                    if (current == 0) {
                        Text(
                            "Phone number",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 18.sp,
                            color = AivoraColors.Ink,
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            if (BuildConfig.DEV_AUTO_OTP) {
                                "Dev build: enter phone and continue — OTP is skipped (still required in release)."
                            } else {
                                "We’ll text you a code to confirm it’s you."
                            },
                            fontSize = 14.sp,
                            color = AivoraColors.TextSecondary,
                            lineHeight = 18.sp,
                        )
                        Spacer(Modifier.height(20.dp))

                        AuthFieldLabel("Country")
                        CountryField(value = country, onClick = { /* local mock */ })
                        Spacer(Modifier.height(14.dp))

                        AuthFieldLabel("Phone")
                        PhoneField(
                            dial = dial,
                            onDialChange = { dial = it; error = null },
                            phone = phone,
                            onPhoneChange = {
                                phone = it.filter { ch -> ch.isDigit() }.take(12)
                                error = null
                            },
                        )

                        if (error != null) {
                            Spacer(Modifier.height(10.dp))
                            Text(error!!, color = AivoraColors.Danger, fontSize = 13.sp)
                        }

                        Spacer(Modifier.height(22.dp))
                        PrimaryButton(
                            text = when {
                                busy && BuildConfig.DEV_AUTO_OTP -> "Signing in…"
                                busy -> "Sending…"
                                BuildConfig.DEV_AUTO_OTP -> "Continue"
                                else -> "Send code"
                            },
                            enabled = !busy,
                            onClick = {
                                if (phone.length < 7) {
                                    error = "Enter a valid phone number"
                                    return@PrimaryButton
                                }
                                busy = true
                                error = null
                                scope.launch {
                                    try {
                                        val result = api.postAuthOtpRequest(
                                            OtpRequestBody(
                                                country = dial.filter { it.isDigit() },
                                                nationalNumber = phone,
                                            ),
                                        )
                                        val verifiedPhone = result.phone
                                            ?: "+${dial.filter { it.isDigit() }}$phone"
                                        e164Phone = verifiedPhone
                                        otp = ""
                                        if (BuildConfig.DEV_AUTO_OTP) {
                                            // Server OTP_DEV_BYPASS accepts 000000; OTP flow stays for release.
                                            val fail = completeVerify(verifiedPhone, "000000")
                                            if (fail != null) {
                                                error = fail
                                                step = 1
                                            } else {
                                                onVerified()
                                            }
                                        } else {
                                            step = 1
                                        }
                                    } catch (e: Exception) {
                                        error = e.message ?: "Could not send code"
                                    } finally {
                                        busy = false
                                    }
                                }
                            },
                        )

                        Spacer(Modifier.height(18.dp))
                        AuthTextLink("Sign in with a passkey") {
                            onNavigate(Routes.SettingsAccountPasskeys)
                        }
                        AuthTextLink("Use email instead") {
                            onNavigate(Routes.SettingsAccountEmail)
                        }
                    } else {
                        Text(
                            "Enter the code",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 18.sp,
                            color = AivoraColors.Ink,
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "Sent to $dial $phone",
                            fontSize = 14.sp,
                            color = AivoraColors.TextSecondary,
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Dev: copy code from admin OTP log, or use 000000",
                            fontSize = 12.sp,
                            color = AivoraColors.TextTertiary,
                        )
                        Spacer(Modifier.height(20.dp))

                        AuthFieldLabel("Verification code")
                        OtpField(
                            value = otp,
                            onValueChange = {
                                otp = it.filter { ch -> ch.isDigit() }.take(6)
                                error = null
                            },
                        )

                        if (error != null) {
                            Spacer(Modifier.height(10.dp))
                            Text(error!!, color = AivoraColors.Danger, fontSize = 13.sp)
                        }

                        Spacer(Modifier.height(22.dp))
                        PrimaryButton(
                            text = if (busy) "Verifying…" else "Verify",
                            enabled = otp.length == 6 && !busy,
                            onClick = {
                                if (otp.length != 6) {
                                    error = "Enter the 6-digit code"
                                    return@PrimaryButton
                                }
                                val targetPhone = e164Phone
                                    ?: "+${dial.filter { it.isDigit() }}$phone"
                                busy = true
                                error = null
                                scope.launch {
                                    try {
                                        val fail = completeVerify(targetPhone, otp)
                                        if (fail != null) error = fail
                                        else onVerified()
                                    } catch (e: Exception) {
                                        error = e.message ?: "Invalid code"
                                    } finally {
                                        busy = false
                                    }
                                }
                            },
                        )
                        Spacer(Modifier.height(18.dp))
                        AuthTextLink("Resend code") {
                            if (busy) return@AuthTextLink
                            busy = true
                            error = null
                            scope.launch {
                                try {
                                    val result = api.postAuthOtpRequest(
                                        OtpRequestBody(
                                            country = dial.filter { it.isDigit() },
                                            nationalNumber = phone,
                                        ),
                                    )
                                    e164Phone = result.phone
                                    otp = ""
                                } catch (e: Exception) {
                                    error = e.message ?: "Could not resend"
                                } finally {
                                    busy = false
                                }
                            }
                        }
                        AuthTextLink("Wrong number?") { step = 0; error = null }
                    }
                }
            }

            Spacer(Modifier.height(36.dp))
            Text(
                "By continuing you agree to Aivora’s Terms & Privacy.",
                fontSize = 12.sp,
                color = AivoraColors.TextTertiary,
                textAlign = TextAlign.Center,
                lineHeight = 16.sp,
                modifier = Modifier.padding(horizontal = 8.dp),
            )
            Spacer(Modifier.height(8.dp))
            AuthTextLink("Skip to home (dev)", muted = true, onClick = onSkipHome)
            Spacer(Modifier.height(20.dp))
        }
    }
}

@Composable
private fun AuthFieldLabel(text: String) {
    Text(
        text,
        fontSize = 13.sp,
        color = AivoraColors.TextSecondary,
        modifier = Modifier.padding(bottom = 8.dp),
    )
}

@Composable
private fun CountryField(value: String, onClick: () -> Unit) {
    val interaction = remember { MutableInteractionSource() }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(FieldShape)
            .background(AivoraColors.SurfaceMuted)
            .border(1.dp, AivoraColors.BorderMuted, FieldShape)
            .clickable(interactionSource = interaction, indication = null, onClick = onClick)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            value,
            modifier = Modifier.weight(1f),
            fontSize = 16.sp,
            color = AivoraColors.InkMuted,
        )
        Icon(
            Icons.Default.KeyboardArrowDown,
            contentDescription = null,
            tint = AivoraColors.TextSecondary,
        )
    }
}

@Composable
private fun PhoneField(
    dial: String,
    onDialChange: (String) -> Unit,
    phone: String,
    onPhoneChange: (String) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(FieldShape)
            .background(AivoraColors.SurfaceMuted)
            .border(1.dp, AivoraColors.BorderMuted, FieldShape)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        BasicTextField(
            value = dial,
            onValueChange = onDialChange,
            singleLine = true,
            textStyle = TextStyle(
                color = AivoraColors.InkMuted,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
            ),
            cursorBrush = SolidColor(AivoraColors.Primary),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            modifier = Modifier.width(56.dp),
        )
        Box(
            modifier = Modifier
                .padding(horizontal = 10.dp)
                .width(1.dp)
                .height(22.dp)
                .background(AivoraColors.BorderMuted),
        )
        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
            if (phone.isEmpty()) {
                Text("300 1234567", color = Color(0xFF9B9B9B), fontSize = 16.sp)
            }
            BasicTextField(
                value = phone,
                onValueChange = onPhoneChange,
                singleLine = true,
                textStyle = TextStyle(color = AivoraColors.InkMuted, fontSize = 16.sp),
                cursorBrush = SolidColor(AivoraColors.Primary),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun OtpField(value: String, onValueChange: (String) -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(FieldShape)
            .background(AivoraColors.SurfaceMuted)
            .border(1.dp, AivoraColors.BorderMuted, FieldShape)
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        if (value.isEmpty()) {
            Text("6-digit code", color = Color(0xFF9B9B9B), fontSize = 16.sp)
        }
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            textStyle = TextStyle(
                color = AivoraColors.InkMuted,
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 6.sp,
            ),
            cursorBrush = SolidColor(AivoraColors.Primary),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun AuthTextLink(
    text: String,
    muted: Boolean = false,
    onClick: () -> Unit,
) {
    val interaction = remember { MutableInteractionSource() }
    Text(
        text,
        color = if (muted) AivoraColors.TextTertiary else AivoraColors.PrimaryDark,
        fontSize = if (muted) 12.sp else 14.sp,
        fontWeight = if (muted) FontWeight.Normal else FontWeight.Medium,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable(interactionSource = interaction, indication = null, onClick = onClick),
    )
}
