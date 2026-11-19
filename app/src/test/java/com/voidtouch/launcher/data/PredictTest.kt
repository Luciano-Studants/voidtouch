package com.voidtouch.launcher.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PredictTest {
    private fun hist(vararg pairs: Triple<Int, String, Int>): Map<Int, Map<String, Int>> {
        var h = emptyMap<Int, Map<String, Int>>()
        pairs.forEach { (hour, key, n) -> repeat(n) { h = Predict.record(h, hour, key) } }
        return h
    }

    @Test fun blocksAreThreeHours() {
        assertEquals(0, Predict.block(0))
        assertEquals(0, Predict.block(2))
        assertEquals(1, Predict.block(3))
        assertEquals(7, Predict.block(23))
    }

    @Test fun suggestsMostOpenedInBlock() {
        val h = hist(Triple(20, "a", 5), Triple(20, "b", 3), Triple(20, "c", 1))
        assertEquals(listOf("a", "b"), Predict.suggest(h, 20, emptySet()))
    }

    @Test fun needsMinimumHistory() {
        val h = hist(Triple(20, "a", 1))
        assertTrue(Predict.suggest(h, 20, emptySet()).isEmpty())
    }

    @Test fun neighborBlocksCountHalf() {
        val h = hist(Triple(17, "a", 4)) // bloco 5; 20h é o bloco 6 (vizinho)
        assertEquals(listOf("a"), Predict.suggest(h, 20, emptySet())) // 4 * 0,5 = 2
        assertTrue(Predict.suggest(h, 8, emptySet()).isEmpty())
    }

    @Test fun midnightWrapsAround() {
        val h = hist(Triple(22, "a", 4)) // bloco 7; 1h é o bloco 0, vizinho do 7
        assertEquals(listOf("a"), Predict.suggest(h, 1, emptySet()))
    }

    @Test fun excludesAndLimits() {
        val h = hist(Triple(20, "a", 9), Triple(20, "b", 8), Triple(20, "c", 7), Triple(20, "d", 6))
        assertEquals(listOf("b", "c", "d"), Predict.suggest(h, 20, setOf("a")))
        assertEquals(3, Predict.suggest(h, 20, emptySet()).size)
    }

    @Test fun keepsOnlyTopPerBlock() {
        var h = emptyMap<Int, Map<String, Int>>()
        (1..30).forEach { i -> repeat(2) { h = Predict.record(h, 10, "k$i") } }
        assertEquals(Predict.KEEP, h[3]!!.size)
    }

    @Test fun roundTripAndGarbage() {
        val h = hist(Triple(20, "a/b", 3), Triple(8, "c/d", 2))
        assertEquals(h, Predict.decode(Predict.encode(h)))
        assertTrue(Predict.decode("lixo").isEmpty())
    }
}
