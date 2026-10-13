package com.example.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "messages",
    indices = [
        Index("conversationId"),
        Index("timestamp")
    ]
)
data class MessageEntity(
    @PrimaryKey val id: String,
    val conversationId: String,
    val senderPhone: String,
    val senderName: String,
    val content: String,
    val mediaType: String = "TEXT", // TEXT, IMAGE, VOICE_NOTE, DOCUMENT, LOCATION
    val mediaUri: String? = null,
    val mediaDurationSeconds: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val status: String = "SENT", // QUEUED, SENDING, SENT, DELIVERED, READ, FAILED
    val isOutgoing: Boolean = true,
    val replyToId: String? = null,
    val replyToContent: String? = null,
    val reactionsJson: String = "", // e.g. "👍:1,❤️:1"
    val isStarred: Boolean = false,
    val isEdited: Boolean = false,
    val isDeletedForEveryone: Boolean = false,
    val isDisappearing: Boolean = false,
    val expiresAt: Long = 0L,
    val isViewOnce: Boolean = false,
    val isViewed: Boolean = false
)
