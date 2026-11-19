package com.voidtouch.launcher.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ScenesTest {
    private val work = Scene("w", "trabalho", 9, 18, focus = true)
    private val night = Scene("n", "noite", 18, 0, quiet = true)

    @Test fun pickByHourIncludingWrap() {
        val list = listOf(work, night)
        assertEquals("w", Scenes.pick(list, 10)?.id)
        assertEquals("n", Scenes.pick(list, 23)?.id)
        assertNull(Scenes.pick(list, 3))
    }

    @Test fun createUsesUnusedPresetThenNumbered() {
        val first = Scenes.create(emptyList(), "a")!!
        assertEquals("manhã", first.name)
        val many = Scenes.PRESETS.mapIndexed { i, p -> Scene("$i", p.first, p.second, p.third) }
        assertEquals("cena 5", Scenes.create(many, "z")!!.name)
    }

    @Test fun createStopsAtLimit() {
        val full = (1..Scenes.MAX).map { Scene("$it", "c$it", 1, 2) }
        assertNull(Scenes.create(full, "x"))
    }

    @Test fun switchSavesLeavingPinsAndLoadsTarget() {
        val a = Scene("a", "a", 1, 2, pins = listOf("x"))
        val b = Scene("b", "b", 3, 4)
        val r = Scenes.switch(listOf(a, b), null, "a", "b", listOf("cur"))
        assertEquals(listOf("cur"), r.scenes.first { it.id == "a" }.pins)
        assertEquals(listOf("cur"), r.pins) // b não tinha favoritos: mantém os de agora
        val back = Scenes.switch(r.scenes, r.defaultPins, "b", "a", listOf("novos"))
        assertEquals(listOf("cur"), back.pins) // a lembra os dela
    }

    @Test fun leavingToNoSceneRestoresDefault() {
        val a = Scene("a", "a", 1, 2, pins = listOf("x"))
        val toA = Scenes.switch(listOf(a), null, null, "a", listOf("base"))
        assertEquals(listOf("x"), toA.pins)
        assertEquals(listOf("base"), toA.defaultPins)
        val out = Scenes.switch(toA.scenes, toA.defaultPins, "a", null, listOf("x2"))
        assertEquals(listOf("base"), out.pins)
        assertEquals(listOf("x2"), out.scenes[0].pins)
    }

    @Test fun sameSceneIsNoop() {
        val r = Scenes.switch(listOf(work), null, "w", "w", listOf("p"))
        assertEquals(listOf("p"), r.pins)
        assertNull(r.scenes[0].pins)
    }

    @Test fun encodeDecodeRoundTrip() {
        val list = listOf(work.copy(pins = listOf("a/b")), night)
        assertEquals(list, Scenes.decode(Scenes.encode(list)))
    }

    @Test fun decodeGarbageIsEmptyAndClamps() {
        assertTrue(Scenes.decode("não é json").isEmpty())
        val s = Scenes.decode("""[{"id":"1","name":"x","start":99,"end":-4}]""")
        assertEquals(23, s[0].start)
        assertEquals(0, s[0].end)
    }

    @Test fun pinsEncoding() {
        assertNull(Scenes.decodePins(""))
        assertEquals(listOf("a", "b"), Scenes.decodePins(Scenes.encodePins(listOf("a", "b"))))
    }
}
