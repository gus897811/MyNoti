package org.eos.mynoti.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.eos.mynoti.domain.model.AppSettings
import org.eos.mynoti.domain.model.TargetApp

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "mynoti_settings"
)

class AppCollectionStore(context: Context) {

    private val dataStore = context.settingsDataStore

    val targetApps: Flow<List<TargetApp>> = dataStore.data.map { prefs ->
        val enabledPackages = prefs[TARGET_APPS]
        AppSettings.defaultTargetApps.map { app ->
            app.copy(enabled = enabledPackages?.contains(app.packageName) ?: app.enabled)
        }
    }

    suspend fun setTargetAppEnabled(packageName: String, enabled: Boolean) {
        dataStore.edit { prefs ->
            val current = prefs[TARGET_APPS] ?: AppSettings.defaultTargetApps
                .filter { it.enabled }
                .map { it.packageName }
                .toSet()
            prefs[TARGET_APPS] = if (enabled) current + packageName else current - packageName
        }
    }

    companion object {
        val TARGET_APPS = stringSetPreferencesKey("target_apps")
    }
}
