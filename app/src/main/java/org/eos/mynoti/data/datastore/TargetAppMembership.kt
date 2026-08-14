package org.eos.mynoti.data.datastore

import org.eos.mynoti.domain.model.AppSettings
import org.eos.mynoti.domain.model.TargetApp

fun defaultTargetPackageNames(): Set<String> {
    return AppSettings.defaultTargetApps.map { it.packageName }.toSet()
}

fun defaultEnabledPackageNames(): Set<String> {
    return AppSettings.defaultTargetApps.filter { it.enabled }.map { it.packageName }.toSet()
}

fun fallbackTargetAppName(packageName: String): String {
    return AppSettings.defaultTargetApps.find { it.packageName == packageName }?.name
        ?: packageName
}

fun resolveTargetApps(
    selectedPackages: Set<String>?,
    enabledPackages: Set<String>?,
    resolveName: (String) -> String
): List<TargetApp> {
    val selected = selectedPackages ?: defaultTargetPackageNames()
    val enabled = enabledPackages ?: defaultEnabledPackageNames()
    val defaultOrder = AppSettings.defaultTargetApps.map { it.packageName }
    val defaultSet = defaultOrder.toSet()
    val extras = selected.filterNot { it in defaultSet }.sorted()
    val ordered = defaultOrder.filter { it in selected } + extras
    return ordered.map { packageName ->
        TargetApp(
            packageName = packageName,
            name = resolveName(packageName),
            enabled = packageName in enabled
        )
    }
}

fun addTargetAppMembership(
    selected: Set<String>,
    enabled: Set<String>,
    packageName: String
): Pair<Set<String>, Set<String>>? {
    val pkg = packageName.trim()
    if (pkg.isEmpty() || pkg in selected) return null
    return (selected + pkg) to (enabled + pkg)
}

fun removeTargetAppMembership(
    selected: Set<String>,
    enabled: Set<String>,
    packageName: String
): Pair<Set<String>, Set<String>> {
    val pkg = packageName.trim()
    return (selected - pkg) to (enabled - pkg)
}
