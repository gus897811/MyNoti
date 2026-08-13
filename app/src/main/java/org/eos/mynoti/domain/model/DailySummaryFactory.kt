package org.eos.mynoti.domain.model

object DailySummaryFactory {
    fun create(
        notifications: List<Notification>,
        settings: AppSettings
    ): DailySummary {
        val visible = notifications.applyAppSettings(settings)
        val important = visible.filter { it.isEffectivelyImportant(settings.highlightKeywords) }
        val assignments = visible.filter { it.type == NotificationType.ASSIGNMENT }
        val upcoming = visible.filter { it.remindAt != null && !it.isReminded }

        val urgentItems = buildUrgentItems(visible)
        val mostUrgent = urgentItems.firstOrNull()

        return DailySummary(
            importantCount = important.size,
            assignmentCount = assignments.size,
            upcomingEventCount = upcoming.size,
            mostUrgentTask = mostUrgent?.let { "${it.title} · ${it.dueLabel}" },
            insight = buildInsight(mostUrgent),
            urgentItems = urgentItems
        )
    }

    private fun buildUrgentItems(notifications: List<Notification>): List<SummaryTask> {
        return notifications
            .filter { it.actions.isNotEmpty() || it.isImportant }
            .sortedWith(
                compareBy<Notification> { it.remindAt == null }
                    .thenBy { it.remindAt }
                    .thenByDescending { it.isImportant }
            )
            .take(3)
            .map { notification ->
                SummaryTask(
                    title = notification.actions.firstOrNull()?.title ?: notification.title,
                    dueLabel = notification.summary ?: notification.displaySummary(),
                    notificationId = notification.id
                )
            }
    }

    private fun buildInsight(mostUrgent: SummaryTask?): String {
        return if (mostUrgent != null) {
            "오늘은 ${mostUrgent.title}의 마감이 가장 가까우므로 먼저 처리하는 것을 추천합니다."
        } else {
            "오늘은 급한 마감이 없습니다. 밀린 알림만 가볍게 확인해 보세요."
        }
    }
}
