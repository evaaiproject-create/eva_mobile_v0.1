package com.example.eva.app.data.api

import com.example.eva.app.data.models.LoginRequest
import com.example.eva.app.data.models.TokenResponse
import com.example.eva.app.data.models.User
import com.example.eva.app.data.models.*
import retrofit2.Response
import retrofit2.http.*

/*
================================================================================
EVA API SERVICE - RETROFIT INTERFACE
================================================================================

PURPOSE:
    This interface defines all the API endpoints that the app can call.
    Retrofit uses this interface to generate the actual HTTP client code.

HOW RETROFIT WORKS:
    1. You define an interface with annotated methods
    2. Retrofit generates the implementation at runtime
    3. You call methods like normal Kotlin functions
    4. Retrofit converts them to HTTP requests

ANNOTATIONS:
    @GET, @POST, @PUT, @DELETE - HTTP method
    @Path - URL path parameter
    @Query - URL query parameter
    @Body - Request body (JSON)
    @Header - HTTP header

EXAMPLE:
    @GET("users/{id}")
    suspend fun getUser(@Path("id") userId: String): Response<User>

    This becomes: GET https://api.example.com/users/123

================================================================================
*/

interface EvaApiService {

    /*
    ================================================================================
    AUTHENTICATION ENDPOINTS
    ================================================================================
    */

    /**
     * Register a new user with Google OAuth.
     *
     * ENDPOINT: POST /auth/register
     *
     * WHEN TO USE:
     *     First time a user signs in with Google.
     *     If user already exists, backend returns 409 Conflict.
     *
     * PARAMETERS:
     *     request - Contains Google ID token and optional device ID
     *
     * RETURNS:
     *     TokenResponse with access_token and user info
     */
    @POST("auth/register")
    suspend fun register(
        @Body request: LoginRequest
    ): Response<TokenResponse>

    /**
     * Login an existing user.
     *
     * ENDPOINT: POST /auth/login
     *
     * WHEN TO USE:
     *     User has signed in before (already registered).
     *     If user doesn't exist, backend returns 404 Not Found.
     *
     * PARAMETERS:
     *     request - Contains Google ID token and optional device ID
     *
     * RETURNS:
     *     TokenResponse with access_token and user info
     */
    @POST("auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<TokenResponse>

    /**
     * Verify if a token is valid.
     *
     * ENDPOINT: GET /auth/verify
     *
     * WHEN TO USE:
     *     Check if stored token is still valid before making requests.
     *     Optional - can just handle 401 errors instead.
     */
    @GET("auth/verify")
    suspend fun verifyToken(
        @Query("token") token: String
    ): Response<Map<String, Boolean>>

    /*
    ================================================================================
    CHAT ENDPOINTS
    ================================================================================
    */

    /**
     * Send a message to EVA and get a response.
     *
     * ENDPOINT: POST /chat/send
     *
     * THIS IS THE MAIN CHAT METHOD.
     *
     * FLOW:
     *     1. User types message
     *     2. App calls this endpoint
     *     3. Backend saves message, sends to Gemini, saves response
     *     4. App receives EVA's response
     *
     * PARAMETERS:
     *     authorization - "Bearer {access_token}"
     *     request - Message text and optional conversation ID
     *
     * RETURNS:
     *     ChatMessageResponse with EVA's response
     */
    @POST("chat/send")
    suspend fun sendMessage(
        @Header("Authorization") authorization: String,
        @Body request: ChatMessageRequest
    ): Response<ChatMessageResponse>

    /**
     * Get chat history for a conversation.
     *
     * ENDPOINT: GET /chat/history/{conversation_id}
     *
     * WHEN TO USE:
     *     Loading a conversation to display previous messages.
     *
     * PARAMETERS:
     *     authorization - "Bearer {access_token}"
     *     conversationId - Which conversation to get
     *     limit - Max messages to return (default 50)
     *
     * RETURNS:
     *     List of ChatMessage objects
     */
    @GET("chat/history/{conversation_id}")
    suspend fun getChatHistory(
        @Header("Authorization") authorization: String,
        @Path("conversation_id") conversationId: String,
        @Query("limit") limit: Int = 50
    ): Response<List<ChatMessage>>

    /**
     * Start a new conversation.
     *
     * ENDPOINT: POST /chat/new
     *
     * WHEN TO USE:
     *     User taps "New Chat" in the navigation drawer.
     *
     * PARAMETERS:
     *     authorization - "Bearer {access_token}"
     *     request - Optional custom title
     *
     * RETURNS:
     *     NewConversationResponse with new conversation ID
     */
    @POST("chat/new")
    suspend fun newConversation(
        @Header("Authorization") authorization: String,
        @Body request: NewConversationRequest? = null
    ): Response<NewConversationResponse>

    /**
     * Get all conversations for the current user.
     *
     * ENDPOINT: GET /chat/conversations
     *
     * WHEN TO USE:
     *     Populating the conversation list in navigation drawer.
     *
     * RETURNS:
     *     List of ConversationInfo objects
     */
    @GET("chat/conversations")
    suspend fun getConversations(
        @Header("Authorization") authorization: String
    ): Response<List<ConversationInfo>>

    /**
     * Delete a conversation.
     *
     * ENDPOINT: DELETE /chat/{conversation_id}
     *
     * WHEN TO USE:
     *     User wants to delete a conversation.
     *     This is permanent!
     */
    @DELETE("chat/{conversation_id}")
    suspend fun deleteConversation(
        @Header("Authorization") authorization: String,
        @Path("conversation_id") conversationId: String
    ): Response<MessageResponse>

    /*
    ================================================================================
    USER ENDPOINTS
    ================================================================================
    */

    /**
     * Get current user's profile.
     *
     * ENDPOINT: GET /users/me
     */
    @GET("users/me")
    suspend fun getCurrentUser(
        @Header("Authorization") authorization: String
    ): Response<User>

    /*
    ================================================================================
    SYSTEM ENDPOINTS
    ================================================================================
    */

    /**
     * Health check endpoint.
     *
     * ENDPOINT: GET /health
     *
     * WHEN TO USE:
     *     Check if backend is reachable before showing errors.
     */
    @GET("health")
    suspend fun healthCheck(): Response<Map<String, Any>>
}