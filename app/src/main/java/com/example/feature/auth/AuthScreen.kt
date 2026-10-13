package com.example.feature.auth

import androidx.compose.animation.AnimatedContent
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.feature.NinjaMainViewModel
import com.example.feature.common.ShurikenIcon
import com.example.ui.theme.NinjaAccentCyan
import com.example.ui.theme.NinjaEmeraldTick
import com.example.ui.theme.NinjaPrimary

@Composable
fun AuthScreen(
    viewModel: NinjaMainViewModel,
    onComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var step by remember { mutableIntStateOf(1) } // 1: Phone, 2: OTP, 3: Profile
    var phoneNumber by remember { mutableStateOf("+1 555-0100") }
    var otpCode by remember { mutableStateOf("739218") }
    var displayName by remember { mutableStateOf("Ghost") }
    var aboutText by remember { mutableStateOf("Living in the shadows 🥷") }
    var registrationPin by remember { mutableStateOf("4829") }
    var selectedEmoji by remember { mutableStateOf("🥷") }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(24.dp))
                ShurikenIcon(size = 54)
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Ninja",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 28.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Encrypted. Untraceable. Instant.",
                    fontSize = 13.sp,
                    color = NinjaAccentCyan,
                    fontWeight = FontWeight.Medium
                )
            }

            AnimatedContent(targetState = step, label = "auth_steps") { targetStep ->
                when (targetStep) {
                    1 -> {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Enter Phone Number",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Ninja will send a one-time verification code. Raw contacts are never stored.",
                                fontSize = 13.sp,
                                color = Color.Gray,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(20.dp))
                            OutlinedTextField(
                                value = phoneNumber,
                                onValueChange = { phoneNumber = it },
                                label = { Text("Phone Number") },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("auth_phone_input")
                            )
                        }
                    }
                    2 -> {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Verify 6-Digit OTP",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Enter the verification code sent to $phoneNumber",
                                fontSize = 13.sp,
                                color = Color.Gray,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(20.dp))
                            OutlinedTextField(
                                value = otpCode,
                                onValueChange = { otpCode = it },
                                label = { Text("6-Digit Code") },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("auth_otp_input")
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Resend code in 0:42s",
                                fontSize = 12.sp,
                                color = NinjaAccentCyan
                            )
                        }
                    }
                    3 -> {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Create Your Profile",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            // Avatar selector
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(NinjaPrimary.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(selectedEmoji, fontSize = 32.sp)
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceAround
                            ) {
                                listOf("🥷", "🛡️", "🦊", "🌑", "🗡️").forEach { emoji ->
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(if (selectedEmoji == emoji) NinjaPrimary.copy(alpha = 0.3f) else Color.Transparent)
                                            .clickable { selectedEmoji = emoji },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(emoji, fontSize = 18.sp)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            OutlinedTextField(
                                value = displayName,
                                onValueChange = { displayName = it },
                                label = { Text("Display Name") },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("auth_display_name_input")
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = aboutText,
                                onValueChange = { aboutText = it },
                                label = { Text("About Line") },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("auth_about_input")
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = registrationPin,
                                onValueChange = { registrationPin = it },
                                label = { Text("Registration Lock PIN (Optional)") },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("auth_pin_input")
                            )
                        }
                    }
                }
            }

            // Bottom action buttons
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(
                    onClick = {
                        if (step == 1) {
                            step = 2
                        } else if (step == 2) {
                            step = 3
                        } else {
                            viewModel.settingsRepository.completeOnboarding(
                                phone = phoneNumber,
                                displayName = displayName,
                                about = aboutText
                            )
                            viewModel.settingsRepository.updateProfile(
                                displayName = displayName,
                                about = aboutText,
                                avatarEmoji = selectedEmoji
                            )
                            onComplete()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("auth_continue_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = NinjaPrimary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = if (step == 3) "Enter Ninja" else "Continue",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }

                if (step > 1) {
                    TextButton(onClick = { step -= 1 }) {
                        Text("Back", color = Color.Gray)
                    }
                } else {
                    Spacer(modifier = Modifier.height(48.dp))
                }
            }
        }
    }
}
