package org.eos.mynoti.data.local

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.eos.mynoti.data.local.dao.ManualCalendarEventDao
import org.eos.mynoti.data.local.entity.ManualCalendarEventEntity
import org.eos.mynoti.data.local.mapper.toCalendarEvent
import org.eos.mynoti.data.repository.ManualCalendarEventRepository
import org.eos.mynoti.domain.model.CalendarEvent
import org.eos.mynoti.domain.model.NotificationType
import java.time.LocalDateTime

class RoomManualCalendarEventRepository(
    private val dao: ManualCalendarEventDao
) : ManualCalendarEventRepository {

    override fun observeEvents(): Flow<List<CalendarEvent>> {
        return dao.observeAll().map { entities -> entities.map { it.toCalendarEvent() } }
    }

    override suspend fun add(
        title: String,
        location: String?,
        eventAt: LocalDateTime,
        type: NotificationType,
        isImportant: Boolean
    ): Long {
        return dao.insert(
            ManualCalendarEventEntity(
                title = title.trim(),
                location = location?.trim()?.takeIf { it.isNotBlank() },
                eventAt = eventAt,
                type = type,
                isImportant = isImportant,
                createdAt = LocalDateTime.now()
            )
        )
    }

    override suspend fun update(
        eventId: Long,
        title: String,
        location: String?,
        eventAt: LocalDateTime,
        type: NotificationType,
        isImportant: Boolean
    ) {
        if (eventId <= 0L) return
        val trimmed = title.trim()
        if (trimmed.isEmpty()) return
        val existing = dao.getById(eventId) ?: return
        dao.update(
            existing.copy(
                title = trimmed,
                location = location?.trim()?.takeIf { it.isNotBlank() },
                eventAt = eventAt,
                type = type,
                isImportant = isImportant
            )
        )
    }

    override suspend fun delete(eventId: Long) {
        if (eventId <= 0L) return
        dao.deleteById(eventId)
    }
}
