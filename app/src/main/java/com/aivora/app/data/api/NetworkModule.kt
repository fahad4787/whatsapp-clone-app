package com.aivora.app.data.api

import com.aivora.app.AivoraApp
import com.aivora.app.BuildConfig
import com.aivora.app.data.local.SessionStore
import com.aivora.app.data.model.RefreshBody
import com.aivora.app.data.model.RefreshResult
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.Authenticator
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.Route
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit

object NetworkModule {
    val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        explicitNulls = false
    }

    private val jsonMedia = "application/json".toMediaType()

    /** Client used only for token refresh (no auth interceptor / authenticator). */
    private val refreshClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    /** Process-wide HTTP stack — one connection pool for the whole app. */
    private val sharedClient: OkHttpClient by lazy { buildOkHttp() }

    private val sharedApi: AivoraApi by lazy {
        Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .client(sharedClient)
            .addConverterFactory(json.asConverterFactory(jsonMedia))
            .build()
            .create(AivoraApi::class.java)
    }

    private fun buildOkHttp(): OkHttpClient {
        val builder = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .addInterceptor { chain ->
                val token = runCatching {
                    SessionStore.accessToken(AivoraApp.instance)
                }.getOrNull()
                val req = chain.request().newBuilder().apply {
                    token?.let { header("Authorization", "Bearer $it") }
                    header("Accept", "application/json")
                }.build()
                chain.proceed(req)
            }
            .authenticator(TokenAuthenticator)
        // PERF H2: never attach BASIC logging in release — CPU + log I/O on every call.
        if (BuildConfig.DEBUG) {
            builder.addInterceptor(
                HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC },
            )
        }
        return builder.build()
    }

    /** Shared OkHttp client (token from [SessionStore]). Extra arg kept for call-site compat. */
    @Suppress("UNUSED_PARAMETER")
    fun okHttp(tokenProvider: () -> String? = { null }): OkHttpClient = sharedClient

    /** Shared Retrofit API. Extra arg kept for call-site compat. */
    @Suppress("UNUSED_PARAMETER")
    fun api(client: OkHttpClient = sharedClient): AivoraApi = sharedApi

    /**
     * Synchronously rotate tokens. Returns new access token, or null if session is dead.
     */
    @Synchronized
    fun refreshAccessBlocking(): String? {
        val app = runCatching { AivoraApp.instance }.getOrNull() ?: return null
        val refresh = SessionStore.refreshToken(app) ?: return null
        val body = json.encodeToString(RefreshBody.serializer(), RefreshBody(refresh = refresh))
        val request = Request.Builder()
            .url(BuildConfig.API_BASE_URL.trimEnd('/') + "/api/auth/refresh")
            .post(body.toRequestBody(jsonMedia))
            .header("Accept", "application/json")
            .build()
        return runCatching {
            refreshClient.newCall(request).execute().use { resp ->
                if (!resp.isSuccessful) {
                    if (resp.code == 401) SessionStore.clear(app)
                    return null
                }
                val raw = resp.body?.string().orEmpty()
                val tokens = json.decodeFromString(RefreshResult.serializer(), raw)
                val access = tokens.access
                val newRefresh = tokens.refresh
                if (access.isNullOrBlank() || newRefresh.isNullOrBlank()) {
                    SessionStore.clear(app)
                    return null
                }
                SessionStore.updateTokens(app, access, newRefresh)
                access
            }
        }.getOrNull()
    }

    private object TokenAuthenticator : Authenticator {
        override fun authenticate(route: Route?, response: Response): Request? {
            if (responseCount(response) >= 2) return null
            if (response.request.url.encodedPath.contains("auth/refresh")) return null
            if (response.request.url.encodedPath.contains("auth/otp")) return null
            val access = refreshAccessBlocking() ?: return null
            return response.request.newBuilder()
                .header("Authorization", "Bearer $access")
                .build()
        }

        private fun responseCount(response: Response): Int {
            var count = 1
            var prior = response.priorResponse
            while (prior != null) {
                count++
                prior = prior.priorResponse
            }
            return count
        }
    }
}
