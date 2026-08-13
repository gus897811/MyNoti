package org.eos.mynoti.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import org.eos.mynoti.data.local.entity.NotificationEntity
import java.time.LocalDateTime

@Dao
interface NotificationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(notification: NotificationEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(notifications: List<NotificationEntity>)

    @Update
    suspend fun update(notification: NotificationEntity)

    @Delete
    suspend fun delete(notification: NotificationEntity)

    @Query("SELECT * FROM notification ORDER BY received_at DESC")
    fun observeAll(): Flow<List<NotificationEntity>>

    @Query(
        """
        SELECT * FROM notification
        WHERE is_important = 1
        ORDER BY received_at DESC
        """
    )
    fun observeImportant(): Flow<List<NotificationEntity>>

    @Query(
        """
        SELECT * FROM notification
        WHERE notification_id = :id
        LIMIT 1
        """
    )
    suspend fun getById(id: Long): NotificationEntity?

    @Query(
        """
        SELECT * FROM notification
        WHERE notification_id = :id
        LIMIT 1
        """
    )
    fun observeById(id: Long): Flow<NotificationEntity?>

    @Query(
        """
        SELECT * FROM notification
        WHERE app_package_name = :packageName
        ORDER BY received_at DESC
        """
    )
    fun observeByPackageName(packageName: String): Flow<List<NotificationEntity>>

    @Query(
        """
        UPDATE notification
        SET is_important = :isImportant
        WHERE notification_id = :id
        """
    )
    suspend fun updateImportance(id: Long, isImportant: Boolean)

    @Query(
        """
        UPDATE notification
        SET remind_at = :remindAt,
            is_reminded = 0
        WHERE notification_id = :id
        """
    )
    suspend fun setReminder(id: Long, remindAt: LocalDateTime)

    @Query(
        """
        UPDATE notification
        SET is_reminded = 1
        WHERE notification_id = :id
        """
    )
    suspend fun markAsReminded(id: Long)

    @Query("DELETE FROM notification WHERE notification_id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT COUNT(*) FROM notification")
    suspend fun count(): Int
}
