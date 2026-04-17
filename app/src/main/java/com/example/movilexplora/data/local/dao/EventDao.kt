package com.example.movilexplora.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.movilexplora.data.local.entity.EventEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EventDao {
    @Query("SELECT * FROM events")
    fun getAllEvents(): Flow<List<EventEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: EventEntity)

    @Update
    suspend fun updateEvent(event: EventEntity)

    @Query("UPDATE events SET status = :status WHERE id = :eventId")
    suspend fun updateEventStatus(eventId: String, status: String)

    @Query("UPDATE events SET status = :status, rejectionReason = :rejectionReason WHERE id = :eventId")
    suspend fun updateEventStatusWithReason(eventId: String, status: String, rejectionReason: String)
}
