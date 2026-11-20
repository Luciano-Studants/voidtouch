package com.voidtouch.launcher.data

import org.junit.Assert.assertEquals
import org.junit.Test

class ParallaxTest {
    @Test fun parado_nao_desloca() = assertEquals(0f, Parallax.target(0f), 0.0001f)
    @Test fun limita_em_um() {
        assertEquals(1f, Parallax.target(9.81f), 0.0001f)
        assertEquals(-1f, Parallax.target(-20f), 0.0001f)
    }
    @Test fun meia_gravidade_e_maximo() = assertEquals(1f, Parallax.target(4.905f), 0.001f)
    @Test fun suaviza_na_direcao_do_alvo() {
        val v = Parallax.smooth(0f, 1f)
        assertEquals(0.12f, v, 0.0001f)
        assertEquals(1f, Parallax.smooth(0.5f, 1f, 5f), 0.0001f)
    }
}
