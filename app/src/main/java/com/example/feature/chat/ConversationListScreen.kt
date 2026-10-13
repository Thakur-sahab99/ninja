package com.example.feature.chat

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.database.entity.ConversationEntity
import com.example.feature.NinjaMainViewModel
import com.example.feature.common.DeliveryStatusTicks
import com.example.feature.common.ShadowModeBadge
import com.example.feature.common.ShurikenIcon
import com.example.ui.theme.NinjaAccentCyan
import com.example.ui.theme.NinjaCardDark
import com.example.ui.theme.NinjaEmeraldTick
import com.example.ui.theme.NinjaPrimary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversationListScreen(
    viewModel: NinjaMainViewModel,
    modifier: Modifier = Modifier
) {
    val conversations by viewModel.conversations.collectAsState()
    val archivedConversations by viewModel.archivedConversations.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    var isSearchActive by remember { mutableStateOf(false) }
    var showNewChatDialog by remember { mutableStateOf(false) }
    var showNewGroupDialog by remember { mutableStateOf(false) }

    val filteredConversations = conversations.filter {
        if (searchQuery.isBlank()) true
        else it.title.contains(searchQuery, ignoreCase = true) ||
                it.lastMessage.contains(searchQuery, ignoreCase = true)
    }

    val pinnedList = filteredConversations.filter { it.isPinned }
    val normalList = filteredConversations.filter { !it.isPinned }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (isSearchActive) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { viewModel.setSearchQuery(it) },
                            placeholder = { Text("Search conversations...", fontSize = 14.sp) },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("search_conversations_input"),
                            trailingIcon = {
                                IconButton(onClick = {
                                    viewModel.setSearchQuery("")
                                    isSearchActive = false
                                }) {
                                    Icon(Icons.Default.Close, contentDescription = "Close search")
                                }
                            }
                        )
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            ShurikenIcon(size = 28)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Ninja",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 22.sp,
                                letterSpacing = 0.5.sp,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }
                },
                actions = {
                    if (!isSearchActive) {
                        ShadowModeBadge(
                            isActive = settings.isShadowModeActive,
                            onToggle = { viewModel.toggleShadowMode() }
                        )
                        IconButton(
                            onClick = { isSearchActive = true },
                            modifier = Modifier.testTag("search_icon_btn")
                        ) {
                            Icon(Icons.Default.Search, contentDescription = "Search chats")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            Column(horizontalAlignment = Alignment.End) {
                // Secondary FAB for group
                FloatingActionButton(
                    onClick = { showNewGroupDialog = true },
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = NinjaAccentCyan,
                    modifier = Modifier
                        .size(44.dp)
                        .testTag("fab_new_group")
                ) {
                    Icon(Icons.Default.GroupAdd, contentDescription = "New Group", modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.height(10.dp))
                // Primary FAB for new chat
                FloatingActionButton(
                    onClick = { showNewChatDialog = true },
                    containerColor = NinjaPrimary,
                    contentColor = Color.White,
                    modifier = Modifier.testTag("fab_new_chat")
                ) {
                    Icon(Icons.Default.Chat, contentDescription = "New Chat")
                }
            }
        },
        modifier = modifier
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("conversations_list")
        ) {
            // Archived tile if any exist
            if (archivedConversations.isNotEmpty()) {
                item {
                    ArchivedChatsTile(
                        count = archivedConversations.size,
                        onClick = { viewModel.openArchivedChats() }
                    )
                }
            }

            // Pinned header if pinned items exist
            if (pinnedList.isNotEmpty()) {
                item {
                    Text(
                        text = "PINNED",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = NinjaAccentCyan,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
                items(pinnedList, key = { it.id }) { conv ->
                    ConversationItemRow(
                        conversation = conv,
                        onClick = { viewModel.openConversation(conv.id) },
                        onTogglePin = { viewModel.togglePin(conv) },
                        onToggleArchive = { viewModel.toggleArchive(conv) }
                    )
                }
            }

            // All conversations header if pinned exist
            if (pinnedList.isNotEmpty() && normalList.isNotEmpty()) {
                item {
                    Text(
                        text = "ALL CHATS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }

            items(normalList, key = { it.id }) { conv ->
                ConversationItemRow(
                    conversation = conv,
                    onClick = { viewModel.openConversation(conv.id) },
                    onTogglePin = { viewModel.togglePin(conv) },
                    onToggleArchive = { viewModel.toggleArchive(conv) }
                )
            }

            if (filteredConversations.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            ShurikenIcon(size = 48)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "No messages in the shadows",
                                fontWeight = FontWeight.Bold,
                                color = Color.Gray
                            )
                            Text(
                                text = "Tap the + button to start an end-to-end encrypted chat",
                                fontSize = 13.sp,
                                color = Color.Gray.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }
        }
    }

    if (showNewChatDialog) {
        NewChatDialog(
            isGroup = false,
            onDismiss = { showNewChatDialog = false },
            onCreate = { name, phone, emoji ->
                viewModel.createNewChat(name, phone, isGroup = false, emoji = emoji)
                showNewChatDialog = false
            }
        )
    }

    if (showNewGroupDialog) {
        NewChatDialog(
            isGroup = true,
            onDismiss = { showNewGroupDialog = false },
            onCreate = { name, phone, emoji ->
                viewModel.createNewChat(name, phone, isGroup = true, emoji = emoji)
                showNewGroupDialog = false
            }
        )
    }
}

@Composable
fun ConversationItemRow(
    conversation: ConversationEntity,
    onClick: () -> Unit,
    onTogglePin: () -> Unit,
    onToggleArchive: () -> Unit
) {
    val timeFormat = remember { SimpleDateFormat("h:mm a", Locale.getDefault()) }
    val formattedTime = remember(conversation.lastMessageTimestamp) {
        timeFormat.format(Date(conversation.lastMessageTimestamp))
    }

    Surface(
        color = Color.Transparent,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("conversation_row_${conversation.id}")
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Text(text = conversation.avatarEmoji, fontSize = 24.sp)
                if (conversation.safetyNumberVerified) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(NinjaEmeraldTick),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.VerifiedUser,
                            contentDescription = "Verified",
                            tint = Color.Black,
                            modifier = Modifier.size(10.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Body
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = conversation.title,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        if (conversation.disappearingTimerSeconds > 0) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.HourglassTop,
                                contentDescription = "Disappearing",
                                tint = NinjaAccentCyan,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    }
                    Text(
                        text = formattedTime,
                        fontSize = 12.sp,
                        color = if (conversation.unreadCount > 0) NinjaPrimary else Color.Gray
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = conversation.lastMessage,
                        fontSize = 14.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = if (conversation.unreadCount > 0) MaterialTheme.colorScheme.onSurface else Color.Gray,
                        modifier = Modifier.weight(1f)
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (conversation.isPinned) {
                            Icon(
                                imageVector = Icons.Default.PushPin,
                                contentDescription = "Pinned",
                                tint = NinjaAccentCyan,
                                modifier = Modifier
                                    .size(14.dp)
                                    .padding(end = 4.dp)
                            )
                        }
                        if (conversation.unreadCount > 0) {
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(NinjaPrimary)
                                    .padding(horizontal = 7.dp, vertical = 2.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = conversation.unreadCount.toString(),
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ArchivedChatsTile(count: Int, onClick: () -> Unit) {
    Surface(
        color = Color.Transparent,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("archived_chats_tile")
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Archive,
                contentDescription = "Archived",
                tint = NinjaAccentCyan,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "Archived",
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = count.toString(),
                color = NinjaAccentCyan,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun NewChatDialog(
    isGroup: Boolean,
    onDismiss: () -> Unit,
    onCreate: (name: String, phone: String, emoji: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phoneOrMembers by remember { mutableStateOf("") }
    var selectedEmoji by remember { mutableStateOf(if (isGroup) "⚔️" else "🥷") }
    val emojiChoices = if (isGroup) listOf("⚔️", "🏯", "⛩️", "👥", "🔥") else listOf("🥷", "🛡️", "🦊", "🌑", "🗡️", "👁️")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isGroup) "Create Encrypted Group" else "Start E2EE Chat") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(if (isGroup) "Group Name" else "Contact Name") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("new_chat_name_input")
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = phoneOrMembers,
                    onValueChange = { phoneOrMembers = it },
                    label = { Text(if (isGroup) "Invite Members (Phones)" else "Phone Number") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("new_chat_phone_input")
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text("Select Emblem:", fontSize = 12.sp, color = Color.Gray)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    emojiChoices.forEach { emoji ->
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(if (selectedEmoji == emoji) NinjaPrimary.copy(alpha = 0.3f) else Color.Transparent)
                                .clickable { selectedEmoji = emoji },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(emoji, fontSize = 20.sp)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onCreate(name, phoneOrMembers.ifBlank { "+1 555-0100" }, selectedEmoji)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = NinjaPrimary),
                modifier = Modifier.testTag("confirm_create_chat_btn")
            ) {
                Text("Start Secure Chat")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
