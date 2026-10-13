package com.example.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.core.database.entity.ConversationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ConversationDao {
    @Query("SELECT * FROM conversations WHERE isArchived = 0 ORDER BY isPinned DESC, lastMessageTimestamp DESC")
    fun getActiveConversations(): Flow<List<ConversationEntity>>

    @Query("SELECT * FROM conversations WHERE isArchived = 1 ORDER BY lastMessageTimestamp DESC")
    fun getArchivedConversations(): Flow<List<ConversationEntity>>

    @Query("SELECT * FROM conversations WHERE id = :id LIMIT 1")
    suspend fun getConversationById(id: String): ConversationEntity?

    @Query("SELECT * FROM conversations WHERE id = :id LIMIT 1")
    fun observeConversationById(id: String): Flow<ConversationEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(conversation: ConversationEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(conversations: List<ConversationEntity>)

    @Update
    suspend fun update(conversation: ConversationEntity)

    @Query("UPDATE conversations SET isPinned = :pinned WHERE id = :id")
    suspend fun updatePinned(id: String, pinned: Boolean)

    @Query("UPDATE conversations SET isArchived = :archived WHERE id = :id")
    suspend fun updateArchived(id: String, archived: Boolean)

    @Query("UPDATE conversations SET disappearingTimerSeconds = :timer WHERE id = :id")
    suspend fun updateDisappearingTimer(id: String, timer: Long)

    @Query("UPDATE conversations SET safetyNumberVerified = :verified WHERE id = :id")
    suspend fun updateSafetyNumberVerified(id: String, verified: Boolean)

    @Query("UPDATE conversations SET unreadCount = 0 WHERE id = :id")
    suspend fun markAsRead(id: String)

    @Query("DELETE FROM conversations WHERE id = :id")
    suspend fun deleteConversation(id: String)
}
