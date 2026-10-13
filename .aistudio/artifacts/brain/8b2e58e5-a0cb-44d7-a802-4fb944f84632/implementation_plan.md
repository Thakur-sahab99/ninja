# Ninja Secure Messenger - Architecture & Phased Implementation Plan

Ninja is a privacy-first, native Android messenger that bridges WhatsApp's approachable, responsive UX with Signal's uncompromising zero-trust privacy architecture. Every conversation, group, media asset, and call is end-to-end encrypted (E2EE) by default with Double Ratchet / X3DH principles, local SQLCipher encrypted persistence, and a signature dark-ninja aesthetic featuring "Shadow Mode".

---

## User Review & Critical Decisions

> [!IMPORTANT]
> **Modular Architecture Strategy & Build Environment Alignment**
> The user proposed decomposing the codebase into dedicated modules: `app`, `core`, `data`, `domain`, `feature-chat`, `feature-calls`, `feature-status`, and `feature-settings`.
> In standard Android Gradle projects, this can be structured either as:
> 1. **Gradle Multi-Module (`:app`, `:core:model`, `:core:crypto`, `:core:database`, `:core:network`, `:feature:chat`, etc.)**: Provides strict compilation boundaries at the cost of longer multi-module Gradle syncs.
> 2. **Modular Architecture via High-Cohesion Package-by-Feature inside `:app` (Clean MVVM Architecture)**: Mirrors the exact separation of concerns (`app`, `core/{crypto,database,network,designsystem}`, `domain/{model,repository,usecase}`, `data/{repository,local,remote}`, `feature/{auth,chat,calls,status,settings}`) while preserving rapid incremental build times and seamless resource consolidation in the cloud Android container.
>
> **Recommendation**: We propose structuring the codebase with clean architectural boundaries strictly mirroring these modules. During Phase 1-2, we establish the clean-architecture package modules within `:app` while configuring Gradle dependencies so that extracting into Gradle subprojects remains frictionless.

---

## 1. Overview & Core Concept

- **What It Does**: Ninja delivers high-speed, zero-knowledge instant messaging, disappearing ephemeral media, encrypted voice/video calling, and 24-hour disappearing status updates.
- **Privacy Model**: Zero-plaintext on relay servers, Sealed Sender envelope routing, client-side SQLCipher database encryption, biometric app lock, anti-screenshot screen security, and Safety Number cryptographic identity verification.
- **Key Differentiator**: "Shadow Mode" toggle that instantly masks typing indicators, read receipts, and online presence in a single gesture, packaged in an ultra-slick charcoal and deep indigo Material 3 design with shuriken iconography and vanish animations.

---

## 2. User Experience & Visual Design

### Visual Identity & Theme
- **Color Palette**:
  - Primary Background: Dark Charcoal (`#0D1117`) and Deep Night (`#161B22`)
  - Elevated Surfaces / Cards: Stealth Slate (`#21262D`)
  - Brand Accent: Shinobi Violet (`#8B5CF6`) and Ninja Cyan (`#06B6D4`)
  - Status Indicators: Emerald Ticks (`#10B981`) for delivered/read receipts
  - Danger / Vanish Accent: Crimson Ember (`#F43F5E`)
- **Typography & Hierarchy**:
  - Clean, legible geometric typography with strong contrast hierarchy (TitleLarge for chat headers, BodyLarge for messages, LabelMedium for timestamps and encryption badges).
- **Iconography**:
  - Material 3 Symbols paired with custom Shuriken brand glyphs, padlock security badges, and discreet disappearing timer icons.

### Key User Flows & Micro-Interactions
1. **Onboarding & Safety Lock**:
   - Phone verification (OTP entry), PIN registration lock setup, profile creation (display name, avatar with EXIF auto-scrubbing, About line).
2. **Main Tabs Navigation**:
   - **Chats**: Pinned chats, unread counter badges, search bar, swipe actions (archive/pin/mute), and "Shadow Mode" quick toggle in top bar.
   - **Status (Stories)**: Disappearing 24-hr status updates with granular privacy visibility rings.
   - **Calls**: Call history with E2EE shield indicators and one-tap voice/video initiation.
3. **Chat Room & Message Interaction**:
   - Message bubble stream with delivery tick state machine (`clock` -> `single tick` -> `double tick` -> `purple double tick` when read).
   - Rich input bar: voice note recorder with live visual waveform, attachments modal (camera, gallery, document, location, contact), emoji/sticker tray.
   - Contextual actions: reply quote preview, inline emoji reactions bar, message edit (15-min limit), disappearing message countdown timer.
   - Expired message "vanish" smoke animation.
4. **Safety Verification**:
   - Contact detail screen with Safety Numbers (60-digit fingerprint + QR code scan) and key change alerts.

---

## 3. Key Product Decisions & Trade-Offs

| Decision | Chosen Approach | Rationale | Alternatives Considered |
|---|---|---|---|
| **Local Persistence** | Room + Encrypted SQLCipher | Signal-grade security ensuring on-device database is unreadable if extracted. | Plain SQLite (insecure) or DataStore-only (unsuitable for message queries). |
| **End-to-End Cryptography** | Double Ratchet + X3DH (Signal Protocol design) with AES-GCM-256 session ratcheting | Industry-standard forward secrecy and break-in recovery. Server only ever relays ciphertext envelopes. | Symmetric static keys (vulnerable to key compromise). |
| **Offline Resilience** | Persistent Message Queue with retry backoff & state machine | Messages created offline transition from `QUEUED` -> `SENDING` -> `SENT` automatically upon network restoration. | In-memory retry (data loss on process death). |
| **Sealed Sender Relay** | Ephemeral encrypted transport envelopes where sender identity is inside the ciphertext payload | Server only inspects delivery recipient token and unauthenticated HMAC challenge. | Plain sender header (exposes metadata graph to relay). |
| **Calling** | WebRTC signaling with E2EE media streams and optional relay proxy mode | Low-latency peer-to-peer voice/video with IP cloaking when relay mode is enabled. | Traditional VoIP SIP (lacks modern webRTC noise cancellation and E2EE standards). |

---

## 4. Technical Architecture & Phased Roadmap

### System Architecture Diagram

```
┌────────────────────────────────────────────────────────────────────────┐
│                        Presentation Layer (Compose UI)                 │
│  ┌───────────────┐  ┌───────────────┐  ┌─────────────┐  ┌────────────┐ │
│  │ Feature Auth  │  │ Feature Chat  │  │Feature Calls│  │ Feature St.│ │
│  └───────┬───────┘  └───────┬───────┘  └──────┬──────┘  └─────┬──────┘ │
├──────────┼──────────────────┼─────────────────┼───────────────┼────────┤
│          ▼                  ▼                 ▼               ▼        │
│                        ViewModel & State Management                    │
│      (AuthViewModel, ChatViewModel, CallViewModel, SettingsViewModel)  │
├────────────────────────────────────────────────────────────────────────┤
│                        Domain Layer (Use Cases)                        │
│  - SendEncryptedMessageUseCase   - ProcessIncomingEnvelopeUseCase      │
│  - RatchetSessionUseCase         - VerifySafetyNumberUseCase           │
│  - EnqueueOfflineMessageUseCase  - ManageDisappearingTimerUseCase      │
├────────────────────────────────────────────────────────────────────────┤
│                     Data Layer (Repositories & Sync)                   │
│  ┌───────────────────────────┐       ┌───────────────────────────────┐ │
│  │     ChatRepositoryImpl    │       │       CryptoRepositoryImpl    │ │
│  │     OfflineMessageQueue   │       │       SignalProtocolEngine    │ │
│  └─────────────┬─────────────┘       └───────────────┬───────────────┘ │
├────────────────┼─────────────────────────────────────┼─────────────────┤
│                ▼                                     ▼                 │
│  ┌───────────────────────────────┐   ┌───────────────────────────────┐ │
│  │ Local: Room + SQLCipher       │   │ Remote Relay: Encrypted Blob  │ │
│  │ - Messages / Conversations    │   │ Firestore/Ktor Transport      │ │
│  │ - PreKeyStore / SessionStore  │   │ FCM Silent Wakeup Pings       │ │
│  └───────────────────────────────┘   └───────────────────────────────┘ │
└────────────────────────────────────────────────────────────────────────┘
```

### Phased Execution Roadmap

- **Phase A: Foundation, Modular Skeleton & Onboarding**
  - Dependency configuration (Room, SQLCipher, Coroutines, Navigation, Material 3 Icons).
  - Clean modular package structure (`core`, `domain`, `data`, `feature`).
  - Ninja Theme tokens (`Color.kt`, `Theme.kt`, `Type.kt`).
  - Auth flow: Phone number entry, OTP verification screen, profile setup, and Registration PIN lock.
- **Phase B: 1:1 E2EE Text Chat & Encrypted Local DB**
  - Cryptographic engine: Key generation, Double Ratchet state representation, X3DH session setup, envelope encrypt/decrypt.
  - Room database with encrypted migrations and entities (`ConversationEntity`, `MessageEntity`, `PreKeyEntity`).
  - Conversation list screen (pinned, archived, search, unread badge).
  - Chat room screen (message bubbles, delivery ticks, quote reply, emoji reactions, message edit, delete for me/everyone).
  - Safety number viewer and QR verification screen.
  - Offline message queue with automatic retry.
- **Phase C: Rich Media, Voice Notes & Ephemeral Timers**
  - Voice note recording with amplitude sampling and custom waveform visualization.
  - Media picker with EXIF scrubbing, image/video bubble rendering, and view-once mode.
  - Disappearing message engine with real-time countdown and "vanish" animation.
- **Phase D: Group Chats & Sender Keys**
  - Group creation, admin management, member roster (up to 256), group invite tokens.
  - Sender Key distribution protocol for efficient group broadcast encryption.
- **Phase E: Encrypted Voice & Video Calls**
  - Call screen with incoming/outgoing state, camera toggle, mute, and speakerphone controls.
  - WebRTC connection management and "Relay All Calls" IP hiding mode.
- **Phase F: Disappearing Status / Stories & Shadow Mode**
  - 24-hour status stories with text/photo creation, viewer progress bars, and audience privacy controls.
  - Global "Shadow Mode" system: one-tap masking of read receipts, typing indicators, and last seen.
- **Phase G: Privacy Settings, Biometric App Lock & Unit Tests**
  - Biometric / PIN App Lock with screen security (`FLAG_SECURE`).
  - Local encrypted backup creation and passphrase restore.
  - Unit tests for the Double Ratchet crypto engine and ChatViewModel.
  - Documentation & architecture README.
