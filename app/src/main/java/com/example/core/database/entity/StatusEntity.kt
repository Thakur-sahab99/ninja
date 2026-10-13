package com.example.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "status_updates")
data class StatusEntity(
    @PrimaryKey val id: String,
    val authorName: String,
    val authorPhone: String,
    val avatarEmoji: String = "🥷",
    val textCaption: String,
    val mediaUri: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val expiresAt: Long = System.currentTimeMillis() + (24 * 60 * 60 * 1000L),
    val isSelf: Boolean = false,
    val isViewed: Boolean = false,
    val backgroundGradientHex: String = "#8B5CF6"
)
