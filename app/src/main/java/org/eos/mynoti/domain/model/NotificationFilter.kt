package org.eos.mynoti.domain.model

data class NotificationFilter(
    val selectedApps: Set<String> = emptySet(),
    val selectedTypes: Set<NotificationType> = emptySet(),
    val importantOnly: Boolean = false
) {
    val isActive: Boolean
        get() = selectedApps.isNotEmpty() || selectedTypes.isNotEmpty() || importantOnly

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
    return appMatches && typeMatches && importantMatches
}

fun List<Notification>.applyFilter(filter: NotificationFilter): List<Notification> {
    return filter { it.matches(filter) }
}
