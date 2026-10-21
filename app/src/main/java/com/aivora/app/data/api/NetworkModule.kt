package com.aivora.app.data.api

import com.aivora.app.BuildConfig
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit

object NetworkModule {
    val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        explicitNulls = false
    }

    fun okHttp(tokenProvider: () -> String?): OkHttpClient {
        val log = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
        return OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .addInterceptor { chain ->
                val req = chain.request().newBuilder().apply {
                    tokenProvider()?.let { header("Authorization", "Bearer $it") }
                    header("Accept", "application/json")
                }.build()
                chain.proceed(req)
            }
            .addInterceptor(log)
            .build()
    }

    fun api(client: OkHttpClient): AivoraApi =
        Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(AivoraApi::class.java)
}
