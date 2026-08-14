package org.eos.mynoti.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import org.eos.mynoti.domain.model.NotificationType
import java.time.LocalDateTime

@Entity(tableName = "manual_calendar_event")
data class ManualCalendarEventEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "event_id")
    val eventId: Long = 0,

    @ColumnInfo(name = "title")
    val title: String,

    @ColumnInfo(name = "location")
    val location: String? = null,

    @ColumnInfo(name = "event_at")
    val eventAt: LocalDateTime,

    @ColumnInfo(name = "type")
    val type: NotificationType = NotificationType.ETC,

    @ColumnInfo(name = "is_important")
    val isImportant: Boolean = false,

    @ColumnInfo(name = "created_at")
    val createdAt: LocalDateTime
)
