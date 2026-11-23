package com.voidtouch.launcher.data

import java.time.YearMonth

/** Grade do mês para a página da agenda quando não há compromissos. Semanas de domingo a sábado. */
object MonthGrid {
    val WEEKDAYS = listOf("D", "S", "T", "Q", "Q", "S", "S")

    /** Dias do mês em semanas de 7 células; `null` é uma célula vazia (antes do dia 1 ou depois do último). */
    fun weeks(month: YearMonth): List<List<Int?>> {
        val lead = month.atDay(1).dayOfWeek.value % 7 // domingo = 0
        return buildList<Int?> {
            repeat(lead) { add(null) }
            for (day in 1..month.lengthOfMonth()) add(day)
            while (size % 7 != 0) add(null)
        }.chunked(7)
    }
}
