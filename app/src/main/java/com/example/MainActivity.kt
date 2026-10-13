package com.example

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.DonutLarge
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.Chat
import androidx.compose.material.icons.outlined.DonutLarge
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.feature.ActiveScreen
import com.example.feature.MainTab
import com.example.feature.NinjaMainViewModel
import com.example.feature.auth.AuthScreen
import com.example.feature.calls.ActiveCallScreen
import com.example.feature.calls.CallsScreen
import com.example.feature.chat.ArchivedChatsScreen
import com.example.feature.chat.ChatRoomScreen
import com.example.feature.chat.ConversationListScreen
import com.example.feature.chat.SafetyNumberScreen
import com.example.feature.settings.SettingsScreen
import com.example.feature.status.StatusScreen
import com.example.ui.theme.NinjaAccentCyan
import com.example.ui.theme.NinjaPrimary
import com.example.ui.theme.NinjaTheme

class MainActivity : ComponentActivity() {
    private val viewModel: NinjaMainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val settings by viewModel.settings.collectAsState()

            // Dynamic FLAG_SECURE enforcement for screenshot protection
            LaunchedEffect(settings.screenSecurityEnabled) {
                try {
                    if (settings.screenSecurityEnabled) {
                        window.setFlags(
                            WindowManager.LayoutParams.FLAG_SECURE,
                            WindowManager.LayoutParams.FLAG_SECURE
                        )
                    } else {
                        window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
                    }
                } catch (e: Exception) {
                    // Safe guard for window flag changes
                }
            }

            NinjaTheme(darkTheme = true) {
                NinjaAppRoot(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun NinjaAppRoot(viewModel: NinjaMainViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val selectedTab by viewModel.selectedTab.collectAsState()
    val conversations by viewModel.conversations.collectAsState()
    val statuses by viewModel.statuses.collectAsState()

    val totalUnread = conversations.sumOf { it.unreadCount }
    val hasUnviewedStatus = statuses.any { !it.isViewed && !it.isSelf }

    when (currentScreen) {
        ActiveScreen.ONBOARDING -> {
            AuthScreen(
                viewModel = viewModel,
                onComplete = { viewModel.navigateBack() }
            )
        }
        ActiveScreen.CHAT_ROOM -> {
            BackHandler { viewModel.navigateBack() }
            ChatRoomScreen(viewModel = viewModel)
        }
        ActiveScreen.SAFETY_NUMBER -> {
            BackHandler { viewModel.navigateBack() }
            SafetyNumberScreen(viewModel = viewModel)
        }
        ActiveScreen.ACTIVE_CALL -> {
            BackHandler { viewModel.navigateBack() }
            ActiveCallScreen(viewModel = viewModel)
        }
        ActiveScreen.ARCHIVED_CHATS -> {
            BackHandler { viewModel.navigateBack() }
            ArchivedChatsScreen(viewModel = viewModel)
        }
        ActiveScreen.MAIN_TABS -> {
            Scaffold(
                bottomBar = {
                    NavigationBar(
                        containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surface,
                        tonalElevation = 8.dp
                    ) {
                        // Chats
                        NavigationBarItem(
                            selected = selectedTab == MainTab.CHATS,
                            onClick = { viewModel.selectTab(MainTab.CHATS) },
                            icon = {
                                BadgedBox(
                                    badge = {
                                        if (totalUnread > 0) {
                                            Badge(containerColor = NinjaPrimary) {
                                                Text(
                                                    text = totalUnread.toString(),
                                                    color = Color.White,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = if (selectedTab == MainTab.CHATS) Icons.Filled.Chat else Icons.Outlined.Chat,
                                        contentDescription = "Chats",
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            },
                            label = { Text("Chats", fontSize = 11.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = NinjaPrimary.copy(alpha = 0.2f),
                                selectedIconColor = NinjaPrimary,
                                selectedTextColor = NinjaPrimary
                            ),
                            modifier = Modifier.testTag("nav_tab_chats")
                        )

                        // Status / Stories
                        NavigationBarItem(
                            selected = selectedTab == MainTab.STATUS,
                            onClick = { viewModel.selectTab(MainTab.STATUS) },
                            icon = {
                                BadgedBox(
                                    badge = {
                                        if (hasUnviewedStatus) {
                                            Badge(containerColor = NinjaAccentCyan)
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = if (selectedTab == MainTab.STATUS) Icons.Filled.DonutLarge else Icons.Outlined.DonutLarge,
                                        contentDescription = "Status",
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            },
                            label = { Text("Status", fontSize = 11.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = NinjaAccentCyan.copy(alpha = 0.2f),
                                selectedIconColor = NinjaAccentCyan,
                                selectedTextColor = NinjaAccentCyan
                            ),
                            modifier = Modifier.testTag("nav_tab_status")
                        )

                        // Calls
                        NavigationBarItem(
                            selected = selectedTab == MainTab.CALLS,
                            onClick = { viewModel.selectTab(MainTab.CALLS) },
                            icon = {
                                Icon(
                                    imageVector = if (selectedTab == MainTab.CALLS) Icons.Filled.Call else Icons.Outlined.Call,
                                    contentDescription = "Calls",
                                    modifier = Modifier.size(22.dp)
                                )
                            },
                            label = { Text("Calls", fontSize = 11.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = NinjaAccentCyan.copy(alpha = 0.2f),
                                selectedIconColor = NinjaAccentCyan,
                                selectedTextColor = NinjaAccentCyan
                            ),
                            modifier = Modifier.testTag("nav_tab_calls")
                        )

                        // Settings & Privacy
                        NavigationBarItem(
                            selected = selectedTab == MainTab.SETTINGS,
                            onClick = { viewModel.selectTab(MainTab.SETTINGS) },
                            icon = {
                                Icon(
                                    imageVector = if (selectedTab == MainTab.SETTINGS) Icons.Filled.Settings else Icons.Outlined.Settings,
                                    contentDescription = "Settings",
                                    modifier = Modifier.size(22.dp)
                                )
                            },
                            label = { Text("Settings", fontSize = 11.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = NinjaPrimary.copy(alpha = 0.2f),
                                selectedIconColor = NinjaPrimary,
                                selectedTextColor = NinjaPrimary
                            ),
                            modifier = Modifier.testTag("nav_tab_settings")
                        )
                    }
                },
                modifier = Modifier.fillMaxSize()
            ) { paddingValues ->
                when (selectedTab) {
                    MainTab.CHATS -> ConversationListScreen(
                        viewModel = viewModel,
                        modifier = Modifier.padding(paddingValues)
                    )
                    MainTab.STATUS -> StatusScreen(
                        viewModel = viewModel,
                        modifier = Modifier.padding(paddingValues)
                    )
                    MainTab.CALLS -> CallsScreen(
                        viewModel = viewModel,
                        modifier = Modifier.padding(paddingValues)
                    )
                    MainTab.SETTINGS -> SettingsScreen(
                        viewModel = viewModel,
                        modifier = Modifier.padding(paddingValues)
                    )
                }
            }
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun NinjaShurikenPreview() {
    NinjaTheme(darkTheme = true) {
        androidx.compose.foundation.layout.Box(
            modifier = Modifier
                .size(100.dp)
                .background(com.example.ui.theme.NinjaBackgroundDark),
            contentAlignment = androidx.compose.ui.Alignment.Center
        ) {
            com.example.feature.common.ShurikenIcon(size = 48)
        }
    }
}
