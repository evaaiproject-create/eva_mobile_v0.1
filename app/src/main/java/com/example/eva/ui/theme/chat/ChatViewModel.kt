package com.example.eva.ui.theme.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eva.app.data.models.ChatUiState
import com.example.eva.app.data.models.MessageStatus
import com.example.eva.app.data.models.UiChatMessage
import com.example.eva.app.data.repository.ChatRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.Instant
import java.util.UUID

/*
================================================================================
CHAT VIEW MODEL
================================================================================

PURPOSE:
    Manages the state and logic for the Chat screen.
    Follows the MVVM (Model-View-ViewModel) architecture pattern.

WHAT IS A VIEWMODEL?
    A ViewModel holds UI-related data that survives configuration changes
    (like screen rotation). It also handles business logic, keeping the
    UI code simple and focused on display.

RESPONSIBILITIES:
    - Manage list of chat messages
    - Send messages to the backend
    - Handle loading and error states
    - Track the current conversation

STATE:
    - uiState: Current state of the chat (Loading, Success, Error)
    - messages: List of messages in the conversation
    - conversationId: Current conversation ID
    - isEvaTyping: Whether EVA is generating a response

================================================================================
*/

class ChatViewModel(
    private val chatRepository: ChatRepository
) : ViewModel() {

    /*
    ================================================================================
    STATE
    ================================================================================
    */

    /**
     * The main UI state for the chat screen.
     */
    private val _uiState = MutableStateFlow<ChatUiState>(ChatUiState.Loading)
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    /**
     * List of messages in the current conversation.
     */
    private val _messages = MutableStateFlow<List<UiChatMessage>>(emptyList())
    val messages: StateFlow<List<UiChatMessage>> = _messages.asStateFlow()

    /**
     * Current conversation ID.
     */
    private var currentConversationId: String? = null

    /**
     * Whether EVA is currently typing/generating a response.
     */
    private val _isEvaTyping = MutableStateFlow(false)
    val isEvaTyping: StateFlow<Boolean> = _isEvaTyping.asStateFlow()

    /*
    ================================================================================
    INITIALIZATION
    ================================================================================
    */

    init {
        // Start with a welcome message
        initializeChat()
    }

    /**
     * Initialize the chat with a welcome message.
     */
    private fun initializeChat() {
        val welcomeMessage = UiChatMessage(
            id = "welcome_${System.currentTimeMillis()}",
            content = "Hello! I'm Eva, your personal AI assistant. How can I help you today?",
            isFromUser = false,
            timestamp = System.currentTimeMillis(),
            status = MessageStatus.SENT
        )

        _messages.value = listOf(welcomeMessage)
        _uiState.value = ChatUiState.Success(
            messages = _messages.value,
            conversationId = currentConversationId ?: "new",
            isEvaTyping = false
        )
    }

    /*
    ================================================================================
    PUBLIC METHODS
    ================================================================================
    */

    /**
     * Send a message to EVA.
     *
     * FLOW:
     *     1. Add user message to UI immediately
     *     2. Show "EVA is typing" indicator
     *     3. Send message to backend
     *     4. Add EVA's response to UI
     *     5. Hide typing indicator
     *
     * PARAMETERS:
     *     messageText - The text the user typed
     */
    fun sendMessage(messageText: String) {
        if (messageText.isBlank()) return

        viewModelScope.launch {
            // Create user message
            val userMessage = UiChatMessage(
                id = "user_${UUID.randomUUID()}",
                content = messageText.trim(),
                isFromUser = true,
                timestamp = System.currentTimeMillis(),
                status = MessageStatus.SENDING
            )

            // Add user message to list
            _messages.value = _messages.value + userMessage
            updateUiState()

            // Show typing indicator
            _isEvaTyping.value = true
            updateUiState()

            // Send to backend
            val result = chatRepository.sendMessage(
                message = messageText.trim(),
                conversationId = currentConversationId
            )

            result.onSuccess { response ->
                // Update conversation ID
                currentConversationId = response.conversationId

                // Mark user message as sent
                updateMessageStatus(userMessage.id, MessageStatus.SENT)

                // Add EVA's response
                val evaMessage = UiChatMessage(
                    id = response.messageId,
                    content = response.response,
                    isFromUser = false,
                    timestamp = System.currentTimeMillis(),
                    status = MessageStatus.SENT
                )

                _messages.value = _messages.value + evaMessage
                _isEvaTyping.value = false
                updateUiState()

            }.onFailure { error ->
                // Mark user message as error
                updateMessageStatus(userMessage.id, MessageStatus.ERROR)
                _isEvaTyping.value = false
                updateUiState()

                // Optionally add error message
                val errorMessage = UiChatMessage(
                    id = "error_${UUID.randomUUID()}",
                    content = "Sorry, I couldn't process that. ${error.message}",
                    isFromUser = false,
                    timestamp = System.currentTimeMillis(),
                    status = MessageStatus.SENT
                )
                _messages.value = _messages.value + errorMessage
                updateUiState()
            }
        }
    }

    /**
     * Start a new conversation.
     *
     * Clears the current messages and resets the conversation ID.
     */
    fun startNewConversation() {
        viewModelScope.launch {
            // Reset state
            currentConversationId = null
            _messages.value = emptyList()
            _isEvaTyping.value = false

            // Create new conversation on backend
            val result = chatRepository.createNewConversation()

            result.onSuccess { response ->
                currentConversationId = response.conversationId
            }

            // Reinitialize with welcome message
            initializeChat()
        }
    }

    /**
     * Load chat history for a specific conversation.
     *
     * PARAMETERS:
     *     conversationId - Which conversation to load
     */
    fun loadConversation(conversationId: String) {
        viewModelScope.launch {
            _uiState.value = ChatUiState.Loading

            val result = chatRepository.getChatHistory(conversationId)

            result.onSuccess { chatMessages ->
                currentConversationId = conversationId

                // Convert API messages to UI messages
                _messages.value = chatMessages.map { msg ->
                    UiChatMessage(
                        id = msg.messageId,
                        content = msg.content,
                        isFromUser = msg.isFromUser,
                        timestamp = parseTimestamp(msg.timestamp),
                        status = MessageStatus.SENT
                    )
                }

                updateUiState()

            }.onFailure { error ->
                _uiState.value = ChatUiState.Error(error.message ?: "Failed to load chat")
            }
        }
    }

    /**
     * Retry sending a failed message.
     *
     * PARAMETERS:
     *     messageId - ID of the message to retry
     */
    fun retryMessage(messageId: String) {
        val message = _messages.value.find { it.id == messageId } ?: return

        // Remove the failed message
        _messages.value = _messages.value.filter { it.id != messageId }

        // Resend it
        sendMessage(message.content)
    }

    /*
    ================================================================================
    PRIVATE METHODS
    ================================================================================
    */

    /**
     * Update the UI state with current messages.
     */
    private fun updateUiState() {
        _uiState.value = ChatUiState.Success(
            messages = _messages.value,
            conversationId = currentConversationId ?: "new",
            isEvaTyping = _isEvaTyping.value
        )
    }

    /**
     * Update the status of a specific message.
     */
    private fun updateMessageStatus(messageId: String, status: MessageStatus) {
        _messages.value = _messages.value.map { msg ->
            if (msg.id == messageId) {
                msg.copy(status = status)
            } else {
                msg
            }
        }
    }

    /**
     * Parse ISO timestamp string to milliseconds.
     */
    private fun parseTimestamp(timestamp: String): Long {
        return try {
            Instant.parse(timestamp).toEpochMilli()
        } catch (e: Exception) {
            System.currentTimeMillis()
        }
    }
}