package com.example.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.core.database.entity.CallEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CallDao {
    @Query("SELECT * FROM calls ORDER BY timestamp DESC")
    fun getAllCalls(): Flow<List<CallEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCall(call: CallEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(calls: List<CallEntity>)

    @Query("DELETE FROM calls WHERE id = :id")
    suspend fun deleteCall(id: String)

    @Query("DELETE FROM calls")
    suspend fun clearHistory()
}
