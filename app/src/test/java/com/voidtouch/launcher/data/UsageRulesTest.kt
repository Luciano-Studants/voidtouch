package com.voidtouch.launcher.data

import org.junit.Assert.assertEquals
import org.junit.Test

class UsageRulesTest {
    private val min = 60_000L

    @Test fun durationFormats() {
        assertEquals("<1 min", UsageRules.duration(30_000))
        assertEquals("35 min", UsageRules.duration(35 * min))
        assertEquals("2 h 05 min", UsageRules.duration(125 * min))
        assertEquals("1 h 00 min", UsageRules.duration(60 * min))
    }

    @Test fun topSortsLimitsAndDropsNoise() {
        val totals = mapOf("a" to 10 * min, "b" to 50 * min, "c" to 30_000L, "d" to 20 * min)
        assertEquals(listOf("b", "d", "a"), UsageRules.top(totals).map { it.first })
        assertEquals(listOf("b"), UsageRules.top(totals, limit = 1).map { it.first })
    }

    @Test fun fractionAndBar() {
        assertEquals(0.5f, UsageRules.fraction(5, 10), 0.001f)
        assertEquals(0f, UsageRules.fraction(5, 0), 0.001f)
        assertEquals(1f, UsageRules.fraction(50, 10), 0.001f)
        assertEquals("██████······", UsageRules.bar(0.5f))
        assertEquals("············", UsageRules.bar(0f))
        assertEquals("████████████", UsageRules.bar(2f))
    }

    @Test fun weekdayLabels() {
        assertEquals("dom", UsageRules.weekday(1))
        assertEquals("sáb", UsageRules.weekday(7))
        assertEquals("?", UsageRules.weekday(9))
    }

    @Test fun averageIgnoresEmptyDays() {
        assertEquals(60 * min, UsageRules.dailyAverage(listOf(0, 30 * min, 90 * min)))
        assertEquals(0, UsageRules.dailyAverage(listOf(0, 0)))
    }
}
