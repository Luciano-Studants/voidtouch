package com.voidtouch.launcher.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class OpenAnimTest {
    @Test fun centersOnTouch() {
        assertEquals(OpenAnim.Rect(450, 950, 100, 100), OpenAnim.rect(500f, 1000f, 100, 1080, 2400, 100))
    }

    @Test fun staysInsideWindow() {
        assertEquals(OpenAnim.Rect(0, 0, 100, 100), OpenAnim.rect(5f, 5f, 100, 1080, 2400, 0))
        assertEquals(OpenAnim.Rect(980, 2300, 100, 100), OpenAnim.rect(1075f, 2395f, 100, 1080, 2400, 0))
    }

    @Test fun oldTouchOrBadWindowGivesNull() {
        assertNull(OpenAnim.rect(500f, 500f, 100, 1080, 2400, OpenAnim.RECENT_MS + 1))
        assertNull(OpenAnim.rect(500f, 500f, 100, 1080, 2400, -1))
        assertNull(OpenAnim.rect(500f, 500f, 100, 0, 0, 10))
    }

    @Test fun sizeShrinksToSmallWindow() {
        assertEquals(50, OpenAnim.rect(10f, 10f, 100, 50, 50, 0)!!.w)
    }
}
