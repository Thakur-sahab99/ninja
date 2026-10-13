package com.example.feature.chat

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.database.entity.ConversationEntity
import com.example.core.database.entity.MessageEntity
import com.example.feature.NinjaMainViewModel
import com.example.feature.common.AudioWaveformVisualizer
import com.example.feature.common.DeliveryStatusTicks
import com.example.feature.common.SecurityShieldBadge
import com.example.ui.theme.BubbleReceived
import com.example.ui.theme.BubbleSent
import com.example.ui.theme.BubbleTextPrimary
import com.example.ui.theme.BubbleTextSecondary
import com.example.ui.theme.NinjaAccentCyan
import com.example.ui.theme.NinjaEmeraldTick
import com.example.ui.theme.NinjaPrimary
import com.example.ui.theme.NinjaShadowGold
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatRoomScreen(
    viewModel: NinjaMainViewModel,
    modifier: Modifier = Modifier
) {
    val conversation by viewModel.currentConversation.collectAsState()
    val messages by viewModel.currentMessages.collectAsState()
    val replyingTo by viewModel.replyingToMessage.collectAsState()
    val editingMessage by viewModel.editingMessage.collectAsState()
    val isRecordingVoice by viewModel.isRecordingVoiceNote.collectAsState()
    val recordingDuration by viewModel.voiceRecordingDuration.collectAsState()
    val viewOnceMessage by viewModel.viewOnceMessage.collectAsState()
    val settings by viewModel.settings.collectAsState()

    var messageText by remember { mutableStateOf("") }
    var showAttachmentSheet by remember { mutableStateOf(false) }
    var showTimerDialog by remember { mutableStateOf(false) }
    var selectedMessageForMenu by remember { mutableStateOf<MessageEntity?>(null) }
    var showMenuDropdown by remember { mutableStateOf(false) }

    val listState = rememberLazyListState()

    // When editing a message, populate the textfield
    LaunchedEffect(editingMessage) {
        if (editingMessage != null) {
            messageText = editingMessage!!.content
        }
    }

    // Scroll to bottom when new messages arrive
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.openSafetyNumberScreen() }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = conversation?.avatarEmoji ?: "🥷", fontSize = 18.sp)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = conversation?.title ?: "Chat",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (settings.isShadowModeActive) {
                                    Text(
                                        text = "Shadow Mode active",
                                        fontSize = 11.sp,
                                        color = NinjaShadowGold,
                                        fontWeight = FontWeight.Medium
                                    )
                                } else {
                                    Text(
                                        text = "Online • E2EE",
                                        fontSize = 11.sp,
                                        color = NinjaEmeraldTick
                                    )
                                }
                            }
                        }
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.closeConversation() },
                        modifier = Modifier.testTag("chat_back_btn")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // Video Call action
                    IconButton(
                        onClick = {
                            conversation?.let {
                                viewModel.startCall(it.title, it.participantPhone, isVideo = true)
                            }
                        },
                        modifier = Modifier.testTag("start_video_call_btn")
                    ) {
                        Icon(Icons.Default.Videocam, contentDescription = "Video Call", tint = NinjaAccentCyan)
                    }
                    // Audio Call action
                    IconButton(
                        onClick = {
                            conversation?.let {
                                viewModel.startCall(it.title, it.participantPhone, isVideo = false)
                            }
                        },
                        modifier = Modifier.testTag("start_audio_call_btn")
                    ) {
                        Icon(Icons.Default.Call, contentDescription = "Voice Call", tint = NinjaAccentCyan)
                    }
                    // Disappearing messages timer toggle
                    IconButton(
                        onClick = { showTimerDialog = true },
                        modifier = Modifier.testTag("disappearing_timer_btn")
                    ) {
                        Icon(
                            imageVector = if ((conversation?.disappearingTimerSeconds ?: 0L) > 0)
                                Icons.Default.HourglassTop else Icons.Default.HourglassBottom,
                            contentDescription = "Disappearing Messages",
                            tint = if ((conversation?.disappearingTimerSeconds ?: 0L) > 0) NinjaAccentCyan else Color.Gray
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .navigationBarsPadding()
            ) {
                // Reply quote banner
                AnimatedVisibility(
                    visible = replyingTo != null,
                    enter = slideInVertically { it } + fadeIn(),
                    exit = slideOutVertically { it } + fadeOut()
                ) {
                    replyingTo?.let { reply ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(3.dp)
                                    .height(28.dp)
                                    .background(NinjaPrimary)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Replying to ${reply.senderName}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = NinjaPrimary
                                )
                                Text(
                                    text = reply.content,
                                    fontSize = 12.sp,
                                    maxLines = 1,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(onClick = { viewModel.setReplyMessage(null) }) {
                                Icon(Icons.Default.Close, contentDescription = "Cancel reply", modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }

                // Edit banner
                AnimatedVisibility(
                    visible = editingMessage != null,
                    enter = slideInVertically { it } + fadeIn(),
                    exit = slideOutVertically { it } + fadeOut()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(NinjaAccentCyan.copy(alpha = 0.15f))
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Editing", tint = NinjaAccentCyan, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Editing message (15-min limit)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = NinjaAccentCyan,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = {
                            viewModel.setEditingMessage(null)
                            messageText = ""
                        }) {
                            Icon(Icons.Default.Close, contentDescription = "Cancel edit", modifier = Modifier.size(16.dp))
                        }
                    }
                }

                // Voice Recording Live Banner
                AnimatedVisibility(visible = isRecordingVoice) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(NinjaPrimary.copy(alpha = 0.2f))
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(Color.Red)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Recording E2EE Audio: ${recordingDuration}s",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Row {
                            IconButton(onClick = { viewModel.stopVoiceRecording(send = false) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Discard Audio", tint = Color.Red)
                            }
                            IconButton(onClick = { viewModel.stopVoiceRecording(send = true) }) {
                                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send Audio", tint = NinjaPrimary)
                            }
                        }
                    }
                }

                // Input bar
                if (!isRecordingVoice) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { showAttachmentSheet = true },
                            modifier = Modifier.testTag("attachment_btn")
                        ) {
                            Icon(Icons.Default.AttachFile, contentDescription = "Attach media", tint = NinjaAccentCyan)
                        }

                        OutlinedTextField(
                            value = messageText,
                            onValueChange = { messageText = it },
                            placeholder = { Text("Encrypted message...", fontSize = 14.sp) },
                            shape = RoundedCornerShape(24.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("message_input_field"),
                            maxLines = 4
                        )

                        Spacer(modifier = Modifier.width(6.dp))

                        if (messageText.isBlank()) {
                            // Voice recorder button
                            IconButton(
                                onClick = { viewModel.startVoiceRecording() },
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .testTag("record_audio_btn")
                            ) {
                                Icon(Icons.Default.Mic, contentDescription = "Record Voice Note", tint = NinjaPrimary)
                            }
                        } else {
                            // Send button
                            IconButton(
                                onClick = {
                                    val textToSend = messageText
                                    messageText = ""
                                    viewModel.sendMessage(textToSend)
                                },
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(NinjaPrimary)
                                    .testTag("send_message_btn")
                            ) {
                                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = Color.White)
                            }
                        }
                    }
                }
            }
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Sealed Sender security strip
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0F1523))
                    .padding(vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                SecurityShieldBadge(
                    isSealedSender = true,
                    isVerified = conversation?.safetyNumberVerified == true
                )
            }

            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp)
                    .testTag("chat_messages_stream")
            ) {
                items(messages, key = { it.id }) { message ->
                    MessageBubble(
                        message = message,
                        onLongClick = {
                            selectedMessageForMenu = message
                            showMenuDropdown = true
                        },
                        onReaction = { emoji ->
                            viewModel.addReaction(message.id, emoji)
                        },
                        onOpenViewOnce = {
                            viewModel.openViewOnce(message)
                        }
                    )
                }
            }
        }
    }

    // Message action dialog / options
    if (showMenuDropdown && selectedMessageForMenu != null) {
        val selected = selectedMessageForMenu!!
        AlertDialog(
            onDismissRequest = {
                showMenuDropdown = false
                selectedMessageForMenu = null
            },
            title = { Text("Message Actions") },
            text = {
                Column {
                    // Emoji reactions row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        listOf("❤️", "👍", "🔥", "😂", "🥷", "👏").forEach { emoji ->
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable {
                                        viewModel.addReaction(selected.id, emoji)
                                        showMenuDropdown = false
                                        selectedMessageForMenu = null
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(emoji, fontSize = 18.sp)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    TextButton(
                        onClick = {
                            viewModel.setReplyMessage(selected)
                            showMenuDropdown = false
                            selectedMessageForMenu = null
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.AutoMirrored.Filled.Reply, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Reply to message")
                        }
                    }
                    TextButton(
                        onClick = {
                            viewModel.toggleStar(selected)
                            showMenuDropdown = false
                            selectedMessageForMenu = null
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(if (selected.isStarred) Icons.Default.StarBorder else Icons.Default.Star, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (selected.isStarred) "Unstar" else "Star message")
                        }
                    }
                    if (selected.isOutgoing) {
                        TextButton(
                            onClick = {
                                viewModel.setEditingMessage(selected)
                                showMenuDropdown = false
                                selectedMessageForMenu = null
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Edit, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Edit message (within 15m)")
                            }
                        }
                        TextButton(
                            onClick = {
                                viewModel.deleteForEveryone(selected.id)
                                showMenuDropdown = false
                                selectedMessageForMenu = null
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.DeleteForever, contentDescription = null, tint = Color.Red)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Delete for everyone", color = Color.Red)
                            }
                        }
                    }
                    TextButton(
                        onClick = {
                            viewModel.deleteForMe(selected.id)
                            showMenuDropdown = false
                            selectedMessageForMenu = null
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Delete, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Delete for me")
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    showMenuDropdown = false
                    selectedMessageForMenu = null
                }) {
                    Text("Close")
                }
            }
        )
    }

    // Disappearing Messages Timer Dialog
    if (showTimerDialog) {
        val timerOptions = listOf(
            0L to "Off",
            5L to "5 seconds (Test Vanish)",
            60L to "1 minute",
            3600L to "1 hour",
            86400L to "1 day",
            604800L to "1 week"
        )
        AlertDialog(
            onDismissRequest = { showTimerDialog = false },
            title = { Text("Disappearing Messages") },
            text = {
                Column {
                    Text(
                        text = "For added privacy, new messages sent in this chat will vanish after the timer expires. Recipient safety numbers are preserved.",
                        fontSize = 13.sp,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    timerOptions.forEach { (seconds, label) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.updateDisappearingTimer(seconds)
                                    showTimerDialog = false
                                }
                                .padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val isSelected = (conversation?.disappearingTimerSeconds ?: 0L) == seconds
                            Box(
                                modifier = Modifier
                                    .size(18.dp)
                                    .clip(CircleShape)
                                    .border(2.dp, if (isSelected) NinjaPrimary else Color.Gray, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSelected) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .clip(CircleShape)
                                            .background(NinjaPrimary)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(label, fontSize = 14.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showTimerDialog = false }) {
                    Text("Done")
                }
            }
        )
    }

    // Attachment Modal Sheet
    if (showAttachmentSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAttachmentSheet = false }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = "Share Encrypted Media (EXIF Scrubbed)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    AttachmentOption(
                        icon = Icons.Default.Image,
                        label = "Gallery Photo",
                        color = NinjaPrimary,
                        onClick = {
                            viewModel.sendMessage(
                                content = "Encrypted image attachment (EXIF removed)",
                                mediaType = "IMAGE"
                            )
                            showAttachmentSheet = false
                        }
                    )
                    AttachmentOption(
                        icon = Icons.Default.Visibility,
                        label = "View Once",
                        color = NinjaAccentCyan,
                        onClick = {
                            viewModel.sendMessage(
                                content = "Confidential View-Once document",
                                mediaType = "IMAGE",
                                isViewOnce = true
                            )
                            showAttachmentSheet = false
                        }
                    )
                    AttachmentOption(
                        icon = Icons.Default.Description,
                        label = "Document",
                        color = NinjaEmeraldTick,
                        onClick = {
                            viewModel.sendMessage(
                                content = "whitepaper_v2.pdf (1.4 MB)",
                                mediaType = "DOCUMENT"
                            )
                            showAttachmentSheet = false
                        }
                    )
                    AttachmentOption(
                        icon = Icons.Default.LocationOn,
                        label = "Location",
                        color = Color(0xFFF59E0B),
                        onClick = {
                            viewModel.sendMessage(
                                content = "Live location: 28.6139° N, 77.2090° E",
                                mediaType = "LOCATION"
                            )
                            showAttachmentSheet = false
                        }
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // View Once display modal
    if (viewOnceMessage != null) {
        val msg = viewOnceMessage!!
        AlertDialog(
            onDismissRequest = { viewModel.closeViewOnce() },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Visibility, contentDescription = null, tint = NinjaAccentCyan)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("View-Once Media")
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(160.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("🔒", fontSize = 42.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "SCREENSHOT BLOCKED",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Red
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = msg.content,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "This media will vanish permanently once closed.",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { viewModel.closeViewOnce() }) {
                    Text("Vanish & Close")
                }
            }
        )
    }
}

@Composable
fun AttachmentOption(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    color: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.18f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = label, tint = color, modifier = Modifier.size(24.dp))
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(text = label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun MessageBubble(
    message: MessageEntity,
    onLongClick: () -> Unit,
    onReaction: (String) -> Unit,
    onOpenViewOnce: () -> Unit
) {
    val isOutgoing = message.isOutgoing
    val bubbleColor = if (isOutgoing) BubbleSent else BubbleReceived
    val timeFormat = remember { SimpleDateFormat("h:mm a", Locale.getDefault()) }
    val formattedTime = remember(message.timestamp) { timeFormat.format(Date(message.timestamp)) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        contentAlignment = if (isOutgoing) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Column(
            horizontalAlignment = if (isOutgoing) Alignment.End else Alignment.Start
        ) {
            Surface(
                shape = RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomStart = if (isOutgoing) 16.dp else 4.dp,
                    bottomEnd = if (isOutgoing) 4.dp else 16.dp
                ),
                color = bubbleColor,
                modifier = Modifier
                    .widthIn(min = 80.dp, max = 300.dp)
                    .clip(
                        RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomStart = if (isOutgoing) 16.dp else 4.dp,
                            bottomEnd = if (isOutgoing) 4.dp else 16.dp
                        )
                    )
                    .combinedClickable(
                        onClick = {
                            if (message.isViewOnce && !message.isViewed) {
                                onOpenViewOnce()
                            }
                        },
                        onLongClick = onLongClick
                    )
                    .testTag("message_bubble_${message.id}")
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    // Quoted preview if present
                    if (!message.replyToContent.isNullOrBlank()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color.Black.copy(alpha = 0.2f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = message.replyToContent,
                                fontSize = 11.sp,
                                fontStyle = FontStyle.Italic,
                                color = BubbleTextSecondary,
                                maxLines = 1
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                    }

                    // Content rendering based on type
                    when {
                        message.isDeletedForEveryone -> {
                            Text(
                                text = "🚫 This message was deleted",
                                fontSize = 13.sp,
                                fontStyle = FontStyle.Italic,
                                color = BubbleTextSecondary
                            )
                        }
                        message.isViewOnce -> {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(vertical = 2.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Visibility,
                                    contentDescription = "View Once",
                                    tint = NinjaAccentCyan,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (message.isViewed) "Opened photo" else "1 View-Once Photo",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (message.isViewed) Color.Gray else NinjaAccentCyan
                                )
                            }
                        }
                        message.mediaType == "VOICE_NOTE" -> {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Play voice note",
                                    tint = NinjaPrimary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                AudioWaveformVisualizer(
                                    durationSec = message.mediaDurationSeconds,
                                    isPlaying = false
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "${message.mediaDurationSeconds}s",
                                    fontSize = 11.sp,
                                    color = BubbleTextSecondary
                                )
                            }
                        }
                        message.mediaType == "DOCUMENT" -> {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Description,
                                    contentDescription = "Document",
                                    tint = NinjaEmeraldTick,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = message.content,
                                    fontSize = 13.sp,
                                    color = BubbleTextPrimary
                                )
                            }
                        }
                        else -> {
                            Text(
                                text = message.content,
                                fontSize = 14.sp,
                                color = BubbleTextPrimary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Meta: time + edit badge + star + delivery ticks
                    Row(
                        modifier = Modifier.align(Alignment.End),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (message.isDisappearing) {
                            Icon(
                                imageVector = Icons.Default.HourglassTop,
                                contentDescription = "Disappearing",
                                tint = Color.Gray,
                                modifier = Modifier
                                    .size(11.dp)
                                    .padding(end = 3.dp)
                            )
                        }
                        if (message.isStarred) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Starred",
                                tint = Color(0xFFF59E0B),
                                modifier = Modifier
                                    .size(11.dp)
                                    .padding(end = 3.dp)
                            )
                        }
                        if (message.isEdited) {
                            Text(
                                text = "edited ",
                                fontSize = 10.sp,
                                fontStyle = FontStyle.Italic,
                                color = BubbleTextSecondary
                            )
                        }
                        Text(
                            text = formattedTime,
                            fontSize = 10.sp,
                            color = BubbleTextSecondary
                        )
                        if (isOutgoing) {
                            Spacer(modifier = Modifier.width(4.dp))
                            DeliveryStatusTicks(status = message.status)
                        }
                    }
                }
            }

            // Reactions pill if any
            if (message.reactionsJson.isNotBlank()) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.padding(top = 2.dp)
                ) {
                    Text(
                        text = message.reactionsJson,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}
