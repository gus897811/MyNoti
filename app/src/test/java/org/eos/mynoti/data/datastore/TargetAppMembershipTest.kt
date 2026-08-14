package org.eos.mynoti.data.datastore

import org.eos.mynoti.data.InstalledAppInfo
import org.eos.mynoti.data.filterInstalledApps
import org.eos.mynoti.domain.model.AppPackages
import org.eos.mynoti.domain.model.AppSettings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TargetAppMembershipTest {

    @Test
    fun missingKeysSeedDefaultFiveAppsEnabled() {
        val apps = resolveTargetApps(
            selectedPackages = null,
            enabledPackages = null,
            resolveName = ::fallbackTargetAppName
        )
        assertEquals(AppSettings.defaultTargetApps.map { it.packageName }, apps.map { it.packageName })
        assertTrue(apps.all { it.enabled })
        assertEquals("KakaoTalk", apps.first { it.packageName == AppPackages.KAKAOTALK }.name)
    }

    @Test
    fun existingEnabledSetIsRespectedWhenSeedingMembership() {
        val apps = resolveTargetApps(
            selectedPackages = null,
            enabledPackages = setOf(AppPackages.LEARNING_X, AppPackages.KAKAOTALK),
            resolveName = ::fallbackTargetAppName
        )
        assertEquals(5, apps.size)
        assertTrue(apps.first { it.packageName == AppPackages.LEARNING_X }.enabled)
        assertTrue(apps.first { it.packageName == AppPackages.KAKAOTALK }.enabled)
        assertFalse(apps.first { it.packageName == AppPackages.HEY_YOUNG }.enabled)
        assertFalse(apps.first { it.packageName == AppPackages.SHINHAN_CARD }.enabled)
    }

    @Test
    fun addedAppsAppearAfterSeedDefaults() {
        val selected = defaultTargetPackageNames() + "com.android.chrome"
        val enabled = defaultEnabledPackageNames() + "com.android.chrome"
        val apps = resolveTargetApps(
            selectedPackages = selected,
            enabledPackages = enabled,
            resolveName = { pkg -> if (pkg == "com.android.chrome") "Chrome" else fallbackTargetAppName(pkg) }
        )
        assertEquals(AppPackages.LEARNING_X, apps.first().packageName)
        assertEquals("com.android.chrome", apps.last().packageName)
        assertEquals("Chrome", apps.last().name)
        assertTrue(apps.last().enabled)
    }

    @Test
    fun emptySelectedSetDoesNotReseed() {
        val apps = resolveTargetApps(
            selectedPackages = emptySet(),
            enabledPackages = emptySet(),
            resolveName = ::fallbackTargetAppName
        )
        assertTrue(apps.isEmpty())
    }

    @Test
    fun addIsNoOpWhenBlankOrDuplicate() {
        val selected = defaultTargetPackageNames()
        val enabled = defaultEnabledPackageNames()
        assertNull(addTargetAppMembership(selected, enabled, "  "))
        assertNull(addTargetAppMembership(selected, enabled, AppPackages.KAKAOTALK))
    }

    @Test
    fun addEnablesImmediately() {
        val selected = defaultTargetPackageNames()
        val enabled = defaultEnabledPackageNames()
        val updated = addTargetAppMembership(selected, enabled, "com.android.chrome")
        assertEquals(selected + "com.android.chrome", updated!!.first)
        assertTrue("com.android.chrome" in updated.second)
    }

    @Test
    fun removeDropsMembershipAndEnabled() {
        val selected = defaultTargetPackageNames()
        val enabled = defaultEnabledPackageNames()
        val updated = removeTargetAppMembership(selected, enabled, AppPackages.KAKAOTALK)
        assertFalse(AppPackages.KAKAOTALK in updated.first)
        assertFalse(AppPackages.KAKAOTALK in updated.second)
        assertEquals(4, updated.first.size)
    }

    @Test
    fun removeLastAppLeavesEmptySets() {
        val updated = removeTargetAppMembership(
            selected = setOf(AppPackages.LEARNING_X),
            enabled = setOf(AppPackages.LEARNING_X),
            packageName = AppPackages.LEARNING_X
        )
        assertTrue(updated.first.isEmpty())
        assertTrue(updated.second.isEmpty())
    }

    @Test
    fun unknownPackageFallsBackToPackageName() {
        assertEquals("com.example.app", fallbackTargetAppName("com.example.app"))
    }

    @Test
    fun pickerHidesAddedAppsAndFiltersByQuery() {
        val apps = listOf(
            InstalledAppInfo(AppPackages.KAKAOTALK, "KakaoTalk"),
            InstalledAppInfo("com.android.chrome", "Chrome"),
            InstalledAppInfo("com.google.android.gm", "Gmail")
        )
        val added = setOf(AppPackages.KAKAOTALK)
        assertEquals(
            listOf("Chrome", "Gmail"),
            filterInstalledApps(apps, added, "").map { it.label }
        )
        assertEquals(
            listOf("Chrome"),
            filterInstalledApps(apps, added, "chr").map { it.label }
        )
        assertTrue(filterInstalledApps(apps, added, "zzz").isEmpty())
        assertEquals(
            listOf("Gmail"),
            filterInstalledApps(apps, added, "com.google").map { it.label }
        )
    }
}
