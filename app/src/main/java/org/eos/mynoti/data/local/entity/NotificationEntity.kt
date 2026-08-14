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

    @ColumnInfo(name = "original_title")
    val originalTitle: String? = null,

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

    // LLM 분석 결과. deadline은 백엔드 AnalyzeResponse와 동일한 이름이다.
    @ColumnInfo(name = "deadline")
    val deadline: LocalDateTime? = null,

    @ColumnInfo(name = "summary")
    val summary: String? = null,

    @ColumnInfo(name = "analysis_status")
    val analysisStatus: AnalysisStatus = AnalysisStatus.PENDING,

    @ColumnInfo(name = "actions_json")
    val actions: List<String> = emptyList()
)
