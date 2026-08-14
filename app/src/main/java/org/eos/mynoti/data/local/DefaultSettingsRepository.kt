package org.eos.mynoti.data.local

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import org.eos.mynoti.data.datastore.AppCollectionStore
import org.eos.mynoti.data.local.dao.KeywordRuleDao
import org.eos.mynoti.data.local.entity.KeywordRuleEntity
import org.eos.mynoti.data.repository.SettingsRepository
import org.eos.mynoti.domain.model.AppSettings
import org.eos.mynoti.domain.model.KeywordRuleType
import org.eos.mynoti.domain.model.ThemePreference
import java.time.LocalDateTime

class DefaultSettingsRepository(
    private val collectionStore: AppCollectionStore,
    private val keywordRuleDao: KeywordRuleDao
) : SettingsRepository {

    override val settings: Flow<AppSettings> = combine(
        collectionStore.targetApps,
        keywordRuleDao.observeAll(),
        collectionStore.themePreference
    ) { apps, rules, themePreference ->
        AppSettings(
            targetApps = apps,
            highlightKeywords = rules
                .filter { it.ruleType == KeywordRuleType.IMPORTANT }
                .map { it.keyword },
            muteKeywords = rules
                .filter { it.ruleType == KeywordRuleType.MUTE }
                .map { it.keyword },
            themePreference = themePreference
        )
    }

    override suspend fun setThemePreference(preference: ThemePreference) {
        collectionStore.setThemePreference(preference)
    }

    override suspend fun setTargetAppEnabled(packageName: String, enabled: Boolean) {
        collectionStore.setTargetAppEnabled(packageName, enabled)
    }

    override suspend fun addTargetApp(packageName: String, name: String) {
        collectionStore.addTargetApp(packageName, name)
    }

    override suspend fun removeTargetApp(packageName: String) {
        collectionStore.removeTargetApp(packageName)
    }

    override suspend fun addHighlightKeyword(keyword: String) {
        insertKeyword(keyword, KeywordRuleType.IMPORTANT)
    }

    override suspend fun removeHighlightKeyword(keyword: String) {
        keywordRuleDao.deleteByKeyword(keyword.trim(), KeywordRuleType.IMPORTANT)
    }

    override suspend fun addMuteKeyword(keyword: String) {
        insertKeyword(keyword, KeywordRuleType.MUTE)
    }

    override suspend fun removeMuteKeyword(keyword: String) {
        keywordRuleDao.deleteByKeyword(keyword.trim(), KeywordRuleType.MUTE)
    }

    private suspend fun insertKeyword(keyword: String, type: KeywordRuleType) {
        val normalized = keyword.trim()
        if (normalized.isEmpty()) return
        keywordRuleDao.insert(
            KeywordRuleEntity(
                keyword = normalized,
                ruleType = type,
                createdAt = LocalDateTime.now()
            )
        )
    }
}
