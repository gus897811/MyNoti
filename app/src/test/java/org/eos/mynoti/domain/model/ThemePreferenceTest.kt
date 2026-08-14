package org.eos.mynoti.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ThemePreferenceTest {

    @Test
    fun missingValueDefaultsToSystem() {
        assertEquals(ThemePreference.SYSTEM, parseThemePreference(null))
    }

    @Test
    fun validNameParses() {
        assertEquals(ThemePreference.DARK, parseThemePreference("DARK"))
        assertEquals(ThemePreference.LIGHT, parseThemePreference("LIGHT"))
        assertEquals(ThemePreference.SYSTEM, parseThemePreference("SYSTEM"))
    }

    @Test
    fun invalidNameDefaultsToSystem() {
        assertEquals(ThemePreference.SYSTEM, parseThemePreference("nope"))
        assertEquals(ThemePreference.SYSTEM, parseThemePreference(""))
    }

    @Test
    fun lightIgnoresSystemDark() {
        assertFalse(ThemePreference.LIGHT.isDark(systemInDark = true))
        assertFalse(ThemePreference.LIGHT.isDark(systemInDark = false))
    }

    @Test
    fun darkIgnoresSystemLight() {
        assertTrue(ThemePreference.DARK.isDark(systemInDark = false))
        assertTrue(ThemePreference.DARK.isDark(systemInDark = true))
    }

    @Test
    fun systemFollowsOs() {
        assertTrue(ThemePreference.SYSTEM.isDark(systemInDark = true))
        assertFalse(ThemePreference.SYSTEM.isDark(systemInDark = false))
    }
}
