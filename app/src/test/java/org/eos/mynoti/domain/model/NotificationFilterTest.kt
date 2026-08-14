package org.eos.mynoti.domain.model

import org.eos.mynoti.data.mock.MockNotificationData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDateTime

class NotificationFilterTest {

    private val notifications = MockNotificationData.create(LocalDateTime.of(2026, 8, 13, 18, 0))

    @Test
    fun noFilterShowsAll() {
        val result = notifications.applyFilter(NotificationFilter())
        assertEquals(notifications.size, result.size)
    }

    @Test
    fun learningXOnly() {
        val result = notifications.applyFilter(
            NotificationFilter(selectedApps = setOf(AppPackages.LEARNING_X))
        )
        assertTrue(result.isNotEmpty())
        assertTrue(result.all { it.appPackageName == AppPackages.LEARNING_X })
    }

    @Test
    fun assignmentFromAnyApp() {
        val result = notifications.applyFilter(
            NotificationFilter(selectedTypes = setOf(NotificationType.ASSIGNMENT))
        )
        assertTrue(result.isNotEmpty())
        assertTrue(result.all { it.type == NotificationType.ASSIGNMENT })
        assertTrue(result.any { it.appPackageName == AppPackages.LEARNING_X })
        assertTrue(result.any { it.appPackageName == AppPackages.HEY_YOUNG })
    }

    @Test
    fun learningXAndAssignment() {
        val result = notifications.applyFilter(
            NotificationFilter(
                selectedApps = setOf(AppPackages.LEARNING_X),
                selectedTypes = setOf(NotificationType.ASSIGNMENT)
            )
        )
        assertTrue(result.isNotEmpty())
        assertTrue(
            result.all {
                it.appPackageName == AppPackages.LEARNING_X && it.type == NotificationType.ASSIGNMENT
            }
        )
    }

    @Test
    fun learningXOrHeyYoung() {
        val result = notifications.applyFilter(
            NotificationFilter(
                selectedApps = setOf(AppPackages.LEARNING_X, AppPackages.HEY_YOUNG)
            )
        )
        assertTrue(result.isNotEmpty())
        assertTrue(
            result.all {
                it.appPackageName == AppPackages.LEARNING_X ||
                    it.appPackageName == AppPackages.HEY_YOUNG
            }
        )
        assertTrue(result.any { it.appPackageName == AppPackages.LEARNING_X })
        assertTrue(result.any { it.appPackageName == AppPackages.HEY_YOUNG })
    }

    @Test
    fun assignmentOrClass() {
        val result = notifications.applyFilter(
            NotificationFilter(
                selectedTypes = setOf(NotificationType.ASSIGNMENT, NotificationType.CLASS)
            )
        )
        assertTrue(result.isNotEmpty())
        assertTrue(
            result.all {
                it.type == NotificationType.ASSIGNMENT || it.type == NotificationType.CLASS
            }
        )
    }

    @Test
    fun appsAndAssignmentAndImportant() {
        val result = notifications.applyFilter(
            NotificationFilter(
                selectedApps = setOf(AppPackages.LEARNING_X, AppPackages.HEY_YOUNG),
                selectedTypes = setOf(NotificationType.ASSIGNMENT),
                importantOnly = true
            )
        )
        assertTrue(result.isNotEmpty())
        assertTrue(
            result.all {
                (it.appPackageName == AppPackages.LEARNING_X ||
                    it.appPackageName == AppPackages.HEY_YOUNG) &&
                    it.type == NotificationType.ASSIGNMENT &&
                    it.isImportant
            }
        )
    }

    @Test
    fun clearFilterReturnsAll() {
        val filtered = notifications.applyFilter(
            NotificationFilter(
                selectedApps = setOf(AppPackages.KAKAOTALK),
                selectedTypes = setOf(NotificationType.COMMUNICATION),
                importantOnly = true
            )
        )
        assertTrue(filtered.size < notifications.size)
        val cleared = notifications.applyFilter(NotificationFilter())
        assertEquals(notifications.size, cleared.size)
    }

    @Test
    fun mockDataCoversRequiredCombinations() {
        assertTrue(
            notifications.any {
                it.appPackageName == AppPackages.LEARNING_X && it.type == NotificationType.ASSIGNMENT
            }
        )
        assertTrue(
            notifications.any {
                it.appPackageName == AppPackages.LEARNING_X && it.type == NotificationType.CLASS
            }
        )
        assertTrue(
            notifications.any {
                it.appPackageName == AppPackages.HEY_YOUNG && it.type == NotificationType.ASSIGNMENT
            }
        )
        assertTrue(
            notifications.any {
                it.appPackageName == AppPackages.HEY_YOUNG && it.type == NotificationType.FINANCIAL
            }
        )
        assertTrue(
            notifications.any {
                it.appPackageName == AppPackages.HEY_YOUNG && it.type == NotificationType.COMMUNICATION
            }
        )
        assertTrue(
            notifications.any {
                it.appPackageName == AppPackages.KAKAOTALK && it.type == NotificationType.COMMUNICATION
            }
        )
        assertTrue(
            notifications.any {
                it.appPackageName in setOf(AppPackages.SHINHAN_CARD, AppPackages.KAKAOBANK) &&
                    it.type == NotificationType.FINANCIAL
            }
        )
        assertTrue(notifications.any { it.isImportant })
        assertTrue(notifications.any { !it.isImportant })
    }
}
