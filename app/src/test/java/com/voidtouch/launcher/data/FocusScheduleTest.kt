package com.voidtouch.launcher.data

import org.junit.Assert.assertEquals
import org.junit.Test

class FocusScheduleTest {
    @Test
    fun enteringWindowTurnsOn() {
        assertEquals(FocusAction.TurnOn, FocusSchedule.decide(true, false))
    }

    @Test
    fun leavingWindowTurnsOff() {
        assertEquals(FocusAction.TurnOff, FocusSchedule.decide(false, true))
    }

    @Test
    fun noChangeDoesNothing() {
        assertEquals(FocusAction.None, FocusSchedule.decide(true, true))
        assertEquals(FocusAction.None, FocusSchedule.decide(false, false))
    }

    @Test
    fun justEnabledInsideWindowTurnsOn_outsideDoesNothing() {
        assertEquals(FocusAction.TurnOn, FocusSchedule.decide(true, null))
        assertEquals(FocusAction.None, FocusSchedule.decide(false, null))
    }
}
