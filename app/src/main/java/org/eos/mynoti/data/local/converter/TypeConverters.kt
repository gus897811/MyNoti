package org.eos.mynoti.data.local.converter

import androidx.room.TypeConverter
import org.eos.mynoti.domain.model.AnalysisStatus
import org.eos.mynoti.domain.model.KeywordRuleType
import org.eos.mynoti.domain.model.NotificationType
import org.json.JSONArray
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

class LocalDateTimeConverter {
    private val zone: ZoneId = ZoneId.of("Asia/Seoul")

    @TypeConverter
    fun toEpochMillis(value: LocalDateTime?): Long? {
        return value?.atZone(zone)?.toInstant()?.toEpochMilli()
    }

    @TypeConverter
    fun fromEpochMillis(value: Long?): LocalDateTime? {
        return value?.let { Instant.ofEpochMilli(it).atZone(zone).toLocalDateTime() }
    }
}

class NotificationTypeConverter {
    @TypeConverter
    fun toName(value: NotificationType?): String? = value?.name

    @TypeConverter
    fun fromName(value: String?): NotificationType {
        return value?.let { runCatching { NotificationType.valueOf(it) }.getOrNull() }
            ?: NotificationType.ETC
    }
}

class KeywordRuleTypeConverter {
    @TypeConverter
    fun toName(value: KeywordRuleType?): String? = value?.name

    @TypeConverter
    fun fromName(value: String?): KeywordRuleType {
        return value?.let { runCatching { KeywordRuleType.valueOf(it) }.getOrNull() }
            ?: KeywordRuleType.IMPORTANT
    }
}

class AnalysisStatusConverter {
    @TypeConverter
    fun toName(value: AnalysisStatus?): String? = value?.name

    @TypeConverter
    fun fromName(value: String?): AnalysisStatus {
        return value?.let { runCatching { AnalysisStatus.valueOf(it) }.getOrNull() }
            ?: AnalysisStatus.PENDING
    }
}

class StringListConverter {
    @TypeConverter
    fun fromList(value: List<String>?): String {
        return JSONArray(value ?: emptyList<String>()).toString()
    }

    @TypeConverter
    fun toList(value: String?): List<String> {
        if (value.isNullOrBlank()) return emptyList()
        val array = JSONArray(value)
        return (0 until array.length()).map { index -> array.getString(index) }
    }
}
