package org.eos.mynoti.data.repository

import kotlinx.coroutines.flow.Flow
import org.eos.mynoti.domain.model.AppSettings

interface SettingsRepository {
    val settings: Flow<AppSettings>

    suspend fun setTargetAppEnabled(packageName: String, enabled: Boolean)
    suspend fun addTargetApp(packageName: String, name: String)
    suspend fun removeTargetApp(packageName: String)
    suspend fun addHighlightKeyword(keyword: String)
    suspend fun removeHighlightKeyword(keyword: String)
    suspend fun addMuteKeyword(keyword: String)
    suspend fun removeMuteKeyword(keyword: String)
}
