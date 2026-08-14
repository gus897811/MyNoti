package org.eos.mynoti.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import org.eos.mynoti.domain.model.AnalysisStatus
import org.eos.mynoti.domain.model.NotificationType
import java.time.LocalDateTime

@Entity(tableName = "notification")
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "notification_id")
    val notificationId: Long = 0,

    @ColumnInfo(name = "app_name")
    val appName: String,

    @ColumnInfo(name = "app_package_name")
    val appPackageName: String,

    @ColumnInfo(name = "title")
    val title: String?,

    @ColumnInfo(name = "content")
    val content: String?,

    @ColumnInfo(name = "received_at")
    val receivedAt: LocalDateTime,

    @ColumnInfo(name = "is_important")
    val isImportant: Boolean = false,

    @ColumnInfo(name = "type")
    val type: NotificationType = NotificationType.ETC,

    @ColumnInfo(name = "created_at")
    val createdAt: LocalDateTime,

    @ColumnInfo(name = "remind_at")
    val remindAt: LocalDateTime?,

    @ColumnInfo(name = "is_reminded")
    val isReminded: Boolean = false,

    // LLM 분석 결과. v1에는 없었고 v2 migration으로 추가한다.
    @ColumnInfo(name = "summary")
    val summary: String? = null,

    @ColumnInfo(name = "action_required")
    val actionRequired: Boolean = false,

    @ColumnInfo(name = "analysis_status")
    val analysisStatus: AnalysisStatus = AnalysisStatus.PENDING,

    @ColumnInfo(name = "actions_json")
    val actions: List<String> = emptyList()
)
