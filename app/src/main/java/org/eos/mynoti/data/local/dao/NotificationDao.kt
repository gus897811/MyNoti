package org.eos.mynoti.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import org.eos.mynoti.data.local.entity.NotificationEntity
import org.eos.mynoti.domain.model.AnalysisStatus
import org.eos.mynoti.domain.model.NotificationType
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

    @Query("DELETE FROM notification WHERE notification_id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT COUNT(*) FROM notification")
    suspend fun count(): Int

    @Query(
        """
        SELECT * FROM notification
        WHERE analysis_status IN ('PENDING', 'FAILED')
        ORDER BY received_at ASC
        LIMIT :limit
        """
    )
    suspend fun getPendingAnalysis(limit: Int): List<NotificationEntity>

    @Query(
        """
        SELECT * FROM notification
        WHERE analysis_status = :status
        ORDER BY received_at ASC
        LIMIT :limit
        """
    )
    suspend fun getByAnalysisStatus(status: AnalysisStatus, limit: Int): List<NotificationEntity>

    @Query(
        """
        UPDATE notification
        SET analysis_status = :status
        WHERE notification_id = :id
        """
    )
    suspend fun updateAnalysisStatus(id: Long, status: AnalysisStatus)

    @Query(
        """
        UPDATE notification
        SET analysis_status = 'PENDING'
        WHERE analysis_status = 'IN_PROGRESS'
        """
    )
    suspend fun resetStuckAnalysis()

    @Query(
        """
        UPDATE notification
        SET title = :title,
            summary = :summary,
            is_important = :isImportant,
            type = :type,
            deadline = :deadline,
            actions_json = :actionsJson,
            analysis_status = :status
        WHERE notification_id = :id
        """
    )
    suspend fun applyAnalysis(
        id: Long,
        title: String?,
        summary: String,
        isImportant: Boolean,
        type: NotificationType,
        deadline: LocalDateTime?,
        actionsJson: String,
        status: AnalysisStatus
    )
}
