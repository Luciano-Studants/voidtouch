package com.voidtouch.launcher.data

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NightModeTest {
    @Test
    fun windowCrossingMidnight() {
        assertTrue(NightMode.isNight(23, 22, 6))
        assertTrue(NightMode.isNight(0, 22, 6))
        assertTrue(NightMode.isNight(5, 22, 6))
        assertFalse(NightMode.isNight(6, 22, 6))
        assertFalse(NightMode.isNight(12, 22, 6))
        assertTrue(NightMode.isNight(22, 22, 6))
    }

    @Test
    fun windowInsideTheDay() {
        assertTrue(NightMode.isNight(13, 12, 14))
        assertFalse(NightMode.isNight(14, 12, 14))
        assertFalse(NightMode.isNight(11, 12, 14))
    }

    @Test
    fun equalStartAndEndMeansNever() {
        for (h in 0..23) assertFalse(NightMode.isNight(h, 8, 8))
    }
}
