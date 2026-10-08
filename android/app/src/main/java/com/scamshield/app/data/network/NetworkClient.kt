package com.scamshield.app.data.network

import android.content.Context
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object NetworkClient {

    private const val PREFS_NAME = "scamshield_prefs"
    private const val KEY_BASE_URL = "backend_base_url"
    // Default 10.0.2.2 points to host machine from Android Emulator.
    // Can be changed in Settings to http://192.168.x.x:8000 for physical phone testing.
    const val DEFAULT_BASE_URL = "http://10.0.2.2:8000/"

    private var currentBaseUrl: String = DEFAULT_BASE_URL
    private var apiServiceInstance: ScamShieldApiService? = null

    fun getBaseUrl(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_BASE_URL, DEFAULT_BASE_URL) ?: DEFAULT_BASE_URL
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
            .baseUrl(currentBaseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service = retrofit.create(ScamShieldApiService::class.java)
        apiServiceInstance = service
        return service
    }
}
