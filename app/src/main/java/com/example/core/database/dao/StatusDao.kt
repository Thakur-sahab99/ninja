package com.example.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.core.database.entity.StatusEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StatusDao {
    @Query("SELECT * FROM status_updates WHERE expiresAt > :currentTime ORDER BY timestamp DESC")
    fun getActiveStatuses(currentTime: Long): Flow<List<StatusEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStatus(status: StatusEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(statuses: List<StatusEntity>)

    @Query("UPDATE status_updates SET isViewed = 1 WHERE id = :id")
    suspend fun markStatusViewed(id: String)

    @Query("DELETE FROM status_updates WHERE expiresAt <= :currentTime")
    suspend fun purgeExpiredStatuses(currentTime: Long): Int
}
