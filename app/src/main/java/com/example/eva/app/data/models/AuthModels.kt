package com.example.eva.app.data.models

import com.google.gson.annotations.SerializedName

/*
================================================================================
AUTH MODELS - AUTHENTICATION DATA STRUCTURES
================================================================================

PURPOSE:
    These classes define the shape of data sent to and received from
    the authentication endpoints (/auth/login, /auth/register).

HOW THEY'RE USED:
    1. LoginRequest is sent TO the backend
    2. Backend validates the Google token
    3. TokenResponse is received FROM the backend
    4. The app stores the access_token for future requests

SERIALIZATION:
    @SerializedName tells Gson how to convert between Kotlin and JSON.
    Example: Kotlin "idToken" <-> JSON "id_token"

================================================================================
*/

/**
 * Request body for login/register endpoints.
 *
 * SENT TO: POST /auth/login or POST /auth/register
 *
 * EXAMPLE JSON:
 *     {
 *         "id_token": "eyJhbGciOiJSUzI1NiIs...",
 *         "device_id": "android_abc123"
 *     }
 *
 * FIELDS:
 *     idToken - The Google ID token obtained from Google Sign-In.
 *               This proves the user successfully authenticated with Google.
 *
 *     deviceId - A unique identifier for this device.
 *                Used for cross-device sync and session management.
 */
data class LoginRequest(
    @SerializedName("id_token")
    val idToken: String,

    @SerializedName("device_id")
    val deviceId: String? = null
)

/**
 * Response from login/register endpoints.
 *
 * RECEIVED FROM: POST /auth/login or POST /auth/register
 *
 * EXAMPLE JSON:
 *     {
 *         "access_token": "eyJhbGciOiJIUzI1NiIs...",
 *         "token_type": "bearer",
 *         "user": {
 *             "uid": "google_user_123",
 *             "email": "user@example.com",
 *             "display_name": "John Doe",
 *             ...
 *         }
 *     }
 *
 * FIELDS:
 *     accessToken - JWT token to include in future API requests.
 *                   Include as: "Authorization: Bearer {accessToken}"
 *
 *     tokenType - Always "bearer" for this API.
 *
 *     user - The user's profile information.
 */
data class TokenResponse(
    @SerializedName("access_token")
    val accessToken: String,

    @SerializedName("token_type")
    val tokenType: String,

    @SerializedName("user")
    val user: User
)

/**
 * User profile information.
 *
 * RECEIVED FROM: Various endpoints that return user data.
 *
 * EXAMPLE JSON:
 *     {
 *         "uid": "google_user_123",
 *         "email": "user@example.com",
 *         "display_name": "John Doe",
 *         "role": "user",
 *         "created_at": "2024-01-15T10:30:00",
 *         "last_login": "2024-01-15T10:30:00",
 *         "devices": ["device_1", "device_2"],
 *         "preferences": {"theme": "dark"}
 *     }
 *
 * FIELDS:
 *     uid - Unique user identifier (from Google).
 *     email - User's email address.
 *     displayName - User's name to display in the UI.
 *     role - User role ("user" or "admin").
 *     createdAt - When the account was created.
 *     lastLogin - When the user last logged in.
 *     devices - List of registered device IDs.
 *     preferences - User's app preferences.
 */
data class User(
    @SerializedName("uid")
    val uid: String,

    @SerializedName("email")
    val email: String,

    @SerializedName("display_name")
    val displayName: String?,

    @SerializedName("role")
    val role: String = "user",

    @SerializedName("created_at")
    val createdAt: String? = null,

    @SerializedName("last_login")
    val lastLogin: String? = null,

    @SerializedName("devices")
    val devices: List<String> = emptyList(),

    @SerializedName("preferences")
    val preferences: Map<String, Any> = emptyMap()
)

/**
 * Generic error response from the API.
 *
 * RECEIVED WHEN: An API request fails (4xx or 5xx status).
 *
 * EXAMPLE JSON:
 *     {
 *         "detail": "Invalid or expired token"
 *     }
 */
data class ErrorResponse(
    @SerializedName("detail")
    val detail: String
)