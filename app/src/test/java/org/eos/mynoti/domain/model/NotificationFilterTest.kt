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
    fun pruneSelectedAppsDropsPackagesNotInTargetList() {
        val filter = NotificationFilter(
            selectedApps = setOf(AppPackages.LEARNING_X, AppPackages.KAKAOTALK),
            importantOnly = true,
            query = "과제"
        )
        val pruned = filter.pruneSelectedApps(setOf(AppPackages.LEARNING_X))
        assertEquals(setOf(AppPackages.LEARNING_X), pruned.selectedApps)
        assertTrue(pruned.importantOnly)
        assertEquals("과제", pruned.query)
    }

    @Test
    fun pruneSelectedAppsToEmptyMeansAllApps() {
        val pruned = NotificationFilter(selectedApps = setOf(AppPackages.KAKAOTALK))
            .pruneSelectedApps(emptySet())
        assertTrue(pruned.selectedApps.isEmpty())
        assertTrue(pruned.isAllApps)
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

    @Test
    fun emptyQueryShowsAll() {
        val result = notifications.applyFilter(NotificationFilter(query = ""))
        assertEquals(notifications.size, result.size)
    }

    @Test
    fun titleOnlyQueryMatchesThatNotification() {
        val result = notifications.applyFilter(NotificationFilter(query = "수아"))
        assertEquals(1, result.size)
        assertEquals("수아", result.single().title)
    }

    @Test
    fun summaryOnlyQueryMatchesThatNotification() {
        val result = notifications.applyFilter(NotificationFilter(query = "50만원"))
        assertEquals(1, result.size)
        assertEquals(AppPackages.KAKAOBANK, result.single().appPackageName)
    }

    @Test
    fun queryIgnoresCase() {
        val result = notifications.applyFilter(NotificationFilter(query = "data structures"))
        assertTrue(result.isNotEmpty())
        assertTrue(result.all { it.searchableText().contains("Data Structures") })
    }

    @Test
    fun queryTrimMatchesUntrimmed() {
        val trimmed = notifications.applyFilter(NotificationFilter(query = "과제"))
        val padded = notifications.applyFilter(NotificationFilter(query = "  과제  "))
        assertEquals(trimmed.map { it.id }, padded.map { it.id })
        assertTrue(trimmed.isNotEmpty())
    }

    @Test
    fun unmatchedQueryReturnsEmpty() {
        val result = notifications.applyFilter(
            NotificationFilter(query = "LearningX에없는문자열XYZ")
        )
        assertTrue(result.isEmpty())
    }

    @Test
    fun selectedAppsAndQueryAreAnd() {
        val result = notifications.applyFilter(
            NotificationFilter(
                selectedApps = setOf(AppPackages.LEARNING_X),
                query = "운영체제"
            )
        )
        assertTrue(result.isNotEmpty())
        assertTrue(
            result.all {
                it.appPackageName == AppPackages.LEARNING_X &&
                    it.searchableText().contains("운영체제")
            }
        )
    }

    @Test
    fun importantOnlyAndQueryAreAnd() {
        val result = notifications.applyFilter(
            NotificationFilter(importantOnly = true, query = "과제")
        )
        assertTrue(result.isNotEmpty())
        assertTrue(result.all { it.isImportant })
        assertTrue(result.all { it.searchableText().contains("과제") })
    }

    @Test
    fun queryMakesFilterActive() {
        assertTrue(NotificationFilter(query = "과제").isActive)
    }

    @Test
    fun blankQueryIsNotActiveWithoutOtherFilters() {
        assertTrue(!NotificationFilter(query = "   ").isActive)
        assertTrue(!NotificationFilter().isActive)
    }

    @Test
    fun defaultFilterHasNoQuery() {
        assertTrue(NotificationFilter().query.isEmpty())
        val cleared = notifications.applyFilter(NotificationFilter())
        assertEquals(notifications.size, cleared.size)
    }
}
