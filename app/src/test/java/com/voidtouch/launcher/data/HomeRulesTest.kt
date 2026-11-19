package com.voidtouch.launcher.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HomeRulesTest {
    private val a = AppEntry("A", "pkg.a", "Main")
    private val b = AppEntry("B", "pkg.b", "Main")
    private val folder = Folder("f1", "Pasta", listOf(a.key, b.key))

    @Test fun pinAddsRemovesAndRespectsLimit() {
        val (added, r1) = HomeRules.togglePin(emptyList(), "x", 2)
        assertEquals(HomeRules.PinResult.Added, r1)
        val (removed, r2) = HomeRules.togglePin(added, "x", 2)
        assertEquals(HomeRules.PinResult.Removed, r2)
        assertTrue(removed.isEmpty())
        val (same, r3) = HomeRules.togglePin(listOf("1", "2"), "3", 2)
        assertEquals(HomeRules.PinResult.Full, r3)
        assertEquals(listOf("1", "2"), same)
    }

    @Test fun favoritesDropMissingApps() {
        val fav = HomeRules.favorites(listOf(a), listOf(a.key, "gone/x"), emptyList(), emptySet(), false)
        assertEquals(1, fav.size)
    }

    @Test fun favoritesHideFocusApps() {
        val fav = HomeRules.favorites(listOf(a, b), listOf(a.key, b.key), emptyList(), setOf(a.key), true)
        assertEquals(listOf<Favorite>(Favorite.App(b)), fav)
    }

    @Test fun folderFavoriteCountsVisibleAppsOnly() {
        val key = FavoriteKeys.folder(folder.id)
        val all = HomeRules.favorites(listOf(a, b), listOf(key), listOf(folder), emptySet(), false)
        assertEquals(2, (all[0] as Favorite.FolderItem).count)
        val focused = HomeRules.favorites(listOf(a, b), listOf(key), listOf(folder), setOf(a.key), true)
        assertEquals(1, (focused[0] as Favorite.FolderItem).count)
        val none = HomeRules.favorites(listOf(a, b), listOf(key), listOf(folder), setOf(a.key, b.key), true)
        assertTrue(none.isEmpty())
    }

    @Test fun deletedFolderFavoriteDisappears() {
        val fav = HomeRules.favorites(listOf(a), listOf(FavoriteKeys.folder("zzz")), emptyList(), emptySet(), false)
        assertTrue(fav.isEmpty())
    }

    @Test fun notificationsHiddenOnlyInFocus() {
        assertTrue(HomeRules.notificationHidden("pkg.a", setOf(a.key), true))
        assertFalse(HomeRules.notificationHidden("pkg.a", setOf(a.key), false))
        assertFalse(HomeRules.notificationHidden("pkg.b", setOf(a.key), true))
    }

    @Test fun toggleFocusRules() {
        assertEquals(HomeRules.FocusToggle.TurnedOff, HomeRules.toggleFocus(5L, emptySet()))
        assertEquals(HomeRules.FocusToggle.NeedsApps, HomeRules.toggleFocus(0L, emptySet()))
        assertEquals(HomeRules.FocusToggle.TurnedOn, HomeRules.toggleFocus(0L, setOf("x")))
    }
}
