package com.voidtouch.launcher.data

import org.junit.Assert.assertEquals
import org.junit.Test

class FontScaleTest {
    @Test fun normalNaoMuda() = assertEquals(1f, FontScale.cap(1f), 0f)
    @Test fun pequenaNaoMuda() = assertEquals(0.85f, FontScale.cap(0.85f), 0f)
    @Test fun grandeELimitada() = assertEquals(FontScale.MAX, FontScale.cap(1.5f), 0f)
    @Test fun invalidaVira1() {
        assertEquals(1f, FontScale.cap(Float.NaN), 0f)
        assertEquals(1f, FontScale.cap(0f), 0f)
    }
}
