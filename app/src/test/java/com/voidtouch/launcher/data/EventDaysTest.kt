package com.voidtouch.launcher.data

import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.ZoneOffset
import org.junit.Assert.assertEquals
import org.junit.Test

class EventDaysTest {
    private val zone = ZoneId.of("America/Belem")
    private val nov = YearMonth.of(2026, 11)

    private fun ms(y: Int, m: Int, d: Int, h: Int = 0) =
        LocalDate.of(y, m, d).atTime(h, 0).atZone(zone).toInstant().toEpochMilli()

    @Test fun singleEvent() {
        val e = AgendaItem("x", ms(2026, 11, 25, 10), ms(2026, 11, 25, 11), false)
        assertEquals(setOf(25), EventDays.days(listOf(e), nov, zone))
    }

    @Test fun multiDayEvent() {
        val e = AgendaItem("x", ms(2026, 11, 10, 20), ms(2026, 11, 12, 8), false)
        assertEquals(setOf(10, 11, 12), EventDays.days(listOf(e), nov, zone))
    }

    @Test fun endAtMidnightDoesNotSpill() {
        val e = AgendaItem("x", ms(2026, 11, 10, 20), ms(2026, 11, 11, 0), false)
        assertEquals(setOf(10), EventDays.days(listOf(e), nov, zone))
    }

    @Test fun otherMonthIgnoredAndClipped() {
        val e = AgendaItem("x", ms(2026, 10, 30, 10), ms(2026, 11, 2, 10), false)
        assertEquals(setOf(1, 2), EventDays.days(listOf(e), nov, zone))
        assertEquals(setOf(30, 31), EventDays.days(listOf(e), YearMonth.of(2026, 10), zone))
    }

    @Test fun allDayUsesUtc() {
        val b = LocalDate.of(2026, 11, 20).atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli()
        val e = AgendaItem("x", b, b + 24L * 3600_000L, true)
        assertEquals(setOf(20), EventDays.days(listOf(e), nov, zone))
    }
}
