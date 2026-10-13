package com.example.core.model

data class UserProfile(
    val phoneNumber: String,
    val displayName: String,
    val about: String,
    val avatarEmoji: String = "🥷",
    val registrationPin: String = "",
    val identityPublicKey: String = "",
    val isOnboarded: Boolean = false
)

data class SecuritySettings(
    val isShadowModeActive: Boolean = false, // Hides read receipts, typing indicator, last seen
    val screenSecurityEnabled: Boolean = true, // FLAG_SECURE
    val biometricLockEnabled: Boolean = false,
    val registrationLockEnabled: Boolean = false,
    val sealedSenderEnabled: Boolean = true,
    val relayAllCallsEnabled: Boolean = true,
    val defaultDisappearingSeconds: Long = 0L, // 0 = off
    val selectedLanguage: String = "en" // "en" or "hi"
)

data class ContactItem(
    val id: String,
    val name: String,
    val phoneNumber: String,
    val avatarEmoji: String,
    val about: String,
    val isOnline: Boolean = false
)
