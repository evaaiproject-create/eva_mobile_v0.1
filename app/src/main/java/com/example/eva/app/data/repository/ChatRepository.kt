package com.example.eva.app.data.repository

import com.example.eva.app.data.api.RetrofitClient
import com.example.eva.app.data.models.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ChatRepository(
    private val authRepository: AuthRepository
) {

    private val apiService = RetrofitClient.apiService

    suspend fun sendMessage(
        message: String,
        conversationId: String? = null
    ): Result<ChatMessageResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val authHeader = authRepository.getAuthHeader()
                    ?: return@withContext Result.failure(Exception("Not authenticated"))

                val request = ChatMessageRequest(
                    message = message,
                    conversationId = conversationId
                )

                val response = apiService.sendMessage(authHeader, request)

                if (response.isSuccessful) {
                    Result.success(response.body()!!)
                } else {
                    handleErrorResponse(response.code(), response.message())
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun getChatHistory(
        conversationId: String,
        limit: Int = 50
    ): Result<List<ChatMessage>> {
        return withContext(Dispatchers.IO) {
            try {
                val authHeader = authRepository.getAuthHeader()
                    ?: return@withContext Result.failure(Exception("Not authenticated"))

                val response = apiService.getChatHistory(authHeader, conversationId, limit)

                if (response.isSuccessful) {
                    Result.success(response.body() ?: emptyList())
                } else {
                    handleErrorResponse(response.code(), response.message())
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun createNewConversation(
        title: String? = null
    ): Result<NewConversationResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val authHeader = authRepository.getAuthHeader()
                    ?: return@withContext Result.failure(Exception("Not authenticated"))

                val request = if (title != null) NewConversationRequest(title) else null
                val response = apiService.newConversation(authHeader, request)

                if (response.isSuccessful) {
                    Result.success(response.body()!!)
                } else {
                    handleErrorResponse(response.code(), response.message())
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun getConversations(): Result<List<ConversationInfo>> {
        return withContext(Dispatchers.IO) {
            try {
                val authHeader = authRepository.getAuthHeader()
                    ?: return@withContext Result.failure(Exception("Not authenticated"))

                val response = apiService.getConversations(authHeader)

                if (response.isSuccessful) {
                    Result.success(response.body() ?: emptyList())
                } else {
                    handleErrorResponse(response.code(), response.message())
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun deleteConversation(
        conversationId: String
    ): Result<MessageResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val authHeader = authRepository.getAuthHeader()
                    ?: return@withContext Result.failure(Exception("Not authenticated"))

                val response = apiService.deleteConversation(authHeader, conversationId)

                if (response.isSuccessful) {
                    Result.success(response.body()!!)
                } else {
                    handleErrorResponse(response.code(), response.message())
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    private fun <T> handleErrorResponse(code: Int, message: String): Result<T> {
        val errorMessage = when (code) {
            401 -> "Session expired. Please login again."
            403 -> "Access denied."
            404 -> "Not found."
            500 -> "Server error. Please try again later."
            else -> "Error: $code - $message"
        }
        return Result.failure(Exception(errorMessage))
    }
}
