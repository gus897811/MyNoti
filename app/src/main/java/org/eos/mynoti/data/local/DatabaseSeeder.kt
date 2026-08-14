package org.eos.mynoti.data.local

import org.eos.mynoti.BuildConfig
import org.eos.mynoti.data.NotificationIngest
import org.eos.mynoti.data.local.dao.KeywordRuleDao
import org.eos.mynoti.data.local.dao.NotificationDao
import org.eos.mynoti.data.local.entity.KeywordRuleEntity
import org.eos.mynoti.data.local.mapper.toEntity
import org.eos.mynoti.data.mock.MockNotificationData
import org.eos.mynoti.domain.model.AppSettings
import org.eos.mynoti.domain.model.KeywordRuleType
import java.time.LocalDateTime

object DatabaseSeeder {

    suspend fun seedIfNeeded(
        notificationDao: NotificationDao,
        keywordRuleDao: KeywordRuleDao
    ) {
        if (BuildConfig.DEBUG && notificationDao.count() == 0) {
            notificationDao.insertAll(
                MockNotificationData.create().map { it.toEntity() }
            )
            // README curl 예시와 동일한 미분석 LearningX 알림 → Worker가 Backend로 보낸다.
            notificationDao.insert(NotificationIngest.learningXSample().toEntity())
        }
        if (keywordRuleDao.count() == 0) {
            val now = LocalDateTime.now()
            AppSettings.defaultHighlightKeywords.forEach { keyword ->
                keywordRuleDao.insert(
                    KeywordRuleEntity(
                        keyword = keyword,
                        ruleType = KeywordRuleType.IMPORTANT,
                        createdAt = now
                    )
                )
            }
            AppSettings.defaultMuteKeywords.forEach { keyword ->
                keywordRuleDao.insert(
                    KeywordRuleEntity(
                        keyword = keyword,
                        ruleType = KeywordRuleType.MUTE,
                        createdAt = now
                    )
                )
            }
        }
    }
}
