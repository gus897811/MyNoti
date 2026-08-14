package org.eos.mynoti.ui.navigation

object Routes {
    const val HOME = "home"
    const val SUMMARY = "summary"
    const val CALENDAR = "calendar"
    const val SETTINGS = "settings"
    const val NOTIFICATION_DETAIL = "notification/{notificationId}"

    fun notificationDetail(id: Long): String = "notification/$id"
}

val topLevelRoutes = setOf(Routes.HOME, Routes.SUMMARY, Routes.CALENDAR, Routes.SETTINGS)
