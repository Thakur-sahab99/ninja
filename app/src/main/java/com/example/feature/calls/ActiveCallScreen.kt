package com.example.feature.calls

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VideocamOff
import androidx.compose.material.icons.filled.VolumeDown
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.feature.NinjaMainViewModel
import com.example.feature.common.ShurikenIcon
import com.example.ui.theme.NinjaAccentCyan
import com.example.ui.theme.NinjaEmeraldTick
import com.example.ui.theme.NinjaPrimary

@Composable
fun ActiveCallScreen(
    viewModel: NinjaMainViewModel,
    modifier: Modifier = Modifier
) {
    val activeCall by viewModel.activeCall.collectAsState()
    val call = activeCall ?: return

    val durationText = "${call.durationSeconds / 60}:${(call.durationSeconds % 60).toString().padStart(2, '0')}"

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF0F172A),
                        Color(0xFF0B0F19),
                        Color(0xFF020617)
                    )
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header: E2EE status & IP cloaking badge
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White.copy(alpha = 0.08f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = "Encrypted",
                            tint = NinjaEmeraldTick,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.size(6.dp))
                        Text(
                            text = if (call.isRelayed) "E2EE • RELAYED (IP HIDDEN)" else "E2EE PEER-TO-PEER",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = NinjaEmeraldTick,
                            letterSpacing = 0.5.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Avatar / Video stream placeholder
                Box(
                    modifier = Modifier
                        .size(130.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(NinjaPrimary.copy(alpha = 0.4f), NinjaAccentCyan.copy(alpha = 0.4f))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (call.isVideo && !call.isVideoPaused) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Videocam,
                                contentDescription = "Camera Active",
                                tint = NinjaAccentCyan,
                                modifier = Modifier.size(36.dp)
                            )
                            Text(
                                text = "WebRTC Video",
                                fontSize = 10.sp,
                                color = Color.White
                            )
                        }
                    } else {
                        Text(text = "🥷", fontSize = 56.sp)
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = call.contactName,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 24.sp,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = durationText,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = NinjaAccentCyan
                )
            }

            // Central Ninja brand micro-visual
            ShurikenIcon(size = 42)

            // Bottom controls bar
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Mute button
                    IconButton(
                        onClick = { viewModel.toggleMuteCall() },
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(if (call.isMuted) Color.White else Color.White.copy(alpha = 0.15f))
                            .testTag("call_mute_btn")
                    ) {
                        Icon(
                            imageVector = if (call.isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                            contentDescription = "Mute",
                            tint = if (call.isMuted) Color.Black else Color.White,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    // Video toggle
                    IconButton(
                        onClick = { viewModel.toggleVideoPause() },
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(if (call.isVideoPaused) Color.White else Color.White.copy(alpha = 0.15f))
                            .testTag("call_video_toggle_btn")
                    ) {
                        Icon(
                            imageVector = if (call.isVideoPaused) Icons.Default.VideocamOff else Icons.Default.Videocam,
                            contentDescription = "Toggle Video",
                            tint = if (call.isVideoPaused) Color.Black else Color.White,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    // Speakerphone
                    IconButton(
                        onClick = { viewModel.toggleSpeakerCall() },
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(if (call.isSpeakerOn) Color.White else Color.White.copy(alpha = 0.15f))
                            .testTag("call_speaker_btn")
                    ) {
                        Icon(
                            imageVector = if (call.isSpeakerOn) Icons.Default.VolumeUp else Icons.Default.VolumeDown,
                            contentDescription = "Speaker",
                            tint = if (call.isSpeakerOn) Color.Black else Color.White,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    // End call button (red)
                    IconButton(
                        onClick = { viewModel.endCall() },
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFEF4444))
                            .testTag("call_end_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.CallEnd,
                            contentDescription = "End Call",
                            tint = Color.White,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
