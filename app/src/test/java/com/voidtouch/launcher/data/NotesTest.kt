package com.voidtouch.launcher.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class NotesTest {
    private fun two(): List<Note> = Notes.add(Notes.add(emptyList(), "a", "primeira", 1), "b", "segunda", 2)

    @Test fun firstNoteBecomesFocusAndNewerDoesNotTakeIt() {
        val list = two()
        assertEquals("a", Notes.focusNote(list)?.id)
        assertEquals(listOf("b", "a"), list.map { it.id }) // mais nova primeiro
    }

    @Test fun blankIsIgnoredAndTextTrimmedAndCut() {
        assertTrue(Notes.add(emptyList(), "a", "   ", 1).isEmpty())
        val long = Notes.add(emptyList(), "a", "x".repeat(500), 1)
        assertEquals(Notes.MAX_TEXT, long[0].text.length)
    }

    @Test fun doneFocusPassesToNextPending() {
        val r = Notes.toggleDone(two(), "a")
        assertTrue(r.first { it.id == "a" }.done)
        assertEquals("b", Notes.focusNote(r)?.id)
    }

    @Test fun allDoneMeansNoFocus() {
        val r = Notes.toggleDone(Notes.toggleDone(two(), "a"), "b")
        assertNull(Notes.focusNote(r))
        assertEquals(0, Notes.pending(r))
    }

    @Test fun reopenedDoneNoteIsPendingAgain() {
        val r = Notes.toggleDone(Notes.toggleDone(two(), "a"), "a")
        assertEquals(2, Notes.pending(r))
    }

    @Test fun setFocusMovesAndRejectsDone() {
        val moved = Notes.setFocus(two(), "b")
        assertEquals("b", Notes.focusNote(moved)?.id)
        assertEquals(1, moved.count { it.focus })
        val done = Notes.toggleDone(two(), "b")
        assertEquals(done, Notes.setFocus(done, "b"))
    }

    @Test fun removeKeepsOneFocus() {
        val r = Notes.remove(two(), "a")
        assertEquals(1, r.size)
        assertEquals("b", Notes.focusNote(r)?.id)
    }

    @Test fun clearDoneRemovesOnlyDone() {
        val r = Notes.clearDone(Notes.toggleDone(two(), "a"))
        assertEquals(listOf("b"), r.map { it.id })
    }

    @Test fun limitDropsDoneFirst() {
        var l = emptyList<Note>()
        (1..Notes.MAX).forEach { l = Notes.add(l, "$it", "n$it", it.toLong()) }
        l = Notes.toggleDone(l, "10")
        l = Notes.add(l, "novo", "novo", 999)
        assertEquals(Notes.MAX, l.size)
        assertTrue(l.none { it.id == "10" })
        assertTrue(l.any { it.id == "1" })
    }

    @Test fun roundTripAndGarbage() {
        val l = Notes.toggleDone(two(), "b")
        assertEquals(l, Notes.decode(Notes.encode(l)))
        assertTrue(Notes.decode("lixo").isEmpty())
    }
}
