package com.eva.app.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.example.eva.app.utils.Constants
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/*
================================================================================
SESSION MANAGER - USER SESSION HANDLING
================================================================================

PURPOSE:
    This class manages the user's login session. It handles:
    - Storing the authentication token securely
    - Saving user information (name, email, picture)
    - Checking if the user is logged in
    - Logging out (clearing session data)

HOW IT WORKS:
    When a user logs in:
    1. Backend returns an access token and user info
    2. SessionManager saves this to SharedPreferences
    3. The token is included in all future API requests

    When the app starts:
    1. SessionManager checks if a token exists
    2. If yes, user goes to chat screen
    3. If no, user goes to login screen

WHAT IS SHAREDPREFERENCES?
    A simple key-value storage system that persists data on the device.
    Data survives app restarts but is deleted when the app is uninstalled.

SECURITY:
    - Tokens are stored in private SharedPreferences (only this app can access)
    - Consider using EncryptedSharedPreferences for production

================================================================================
*/

class SessionManager(context: Context) {

    /*
    ================================================================================
    PROPERTIES
    ================================================================================
    */

    /**
     * SharedPreferences instance for storing auth data.
     *
     * MODE_PRIVATE means only this app can read/write this file.
     */
    private val prefs: SharedPreferences = context.getSharedPreferences(
        Constants.PREFS_AUTH,
        Context.MODE_PRIVATE
    )

    /**
     * StateFlow that emits the current login state.
     *
     * UI components can observe this to react to login/logout.
     * StateFlow is like LiveData but works better with Compose.
     */
    private val _isLoggedIn = MutableStateFlow(checkLoginStatus())
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    /**
     * StateFlow that emits the current user info.
     */
    private val _currentUser = MutableStateFlow(loadUserInfo())
    val currentUser: StateFlow<UserInfo?> = _currentUser.asStateFlow()

    /*
    ================================================================================
    DATA CLASSES
    ================================================================================
    */

    /**
     * Simple data class to hold user information.
     *
     * This is separate from the API User model because we only store
     * essential info locally.
     */
    data class UserInfo(
        val userId: String,
        val email: String,
        val displayName: String,
        val pictureUrl: String?
    )

    /*
    ================================================================================
    PUBLIC METHODS
    ================================================================================
    */

    /**
     * Save user session after successful login.
     *
     * WHEN TO CALL:
     *     After receiving a successful login response from the backend.
     *
     * PARAMETERS:
     *     accessToken - JWT token for API authentication
     *     userId - Unique user identifier
     *     email - User's email address
     *     displayName - User's display name
     *     pictureUrl - URL to user's profile picture (optional)
     *
     * EXAMPLE:
     *     sessionManager.saveSession(
     *         accessToken = "eyJhbG...",
     *         userId = "user_123",
     *         email = "user@example.com",
     *         displayName = "John Doe",
     *         pictureUrl = "https://..."
     *     )
     */
    fun saveSession(
        accessToken: String,
        userId: String,
        email: String,
        displayName: String,
        pictureUrl: String? = null
    ) {
        // Save all values to SharedPreferences
        prefs.edit {
            putString(Constants.KEY_ACCESS_TOKEN, accessToken)
            putString(Constants.KEY_USER_ID, userId)
            putString(Constants.KEY_USER_EMAIL, email)
            putString(Constants.KEY_USER_NAME, displayName)
            putString(Constants.KEY_USER_PICTURE, pictureUrl)
            putBoolean(Constants.KEY_IS_LOGGED_IN, true)
        }

        // Update StateFlows so UI reacts immediately
        _isLoggedIn.value = true
        _currentUser.value = UserInfo(userId, email, displayName, pictureUrl)
    }

    /**
     * Get the stored access token.
     *
     * RETURNS:
     *     The JWT token if logged in, null otherwise.
     *
     * USAGE:
     *     Include this token in API requests:
     *     "Authorization: Bearer ${sessionManager.getAccessToken()}"
     */
    fun getAccessToken(): String? {
        return prefs.getString(Constants.KEY_ACCESS_TOKEN, null)
    }

    /**
     * Get the current user's ID.
     */
    fun getUserId(): String? {
        return prefs.getString(Constants.KEY_USER_ID, null)
    }

    /**
     * Get the current user's display name.
     */
    fun getUserName(): String? {
        return prefs.getString(Constants.KEY_USER_NAME, null)
    }

    /**
     * Get the current user's email.
     */
    fun getUserEmail(): String? {
        return prefs.getString(Constants.KEY_USER_EMAIL, null)
    }

    /**
     * Get the current user's profile picture URL.
     */
    fun getUserPicture(): String? {
        return prefs.getString(Constants.KEY_USER_PICTURE, null)
    }

    /**
     * Check if user is currently logged in.
     *
     * RETURNS:
     *     true if user has a valid session, false otherwise.
     *
     * NOTE:
     *     This only checks if a token EXISTS, not if it's still valid.
     *     The backend will return 401 if the token is expired.
     */
    fun isUserLoggedIn(): Boolean {
        return prefs.getBoolean(Constants.KEY_IS_LOGGED_IN, false) &&
                getAccessToken() != null
    }

    /**
     * Clear the user session (logout).
     *
     * WHEN TO CALL:
     *     - User taps "Logout"
     *     - Backend returns 401 (token expired)
     *     - User requests account deletion
     *
     * WHAT IT DOES:
     *     Clears all stored session data and updates StateFlows.
     */
    fun clearSession() {
        // Clear all stored values
        prefs.edit {
            remove(Constants.KEY_ACCESS_TOKEN)
            remove(Constants.KEY_USER_ID)
            remove(Constants.KEY_USER_EMAIL)
            remove(Constants.KEY_USER_NAME)
            remove(Constants.KEY_USER_PICTURE)
            putBoolean(Constants.KEY_IS_LOGGED_IN, false)
        }

        // Update StateFlows
        _isLoggedIn.value = false
        _currentUser.value = null
    }

    /**
     * Save the current conversation ID.
     *
     * Used to remember which conversation the user was viewing.
     */
    fun saveCurrentConversation(conversationId: String) {
        prefs.edit {
            putString(Constants.KEY_CURRENT_CONVERSATION, conversationId)
        }
    }

    /**
     * Get the last viewed conversation ID.
     */
    fun getCurrentConversation(): String? {
        return prefs.getString(Constants.KEY_CURRENT_CONVERSATION, null)
    }

    /*
    ================================================================================
    PRIVATE METHODS
    ================================================================================
    */

    /**
     * Check login status from SharedPreferences.
     * Called during initialization.
     */
    private fun checkLoginStatus(): Boolean {
        return isUserLoggedIn()
    }

    /**
     * Load user info from SharedPreferences.
     * Called during initialization.
     */
    private fun loadUserInfo(): UserInfo? {
        if (!isUserLoggedIn()) return null

        val userId = getUserId() ?: return null
        val email = getUserEmail() ?: return null
        val name = getUserName() ?: "User"
        val picture = getUserPicture()

        return UserInfo(userId, email, name, picture)
    }
}