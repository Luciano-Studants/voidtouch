package com.voidtouch.launcher.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class UniversalSearchTest {
    @Test fun calcBasics() {
        assertEquals("14", Calc.answer("2+3*4"))
        assertEquals("20", Calc.answer("(2+3)*4"))
        assertEquals("1024", Calc.answer("2^10"))
        assertEquals("2,5", Calc.answer("5/2"))
        assertEquals("6", Calc.answer("2 x 3"))
        assertEquals("-3", Calc.answer("-1-2"))
    }

    @Test fun calcPercent() {
        assertEquals("34,5", Calc.answer("15% de 230"))
        assertEquals("0,5", Calc.answer("50%"))
    }

    @Test fun calcRejectsNonMath() {
        assertNull(Calc.answer("2048"))
        assertNull(Calc.answer("whatsapp"))
        assertNull(Calc.answer("1/0"))
        assertNull(Calc.answer("2+"))
        assertNull(Calc.answer("3 apps"))
    }

    @Test fun unitConversions() {
        assertEquals("10 km = 6,21 mi", Units.answer("10 km em mi"))
        assertEquals("100 °F = 37,78 °C", Units.answer("100 f para c"))
        assertEquals("2 kg = 4,41 lb", Units.answer("2 kg to lb"))
    }

    @Test fun unitsRejectMixedDimensionsAndUnknown() {
        assertNull(Units.answer("10 km em kg"))
        assertNull(Units.answer("10 foo em bar"))
    }

    @Test fun hitsFindAnswersActionsAndSettings() {
        assertEquals("14", (UniversalSearch.hits("2+3*4").first() as Hit.Answer).title)
        assertTrue(UniversalSearch.hits("lanter").any { it is Hit.Action && it.action == QuickAction.Torch })
        assertTrue(UniversalSearch.hits("wifi").any { it is Hit.Setting })
        assertTrue(UniversalSearch.hits("blue").any { it is Hit.Setting && it.title == "bluetooth" })
    }

    @Test fun hitsAreAccentInsensitiveAndBounded() {
        assertTrue(UniversalSearch.hits("localiza").any { it is Hit.Setting })
        assertTrue(UniversalSearch.hits("a").isEmpty())
        assertTrue(UniversalSearch.hits("s").size <= 4)
    }

    @Test fun launchCountsKeepTopAndRoundTrip() {
        var c = emptyMap<String, Int>()
        repeat(3) { c = LaunchCounts.increment(c, "a") }
        c = LaunchCounts.increment(c, "b")
        assertEquals(3, c["a"])
        assertEquals(c, LaunchCounts.decode(LaunchCounts.encode(c)))
        (1..150).forEach { c = LaunchCounts.increment(c, "k$it") }
        assertEquals(LaunchCounts.KEEP, c.size)
        assertTrue("a" in c)
    }

    @Test fun searchBoostsMostOpenedInsideGroup() {
        val a = AppEntry("Camera Um", "p.a", "M")
        val b = AppEntry("Camera Dois", "p.b", "M")
        val r = Search.filter(listOf(a, b), "camera", mapOf(b.key to 5))
        assertEquals(listOf(b, a), r)
        assertEquals(listOf(a, b), Search.filter(listOf(a, b), "camera"))
    }
}
