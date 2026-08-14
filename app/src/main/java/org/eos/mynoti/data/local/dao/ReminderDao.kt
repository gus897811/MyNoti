package org.eos.mynoti.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import org.eos.mynoti.data.local.entity.ReminderEntity
import org.eos.mynoti.data.local.entity.ReminderWithNotificationRow

@Dao
interface ReminderDao {

    @Insert
    suspend fun insert(reminder: ReminderEntity): Long

    @Query("SELECT * FROM reminder WHERE reminder_id = :id LIMIT 1")
    suspend fun getById(id: Long): ReminderEntity?

    @Query(
        """
        SELECT * FROM reminder
        WHERE notification_id = :notificationId
        ORDER BY remind_at ASC
        """
    )
    fun observeByNotificationId(notificationId: Long): Flow<List<ReminderEntity>>

    @Query(
        """
        SELECT * FROM reminder
        WHERE notification_id = :notificationId
        ORDER BY remind_at ASC
        """
    )
    suspend fun getByNotificationId(notificationId: Long): List<ReminderEntity>

    @Query("SELECT * FROM reminder WHERE is_fired = 0")
    suspend fun getPending(): List<ReminderEntity>

    @Query(
        """
        SELECT reminder.*, notification.title AS title, notification.app_name AS app_name
        FROM reminder
        INNER JOIN notification
            ON notification.notification_id = reminder.notification_id
        ORDER BY reminder.remind_at ASC, reminder.reminder_id ASC
        """
    )
    fun observeAllWithNotification(): Flow<List<ReminderWithNotificationRow>>

    @Query("UPDATE reminder SET is_fired = 1 WHERE reminder_id = :id")
    suspend fun markFired(id: Long)

    @Query("DELETE FROM reminder WHERE reminder_id = :id")
    suspend fun deleteById(id: Long)
}
