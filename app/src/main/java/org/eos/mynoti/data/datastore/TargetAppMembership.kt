package org.eos.mynoti.data.datastore

import org.eos.mynoti.domain.model.AppPackages
import org.eos.mynoti.domain.model.AppSettings
import org.eos.mynoti.domain.model.TargetApp

private const val LEGACY_LEARNING_X = "com.learningx.app"
private const val LEGACY_HEY_YOUNG = "kr.co.heyyoung.campus"
private const val LEGACY_SHINHAN_CARD = "com.shinhancard.app"

val legacyTargetAppPackages: Map<String, String> = mapOf(
    LEGACY_LEARNING_X to AppPackages.LEARNING_X,
    LEGACY_HEY_YOUNG to AppPackages.HEY_YOUNG,
    LEGACY_SHINHAN_CARD to AppPackages.SHINHAN_CARD
)

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

fun remapLegacyPackageName(packageName: String): String {
    return legacyTargetAppPackages[packageName] ?: packageName
}

fun remapLegacyPackageSet(packages: Set<String>): Set<String> {
    return packages.map(::remapLegacyPackageName).toSet()
}

fun remapLegacyPackageSetOrNull(packages: Set<String>?): Set<String>? {
    return packages?.let(::remapLegacyPackageSet)
}

fun resolveTargetApps(
    selectedPackages: Set<String>?,
    enabledPackages: Set<String>?,
    resolveName: (String) -> String
): List<TargetApp> {
    val selected = remapLegacyPackageSetOrNull(selectedPackages) ?: defaultTargetPackageNames()
    val enabled = remapLegacyPackageSetOrNull(enabledPackages) ?: defaultEnabledPackageNames()
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
