package org.eos.mynoti.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import org.eos.mynoti.data.InstalledAppCatalog
import org.eos.mynoti.domain.model.TargetApp
import org.eos.mynoti.domain.model.ThemePreference
import org.eos.mynoti.domain.model.parseThemePreference

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "mynoti_settings"
)

class AppCollectionStore(
    context: Context,
    private val catalog: InstalledAppCatalog
) {

    private val dataStore = context.settingsDataStore

    val targetApps: Flow<List<TargetApp>> = dataStore.data.map { prefs ->
        resolveTargetApps(
            selectedPackages = prefs[TARGET_APP_PACKAGES],
            enabledPackages = prefs[TARGET_APPS],
            resolveName = catalog::labelFor
        )
    }.flowOn(Dispatchers.IO)

    val themePreference: Flow<ThemePreference> = dataStore.data.map { prefs ->
        parseThemePreference(prefs[THEME_PREFERENCE])
    }

    suspend fun setThemePreference(preference: ThemePreference) {
        dataStore.edit { prefs ->
            prefs[THEME_PREFERENCE] = preference.name
        }
    }

    suspend fun setTargetAppEnabled(packageName: String, enabled: Boolean) {
        val pkg = remapLegacyPackageName(packageName.trim())
        if (pkg.isEmpty()) return
        dataStore.edit { prefs ->
            val selected = remapLegacyPackageSet(prefs[TARGET_APP_PACKAGES] ?: defaultTargetPackageNames())
            val current = remapLegacyPackageSet(prefs[TARGET_APPS] ?: defaultEnabledPackageNames())
            prefs[TARGET_APP_PACKAGES] = selected
            prefs[TARGET_APPS] = if (enabled) current + pkg else current - pkg
        }
    }

    @Suppress("UNUSED_PARAMETER")
    suspend fun addTargetApp(packageName: String, name: String) {
        val pkg = remapLegacyPackageName(packageName.trim())
        dataStore.edit { prefs ->
            val selected = remapLegacyPackageSet(prefs[TARGET_APP_PACKAGES] ?: defaultTargetPackageNames())
            val enabled = remapLegacyPackageSet(prefs[TARGET_APPS] ?: defaultEnabledPackageNames())
            val updated = addTargetAppMembership(selected, enabled, pkg) ?: run {
                prefs[TARGET_APP_PACKAGES] = selected
                prefs[TARGET_APPS] = enabled
                return@edit
            }
            prefs[TARGET_APP_PACKAGES] = updated.first
            prefs[TARGET_APPS] = updated.second
        }
    }

    suspend fun removeTargetApp(packageName: String) {
        val pkg = remapLegacyPackageName(packageName.trim())
        dataStore.edit { prefs ->
            val selected = remapLegacyPackageSet(prefs[TARGET_APP_PACKAGES] ?: defaultTargetPackageNames())
            val enabled = remapLegacyPackageSet(prefs[TARGET_APPS] ?: defaultEnabledPackageNames())
            val updated = removeTargetAppMembership(selected, enabled, pkg)
            prefs[TARGET_APP_PACKAGES] = updated.first
            prefs[TARGET_APPS] = updated.second
        }
    }

    companion object {
        val TARGET_APPS = stringSetPreferencesKey("target_apps")
        val TARGET_APP_PACKAGES = stringSetPreferencesKey("target_app_packages")
        val THEME_PREFERENCE = stringPreferencesKey("theme_preference")
    }
}
