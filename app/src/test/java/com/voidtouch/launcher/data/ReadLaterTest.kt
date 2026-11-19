package com.voidtouch.launcher.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ReadLaterTest {
    private fun item(id: String, text: String = "oi") = LaterItem(id, "p", "App", "Maria", text, 1L)

    @Test fun addPutsNewestFirstAndDedupes() {
        val a = ReadLater.add(emptyList(), item("1"))
        val b = ReadLater.add(a, item("2", "outra"))
        assertEquals(listOf("2", "1"), b.map { it.id })
        val c = ReadLater.add(b, item("3", "oi"))
        assertEquals(listOf("3", "2"), c.map { it.id })
    }

    @Test fun addRespectsLimit() {
        var l = emptyList<LaterItem>()
        (1..40).forEach { l = ReadLater.add(l, item("$it", "t$it")) }
        assertEquals(ReadLater.MAX, l.size)
        assertEquals("40", l.first().id)
    }

    @Test fun removeById() {
        val l = listOf(item("1"), item("2", "x"))
        assertEquals(listOf("2"), ReadLater.remove(l, "1").map { it.id })
    }

    @Test fun roundTripAndGarbage() {
        val l = listOf(item("1"), item("2", "x"))
        assertEquals(l, ReadLater.decode(ReadLater.encode(l)))
        assertTrue(ReadLater.decode("lixo").isEmpty())
    }

    @Test fun groupsByConversationKeepingOrder() {
        val items = listOf("a|x", "b|y", "a|x", "a|z")
        val g = NoteGroups.group(items) { it }
        assertEquals(listOf("a|x", "b|y", "a|z"), g.map { it.first })
        assertEquals(2, g[0].count)
        assertEquals(1, g[1].count)
    }
}
