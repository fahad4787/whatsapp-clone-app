package com.aivora.app.ui.feature.auth

import com.aivora.app.ui.icons.AppIcons

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
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.aivora.app.data.model.AppCountryDto
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
import com.aivora.app.ui.components.AivoraNoOverscroll
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
    var countries by remember { mutableStateOf<List<AppCountryDto>>(emptyList()) }
    var pickCountry by remember { mutableStateOf(false) }
    var forceUpdateUrl by remember { mutableStateOf<String?>(null) }
    // Prefill last signed-in number so rebuilds don't force retyping.
    var phone by remember {
        mutableStateOf(
            SessionStore.phone(context)
                ?.removePrefix("+92")
                ?.removePrefix("92")
                ?.filter { it.isDigit() }
                .orEmpty(),
        )
    }
    var e164Phone by remember { mutableStateOf(SessionStore.phone(context)) }
    var otp by remember { mutableStateOf("") }
    var pin by remember { mutableStateOf("") }
    var twoStepChallenge by remember { mutableStateOf<String?>(null) }
    var otpViaAdmin by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var busy by remember { mutableStateOf(false) }
    var emailMode by remember { mutableStateOf(false) }
    var loginEmail by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        runCatching { api.getAppConfig() }.onSuccess { cfg ->
            val list = cfg.countries.filter { it.enabled && !it.name.isNullOrBlank() }
            if (list.isNotEmpty()) {
                countries = list
                val match = list.firstOrNull { it.name.equals(country, ignoreCase = true) }
                    ?: list.firstOrNull { it.iso == "PK" }
                    ?: list.first()
                country = match.name ?: country
                dial = match.dial?.takeIf { it.isNotBlank() } ?: dial
            }
            otpViaAdmin = cfg.otpViaAdmin
            if (BuildConfig.VERSION_CODE < cfg.minAndroidVersionCode) {
                forceUpdateUrl = cfg.downloadUrl
            }
        }
    }

    if (pickCountry && countries.isNotEmpty()) {
        var countryQuery by remember { mutableStateOf("") }
        val shown = remember(countryQuery, countries) {
            val q = countryQuery.trim()
            if (q.isEmpty()) countries
            else countries.filter {
                it.name.orEmpty().contains(q, ignoreCase = true) ||
                    it.dial.orEmpty().contains(q.removePrefix("+"), ignoreCase = true)
            }
        }
        com.aivora.app.ui.components.AivoraBottomSheet(
            onDismiss = { pickCountry = false },
            title = "Select country",
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                com.aivora.app.ui.components.SearchField(
                    value = countryQuery,
                    onValueChange = { countryQuery = it },
                    placeholder = "Search country or code",
                )
                Spacer(Modifier.height(8.dp))
                AivoraNoOverscroll {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(320.dp)
                            .verticalScroll(rememberScrollState()),
                    ) {
                        if (shown.isEmpty()) {
                            Text(
                                "No matching country",
                                modifier = Modifier.padding(vertical = 20.dp),
                                fontSize = 15.sp,
                                color = AivoraColors.TextSecondary,
                            )
                        }
                        shown.forEach { row ->
                            val selected = row.name.equals(country, ignoreCase = true)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        country = row.name ?: country
                                        dial = row.dial?.takeIf { it.isNotBlank() } ?: dial
                                        pickCountry = false
                                    }
                                    .padding(vertical = 14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    row.name.orEmpty(),
                                    modifier = Modifier.weight(1f),
                                    fontSize = 16.sp,
                                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                                    color = AivoraColors.Ink,
                                )
                                Text(
                                    row.dial.orEmpty(),
                                    fontSize = 15.sp,
                                    color = AivoraColors.TextSecondary,
                                )
                                if (selected) {
                                    Spacer(Modifier.width(10.dp))
                                    Icon(
                                        AppIcons.Check,
                                        contentDescription = "Selected",
                                        tint = AivoraColors.Primary,
                                        modifier = Modifier.size(20.dp),
                                    )
                                }
                            }
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
            }
        }
    }

    suspend fun finishLogin(access: String, refresh: String, targetPhone: String): String? {
        SessionStore.save(context, access, refresh, targetPhone)
        LoginHandoff.hasProfile = false
        try {
            val me = api.getMe()
            me.id?.let { SessionStore.saveUserId(context, it) }
            LoginHandoff.hasProfile = !me.displayName.isNullOrBlank()
        } catch (_: Exception) { /* optional */ }
        com.aivora.app.data.push.PushRegistrar.refresh(context)
        return null
    }

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
        if (tokens.twoStepRequired && !tokens.challenge.isNullOrBlank()) {
            twoStepChallenge = tokens.challenge
            pin = ""
            step = 2
            return null
        }
        val access = tokens.access
        val refresh = tokens.refresh
        if (access.isNullOrBlank() || refresh.isNullOrBlank()) {
            return "Invalid server response"
        }
        return finishLogin(access, refresh, targetPhone)
    }

    suspend fun completeTwoStep(targetPhone: String, pinCode: String): String? {
        val challenge = twoStepChallenge ?: return "Missing two-step challenge"
        val tokens = api.postAuthTwoStep(
            com.aivora.app.data.model.TwoStepBody(
                challenge = challenge,
                pin = pinCode,
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
            return "Invalid PIN"
        }
        twoStepChallenge = null
        return finishLogin(access, refresh, targetPhone)
    }

    suspend fun completeEmailVerify(email: String, code: String): String? {
        val tokens = api.postAuthEmailVerify(
            com.aivora.app.data.model.EmailSignInVerifyBody(
                email = email,
                code = code,
                device = DeviceInfo(
                    platform = "android",
                    deviceName = android.os.Build.MODEL,
                    appVersion = BuildConfig.VERSION_NAME,
                ),
            ),
        )
        if (tokens.twoStepRequired && !tokens.challenge.isNullOrBlank()) {
            twoStepChallenge = tokens.challenge
            pin = ""
            step = 2
            return null
        }
        val access = tokens.access
        val refresh = tokens.refresh
        if (access.isNullOrBlank() || refresh.isNullOrBlank()) {
            return "Invalid server response"
        }
        val phoneForSession = SessionStore.phone(context) ?: email
        return finishLogin(access, refresh, phoneForSession)
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
                    if (current == 0 && emailMode) {
                        Text(
                            "Email sign-in",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 18.sp,
                            color = AivoraColors.Ink,
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "We’ll send a one-time code to your email.",
                            fontSize = 14.sp,
                            color = AivoraColors.TextSecondary,
                            lineHeight = 18.sp,
                        )
                        Spacer(Modifier.height(14.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(AivoraColors.Primary.copy(alpha = 0.08f))
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            Icon(
                                imageVector = AppIcons.Info,
                                contentDescription = null,
                                tint = AivoraColors.Primary,
                                modifier = Modifier.size(18.dp).padding(top = 1.dp),
                            )
                            Text(
                                "Only for accounts that added a verified email after signing in with a phone number. New here? Sign in with your phone first.",
                                fontSize = 13.sp,
                                lineHeight = 18.sp,
                                color = AivoraColors.Ink,
                            )
                        }
                        Spacer(Modifier.height(18.dp))
                        AuthFieldLabel("Email")
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
                            if (loginEmail.isEmpty()) {
                                Text("you@example.com", color = Color(0xFF9B9B9B), fontSize = 16.sp)
                            }
                            BasicTextField(
                                value = loginEmail,
                                onValueChange = {
                                    loginEmail = it.trim()
                                    error = null
                                },
                                singleLine = true,
                                textStyle = TextStyle(color = AivoraColors.InkMuted, fontSize = 16.sp),
                                cursorBrush = SolidColor(AivoraColors.Primary),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                        if (error != null) {
                            Spacer(Modifier.height(10.dp))
                            Text(error!!, color = AivoraColors.Danger, fontSize = 13.sp)
                        }
                        Spacer(Modifier.height(22.dp))
                        PrimaryButton(
                            text = if (busy) "Sending…" else "Send code",
                            enabled = !busy,
                            onClick = {
                                if (!loginEmail.contains("@")) {
                                    error = "Enter a valid email"
                                    return@PrimaryButton
                                }
                                busy = true
                                error = null
                                scope.launch {
                                    try {
                                        val result = api.postAuthEmailRequest(
                                            com.aivora.app.data.model.EmailSignInRequestBody(
                                                email = loginEmail,
                                            ),
                                        )
                                        otp = ""
                                        otpViaAdmin = result.otpViaAdmin
                                        step = 1
                                    } catch (e: Exception) {
                                        error = com.aivora.app.data.api.ApiErrors.auth(e, "Could not send code")
                                    } finally {
                                        busy = false
                                    }
                                }
                            },
                        )
                        Spacer(Modifier.height(18.dp))
                        AuthTextLink("Use phone instead") {
                            emailMode = false
                            error = null
                            otp = ""
                        }
                    } else if (current == 0) {
                        Text(
                            "Phone number",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 18.sp,
                            color = AivoraColors.Ink,
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "We’ll send you a code to confirm it’s you.",
                            fontSize = 14.sp,
                            color = AivoraColors.TextSecondary,
                            lineHeight = 18.sp,
                        )
                        Spacer(Modifier.height(20.dp))

                        if (forceUpdateUrl != null) {
                            Text(
                                "Update required to continue. Ask admin for the latest APK.",
                                color = AivoraColors.Danger,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(bottom = 12.dp),
                            )
                        }
                        AuthFieldLabel("Country")
                        CountryField(
                            value = country,
                            onClick = {
                                if (countries.isNotEmpty()) pickCountry = true
                            },
                        )
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
                            text = if (busy) "Sending…" else "Send code",
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
                                        otpViaAdmin = result.otpViaAdmin
                                        step = 1
                                    } catch (e: Exception) {
                                        val http = e as? retrofit2.HttpException
                                        val body = if (http?.code() == 429)
                                            http.response()?.errorBody()?.string().orEmpty() else ""
                                        if (body.contains("rate_limited")) {
                                            // A code was just sent: go straight to code entry.
                                            e164Phone = Regex("\"phone\"\\s*:\\s*\"([^\"]+)\"")
                                                .find(body)?.groupValues?.get(1)
                                                ?: "+${dial.filter { it.isDigit() }}$phone"
                                            otp = ""
                                            step = 1
                                        } else {
                                            error = com.aivora.app.data.api.ApiErrors.auth(e, "Could not send code")
                                        }
                                    } finally {
                                        busy = false
                                    }
                                }
                            },
                        )

                        Spacer(Modifier.height(18.dp))
                        AuthTextLink("Use email instead") {
                            emailMode = true
                            step = 0
                            error = null
                            otp = ""
                        }
                        AuthTextLink("Link this device") {
                            onNavigate(Routes.DeviceLinkPair)
                        }
                    } else if (current == 1 && emailMode) {
                        Text(
                            "Enter the code",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 18.sp,
                            color = AivoraColors.Ink,
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "Sent to $loginEmail",
                            fontSize = 14.sp,
                            color = AivoraColors.TextSecondary,
                        )
                        if (otpViaAdmin) {
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "Your code is sent by Aivora support. Contact them if it doesn’t arrive.",
                                fontSize = 12.sp,
                                color = AivoraColors.TextTertiary,
                            )
                        }
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
                                busy = true
                                error = null
                                scope.launch {
                                    try {
                                        val fail = completeEmailVerify(loginEmail, otp)
                                        if (fail != null) error = fail
                                        else if (twoStepChallenge == null) onVerified()
                                    } catch (e: Exception) {
                                        error = com.aivora.app.data.api.ApiErrors.auth(e, "Invalid code")
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
                                    val result = api.postAuthEmailRequest(
                                        com.aivora.app.data.model.EmailSignInRequestBody(
                                            email = loginEmail,
                                        ),
                                    )
                                    otpViaAdmin = result.otpViaAdmin
                                    otp = ""
                                } catch (e: Exception) {
                                    error = com.aivora.app.data.api.ApiErrors.auth(e, "Could not resend")
                                } finally {
                                    busy = false
                                }
                            }
                        }
                        AuthTextLink("Wrong email?") {
                            step = 0
                            error = null
                        }
                    } else if (current == 1) {
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
                        if (otpViaAdmin) {
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "Your code is sent by Aivora support. Contact them if it doesn’t arrive.",
                                fontSize = 12.sp,
                                color = AivoraColors.TextTertiary,
                            )
                        }
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
                                        else if (twoStepChallenge == null) onVerified()
                                    } catch (e: Exception) {
                                        error = com.aivora.app.data.api.ApiErrors.auth(e, "Invalid code")
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
                                    otpViaAdmin = result.otpViaAdmin
                                    otp = ""
                                } catch (e: Exception) {
                                    error = com.aivora.app.data.api.ApiErrors.auth(e, "Could not resend")
                                } finally {
                                    busy = false
                                }
                            }
                        }
                        AuthTextLink("Wrong number?") { step = 0; error = null }
                    } else {
                        Text(
                            "Two-step verification",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 18.sp,
                            color = AivoraColors.Ink,
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "Enter your 6-digit account PIN to finish signing in.",
                            fontSize = 14.sp,
                            color = AivoraColors.TextSecondary,
                            lineHeight = 18.sp,
                        )
                        Spacer(Modifier.height(20.dp))
                        AuthFieldLabel("PIN")
                        OtpField(
                            value = pin,
                            onValueChange = {
                                pin = it.filter { ch -> ch.isDigit() }.take(6)
                                error = null
                            },
                        )
                        if (error != null) {
                            Spacer(Modifier.height(10.dp))
                            Text(error!!, color = AivoraColors.Danger, fontSize = 13.sp)
                        }
                        Spacer(Modifier.height(22.dp))
                        PrimaryButton(
                            text = if (busy) "Checking…" else "Continue",
                            enabled = pin.length == 6 && !busy,
                            onClick = {
                                val targetPhone = e164Phone
                                    ?: "+${dial.filter { it.isDigit() }}$phone"
                                busy = true
                                error = null
                                scope.launch {
                                    try {
                                        val fail = completeTwoStep(targetPhone, pin)
                                        if (fail != null) error = fail
                                        else onVerified()
                                    } catch (e: Exception) {
                                        error = com.aivora.app.data.api.ApiErrors.auth(e, "Invalid PIN")
                                    } finally {
                                        busy = false
                                    }
                                }
                            },
                        )
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
            AppIcons.KeyboardArrowDown,
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


/** Result of the profile lookup done during login, so existing users skip the profile-setup screen. */
object LoginHandoff {
    @Volatile var hasProfile: Boolean = false
}
