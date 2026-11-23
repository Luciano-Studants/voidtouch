package com.voidtouch.launcher.data

import org.junit.Assert.assertEquals
import org.junit.Test

class DailyOpensTest {
    @Test fun countsWithinDay() {
        var s = DailyOpens.empty("d1")
        s = DailyOpens.increment(s, "d1", "a")
        s = DailyOpens.increment(s, "d1", "a")
        assertEquals(2, DailyOpens.count(s, "d1", "a"))
        assertEquals(0, DailyOpens.count(s, "d1", "b"))
    }

    @Test fun resetsOnNewDay() {
        val s = DailyOpens.increment(DailyOpens.empty("d1"), "d1", "a")
        assertEquals(0, DailyOpens.count(s, "d2", "a"))
        assertEquals(1, DailyOpens.increment(s, "d2", "a").counts["a"])
    }

    @Test fun encodeDecodeRoundTrip() {
        val s = DailyOpens.increment(DailyOpens.empty("d1"), "d1", "a")
        assertEquals(s, DailyOpens.decode(DailyOpens.encode(s), "d1"))
    }

    @Test fun badTextGivesEmpty() {
        assertEquals(DailyOpens.empty("d9"), DailyOpens.decode("lixo", "d9"))
    }

    @Test fun phrases() {
        assertEquals("você ainda não abriu hoje", DailyOpens.phrase(0))
        assertEquals("você abriu 1 vez hoje", DailyOpens.phrase(1))
        assertEquals("você abriu 4 vezes hoje", DailyOpens.phrase(4))
    }
}
