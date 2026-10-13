package com.example.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "calls")
data class CallEntity(
    @PrimaryKey val id: String,
    val contactName: String,
    val contactPhone: String,
    val isVideo: Boolean = false,
    val isOutgoing: Boolean = true,
    val callStatus: String = "COMPLETED", // COMPLETED, MISSED, REJECTED
    val timestamp: Long = System.currentTimeMillis(),
    val durationSeconds: Int = 0,
    val isRelayed: Boolean = true // IP hidden via relay proxy
)
