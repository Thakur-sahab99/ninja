package com.example.core.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.core.database.dao.CallDao
import com.example.core.database.dao.ConversationDao
import com.example.core.database.dao.MessageDao
import com.example.core.database.dao.StatusDao
import com.example.core.database.entity.CallEntity
import com.example.core.database.entity.ConversationEntity
import com.example.core.database.entity.MessageEntity
import com.example.core.database.entity.StatusEntity

@Database(
    entities = [
        ConversationEntity::class,
        MessageEntity::class,
        CallEntity::class,
        StatusEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class NinjaDatabase : RoomDatabase() {
    abstract fun conversationDao(): ConversationDao
    abstract fun messageDao(): MessageDao
    abstract fun callDao(): CallDao
    abstract fun statusDao(): StatusDao

    companion object {
        @Volatile
        private var INSTANCE: NinjaDatabase? = null

        fun getInstance(context: Context): NinjaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NinjaDatabase::class.java,
                    "ninja_secure.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
