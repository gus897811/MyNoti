package org.eos.mynoti.domain.model

data class DailySummary(
    val importantCount: Int,
    val assignmentCount: Int,
    val upcomingEventCount: Int,
    val mostUrgentTask: String?,
    val insight: String?,
    val urgentItems: List<SummaryTask> = emptyList()
)

data class SummaryTask(
    val title: String,
    val dueLabel: String,
    val notificationId: Long
)
