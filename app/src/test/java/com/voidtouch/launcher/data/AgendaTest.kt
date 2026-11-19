package com.voidtouch.launcher.data

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.ZoneId
import java.time.ZonedDateTime

class AgendaTest {
    private val zone = ZoneId.of("UTC")
    private fun ms(day: Int, hour: Int, min: Int = 0) =
        ZonedDateTime.of(2026, 11, day, hour, min, 0, 0, zone).toInstant().toEpochMilli()

    private val now = ms(19, 10)

    @Test
    fun todayLabel() {
        val item = AgendaItem("x", ms(19, 14, 30), ms(19, 15, 30), false)
        assertEquals("hoje 14:30", Agenda.label(item, now, zone))
    }

    @Test
    fun tomorrowLabel() {
        val item = AgendaItem("x", ms(20, 9), ms(20, 10), false)
        assertEquals("amanhã 09:00", Agenda.label(item, now, zone))
    }

    @Test
    fun runningEventLabel() {
        val item = AgendaItem("x", ms(19, 9), ms(19, 11), false)
        assertEquals("agora até 11:00", Agenda.label(item, now, zone))
    }

    @Test
    fun allDayLabel() {
        val item = AgendaItem("x", ms(19, 0), ms(20, 0), true)
        assertEquals("hoje · dia todo", Agenda.label(item, now, zone))
    }
}
