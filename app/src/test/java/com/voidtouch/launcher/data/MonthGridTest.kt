package com.voidtouch.launcher.data

import java.time.YearMonth
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MonthGridTest {
    @Test fun novembro_de_2026_comeca_no_domingo_e_tem_5_semanas() {
        val weeks = MonthGrid.weeks(YearMonth.of(2026, 11))
        assertEquals(5, weeks.size)
        assertEquals(1, weeks.first().first())
        assertEquals(30, weeks.last()[1]) // 30/11/2026 é segunda
        assertNull(weeks.last().last())
    }

    @Test fun fevereiro_de_2026_cabe_em_4_semanas() =
        assertEquals(4, MonthGrid.weeks(YearMonth.of(2026, 2)).size)

    @Test fun janeiro_de_2027_comeca_na_sexta_e_tem_6_semanas() {
        val weeks = MonthGrid.weeks(YearMonth.of(2027, 1))
        assertEquals(6, weeks.size)
        assertEquals(listOf(null, null, null, null, null, 1, 2), weeks.first())
    }

    @Test fun toda_semana_tem_7_celulas_e_todos_os_dias_aparecem() {
        for (m in 1..12) {
            val ym = YearMonth.of(2028, m)
            val weeks = MonthGrid.weeks(ym)
            assertEquals(true, weeks.all { it.size == 7 })
            assertEquals(ym.lengthOfMonth(), weeks.flatten().filterNotNull().size)
        }
    }
}
