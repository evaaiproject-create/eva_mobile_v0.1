package com.example.eva.app.data.models

import com.google.gson.annotations.SerializedName





data class ChatMessageRequest(
    @SerializedName("message")
    val message: String,

    @SerializedName("conversation_id")
    val conversationId: String? = null
)


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

    val isFromUser: Boolean
        get() = role == "user"


    val isFromEva: Boolean
        get() = role == "assistant"
}


data class NewConversationRequest(
    @SerializedName("title")
    val title: String? = null
)


data class NewConversationResponse(
    @SerializedName("conversation_id")
    val conversationId: String,

    @SerializedName("title")
    val title: String,

    @SerializedName("created_at")
    val createdAt: String
)


data class MessageResponse(
    @SerializedName("message")
    val message: String
)

data class UiChatMessage(
    val id: String,
    val content: String,
    val isFromUser: Boolean,
    val timestamp: Long,
    val status: MessageStatus = MessageStatus.SENT
)


enum class MessageStatus {
    SENDING,    // Message is being sent
    SENT,       // Message sent successfully
    ERROR       // Failed to send
}


sealed class ChatUiState {

    object Loading : ChatUiState()


    data class Success(
        val messages: List<UiChatMessage>,
        val conversationId: String,
        val isEvaTyping: Boolean = false
    ) : ChatUiState()


    data class Error(val message: String) : ChatUiState()
}