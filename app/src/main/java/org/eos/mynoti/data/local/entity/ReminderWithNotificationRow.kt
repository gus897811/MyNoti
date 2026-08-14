package org.eos.mynoti.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Embedded

data class ReminderWithNotificationRow(
    @Embedded val reminder: ReminderEntity,
    @ColumnInfo(name = "title") val title: String?,
    @ColumnInfo(name = "app_name") val appName: String
)
