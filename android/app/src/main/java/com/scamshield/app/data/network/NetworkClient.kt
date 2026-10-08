package com.scamshield.app.data.network

import android.content.Context
import android.os.Build
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object NetworkClient {

    private const val PREFS_NAME = "scamshield_prefs"
    private const val KEY_BASE_URL = "backend_base_url"

    private fun isEmulator(): Boolean {
        return (Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.startsWith("unknown")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86")
                || Build.MANUFACTURER.contains("Genymotion")
                || (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic"))
                || "google_sdk" == Build.PRODUCT)
    }

    fun getDefaultBaseUrl(): String {
        return if (isEmulator()) "http://10.0.2.2:8000/" else "http://127.0.0.1:8000/"
    }

    const val DEFAULT_BASE_URL = "http://10.0.2.2:8000/"

    private var currentBaseUrl: String? = null
    private var apiServiceInstance: ScamShieldApiService? = null

    fun getBaseUrl(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_BASE_URL, null) ?: getDefaultBaseUrl()
    }

    fun setBaseUrl(context: Context, newUrl: String) {
        val normalized = if (newUrl.endsWith("/")) newUrl else "$newUrl/"
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_BASE_URL, normalized).apply()
        currentBaseUrl = normalized
        apiServiceInstance = null // Invalidate instance to rebuild with new URL
    }

    fun getApiService(context: Context): ScamShieldApiService {
        val configuredUrl = getBaseUrl(context)
        if (apiServiceInstance != null && currentBaseUrl == configuredUrl) {
            return apiServiceInstance!!
        }

        currentBaseUrl = configuredUrl

        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(8, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(8, TimeUnit.SECONDS)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(configuredUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service = retrofit.create(ScamShieldApiService::class.java)
        apiServiceInstance = service
        return service
    }
}
