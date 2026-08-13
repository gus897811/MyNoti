package org.eos.mynoti.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.eos.mynoti.data.repository.SettingsRepository
import org.eos.mynoti.domain.model.AppSettings

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "mynoti_settings"
)

class DataStoreSettingsRepository(
    context: Context
) : SettingsRepository {

    private val dataStore = context.settingsDataStore

    override val settings: Flow<AppSettings> = dataStore.data.map { prefs ->
        val enabledPackages = prefs[TARGET_APPS]
        val targetApps = AppSettings.defaultTargetApps.map { app ->
            app.copy(enabled = enabledPackages?.contains(app.packageName) ?: app.enabled)
        }
        AppSettings(
            targetApps = targetApps,
            highlightKeywords = prefs[HIGHLIGHT_KEYWORDS]?.toSortedList()
                ?: AppSettings.defaultHighlightKeywords,
            muteKeywords = prefs[MUTE_KEYWORDS]?.toSortedList()
                ?: AppSettings.defaultMuteKeywords
        )
    }

    override suspend fun setTargetAppEnabled(packageName: String, enabled: Boolean) {
        dataStore.edit { prefs ->
            val current = prefs[TARGET_APPS] ?: AppSettings.defaultTargetApps
                .filter { it.enabled }
                .map { it.packageName }
                .toSet()
            prefs[TARGET_APPS] = if (enabled) current + packageName else current - packageName
        }
    }

    override suspend fun addHighlightKeyword(keyword: String) {
        addKeyword(HIGHLIGHT_KEYWORDS, AppSettings.defaultHighlightKeywords, keyword)
    }

    override suspend fun removeHighlightKeyword(keyword: String) {
        removeKeyword(HIGHLIGHT_KEYWORDS, AppSettings.defaultHighlightKeywords, keyword)
    }

    override suspend fun addMuteKeyword(keyword: String) {
        addKeyword(MUTE_KEYWORDS, AppSettings.defaultMuteKeywords, keyword)
    }

    override suspend fun removeMuteKeyword(keyword: String) {
        removeKeyword(MUTE_KEYWORDS, AppSettings.defaultMuteKeywords, keyword)
    }

    private suspend fun addKeyword(
        key: Preferences.Key<Set<String>>,
        defaults: List<String>,
        keyword: String
    ) {
        val normalized = keyword.trim()
        if (normalized.isEmpty()) return
        dataStore.edit { prefs ->
            val current = prefs[key] ?: defaults.toSet()
            prefs[key] = current + normalized
        }
    }

    private suspend fun removeKeyword(
        key: Preferences.Key<Set<String>>,
        defaults: List<String>,
        keyword: String
    ) {
        dataStore.edit { prefs ->
            val current = prefs[key] ?: defaults.toSet()
            prefs[key] = current - keyword
        }
    }

    private fun Set<String>.toSortedList(): List<String> = sorted()

    companion object {
        val TARGET_APPS = stringSetPreferencesKey("target_apps")
        val HIGHLIGHT_KEYWORDS = stringSetPreferencesKey("highlight_keywords")
        val MUTE_KEYWORDS = stringSetPreferencesKey("mute_keywords")
    }
}
