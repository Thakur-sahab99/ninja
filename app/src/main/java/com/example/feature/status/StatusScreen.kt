package com.example.feature.status

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.database.entity.StatusEntity
import com.example.feature.NinjaMainViewModel
import com.example.ui.theme.NinjaAccentCyan
import com.example.ui.theme.NinjaEmeraldTick
import com.example.ui.theme.NinjaPrimary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatusScreen(
    viewModel: NinjaMainViewModel,
    modifier: Modifier = Modifier
) {
    val statuses by viewModel.statuses.collectAsState()
    var showAddStatusDialog by remember { mutableStateOf(false) }
    var activeViewingStatus by remember { mutableStateOf<StatusEntity?>(null) }

    val myStatus = statuses.firstOrNull { it.isSelf }
    val recentUpdates = statuses.filter { !it.isSelf }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Status", fontWeight = FontWeight.ExtraBold, fontSize = 22.sp) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddStatusDialog = true },
                containerColor = NinjaPrimary,
                contentColor = Color.White,
                modifier = Modifier.testTag("fab_add_status")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Status")
            }
        },
        modifier = modifier
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("status_list")
        ) {
            // My Status section
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            if (myStatus != null) activeViewingStatus = myStatus
                            else showAddStatusDialog = true
                        }
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .border(
                                2.dp,
                                if (myStatus != null) NinjaPrimary else Color.Transparent,
                                CircleShape
                            )
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "🥷", fontSize = 24.sp)
                        if (myStatus == null) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .size(18.dp)
                                    .clip(CircleShape)
                                    .background(NinjaPrimary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "My Status",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (myStatus != null) "Tap to view update • Disappears in 24h" else "Tap to add ephemeral status update",
                            fontSize = 13.sp,
                            color = Color.Gray
                        )
                    }
                }
            }

            // Privacy reminder banner
            item {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = NinjaAccentCyan, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Your status updates are end-to-end encrypted and visible only to your contacts.",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }
                }
            }

            // Recent updates header
            item {
                Text(
                    text = "RECENT UPDATES",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = NinjaAccentCyan,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)
                )
            }

            items(recentUpdates, key = { it.id }) { status ->
                StatusItemRow(
                    status = status,
                    onClick = { activeViewingStatus = status }
                )
            }
        }
    }

    // Add status modal dialog
    if (showAddStatusDialog) {
        var caption by remember { mutableStateOf("") }
        var selectedBg by remember { mutableStateOf("#8B5CF6") }
        val colorOptions = listOf("#8B5CF6", "#06B6D4", "#10B981", "#EC4899", "#F59E0B", "#1E293B")

        AlertDialog(
            onDismissRequest = { showAddStatusDialog = false },
            title = { Text("New Disappearing Status") },
            text = {
                Column {
                    OutlinedTextField(
                        value = caption,
                        onValueChange = { caption = it },
                        placeholder = { Text("What's on your mind? (Disappears in 24h)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .testTag("status_caption_input")
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Text("Select Background Tone:", fontSize = 12.sp, color = Color.Gray)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        colorOptions.forEach { hex ->
                            val color = Color(android.graphics.Color.parseColor(hex))
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .border(
                                        2.dp,
                                        if (selectedBg == hex) Color.White else Color.Transparent,
                                        CircleShape
                                    )
                                    .clickable { selectedBg = hex }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (caption.isNotBlank()) {
                            viewModel.addStatus(caption, selectedBg)
                            showAddStatusDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NinjaPrimary),
                    modifier = Modifier.testTag("confirm_status_btn")
                ) {
                    Text("Post Status")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddStatusDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Story viewer dialog
    if (activeViewingStatus != null) {
        val viewing = activeViewingStatus!!
        StoryViewerDialog(
            status = viewing,
            onDismiss = { activeViewingStatus = null }
        )
    }
}

@Composable
fun StatusItemRow(status: StatusEntity, onClick: () -> Unit) {
    val timeFormat = remember { SimpleDateFormat("h:mm a", Locale.getDefault()) }
    val formattedTime = remember(status.timestamp) { timeFormat.format(Date(status.timestamp)) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar with status ring
        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(CircleShape)
                .border(
                    2.dp,
                    if (status.isViewed) Color.Gray else NinjaAccentCyan,
                    CircleShape
                )
                .padding(3.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Text(text = status.avatarEmoji, fontSize = 22.sp)
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column {
            Text(
                text = status.authorName,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "$formattedTime • 24h Ephemeral",
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun StoryViewerDialog(status: StatusEntity, onDismiss: () -> Unit) {
    val progress = remember { Animatable(0f) }

    LaunchedEffect(status.id) {
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 5000, easing = LinearEasing)
        )
        onDismiss()
    }

    val parsedBg = remember(status.backgroundGradientHex) {
        try {
            Color(android.graphics.Color.parseColor(status.backgroundGradientHex))
        } catch (e: Exception) {
            NinjaPrimary
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.95f))
            .clickable { onDismiss() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header progress bar + author info
            Column {
                LinearProgressIndicator(
                    progress = { progress.value },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = Color.White,
                    trackColor = Color.White.copy(alpha = 0.3f),
                )
                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(status.avatarEmoji, fontSize = 24.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = status.authorName,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Text(
                                text = "Disappearing in 24 hours",
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 11.sp
                            )
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }
            }

            // Central Story Content card
            Card(
                colors = CardDefaults.cardColors(containerColor = parsedBg),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(320.dp)
                    .padding(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = status.textCaption,
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        lineHeight = 30.sp
                    )
                }
            }

            // Bottom prompt
            Text(
                text = "Tap anywhere to advance • E2EE Story",
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 11.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )
        }
    }
}
