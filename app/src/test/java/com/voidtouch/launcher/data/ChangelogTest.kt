package com.voidtouch.launcher.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ChangelogTest {
    @Test
    fun versionsAreStrictlyDescending() {
        val all = Changelog.all
        for (i in 0 until all.size - 1) {
            assertTrue(
                "${all[i].version} deveria ser mais nova que ${all[i + 1].version}",
                Version.isNewer(all[i].version, all[i + 1].version),
            )
        }
    }

    @Test
    fun everyReleaseHasNotes() {
        Changelog.all.forEach { note ->
            assertTrue("v${note.version} sem itens", note.items.isNotEmpty())
            assertTrue(note.items.all { it.isNotBlank() })
        }
    }

    @Test
    fun forVersion_findsEntry() {
        val first = Changelog.all.first()
        assertEquals(first, Changelog.forVersion(first.version))
    }
}
