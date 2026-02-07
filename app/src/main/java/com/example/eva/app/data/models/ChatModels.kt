package com.eva.app.data.models

import com.google.gson.annotations.SerializedName

/*
================================================================================
CHAT MODELS - CHAT DATA STRUCTURES
================================================================================

PURPOSE:
    These classes define the shape of data for chat functionality.
    They map directly to the backend's /chat/* endpoints.

MODELS:
    - ChatMessageRequest: What we send when user types a message
    - ChatMessageResponse: What we receive after sending a message
    - ConversationInfo: Metadata about a conversation (for the sidebar)
    - ChatMessage: A single message in the chat history

================================================================================
*/

/**
 * Request body for sending a chat message.
 *
 * SENT TO: POST /chat/send
 *
 * EXAMPLE JSON:
 *     {
 *         "message": "Hello EVA!",
 *         "conversation_id": "conv_abc123"
 *     }
 */
data class ChatMessageRequest(
    @SerializedName("message")
    val message: String,

    @SerializedName("conversation_id")
    val conversationId: String? = null
)

/**
 * Response from sending a chat message.
 *
 * RECEIVED FROM: POST /chat/send
 *
 * EXAMPLE JSON:
 *     {
 *         "message_id": "msg_xyz789",
 *         "conversation_id": "conv_abc123",
 *         "response": "Hello! How can I help you?",
 *         "timestamp": "2024-01-15T10:30:00",
 *         "emotion_detected": null
 *     }
 */
data class ChatMessageResponse(
    @SerializedName("message_id")
    val messageId: String,

    @SerializedName("conversation_id")
    val conversationId: String,

    @SerializedName("response")
    val response: String,

    @SerializedName("timestamp")
    val timestamp: String,

    @SerializedName("emotion_detected")
    val emotionDetected: String? = null
)

/**
 * Information about a conversation.
 *
 * RECEIVED FROM: GET /chat/conversations
 *
 * Used to populate the conversation list in the navigation drawer.
 *
 * EXAMPLE JSON:
 *     {
 *         "conversation_id": "conv_abc123",
 *         "title": "Chat about weather",
 *         "created_at": "2024-01-15T10:00:00",
 *         "updated_at": "2024-01-15T10:30:00",
 *         "message_count": 5
 *     }
 */
data class ConversationInfo(
    @SerializedName("conversation_id")
    val conversationId: String,

    @SerializedName("title")
    val title: String,

    @SerializedName("created_at")
    val createdAt: String,

    @SerializedName("updated_at")
    val updatedAt: String,

    @SerializedName("message_count")
    val messageCount: Int
)

/**
 * A single chat message from history.
 *
 * RECEIVED FROM: GET /chat/history/{conversation_id}
 *
 * EXAMPLE JSON:
 *     {
 *         "message_id": "msg_abc123",
 *         "conversation_id": "conv_xyz789",
 *         "content": "Hello!",
 *         "role": "user",
 *         "timestamp": "2024-01-15T10:30:00"
 *     }
 */
data class ChatMessage(
    @SerializedName("message_id")
    val messageId: String,

    @SerializedName("conversation_id")
    val conversationId: String,

    @SerializedName("content")
    val content: String,

    @SerializedName("role")
    val role: String,  // "user" or "assistant"

    @SerializedName("timestamp")
    val timestamp: String
) {
    /**
     * Check if this message is from the user.
     */
    val isFromUser: Boolean
        get() = role == "user"

    /**
     * Check if this message is from EVA (the assistant).
     */
    val isFromEva: Boolean
        get() = role == "assistant"
}

/**
 * Request to create a new conversation.
 *
 * SENT TO: POST /chat/new
 */
data class NewConversationRequest(
    @SerializedName("title")
    val title: String? = null
)

/**
 * Response from creating a new conversation.
 *
 * RECEIVED FROM: POST /chat/new
 */
data class NewConversationResponse(
    @SerializedName("conversation_id")
    val conversationId: String,

    @SerializedName("title")
    val title: String,

    @SerializedName("created_at")
    val createdAt: String
)

/**
 * Generic message response (for delete operations, etc.)
 *
 * RECEIVED FROM: DELETE /chat/{id}
 */
data class MessageResponse(
    @SerializedName("message")
    val message: String
)

/*
================================================================================
UI STATE MODELS
================================================================================
These models are used for UI state management, not API communication.
================================================================================
*/

/**
 * Represents a message in the chat UI.
 *
 * This combines API data with UI-specific properties.
 */
data class UiChatMessage(
    val id: String,
    val content: String,
    val isFromUser: Boolean,
    val timestamp: Long,
    val status: MessageStatus = MessageStatus.SENT
)

/**
 * Status of a message in the UI.
 */
enum class MessageStatus {
    SENDING,    // Message is being sent
    SENT,       // Message sent successfully
    ERROR       // Failed to send
}

/**
 * State of the chat screen.
 */
sealed class ChatUiState {
    /**
     * Loading state - fetching data.
     */
    object Loading : ChatUiState()

    /**
     * Success state - data loaded.
     */
    data class Success(
        val messages: List<UiChatMessage>,
        val conversationId: String,
        val isEvaTyping: Boolean = false
    ) : ChatUiState()

    /**
     * Error state - something went wrong.
     */
    data class Error(val message: String) : ChatUiState()
}