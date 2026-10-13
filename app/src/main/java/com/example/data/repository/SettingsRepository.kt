package com.example.data.repository

import com.example.core.model.SecuritySettings
import com.example.core.model.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class SettingsRepository {
    private val _settings = MutableStateFlow(
        SecuritySettings(
            isShadowModeActive = false,
            screenSecurityEnabled = false,
            biometricLockEnabled = false,
            registrationLockEnabled = false,
            sealedSenderEnabled = true,
            relayAllCallsEnabled = true,
            defaultDisappearingSeconds = 0L,
            selectedLanguage = "en"
        )
    )
    val settings: StateFlow<SecuritySettings> = _settings.asStateFlow()

    private val _userProfile = MutableStateFlow(
        UserProfile(
            phoneNumber = "+1 555-0100",
            displayName = "Ghost",
            about = "Living in the shadows 🥷",
            avatarEmoji = "🥷",
            identityPublicKey = "c4b901aef0319bc828d115ee81fa4902",
            isOnboarded = true
        )
    )
    val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

    fun toggleShadowMode() {
        _settings.update { it.copy(isShadowModeActive = !it.isShadowModeActive) }
    }

    fun setScreenSecurity(enabled: Boolean) {
        _settings.update { it.copy(screenSecurityEnabled = enabled) }
    }

    fun setBiometricLock(enabled: Boolean) {
        _settings.update { it.copy(biometricLockEnabled = enabled) }
    }

    fun setSealedSender(enabled: Boolean) {
        _settings.update { it.copy(sealedSenderEnabled = enabled) }
    }

    fun setRelayAllCalls(enabled: Boolean) {
        _settings.update { it.copy(relayAllCallsEnabled = enabled) }
    }

    fun setDefaultDisappearingTimer(seconds: Long) {
        _settings.update { it.copy(defaultDisappearingSeconds = seconds) }
    }

    fun setLanguage(lang: String) {
        _settings.update { it.copy(selectedLanguage = lang) }
    }

    fun updateProfile(displayName: String, about: String, avatarEmoji: String) {
        _userProfile.update {
            it.copy(
                displayName = displayName,
                about = about,
                avatarEmoji = avatarEmoji
            )
        }
    }

    fun completeOnboarding(phone: String, displayName: String, about: String) {
        _userProfile.update {
            it.copy(
                phoneNumber = phone,
                displayName = displayName.ifBlank { "Ghost" },
                about = about.ifBlank { "Living in the shadows 🥷" },
                isOnboarded = true
            )
        }
    }
}
