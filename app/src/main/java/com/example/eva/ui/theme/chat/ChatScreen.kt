package com.example.eva.ui.theme.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.eva.R
import com.example.eva.app.data.models.MessageStatus
import com.example.eva.app.data.models.UiChatMessage
import com.example.eva.app.data.repository.AuthRepository
import com.example.eva.app.data.repository.ChatRepository
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/*
================================================================================
CHAT SCREEN
================================================================================

PURPOSE:
    The main chat interface where users communicate with EVA.

COMPONENTS:
    - Top App Bar: Menu button, title, call button
    - Message List: Scrollable list of chat messages
    - Input Area: Text field and send button

FEATURES:
    - Real-time messaging with EVA
    - "EVA is typing" indicator
    - Message timestamps
    - Scroll to bottom on new messages
    - Keyboard handling

================================================================================
*/

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    chatRepository: ChatRepository,
    authRepository: AuthRepository,
    onOpenDrawer: () -> Unit,
    onStartCall: () -> Unit
) {
    /*
    ================================================================================
    VIEW MODEL
    ================================================================================
    */

    // Create ViewModel (in production, use ViewModelProvider or Hilt)
    val viewModel = remember { ChatViewModel(chatRepository) }

    /*
    ================================================================================
    STATE
    ================================================================================
    */

    val uiState by viewModel.uiState.collectAsState()
    val messages by viewModel.messages.collectAsState()
    val isEvaTyping by viewModel.isEvaTyping.collectAsState()

    // Input text state
    var inputText by remember { mutableStateOf("") }

    // List state for scrolling
    val listState = rememberLazyListState()

    /*
    ================================================================================
    EFFECTS
    ================================================================================
    */

    // Scroll to bottom when new messages arrive
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    /*
    ================================================================================
    FUNCTIONS
    ================================================================================
    */

    fun sendMessage() {
        if (inputText.isNotBlank()) {
            viewModel.sendMessage(inputText)
            inputText = ""
        }
    }

    /*
    ================================================================================
    UI
    ================================================================================
    */

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = stringResource(R.string.chat_title),
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            text = stringResource(R.string.chat_subtitle),
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onOpenDrawer) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = stringResource(R.string.cd_menu)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onStartCall) {
                        Icon(
                            imageVector = Icons.Default.Call,
                            contentDescription = stringResource(R.string.cd_start_call),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            // Input area
            ChatInputBar(
                inputText = inputText,
                onInputChange = { inputText = it },
                onSend = { sendMessage() },
                isEnabled = !isEvaTyping
            )
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Message list
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(messages, key = { it.id }) { message ->
                    ChatMessageItem(message = message)
                }

                // Typing indicator
                if (isEvaTyping) {
                    item {
                        TypingIndicator()
                    }
                }
            }
        }
    }
}

/*
================================================================================
CHAT MESSAGE ITEM
================================================================================
*/

@Composable
fun ChatMessageItem(message: UiChatMessage) {
    val alignment = if (message.isFromUser) Alignment.CenterEnd else Alignment.CenterStart

    val bubbleColor = if (message.isFromUser) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.surface
    }

    val bubbleShape = if (message.isFromUser) {
        RoundedCornerShape(16.dp, 16.dp, 4.dp, 16.dp)
    } else {
        RoundedCornerShape(16.dp, 16.dp, 16.dp, 4.dp)
    }

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = alignment
    ) {
        Column(
            horizontalAlignment = if (message.isFromUser) Alignment.End else Alignment.Start
        ) {
            // Message bubble
            Box(
                modifier = Modifier
                    .widthIn(max = 280.dp)
                    .background(bubbleColor, bubbleShape)
                    .padding(12.dp)
            ) {
                Text(
                    text = message.content,
                    color = Color.White,
                    fontSize = 15.sp
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Timestamp and status
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formatTimestamp(message.timestamp),
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Show status for user messages
                if (message.isFromUser && message.status == MessageStatus.ERROR) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "• Failed",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

/*
================================================================================
TYPING INDICATOR
================================================================================
*/

@Composable
fun TypingIndicator() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(start = 8.dp)
    ) {
        Text(
            text = stringResource(R.string.chat_typing),
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/*
================================================================================
CHAT INPUT BAR
================================================================================
*/

@Composable
fun ChatInputBar(
    inputText: String,
    onInputChange: (String) -> Unit,
    onSend: () -> Unit,
    isEnabled: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(8.dp)
            .imePadding(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Text input
        TextField(
            value = inputText,
            onValueChange = onInputChange,
            modifier = Modifier
                .weight(1f)
                .padding(end = 8.dp),
            placeholder = {
                Text(
                    text = stringResource(R.string.chat_input_hint),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.background,
                unfocusedContainerColor = MaterialTheme.colorScheme.background,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            shape = RoundedCornerShape(24.dp),
            singleLine = false,
            maxLines = 4,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
            keyboardActions = KeyboardActions(onSend = { onSend() })
        )

        // Send button
        IconButton(
            onClick = onSend,
            enabled = inputText.isNotBlank() && isEnabled,
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(
                    if (inputText.isNotBlank() && isEnabled) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    }
                )
        ) {
            Icon(
                imageVector = Icons.Default.Send,
                contentDescription = stringResource(R.string.cd_send_message),
                tint = Color.White
            )
        }
    }
}

/*
================================================================================
HELPER FUNCTIONS
================================================================================
*/

private fun formatTimestamp(timestamp: Long): String {
    val formatter = SimpleDateFormat("h:mm a", Locale.getDefault())
    return formatter.format(Date(timestamp))
}
