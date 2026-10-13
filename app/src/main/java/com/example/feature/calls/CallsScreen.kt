package com.example.feature.calls

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CallMade
import androidx.compose.material.icons.automirrored.filled.CallReceived
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.database.entity.CallEntity
import com.example.feature.NinjaMainViewModel
import com.example.ui.theme.NinjaAccentCyan
import com.example.ui.theme.NinjaEmeraldTick
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CallsScreen(
    viewModel: NinjaMainViewModel,
    modifier: Modifier = Modifier
) {
    val calls by viewModel.calls.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Calls", fontWeight = FontWeight.ExtraBold, fontSize = 22.sp) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        modifier = modifier
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("calls_list")
        ) {
            // Call Relay Privacy Banner
            item {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = "Encrypted Calls",
                            tint = NinjaEmeraldTick,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "End-to-End Encrypted Calling",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Relay Mode hides your IP address from call peers.",
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }

            items(calls, key = { it.id }) { call ->
                CallItemRow(
                    call = call,
                    onRedial = { isVideo ->
                        viewModel.startCall(call.contactName, call.contactPhone, isVideo)
                    }
                )
            }

            if (calls.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No recent calls", color = Color.Gray)
                    }
                }
            }
        }
    }
}

@Composable
fun CallItemRow(
    call: CallEntity,
    onRedial: (isVideo: Boolean) -> Unit
) {
    val timeFormat = remember { SimpleDateFormat("MMM d, h:mm a", Locale.getDefault()) }
    val formattedTime = remember(call.timestamp) { timeFormat.format(Date(call.timestamp)) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onRedial(call.isVideo) }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "🥷", fontSize = 20.sp)
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = call.contactName,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (call.isOutgoing) Icons.AutoMirrored.Filled.CallMade else Icons.AutoMirrored.Filled.CallReceived,
                    contentDescription = null,
                    tint = if (call.callStatus == "MISSED") Color.Red else NinjaEmeraldTick,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "$formattedTime • ${call.durationSeconds / 60}m ${call.durationSeconds % 60}s",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }

        IconButton(
            onClick = { onRedial(call.isVideo) },
            modifier = Modifier.testTag("redial_btn_${call.id}")
        ) {
            Icon(
                imageVector = if (call.isVideo) Icons.Default.Videocam else Icons.Default.Call,
                contentDescription = "Redial",
                tint = NinjaAccentCyan
            )
        }
    }
}
