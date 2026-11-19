package com.voidtouch.launcher.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class LiveTest {
    @Test fun classifyRules() {
        assertEquals(LiveKind.Call, Live.classify("call", true, false, true))
        assertEquals(LiveKind.Navigation, Live.classify("navigation", true, false, false))
        assertEquals(LiveKind.Timer, Live.classify(null, true, false, true))
        assertEquals(LiveKind.Timer, Live.classify("stopwatch", true, false, false))
        assertEquals(LiveKind.Progress, Live.classify(null, true, true, false))
        assertEquals(LiveKind.Progress, Live.classify("progress", true, false, false))
    }

    @Test fun classifyIgnoresMusicAndFinished() {
        assertNull(Live.classify("transport", true, true, true))
        assertNull(Live.classify("progress", false, true, false))
        assertNull(Live.classify(null, true, false, false))
        assertNull(Live.classify("msg", true, false, false))
    }

    @Test fun percentValues() {
        assertEquals(42, Live.percent(42, 100))
        assertEquals(50, Live.percent(1, 2))
        assertEquals(100, Live.percent(150, 100))
        assertNull(Live.percent(5, 0))
    }

    @Test fun clockFormat() {
        assertEquals("00:09", Live.clock(9_000))
        assertEquals("05:07", Live.clock(307_000))
        assertEquals("1:01:01", Live.clock(3_661_000))
    }

    @Test fun valueTextCountdownElapsedAndPercent() {
        assertEquals("01:00", Live.valueText(true, 61_000, true, null, 1_000))
        assertEquals("00:00", Live.valueText(true, 1_000, true, null, 5_000))
        assertEquals("00:10", Live.valueText(true, 1_000, false, null, 11_000))
        assertEquals("30%", Live.valueText(false, 0, false, 30, 0))
        assertEquals("", Live.valueText(false, 0, false, null, 0))
    }

    @Test fun topSortsByImportanceAndCaps() {
        val kinds = listOf(LiveKind.Progress, LiveKind.Timer, LiveKind.Call, LiveKind.Progress, LiveKind.Progress, LiveKind.Navigation)
        val r = Live.top(kinds) { it }
        assertEquals(Live.MAX, r.size)
        assertEquals(listOf(LiveKind.Call, LiveKind.Navigation, LiveKind.Timer, LiveKind.Progress), r)
    }
}
