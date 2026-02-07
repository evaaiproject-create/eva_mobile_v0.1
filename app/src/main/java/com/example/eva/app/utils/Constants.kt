package com.example.eva.app.utils

object Constants {

    /*
    ================================================================================
    API CONFIGURATION
    ================================================================================
    These values configure how the app communicates with your Cloud Run backend.
    ================================================================================
    */

    /**
     * Base URL for the Eva backend API.
     *
     * This is your Cloud Run service URL.
     * All API calls will be made to endpoints under this URL.
     *
     * Example: If BASE_URL is "https://example.run.app/"
     *          Then chat endpoint is "https://example.run.app/chat/send"
     *
     * IMPORTANT: Must end with a trailing slash "/"
     */
    const val BASE_URL = "https://ai-assist-81503423918.europe-west1.run.app/"

    /**
     * API request timeout in seconds.
     *
     * How long to wait for a response before giving up.
     * AI responses can take a few seconds, so we use a longer timeout.
     */
    const val API_TIMEOUT_SECONDS = 60L

    /**
     * Connection timeout in seconds.
     *
     * How long to wait when establishing a connection.
     * If the server doesn't respond in this time, show an error.
     */
    const val CONNECTION_TIMEOUT_SECONDS = 30L

    /*
    ================================================================================
    GOOGLE SIGN-IN CONFIGURATION
    ================================================================================
    These values are used for Google Sign-In authentication.

    HOW TO GET YOUR WEB CLIENT ID:
    1. Go to Google Cloud Console (console.cloud.google.com)
    2. Select your project
    3. Go to APIs & Services > Credentials
    4. Find the OAuth 2.0 Client ID of type "Web application"
    5. Copy the Client ID (ends with .apps.googleusercontent.com)

    NOTE: You need the WEB client ID, not the Android client ID!
          The Android client ID is only for configuring the OAuth consent screen.
    ================================================================================
    */

    /**
     * Google OAuth Web Client ID.
     *
     * This is the Client ID from your Google Cloud Console.
     * It's used to verify the user's identity with Google.
     *
     * SECURITY NOTE: This is safe to include in the app - it's not a secret.
     *                It only identifies your app, not authenticates it.
     */
    const val GOOGLE_WEB_CLIENT_ID = "81503423918-3sujbguhqjn6ns89hjb3858t92aksher.apps.googleusercontent.com"

    /*
    ================================================================================
    APP CONFIGURATION
    ================================================================================
    General app settings and limits.
    ================================================================================
    */

    /**
     * Splash screen display duration in milliseconds.
     *
     * How long to show the splash screen before checking login status.
     * 2000ms (2 seconds) feels snappy but gives time to load.
     */
    const val SPLASH_DELAY_MS = 2000L

    /**
     * Maximum message length in characters.
     *
     * Prevents users from sending extremely long messages.
     * Matches the backend validation limit.
     */
    const val MAX_MESSAGE_LENGTH = 10000

    /**
     * Number of messages to load per page.
     *
     * When loading chat history, how many messages to fetch at once.
     * Lower = faster initial load, more requests for history
     * Higher = slower initial load, fewer requests
     */
    const val MESSAGES_PER_PAGE = 50

    /*
    ================================================================================
    SHARED PREFERENCES KEYS
    ================================================================================
    Keys for storing data in SharedPreferences/DataStore.

    SharedPreferences is a simple key-value storage system.
    We use it to persist data between app launches.
    ================================================================================
    */

    /**
     * Name of the preferences file for authentication data.
     */
    const val PREFS_AUTH = "auth_prefs"

    /**
     * Key for storing the JWT access token.
     *
     * This token is sent with every API request to authenticate the user.
     */
    const val KEY_ACCESS_TOKEN = "access_token"

    /**
     * Key for storing the user's ID.
     */
    const val KEY_USER_ID = "user_id"

    /**
     * Key for storing the user's email.
     */
    const val KEY_USER_EMAIL = "user_email"

    /**
     * Key for storing the user's display name.
     */
    const val KEY_USER_NAME = "user_name"

    /**
     * Key for storing the user's profile picture URL.
     */
    const val KEY_USER_PICTURE = "user_picture"

    /**
     * Key for storing whether the user is logged in.
     */
    const val KEY_IS_LOGGED_IN = "is_logged_in"

    /**
     * Key for storing the current conversation ID.
     */
    const val KEY_CURRENT_CONVERSATION = "current_conversation_id"

    /*
    ================================================================================
    NAVIGATION ROUTES
    ================================================================================
    Route names for Jetpack Compose Navigation.
    These are like URLs for screens in your app.
    ================================================================================
    */

    const val ROUTE_SPLASH = "splash"
    const val ROUTE_LOGIN = "login"
    const val ROUTE_CHAT = "chat"
    const val ROUTE_CALL = "call"
    const val ROUTE_TEMPLATE_1 = "template1"
    const val ROUTE_TEMPLATE_2 = "template2"
    const val ROUTE_TEMPLATE_3 = "template3"
    const val ROUTE_SETTINGS = "settings"
}