package com.voidtouch.launcher.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.ZoneId

class DockTest {
    private val idle = Dock.IDLE_MS

    @Test fun showsOnlyWhenEverythingAligns() {
        assertTrue(Dock.shouldShow(true, true, true, false, idle))
        assertFalse(Dock.shouldShow(false, true, true, false, idle))
        assertFalse(Dock.shouldShow(true, false, true, false, idle))
        assertFalse(Dock.shouldShow(true, true, false, false, idle))
        assertFalse(Dock.shouldShow(true, true, true, true, idle))
        assertFalse(Dock.shouldShow(true, true, true, false, idle - 1))
    }

    @Test fun shiftCyclesAndIsNeverNegative() {
        assertEquals(0, Dock.shift(0))
        assertEquals(6, Dock.shift(1))
        assertEquals(12, Dock.shift(2))
        assertEquals(0, Dock.shift(3))
        assertEquals(12, Dock.shift(-1))
    }

    @Test fun clockAndDateText() {
        val zone = ZoneId.of("UTC")
        val t = 1_795_132_800_000L + (20 * 3600 + 5 * 60) * 1000L // 2026-11-20 20:05 UTC
        assertEquals("20H05", Dock.clockText(t, ClockStyle.H, zone))
        assertEquals("20:05", Dock.clockText(t, ClockStyle.Colon, zone))
        assertTrue(Dock.dateText(t, zone).contains("de novembro"))
    }
}
