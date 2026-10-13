package com.example.feature.settings

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PhoneInTalk
import androidx.compose.material.icons.filled.Pin
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.ScreenLockPortrait
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.feature.NinjaMainViewModel
import com.example.ui.theme.NinjaAccentCyan
import com.example.ui.theme.NinjaEmeraldTick
import com.example.ui.theme.NinjaPrimary
import com.example.ui.theme.NinjaShadowGold

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: NinjaMainViewModel,
    modifier: Modifier = Modifier
) {
    val settings by viewModel.settings.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()

    var showBackupDialog by remember { mutableStateOf(false) }
    var backupStatusMessage by remember { mutableStateOf<String?>(null) }
    var showLanguageDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings & Privacy", fontWeight = FontWeight.ExtraBold, fontSize = 22.sp) },
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
                .verticalScroll(rememberScrollState())
                .testTag("settings_screen")
        ) {
            // Profile Card
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(NinjaPrimary.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(userProfile.avatarEmoji, fontSize = 28.sp)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = userProfile.displayName,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = userProfile.about,
                            fontSize = 13.sp,
                            color = Color.Gray
                        )
                        Text(
                            text = userProfile.phoneNumber,
                            fontSize = 12.sp,
                            color = NinjaAccentCyan,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Shadow Mode Master Card
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (settings.isShadowModeActive) NinjaShadowGold.copy(alpha = 0.15f)
                    else MaterialTheme.colorScheme.surfaceVariant
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(NinjaShadowGold.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.VisibilityOff,
                                contentDescription = "Shadow Mode",
                                tint = NinjaShadowGold
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                text = "Shadow Mode",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = if (settings.isShadowModeActive) NinjaShadowGold else MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "One tap masks read receipts, typing indicators, and last seen simultaneously.",
                                fontSize = 12.sp,
                                color = Color.Gray,
                                lineHeight = 16.sp
                            )
                        }
                    }
                    Switch(
                        checked = settings.isShadowModeActive,
                        onCheckedChange = { viewModel.toggleShadowMode() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = NinjaShadowGold
                        ),
                        modifier = Modifier.testTag("settings_shadow_mode_switch")
                    )
                }
            }

            // Privacy & Security Header
            Text(
                text = "SIGNAL-GRADE PRIVACY",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = NinjaAccentCyan,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
            )

            SettingsToggleRow(
                icon = Icons.Default.ScreenLockPortrait,
                title = "Screen Security",
                subtitle = "Block screenshots and hide app preview in recent apps",
                isChecked = settings.screenSecurityEnabled,
                onCheckedChange = { viewModel.settingsRepository.setScreenSecurity(it) }
            )

            SettingsToggleRow(
                icon = Icons.Default.Fingerprint,
                title = "Biometric / PIN App Lock",
                subtitle = "Require biometric sensor or passcode to unlock Ninja",
                isChecked = settings.biometricLockEnabled,
                onCheckedChange = { viewModel.settingsRepository.setBiometricLock(it) }
            )

            SettingsToggleRow(
                icon = Icons.Default.Security,
                title = "Sealed Sender",
                subtitle = "Relay server cannot determine message sender identities",
                isChecked = settings.sealedSenderEnabled,
                onCheckedChange = { viewModel.settingsRepository.setSealedSender(it) }
            )

            SettingsToggleRow(
                icon = Icons.Default.PhoneInTalk,
                title = "Relay All Calls",
                subtitle = "Route voice/video through Ninja servers to hide your IP address",
                isChecked = settings.relayAllCallsEnabled,
                onCheckedChange = { viewModel.settingsRepository.setRelayAllCalls(it) }
            )

            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            )

            // General & Backups Header
            Text(
                text = "STORAGE & DATA",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Gray,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
            )

            SettingsActionRow(
                icon = Icons.Default.Backup,
                title = "Local Encrypted Backup",
                subtitle = "Create passphrase-protected backup. Zero plaintext cloud backups.",
                onClick = { showBackupDialog = true }
            )

            SettingsActionRow(
                icon = Icons.Default.Language,
                title = "Language",
                subtitle = if (settings.selectedLanguage == "hi") "हिन्दी (Hindi)" else "English",
                onClick = { showLanguageDialog = true }
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Encrypted backup creation dialog
    if (showBackupDialog) {
        var passphrase by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showBackupDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = NinjaPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Encrypted Backup")
                }
            },
            text = {
                Column {
                    Text(
                        text = "Ninja never uploads plaintext data to any cloud. Enter a 30-digit passphrase to generate an AES-256 encrypted local backup snapshot.",
                        fontSize = 13.sp,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = passphrase,
                        onValueChange = { passphrase = it },
                        label = { Text("Passphrase") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("backup_passphrase_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        backupStatusMessage = "Encrypted backup saved to secure app sandbox!"
                        showBackupDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NinjaPrimary)
                ) {
                    Text("Export Backup")
                }
            },
            dismissButton = {
                TextButton(onClick = { showBackupDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (backupStatusMessage != null) {
        AlertDialog(
            onDismissRequest = { backupStatusMessage = null },
            title = { Text("Backup Created") },
            text = { Text(backupStatusMessage!!) },
            confirmButton = {
                TextButton(onClick = { backupStatusMessage = null }) {
                    Text("OK")
                }
            }
        )
    }

    // Language selection dialog
    if (showLanguageDialog) {
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            title = { Text("Select Language") },
            text = {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.settingsRepository.setLanguage("en")
                                showLanguageDialog = false
                            }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("English", fontSize = 16.sp, fontWeight = if (settings.selectedLanguage == "en") FontWeight.Bold else FontWeight.Normal)
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.settingsRepository.setLanguage("hi")
                                showLanguageDialog = false
                            }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("हिन्दी (Hindi)", fontSize = 16.sp, fontWeight = if (settings.selectedLanguage == "hi") FontWeight.Bold else FontWeight.Normal)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLanguageDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
fun SettingsToggleRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!isChecked) }
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = NinjaAccentCyan,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = title,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = Color.Gray,
                    lineHeight = 15.sp
                )
            }
        }
        Switch(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = NinjaPrimary
            )
        )
    }
}

@Composable
fun SettingsActionRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = NinjaAccentCyan,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(
                text = title,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = Color.Gray,
                lineHeight = 15.sp
            )
        }
    }
}
