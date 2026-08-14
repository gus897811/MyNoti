package org.eos.mynoti.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(
    tableName = "reminder",
    foreignKeys = [
        ForeignKey(
            entity = NotificationEntity::class,
            parentColumns = ["notification_id"],
            childColumns = ["notification_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["notification_id"]),
        Index(value = ["remind_at"])
    ]
)
data class ReminderEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "reminder_id")
    val reminderId: Long = 0,

    @ColumnInfo(name = "notification_id")
    val notificationId: Long,

    @ColumnInfo(name = "remind_at")
    val remindAt: LocalDateTime,

    @ColumnInfo(name = "is_fired")
    val isFired: Boolean = false,

    @ColumnInfo(name = "created_at")
    val createdAt: LocalDateTime
)
