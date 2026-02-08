package com.example.eva.app.data.api

import com.example.eva.app.utils.Constants
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/*
================================================================================
RETROFIT CLIENT - HTTP CLIENT CONFIGURATION
================================================================================

PURPOSE:
    This object creates and configures the Retrofit HTTP client.
    It's a singleton (object) so the same client is used throughout the app.

WHAT IS RETROFIT?
    Retrofit is a type-safe HTTP client for Android.
    It turns your API interface into a callable object.

WHAT IS OKHTTP?
    OkHttp is the underlying HTTP engine that Retrofit uses.
    We configure timeouts, logging, and interceptors here.

CONFIGURATION:
    - Base URL: Your Cloud Run backend
    - Timeouts: 60 seconds for AI responses
    - Logging: Shows API calls in Logcat (debug builds only)
    - Gson: Converts JSON to Kotlin objects

================================================================================
*/

object RetrofitClient {

    /**
     * Logging interceptor for debugging.
     *
     * This logs all HTTP requests and responses to Logcat.
     * LEVEL.BODY shows the full request/response including JSON.
     *
     * WARNING: Set to LEVEL.NONE in production to avoid logging sensitive data!
     */
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    /**
     * OkHttp client with custom configuration.
     *
     * TIMEOUTS:
     *     connectTimeout - Time to establish connection
     *     readTimeout - Time to wait for response
     *     writeTimeout - Time to upload request body
     *
     * AI responses can take several seconds, so we use longer timeouts.
     */
    private val okHttpClient = OkHttpClient.Builder()
        // Add logging (shows requests in Logcat)
        .addInterceptor(loggingInterceptor)

        // Connection timeout: 30 seconds
        .connectTimeout(Constants.CONNECTION_TIMEOUT_SECONDS, TimeUnit.SECONDS)

        // Read timeout: 60 seconds (AI can take a while)
        .readTimeout(Constants.API_TIMEOUT_SECONDS, TimeUnit.SECONDS)

        // Write timeout: 30 seconds
        .writeTimeout(Constants.CONNECTION_TIMEOUT_SECONDS, TimeUnit.SECONDS)

        .build()

    /**
     * Retrofit instance configured for the Eva API.
     *
     * BASE URL:
     *     The Cloud Run URL from Constants.
     *     All endpoint paths are appended to this.
     *
     * CONVERTERS:
     *     GsonConverterFactory converts JSON to/from Kotlin objects.
     */
    private val retrofit = Retrofit.Builder()
        .baseUrl(Constants.BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    /**
     * The API service instance.
     *
     * USAGE:
     *     val response = RetrofitClient.apiService.login(request)
     *
     * This is a lazy val, so it's only created when first accessed.
     */
    val apiService: EvaApiService by lazy {
        retrofit.create(EvaApiService::class.java)
    }
}