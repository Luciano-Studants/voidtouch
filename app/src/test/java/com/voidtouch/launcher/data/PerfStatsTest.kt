package com.voidtouch.launcher.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PerfStatsTest {
    @Test fun summaryOfSamples() {
        val p = PerfStats()
        p.record("a", 10); p.record("a", 30); p.record("a", 20)
        val s = p.summary("a")!!
        assertEquals(20L, s.last)
        assertEquals(20L, s.average)
        assertEquals(30L, s.worst)
        assertEquals(3, s.count)
    }

    @Test fun unknownIsNull() {
        val p = PerfStats()
        assertNull(p.summary("x"))
        assertFalse(p.has("x"))
    }

    @Test fun keepsOnlyRecentSamples() {
        val p = PerfStats(keep = 3)
        (1..10).forEach { p.record("a", it.toLong()) }
        val s = p.summary("a")!!
        assertEquals(3, s.count)
        assertEquals(10L, s.last)
        assertTrue(s.worst == 10L)
    }

    @Test fun negativeBecomesZero() {
        val p = PerfStats()
        p.record("a", -5)
        assertEquals(0L, p.summary("a")!!.last)
    }
}
