package com.example.feature.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.crypto.DoubleRatchetEngine
import com.example.feature.NinjaMainViewModel
import com.example.ui.theme.NinjaAccentCyan
import com.example.ui.theme.NinjaEmeraldTick
import com.example.ui.theme.NinjaPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SafetyNumberScreen(
    viewModel: NinjaMainViewModel,
    modifier: Modifier = Modifier
) {
    val conversation by viewModel.currentConversation.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()

    val safetyNumberData = remember(conversation, userProfile) {
        val remoteKey = conversation?.remoteIdentityKey ?: "remote_key_default_ninja_test"
        val localKey = userProfile.identityPublicKey.ifBlank { "local_key_default_ninja_test" }
        DoubleRatchetEngine.generateSafetyNumber(localKey, remoteKey)
    }

    val isVerified = conversation?.safetyNumberVerified == true

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Verify Safety Number", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.navigateBack() },
                        modifier = Modifier.testTag("safety_back_btn")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            // Simulated Signal QR Code container
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .size(200.dp)
                    .padding(8.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.QrCode,
                            contentDescription = "QR Code",
                            tint = Color.Black,
                            modifier = Modifier.size(130.dp)
                        )
                        Text(
                            text = "SCAN TO VERIFY",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "60-Digit Cryptographic Fingerprint",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = NinjaAccentCyan
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 12 blocks of 5 digits
            val numberBlocks = safetyNumberData.combinedNumericCode.split(" ")
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    for (row in 0 until 4) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            for (col in 0 until 3) {
                                val idx = row * 3 + col
                                if (idx < numberBlocks.size) {
                                    Text(
                                        text = numberBlocks[idx],
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace,
                                        letterSpacing = 1.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Verification toggle card
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (isVerified) NinjaEmeraldTick.copy(alpha = 0.15f)
                    else MaterialTheme.colorScheme.surfaceVariant
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isVerified) Icons.Default.CheckCircle else Icons.Default.Security,
                            contentDescription = null,
                            tint = if (isVerified) NinjaEmeraldTick else Color.Gray,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Mark as verified",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (isVerified) "Identity cryptographically trusted" else "Compare numbers with contact",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                    }
                    Switch(
                        checked = isVerified,
                        onCheckedChange = { viewModel.verifySafetyNumber(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = NinjaEmeraldTick
                        ),
                        modifier = Modifier.testTag("verify_safety_switch")
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "If you wish to verify the security of your end-to-end encryption with ${conversation?.title ?: "this contact"}, compare the numbers above with the numbers on their device, or scan their QR code.",
                fontSize = 12.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
