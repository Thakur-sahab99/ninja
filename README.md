# Ninja Messenger 🥷
**A secure, end-to-end encrypted Android messenger that combines WhatsApp's familiar UX with Signal's uncompromising privacy.**

---

## 🌟 Key Highlights & Philosophy

- **WhatsApp-Familiar UX**: Conversation list with pinned/archived tabs, delivery ticks (`clock` -> `sent` -> `delivered` -> `read`), swipe actions, inline emoji reactions, rich attachments, and voice notes with visual waveforms.
- **Signal-Grade Privacy**: Double Ratchet + X3DH end-to-end encryption by default for all chats, groups, and media.
- **Sealed Sender**: Server only sees anonymous routing tokens; sender identities are sealed within encrypted envelopes.
- **Shadow Mode**: One-tap master switch to instantly hide read receipts, typing indicators, and online presence.
- **Disappearing Messages**: Ephemeral message countdowns with a ninja smoke vanish effect upon expiry.
- **View-Once Media**: Protected view-once images and videos with automatic screen security (`FLAG_SECURE`) to block screenshots.
- **Relayed Calls**: WebRTC voice and video calls with an optional relay-all-calls proxy to cloak client IP addresses.
- **Safety Numbers**: 60-digit fingerprint comparison in 12 blocks of 5 digits and QR code verification.

---

## 🏗️ Architecture & Module Structure

The project implements **Clean Architecture + MVVM** using Kotlin, Jetpack Compose, Coroutines, Flow, and Room:

```
app/src/main/java/com/example/
├── MainActivity.kt                      # Root host, edge-to-edge, dynamic FLAG_SECURE
├── core/
│   ├── crypto/
│   │   ├── DoubleRatchetEngine.kt       # AES-256-GCM, HKDF-SHA256, 60-digit safety numbers
│   │   └── CryptoModels.kt              # Envelopes, key pairs, prekey bundles
│   ├── database/
│   │   ├── NinjaDatabase.kt             # Room database (SQLCipher encrypted persistence)
│   │   ├── entity/                      # ConversationEntity, MessageEntity, CallEntity, StatusEntity
│   │   └── dao/                         # ConversationDao, MessageDao, CallDao, StatusDao
│   └── model/
│       └── UserProfile.kt               # UserProfile, SecuritySettings, ContactItem
├── data/
│   └── repository/
│       ├── ChatRepository.kt           # Offline message queue, ratchet encryption, timers
│       └── SettingsRepository.kt       # Shadow mode, biometric lock, screen security, language
└── feature/
    ├── NinjaMainViewModel.kt            # Central UI state machine, calling, voice recording
    ├── common/
    │   └── Components.kt                # Shuriken animated glyph, DeliveryStatusTicks, Waveforms
    ├── auth/
    │   └── AuthScreen.kt                # Phone number OTP, profile customization, registration PIN
    ├── chat/
    │   ├── ConversationListScreen.kt    # Chats list, pinned/archived, search, new chat FAB
    │   ├── ChatRoomScreen.kt            # E2EE message stream, waveform notes, reactions, view-once
    │   ├── SafetyNumberScreen.kt        # 60-digit fingerprint & QR code verification
    │   └── ArchivedChatsScreen.kt       # Archived conversations view
    ├── calls/
    │   ├── CallsScreen.kt               # Call log, relay security indicators, redial
    │   └── ActiveCallScreen.kt          # WebRTC voice/video call UI with mute/speaker/video pause
    ├── status/
    │   └── StatusScreen.kt              # 24-hr disappearing status stories with progress bars
    └── settings/
        └── SettingsScreen.kt            # Shadow mode, sealed sender, relay calls, local backup
```

---

## 🔒 Security Model

1. **Double Ratchet & Forward Secrecy**: Every message key is derived dynamically via HKDF-SHA256 from a ratchet chain. Once a message is decrypted, its key is destroyed, preventing backward decryption even if a future key is intercepted.
2. **Sealed Sender Envelope**: The transport envelope wraps the sender's identity inside the ciphertext. The relay server cannot construct a social graph of who is communicating with whom.
3. **Local Encryption (SQLCipher)**: Messages and keys stored on-device are protected using AES-256 local database encryption.
4. **Metadata Scrubbing**: Media attachments are scrubbed of EXIF and geolocation metadata prior to transmission.
5. **Screen Security**: When enabled, Android's `FLAG_SECURE` prevents other apps, screenshots, and screen recordings from capturing sensitive chats or view-once media.

---

## 🚀 Setup & Build Instructions

### Prerequisites
- Android Studio Ladybug / Meerkat or AI Studio Build
- Kotlin 2.2+, Jetpack Compose, AGP 9.1+
- Min SDK: 26, Target SDK: 36

### Compiling & Testing
To compile the debug APK:
```bash
gradle assembleDebug
```

To run unit tests for the cryptographic layer:
```bash
gradle :app:testDebugUnitTest
```
