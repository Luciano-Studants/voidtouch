package com.voidtouch.launcher.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LightAutoTest {
    private fun run(start: LightAuto.State, vararg lux: Float) = lux.fold(start) { s, l -> LightAuto.next(s, l) }

    @Test fun turnsOnAfterSteadyBrightLight() {
        assertFalse(run(LightAuto.State(), 20000f, 20000f).on)
        assertTrue(run(LightAuto.State(), 20000f, 20000f, 20000f).on)
    }

    @Test fun shadowDoesNotFlip() {
        assertFalse(run(LightAuto.State(), 20000f, 20000f, 100f, 20000f, 20000f).on)
    }

    @Test fun turnsOffAfterSteadyDimLight() {
        val on = LightAuto.State(on = true)
        assertTrue(run(on, 1000f, 1000f).on)
        assertFalse(run(on, 1000f, 1000f, 1000f).on)
    }

    @Test fun hysteresisKeepsOnBetweenThresholds() {
        assertTrue(run(LightAuto.State(on = true), 8000f, 8000f, 8000f, 8000f).on)
        assertFalse(run(LightAuto.State(), 8000f, 8000f, 8000f, 8000f).on)
    }

    @Test fun streakResets() {
        assertEquals(0, run(LightAuto.State(), 20000f, 20000f, 100f).streak)
    }
}
