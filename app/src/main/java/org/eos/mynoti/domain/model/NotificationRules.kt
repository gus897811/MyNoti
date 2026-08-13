package org.eos.mynoti.domain.model

fun Notification.searchableText(): String {
    return listOf(title, content, summary.orEmpty()).joinToString(" ")
}

fun Notification.matchesAnyKeyword(keywords: List<String>): Boolean {
    if (keywords.isEmpty()) return false
    val haystack = searchableText()
    return keywords.any { keyword ->
        keyword.isNotBlank() && haystack.contains(keyword.trim(), ignoreCase = true)
    }
}

fun Notification.isEffectivelyImportant(highlightKeywords: List<String>): Boolean {
    return isImportant || matchesAnyKeyword(highlightKeywords)
}

fun List<Notification>.applyAppSettings(settings: AppSettings): List<Notification> {
    return filter { it.appPackageName in settings.enabledPackageNames }
        .filterNot { it.matchesAnyKeyword(settings.muteKeywords) }
}
