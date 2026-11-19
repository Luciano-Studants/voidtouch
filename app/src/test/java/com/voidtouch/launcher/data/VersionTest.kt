package com.voidtouch.launcher.data

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class VersionTest {
    @Test
    fun compareNumerically_notAsText() {
        assertTrue(Version.isNewer("0.10.0", "0.9.0"))
        assertFalse(Version.isNewer("0.9.0", "0.10.0"))
    }

    @Test
    fun equalVersionsAreNotNewer() {
        assertFalse(Version.isNewer("0.9.0", "0.9.0"))
        assertFalse(Version.isNewer("0.9", "0.9.0"))
    }

    @Test
    fun majorBeatsMinor() {
        assertTrue(Version.isNewer("1.0", "0.99.9"))
    }
}
