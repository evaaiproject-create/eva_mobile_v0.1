package com.eva.app.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eva.app.R
import com.eva.app.data.models.ConversationInfo
import com.eva.app.data.repository.AuthRepository
import com.eva.app.data.repository.ChatRepository
import com.eva.app.utils.Constants
import kotlinx.coroutines.launch

/*
================================================================================
NAVIGATION DRAWER CONTENT
================================================================================

PURPOSE:
    The hamburger menu content that slides in from the left.

STRUCTURE:
    ┌─────────────────────────┐
    │  Eva Logo & User Info   │
    ├─────────────────────────┤
    │  New Chat ▼             │
    │    └─ Previous Chat 1   │
    │    └─ Previous Chat 2   │
    ├─────────────────────────┤
    │  Template 1             │
    │  Template 2             │
    │  Template 3             │
    ├─────────────────────────┤
    │  Settings               │
    │  Logout                 │
    ├─────────────────────────┤
    │  v0.1                   │
    └─────────────────────────┘

================================================================================
*/

@Composable
fun NavigationDrawerContent(
    authRepository: AuthRepository,
    chatRepository: ChatRepository,
    onNavigate: (String) -> Unit,
    onNewChat: () -> Unit,
    onLogout: () -> Unit,
    onClose: () -> Unit
) {
    /*
    ================================================================================
    STATE
    ================================================================================
    */

    val scope = rememberCoroutineScope()

    // Previous chats list
    var conversations by remember { mutableStateOf<List<ConversationInfo>>(emptyList()) }

    // Expand/collapse state for previous chats
    var isChatsExpanded by remember { mutableStateOf(false) }

    // User info
    val sessionManager = authRepository.getSessionManager()
    val userName = sessionManager.getUserName() ?: "User"
    val userEmail = sessionManager.getUserEmail() ?: ""

    /*
    ================================================================================
    EFFECTS
    ================================================================================
    */

    // Load conversations when drawer opens
    LaunchedEffect(Unit) {
        scope.launch {
            val result = chatRepository.getConversations()
            result.onSuccess { conversations = it }
        }
    }

    /*
    ================================================================================
    UI
    ================================================================================
    */

    ModalDrawerSheet(
        modifier = Modifier.width(300.dp),
        drawerContainerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(16.dp)
        ) {
            // Header with user info
            DrawerHeader(userName = userName, userEmail = userEmail)

            Spacer(modifier = Modifier.height(24.dp))

            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

            Spacer(modifier = Modifier.height(16.dp))

            // Scrollable content
            LazyColumn(
                modifier = Modifier.weight(1f)
            ) {
                // New Chat with expandable previous chats
                item {
                    DrawerItemWithExpand(
                        icon = Icons.Default.Add,
                        label = stringResource(R.string.nav_new_chat),
                        isExpanded = isChatsExpanded,
                        onToggleExpand = { isChatsExpanded = !isChatsExpanded },
                        onClick = onNewChat
                    )
                }

                // Previous chats (when expanded)
                if (isChatsExpanded) {
                    items(conversations) { conversation ->
                        PreviousChatItem(
                            title = conversation.title,
                            onClick = {
                                // Navigate to chat and load this conversation
                                onNavigate(Constants.ROUTE_CHAT)
                                // TODO: Pass conversation ID to load
                            }
                        )
                    }

                    if (conversations.isEmpty()) {
                        item {
                            Text(
                                text = "No previous chats",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(start = 48.dp, top = 8.dp, bottom = 8.dp)
                            )
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(16.dp)) }

                // Template items
                item {
                    DrawerItem(
                        icon = Icons.Outlined.Folder,
                        label = stringResource(R.string.nav_template_1),
                        onClick = { onNavigate(Constants.ROUTE_TEMPLATE_1) }
                    )
                }

                item {
                    DrawerItem(
                        icon = Icons.Outlined.Folder,
                        label = stringResource(R.string.nav_template_2),
                        onClick = { onNavigate(Constants.ROUTE_TEMPLATE_2) }
                    )
                }

                item {
                    DrawerItem(
                        icon = Icons.Outlined.Folder,
                        label = stringResource(R.string.nav_template_3),
                        onClick = { onNavigate(Constants.ROUTE_TEMPLATE_3) }
                    )
                }
            }

            // Bottom section
            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

            Spacer(modifier = Modifier.height(16.dp))

            // Settings
            DrawerItem(
                icon = Icons.Default.Settings,
                label = stringResource(R.string.nav_settings),
                onClick = { onNavigate(Constants.ROUTE_SETTINGS) }
            )

            // Logout
            DrawerItem(
                icon = Icons.Default.Logout,
                label = stringResource(R.string.nav_logout),
                onClick = onLogout,
                tint = MaterialTheme.colorScheme.error
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Version
            Text(
                text = stringResource(R.string.app_version),
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}

/*
================================================================================
DRAWER HEADER
================================================================================
*/

@Composable
private fun DrawerHeader(
    userName: String,
    userEmail: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        // Avatar
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = userName.firstOrNull()?.uppercase() ?: "U",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column {
            Text(
                text = userName,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = userEmail,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/*
================================================================================
DRAWER ITEM
================================================================================
*/

@Composable
private fun DrawerItem(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    tint: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = label,
            fontSize = 15.sp,
            color = tint
        )
    }
}

/*
================================================================================
DRAWER ITEM WITH EXPAND
================================================================================
*/

@Composable
private fun DrawerItemWithExpand(
    icon: ImageVector,
    label: String,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Main clickable area (starts new chat)
        Row(
            modifier = Modifier
                .weight(1f)
                .clickable(onClick = onClick),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = label,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        // Expand/collapse button
        Icon(
            imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .size(24.dp)
                .clickable(onClick = onToggleExpand)
        )
    }
}

/*
================================================================================
PREVIOUS CHAT ITEM
================================================================================
*/

@Composable
private fun PreviousChatItem(
    title: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(start = 48.dp, top = 8.dp, bottom = 8.dp, end = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Chat,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = title,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1
        )
    }
}