package com.example.data.repository

import com.example.core.crypto.DoubleRatchetEngine
import com.example.core.database.NinjaDatabase
import com.example.core.database.entity.CallEntity
import com.example.core.database.entity.ConversationEntity
import com.example.core.database.entity.MessageEntity
import com.example.core.database.entity.StatusEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.util.UUID

class ChatRepository(
    private val database: NinjaDatabase,
    private val repositoryScope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {
    val conversationsFlow: Flow<List<ConversationEntity>> =
        database.conversationDao().getActiveConversations()

    val archivedConversationsFlow: Flow<List<ConversationEntity>> =
        database.conversationDao().getArchivedConversations()

    val callsFlow: Flow<List<CallEntity>> =
        database.callDao().getAllCalls()

    fun getStatusFlow(currentTime: Long): Flow<List<StatusEntity>> =
        database.statusDao().getActiveStatuses(currentTime)

    fun getMessagesFlow(conversationId: String): Flow<List<MessageEntity>> =
        database.messageDao().getMessagesForConversation(conversationId)

    fun observeConversation(conversationId: String): Flow<ConversationEntity?> =
        database.conversationDao().observeConversationById(conversationId)

    suspend fun getConversation(id: String): ConversationEntity? =
        database.conversationDao().getConversationById(id)

    suspend fun seedInitialDataIfEmpty() {
        val existing = database.conversationDao().getConversationById("conv_1")
        if (existing == null) {
            val sampleConversations = listOf(
                ConversationEntity(
                    id = "conv_1",
                    title = "Cipher (Chief Architect)",
                    participantPhone = "+1 555-0199",
                    lastMessage = "Double Ratchet test vector verified. Sealed sender is live.",
                    lastMessageTimestamp = System.currentTimeMillis() - 1000 * 60 * 3,
                    unreadCount = 2,
                    isPinned = true,
                    disappearingTimerSeconds = 60L,
                    remoteIdentityKey = "3a9f02b1c8e744d90a12fe4490cd8812",
                    avatarEmoji = "🛡️"
                ),
                ConversationEntity(
                    id = "conv_2",
                    title = "Kage Protocol Working Group",
                    participantPhone = "group_kage",
                    lastMessage = "Aria: Sender keys rotated for epoch #4.",
                    lastMessageTimestamp = System.currentTimeMillis() - 1000 * 60 * 25,
                    unreadCount = 0,
                    isPinned = true,
                    isGroup = true,
                    disappearingTimerSeconds = 3600L,
                    avatarEmoji = "⚔️"
                ),
                ConversationEntity(
                    id = "conv_3",
                    title = "Kitsune",
                    participantPhone = "+91 98765 43210",
                    lastMessage = "Check the safety number when you have a moment.",
                    lastMessageTimestamp = System.currentTimeMillis() - 1000 * 60 * 120,
                    unreadCount = 0,
                    disappearingTimerSeconds = 0L,
                    safetyNumberVerified = true,
                    remoteIdentityKey = "5e8d91c0bb227f10d938290fa8bc0091",
                    avatarEmoji = "🦊"
                ),
                ConversationEntity(
                    id = "conv_4",
                    title = "Shadow Ops Logistics",
                    participantPhone = "+1 555-0144",
                    lastMessage = "Voice note received (0:14). Playback ready.",
                    lastMessageTimestamp = System.currentTimeMillis() - 1000 * 60 * 480,
                    unreadCount = 0,
                    avatarEmoji = "🌑"
                ),
                ConversationEntity(
                    id = "conv_5",
                    title = "Ronin (Archived)",
                    participantPhone = "+44 7700 900077",
                    lastMessage = "Relayed call ended without packet drops.",
                    lastMessageTimestamp = System.currentTimeMillis() - 1000 * 60 * 1440,
                    unreadCount = 0,
                    isArchived = true,
                    avatarEmoji = "🗡️"
                )
            )
            database.conversationDao().insertAll(sampleConversations)

            // Seed sample messages for conv_1
            val now = System.currentTimeMillis()
            val sampleMessages = listOf(
                MessageEntity(
                    id = "msg_1",
                    conversationId = "conv_1",
                    senderPhone = "+1 555-0199",
                    senderName = "Cipher",
                    content = "Welcome to Ninja. All messages are encrypted end-to-end.",
                    timestamp = now - 1000 * 60 * 15,
                    status = "READ",
                    isOutgoing = false
                ),
                MessageEntity(
                    id = "msg_2",
                    conversationId = "conv_1",
                    senderPhone = "me",
                    senderName = "Me",
                    content = "Understood. Testing Sealed Sender envelope delivery.",
                    timestamp = now - 1000 * 60 * 10,
                    status = "READ",
                    isOutgoing = true
                ),
                MessageEntity(
                    id = "msg_3",
                    conversationId = "conv_1",
                    senderPhone = "+1 555-0199",
                    senderName = "Cipher",
                    content = "Double Ratchet test vector verified. Sealed sender is live.",
                    timestamp = now - 1000 * 60 * 3,
                    status = "DELIVERED",
                    isOutgoing = false,
                    isDisappearing = true,
                    expiresAt = now + 1000 * 60 * 10
                )
            )
            database.messageDao().insertAll(sampleMessages)

            // Seed sample statuses
            val sampleStatuses = listOf(
                StatusEntity(
                    id = "status_1",
                    authorName = "Cipher",
                    authorPhone = "+1 555-0199",
                    avatarEmoji = "🛡️",
                    textCaption = "Keys rotated. Zero knowledge verified.",
                    backgroundGradientHex = "#8B5CF6",
                    isSelf = false
                ),
                StatusEntity(
                    id = "status_2",
                    authorName = "Kitsune",
                    authorPhone = "+91 98765 43210",
                    avatarEmoji = "🦊",
                    textCaption = "Stealth mode active. Always vanishing.",
                    backgroundGradientHex = "#06B6D4",
                    isSelf = false
                )
            )
            database.statusDao().insertAll(sampleStatuses)

            // Seed sample calls
            val sampleCalls = listOf(
                CallEntity(
                    id = "call_1",
                    contactName = "Cipher",
                    contactPhone = "+1 555-0199",
                    isVideo = true,
                    isOutgoing = false,
                    callStatus = "COMPLETED",
                    timestamp = now - 1000 * 60 * 45,
                    durationSeconds = 184,
                    isRelayed = true
                ),
                CallEntity(
                    id = "call_2",
                    contactName = "Kitsune",
                    contactPhone = "+91 98765 43210",
                    isVideo = false,
                    isOutgoing = true,
                    callStatus = "COMPLETED",
                    timestamp = now - 1000 * 60 * 300,
                    durationSeconds = 412,
                    isRelayed = true
                )
            )
            database.callDao().insertAll(sampleCalls)
        }
    }

    suspend fun sendMessage(
        conversationId: String,
        content: String,
        mediaType: String = "TEXT",
        mediaUri: String? = null,
        mediaDurationSeconds: Int = 0,
        replyToId: String? = null,
        replyToContent: String? = null,
        isViewOnce: Boolean = false,
        isDisappearing: Boolean = false,
        disappearingTimerSeconds: Long = 0L
    ) {
        val messageId = UUID.randomUUID().toString()
        val now = System.currentTimeMillis()
        val expiresAt = if (isDisappearing && disappearingTimerSeconds > 0) {
            now + (disappearingTimerSeconds * 1000L)
        } else 0L

        // Double Ratchet E2EE Envelope simulation
        val dummySessionKey = "a1b2c3d4e5f60718293a4b5c6d7e8f90"
        val envelope = DoubleRatchetEngine.encryptMessage(content, dummySessionKey, now)

        val message = MessageEntity(
            id = messageId,
            conversationId = conversationId,
            senderPhone = "me",
            senderName = "Me",
            content = content,
            mediaType = mediaType,
            mediaUri = mediaUri,
            mediaDurationSeconds = mediaDurationSeconds,
            timestamp = now,
            status = "QUEUED",
            isOutgoing = true,
            replyToId = replyToId,
            replyToContent = replyToContent,
            isViewOnce = isViewOnce,
            isDisappearing = isDisappearing,
            expiresAt = expiresAt
        )

        database.messageDao().insert(message)

        // Update conversation summary
        val conv = database.conversationDao().getConversationById(conversationId)
        if (conv != null) {
            val previewText = when (mediaType) {
                "IMAGE" -> "📷 Photo"
                "VOICE_NOTE" -> "🎤 Voice note"
                "DOCUMENT" -> "📄 Document"
                else -> content
            }
            database.conversationDao().insertOrUpdate(
                conv.copy(
                    lastMessage = previewText,
                    lastMessageTimestamp = now
                )
            )
        }

        // Simulate background transmission through relay & ratchet state progression
        repositoryScope.launch {
            delay(400)
            database.messageDao().updateStatus(messageId, "SENT")
            delay(600)
            database.messageDao().updateStatus(messageId, "DELIVERED")
            delay(900)
            database.messageDao().updateStatus(messageId, "READ")
        }
    }

    suspend fun addReaction(messageId: String, emoji: String) {
        val msg = database.messageDao().getMessageById(messageId) ?: return
        val existing = msg.reactionsJson
        val updated = if (existing.contains(emoji)) {
            ""
        } else {
            emoji
        }
        database.messageDao().updateReactions(messageId, updated)
    }

    suspend fun editMessage(messageId: String, newContent: String) {
        val msg = database.messageDao().getMessageById(messageId) ?: return
        val fifteenMinutesMillis = 15 * 60 * 1000L
        if (System.currentTimeMillis() - msg.timestamp <= fifteenMinutesMillis) {
            database.messageDao().editMessage(messageId, newContent)
        }
    }

    suspend fun deleteForEveryone(messageId: String) {
        database.messageDao().deleteForEveryone(messageId)
    }

    suspend fun deleteForMe(messageId: String) {
        database.messageDao().deleteMessage(messageId)
    }

    suspend fun toggleStar(messageId: String, currentStarred: Boolean) {
        database.messageDao().updateStarred(messageId, !currentStarred)
    }

    suspend fun markViewOnceOpened(messageId: String) {
        database.messageDao().markViewOnceOpened(messageId)
    }

    suspend fun togglePin(conversationId: String, currentPinned: Boolean) {
        database.conversationDao().updatePinned(conversationId, !currentPinned)
    }

    suspend fun toggleArchive(conversationId: String, currentArchived: Boolean) {
        database.conversationDao().updateArchived(conversationId, !currentArchived)
    }

    suspend fun updateDisappearingTimer(conversationId: String, seconds: Long) {
        database.conversationDao().updateDisappearingTimer(conversationId, seconds)
    }

    suspend fun setSafetyNumberVerified(conversationId: String, verified: Boolean) {
        database.conversationDao().updateSafetyNumberVerified(conversationId, verified)
    }

    suspend fun markAsRead(conversationId: String) {
        database.conversationDao().markAsRead(conversationId)
    }

    suspend fun createNewConversation(
        title: String,
        phone: String,
        isGroup: Boolean = false,
        avatarEmoji: String = "🥷"
    ): String {
        val id = "conv_" + UUID.randomUUID().toString().take(8)
        val conversation = ConversationEntity(
            id = id,
            title = title,
            participantPhone = phone,
            lastMessage = "Conversation started with E2EE",
            lastMessageTimestamp = System.currentTimeMillis(),
            unreadCount = 0,
            isGroup = isGroup,
            avatarEmoji = avatarEmoji,
            remoteIdentityKey = UUID.randomUUID().toString().replace("-", "")
        )
        database.conversationDao().insertOrUpdate(conversation)
        return id
    }

    suspend fun purgeExpiredMessages() {
        database.messageDao().purgeExpiredMessages(System.currentTimeMillis())
        database.statusDao().purgeExpiredStatuses(System.currentTimeMillis())
    }

    suspend fun addStatus(caption: String, bgHex: String = "#8B5CF6") {
        val status = StatusEntity(
            id = UUID.randomUUID().toString(),
            authorName = "You",
            authorPhone = "me",
            avatarEmoji = "🥷",
            textCaption = caption,
            timestamp = System.currentTimeMillis(),
            isSelf = true,
            backgroundGradientHex = bgHex
        )
        database.statusDao().insertStatus(status)
    }

    suspend fun addCallLog(contactName: String, phone: String, isVideo: Boolean, durationSec: Int) {
        val call = CallEntity(
            id = UUID.randomUUID().toString(),
            contactName = contactName,
            contactPhone = phone,
            isVideo = isVideo,
            isOutgoing = true,
            callStatus = "COMPLETED",
            timestamp = System.currentTimeMillis(),
            durationSeconds = durationSec,
            isRelayed = true
        )
        database.callDao().insertCall(call)
    }
}
