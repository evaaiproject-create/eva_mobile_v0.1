package com.example.eva.app.data.repository

import android.content.Context
import android.provider.Settings
import com.example.eva.app.data.api.RetrofitClient
import com.example.eva.app.data.models.LoginRequest
import com.example.eva.app.data.models.TokenResponse
import com.example.eva.app.utils.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/*
================================================================================
AUTH REPOSITORY - AUTHENTICATION LOGIC
================================================================================

PURPOSE:
    This class handles all authentication-related operations.
    It acts as a bridge between the UI and the API.

REPOSITORY PATTERN:
    The repository pattern separates data access from business logic.
    Benefits:
    - UI doesn't need to know about API details
    - Easy to test (can mock the repository)
    - Single place to handle errors and caching

RESPONSIBILITIES:
    - Login with Google ID token
    - Register new users
    - Store session data
    - Handle authentication errors

================================================================================
*/

class AuthRepository(private val context: Context) {

    /*
    ================================================================================
    PROPERTIES
    ================================================================================
    */

    /**
     * API service for making network requests.
     */
    private val apiService = RetrofitClient.apiService

    /**
     * Session manager for storing/retrieving user session.
     */
    private val sessionManager = SessionManager(context)

    /**
     * Unique device identifier.
     *
     * Used for cross-device sync and session management.
     * ANDROID_ID is unique per app installation.
     */
    private val deviceId: String by lazy {
        Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
    }

    /*
    ================================================================================
    PUBLIC METHODS
    ================================================================================
    */

    /**
     * Authenticate user with Google ID token.
     *
     * This method handles both login and registration:
     * 1. First tries to login
     * 2. If user doesn't exist (404), registers them
     * 3. Saves session on success
     *
     * PARAMETERS:
     *     idToken - Google ID token from Google Sign-In
     *
     * RETURNS:
     *     Result.success with TokenResponse on success
     *     Result.failure with exception on error
     *
     * USAGE:
     *     val result = authRepository.authenticateWithGoogle(idToken)
     *     result.onSuccess { tokenResponse ->
     *         // Navigate to chat
     *     }.onFailure { error ->
     *         // Show error message
     *     }
     */
    suspend fun authenticateWithGoogle(idToken: String): Result<TokenResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val request = LoginRequest(
                    idToken = idToken,
                    deviceId = deviceId
                )

                // First, try to login
                val loginResponse = apiService.login(request)

                when {
                    // Login successful
                    loginResponse.isSuccessful -> {
                        val tokenResponse = loginResponse.body()!!
                        saveSession(tokenResponse)
                        Result.success(tokenResponse)
                    }

                    // User not found - need to register
                    loginResponse.code() == 404 -> {
                        // Try to register
                        val registerResponse = apiService.register(request)

                        if (registerResponse.isSuccessful) {
                            val tokenResponse = registerResponse.body()!!
                            saveSession(tokenResponse)
                            Result.success(tokenResponse)
                        } else {
                            Result.failure(
                                Exception("Registration failed: ${registerResponse.code()}")
                            )
                        }
                    }

                    // Other error
                    else -> {
                        Result.failure(
                            Exception("Login failed: ${loginResponse.code()}")
                        )
                    }
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * Check if user is currently logged in.
     *
     * RETURNS:
     *     true if user has a valid session
     */
    fun isLoggedIn(): Boolean {
        return sessionManager.isUserLoggedIn()
    }

    /**
     * Get the current access token.
     *
     * RETURNS:
     *     JWT token if logged in, null otherwise
     */
    fun getAccessToken(): String? {
        return sessionManager.getAccessToken()
    }

    /**
     * Get the authorization header value.
     *
     * RETURNS:
     *     "Bearer {token}" if logged in, null otherwise
     *
     * USAGE:
     *     val auth = authRepository.getAuthHeader()
     *     apiService.someEndpoint(auth, ...)
     */
    fun getAuthHeader(): String? {
        val token = getAccessToken() ?: return null
        return "Bearer $token"
    }

    /**
     * Logout the current user.
     *
     * Clears all stored session data.
     */
    fun logout() {
        sessionManager.clearSession()
    }

    /**
     * Get the session manager instance.
     *
     * Useful for observing login state changes.
     */
    fun getSessionManager(): SessionManager {
        return sessionManager
    }

    /*
    ================================================================================
    PRIVATE METHODS
    ================================================================================
    */

    /**
     * Save session data after successful authentication.
     */
    private fun saveSession(tokenResponse: TokenResponse) {
        sessionManager.saveSession(
            accessToken = tokenResponse.accessToken,
            userId = tokenResponse.user.uid,
            email = tokenResponse.user.email,
            displayName = tokenResponse.user.displayName ?: "User",
            pictureUrl = null // Google picture URL not included in our response
        )
    }
}