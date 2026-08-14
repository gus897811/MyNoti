package org.eos.mynoti.data

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Build
import org.eos.mynoti.data.datastore.fallbackTargetAppName
import java.text.Collator
import java.util.Locale

data class InstalledAppInfo(
    val packageName: String,
    val label: String
)

class InstalledAppCatalog(context: Context) {

    private val appContext = context.applicationContext
    private val packageManager = appContext.packageManager

    fun listLaunchableApps(): List<InstalledAppInfo> {
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        val resolved = queryLaunchers(intent)
        val collator = Collator.getInstance(Locale.getDefault())
        return resolved
            .mapNotNull { info ->
                val packageName = info.activityInfo?.packageName ?: return@mapNotNull null
                if (packageName == appContext.packageName) return@mapNotNull null
                val label = info.loadLabel(packageManager).toString().ifBlank { packageName }
                InstalledAppInfo(packageName = packageName, label = label)
            }
            .distinctBy { it.packageName }
            .sortedWith(compareBy(collator) { it.label })
    }

    fun labelFor(packageName: String): String {
        val installedLabel = runCatching {
            val applicationInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                packageManager.getApplicationInfo(
                    packageName,
                    PackageManager.ApplicationInfoFlags.of(0)
                )
            } else {
                @Suppress("DEPRECATION")
                packageManager.getApplicationInfo(packageName, 0)
            }
            packageManager.getApplicationLabel(applicationInfo).toString()
        }.getOrNull()?.takeIf { it.isNotBlank() }
        return installedLabel ?: fallbackTargetAppName(packageName)
    }

    fun iconDrawable(packageName: String): Drawable? {
        return runCatching { packageManager.getApplicationIcon(packageName) }.getOrNull()
    }

    private fun queryLaunchers(intent: Intent) = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        packageManager.queryIntentActivities(intent, PackageManager.ResolveInfoFlags.of(0))
    } else {
        @Suppress("DEPRECATION")
        packageManager.queryIntentActivities(intent, 0)
    }
}

fun filterInstalledApps(
    apps: List<InstalledAppInfo>,
    addedPackages: Set<String>,
    query: String
): List<InstalledAppInfo> {
    val normalizedQuery = query.trim()
    return apps
        .filter { it.packageName !in addedPackages }
        .filter { app ->
            normalizedQuery.isEmpty() ||
                app.label.contains(normalizedQuery, ignoreCase = true) ||
                app.packageName.contains(normalizedQuery, ignoreCase = true)
        }
}
