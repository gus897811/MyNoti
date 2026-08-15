package org.eos.mynoti.ui.home

import org.eos.mynoti.domain.model.NotificationType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HomeFilterPresetTest {

    @Test
    fun importantPresetSelectsOnlyImportant() {
        val filter = HomeFilterPreset.IMPORTANT.toFilter()
        assertTrue(filter.importantOnly)
        assertTrue(filter.selectedTypes.isEmpty())
        assertTrue(filter.selectedApps.isEmpty())
        assertTrue(filter.query.isBlank())
        assertTrue(filter.isActive)
    }

    @Test
    fun assignmentPresetSelectsOnlyAssignmentType() {
        val filter = HomeFilterPreset.ASSIGNMENT.toFilter()
        assertEquals(setOf(NotificationType.ASSIGNMENT), filter.selectedTypes)
        assertTrue(!filter.importantOnly)
        assertTrue(filter.selectedApps.isEmpty())
        assertTrue(filter.query.isBlank())
        assertTrue(filter.isActive)
    }
}
