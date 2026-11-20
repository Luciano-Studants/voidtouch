package com.voidtouch.launcher.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FocusSessionTest {
    private val min = 60_000L

    @Test fun expiryOnlyForTimedSessions() {
        assertFalse(FocusSession.expired(0, 999_999))
        assertFalse(FocusSession.expired(100, 99))
        assertTrue(FocusSession.expired(100, 100))
    }

    @Test fun remainingNeverNegative() {
        assertEquals(500, FocusSession.remainingMs(1_000, 500))
        assertEquals(0, FocusSession.remainingMs(1_000, 5_000))
    }

    @Test fun labelStates() {
        assertEquals("foco desligado", FocusSession.label(0, 0, 0))
        assertEquals("foco ligado · 23 min", FocusSession.label(0 + 1, 0, 1 + 23 * min))
        assertEquals("foco ligado · restam 12 min", FocusSession.label(1, 12 * min + 1, 1))
        assertEquals("foco ligado · restam 1 min", FocusSession.label(1, 30_000L + 1, 1))
        assertEquals("foco ligado · restam 1 h 05 min", FocusSession.label(1, 65 * min + 1, 1))
    }

    @Test fun summaryTexts() {
        assertNull(FocusSession.summary(30_000, 3))
        assertEquals("você focou 25 min · nenhuma notificação esperando", FocusSession.summary(25 * min, 0))
        assertEquals("você focou 50 min · 1 notificação esperando", FocusSession.summary(50 * min, 1))
        assertEquals("você focou 1 h 30 min · 4 notificações esperando", FocusSession.summary(90 * min, 4))
    }

    @Test fun choices() {
        assertEquals("25 min", FocusSession.choiceLabel(25))
        assertEquals("sem fim", FocusSession.choiceLabel(0))
        assertEquals(4, FocusSession.CHOICES.size)
    }
}
