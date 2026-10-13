package com.example.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.core.database.entity.MessageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {
    @Query("SELECT * FROM messages WHERE conversationId = :conversationId ORDER BY timestamp ASC")
    fun getMessagesForConversation(conversationId: String): Flow<List<MessageEntity>>

    @Query("SELECT * FROM messages WHERE conversationId = :conversationId ORDER BY timestamp DESC LIMIT :limit OFFSET :offset")
    suspend fun getPagedMessages(conversationId: String, limit: Int, offset: Int): List<MessageEntity>

    @Query("SELECT * FROM messages WHERE status = 'QUEUED' ORDER BY timestamp ASC")
    suspend fun getQueuedMessages(): List<MessageEntity>

    @Query("SELECT * FROM messages WHERE content LIKE '%' || :query || '%' ORDER BY timestamp DESC")
    suspend fun searchMessages(query: String): List<MessageEntity>

    @Query("SELECT * FROM messages WHERE id = :id LIMIT 1")
    suspend fun getMessageById(id: String): MessageEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(message: MessageEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(messages: List<MessageEntity>)

    @Update
    suspend fun update(message: MessageEntity)

    @Query("UPDATE messages SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: String, status: String)

    @Query("UPDATE messages SET reactionsJson = :reactionsJson WHERE id = :id")
    suspend fun updateReactions(id: String, reactionsJson: String)

    @Query("UPDATE messages SET content = :newContent, isEdited = 1 WHERE id = :id")
    suspend fun editMessage(id: String, newContent: String)

    @Query("UPDATE messages SET isDeletedForEveryone = 1, content = 'This message was deleted' WHERE id = :id")
    suspend fun deleteForEveryone(id: String)

    @Query("UPDATE messages SET isStarred = :starred WHERE id = :id")
    suspend fun updateStarred(id: String, starred: Boolean)

    @Query("UPDATE messages SET isViewed = 1 WHERE id = :id")
    suspend fun markViewOnceOpened(id: String)

    @Query("DELETE FROM messages WHERE isDisappearing = 1 AND expiresAt > 0 AND expiresAt <= :currentTime")
    suspend fun purgeExpiredMessages(currentTime: Long): Int

    @Query("DELETE FROM messages WHERE id = :id")
    suspend fun deleteMessage(id: String)

    @Query("DELETE FROM messages WHERE conversationId = :conversationId")
    suspend fun clearChat(conversationId: String)
}
