package org.eos.mynoti.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import org.eos.mynoti.data.local.entity.ManualCalendarEventEntity

@Dao
interface ManualCalendarEventDao {

    @Insert
    suspend fun insert(event: ManualCalendarEventEntity): Long

    @Query("SELECT * FROM manual_calendar_event ORDER BY event_at ASC")
    fun observeAll(): Flow<List<ManualCalendarEventEntity>>

    @Query("DELETE FROM manual_calendar_event WHERE event_id = :id")
    suspend fun deleteById(id: Long)
}
