package org.eos.mynoti.data.repository

import kotlinx.coroutines.flow.Flow
import org.eos.mynoti.domain.model.CalendarEvent
import org.eos.mynoti.domain.model.NotificationType
import java.time.LocalDateTime

interface ManualCalendarEventRepository {

    fun observeEvents(): Flow<List<CalendarEvent>>

    suspend fun add(
        title: String,
        location: String?,
        eventAt: LocalDateTime,
        type: NotificationType,
        isImportant: Boolean
    ): Long

    suspend fun update(
        eventId: Long,
        title: String,
        location: String?,
        eventAt: LocalDateTime,
        type: NotificationType,
        isImportant: Boolean
    )

    suspend fun delete(eventId: Long)
}
