package com.example.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "conversations")
data class ConversationEntity(
    @PrimaryKey val id: String,
    val title: String,
    val participantPhone: String,
    val lastMessage: String,
    val lastMessageTimestamp: Long,
    val unreadCount: Int = 0,
    val isPinned: Boolean = false,
    val isArchived: Boolean = false,
    val isMuted: Boolean = false,
    val isGroup: Boolean = false,
    val disappearingTimerSeconds: Long = 0L, // 0 = off, 5, 60, 3600, 86400, 604800
    val safetyNumberVerified: Boolean = false,
    val remoteIdentityKey: String = "",
    val avatarEmoji: String = "🥷"
)
