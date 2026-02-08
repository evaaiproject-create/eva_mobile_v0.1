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
     * ENDPOINT: POST /api/auth/register
     */
    @POST("api/auth/register")
    suspend fun register(
        @Body request: LoginRequest
    ): Response<TokenResponse>

    /**
     * Login an existing user.
     * ENDPOINT: POST /api/auth/login
     */
    @POST("api/auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<TokenResponse>

    /**
     * Verify if a token is valid.
     * ENDPOINT: GET /api/auth/verify
     */
    @GET("api/auth/verify")
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
     * ENDPOINT: POST /api/chat/send
     */
    @POST("api/chat/send")
    suspend fun sendMessage(
        @Header("Authorization") authorization: String,
        @Body request: ChatMessageRequest
    ): Response<ChatMessageResponse>

    /**
     * Get chat history for a conversation.
     * ENDPOINT: GET /api/chat/history/{conversation_id}
     */
    @GET("api/chat/history/{conversation_id}")
    suspend fun getChatHistory(
        @Header("Authorization") authorization: String,
        @Path("conversation_id") conversationId: String,
        @Query("limit") limit: Int = 50
    ): Response<List<ChatMessage>>

    /**
     * Start a new conversation.
     * ENDPOINT: POST /api/chat/new
     */
    @POST("api/chat/new")
    suspend fun newConversation(
        @Header("Authorization") authorization: String,
        @Body request: NewConversationRequest? = null
    ): Response<NewConversationResponse>

    /**
     * Get all conversations for the current user.
     * ENDPOINT: GET /api/chat/conversations
     */
    @GET("api/chat/conversations")
    suspend fun getConversations(
        @Header("Authorization") authorization: String
    ): Response<List<ConversationInfo>>

    /**
     * Delete a conversation.
     * ENDPOINT: DELETE /api/chat/{conversation_id}
     */
    @DELETE("api/chat/{conversation_id}")
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
     * ENDPOINT: GET /api/users/me
     */
    @GET("api/users/me")
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
     * ENDPOINT: GET /api/health
     */
    @GET("api/health")
    suspend fun healthCheck(): Response<Map<String, Any>>
}