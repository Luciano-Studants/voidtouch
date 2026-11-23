package com.voidtouch.launcher.data

import java.time.Instant
import java.time.YearMonth
import java.time.ZoneId
import java.time.ZoneOffset

/** Dias de um mês que têm compromisso, para marcar um ponto na grade do mês. */
object EventDays {
    fun days(items: List<AgendaItem>, month: YearMonth, zone: ZoneId): Set<Int> {
        val out = HashSet<Int>()
        for (item in items) {
            // Eventos de dia inteiro vêm em UTC; os outros, no fuso do aparelho.
            val z = if (item.allDay) ZoneOffset.UTC else zone
            val first = Instant.ofEpochMilli(item.begin).atZone(z).toLocalDate()
            // O fim é exclusivo: um evento até 00:00 não ocupa o dia seguinte.
            val last = Instant.ofEpochMilli(maxOf(item.begin, item.end - 1)).atZone(z).toLocalDate()
            var d = first
            while (!d.isAfter(last)) {
                if (YearMonth.from(d) == month) out += d.dayOfMonth
                d = d.plusDays(1)
            }
        }
        return out
    }
}
