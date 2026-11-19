package com.voidtouch.launcher.data

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UpdateCheckRulesTest {
    @Test fun sizeMustMatchAnnounced() {
        assertTrue(UpdateCheckRules.sizeOk(1000, 1000))
        assertFalse(UpdateCheckRules.sizeOk(900, 1000))
    }

    @Test fun unknownAnnouncedSizeNeedsSomeBytes() {
        assertTrue(UpdateCheckRules.sizeOk(10, -1))
        assertFalse(UpdateCheckRules.sizeOk(0, -1))
    }

    @Test fun versionMatchIgnoresPrefix() {
        assertTrue(UpdateCheckRules.versionMatches("0.21.0", "v0.21.0"))
        assertFalse(UpdateCheckRules.versionMatches("0.20.0", "0.21.0"))
        assertFalse(UpdateCheckRules.versionMatches(null, "0.21.0"))
    }
}
