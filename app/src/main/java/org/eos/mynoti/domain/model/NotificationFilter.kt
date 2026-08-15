package org.eos.mynoti.domain.model

data class NotificationFilter(
    val selectedApps: Set<String> = emptySet(),
    val selectedTypes: Set<NotificationType> = emptySet(),
    val importantOnly: Boolean = false,
    val query: String = ""
) {
    val isActive: Boolean
        get() = selectedApps.isNotEmpty() ||
            selectedTypes.isNotEmpty() ||
            importantOnly ||
            query.isNotBlank()

    val isAllApps: Boolean
        get() = selectedApps.isEmpty()

    fun pruneSelectedApps(validPackages: Set<String>): NotificationFilter {
        return copy(selectedApps = selectedApps.intersect(validPackages))
    }
}

fun Notification.matches(filter: NotificationFilter): Boolean {
    val appMatches = filter.selectedApps.isEmpty() || appPackageName in filter.selectedApps
    val typeMatches = filter.selectedTypes.isEmpty() || type in filter.selectedTypes
    val importantMatches = !filter.importantOnly || isImportant
    val trimmedQuery = filter.query.trim()
    val queryMatches = trimmedQuery.isEmpty() ||
        searchableText().contains(trimmedQuery, ignoreCase = true) ||
        appName.contains(trimmedQuery, ignoreCase = true)
    return appMatches && typeMatches && importantMatches && queryMatches
}

fun List<Notification>.applyFilter(filter: NotificationFilter): List<Notification> {
    return filter { it.matches(filter) }
}
