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

class FavoritesMoveTest {
    private val l = listOf("a", "b", "c")
    @Test fun sobe() = assertEquals(listOf("b", "a", "c"), HomeRules.move(l, "b", -1))
    @Test fun desce() = assertEquals(listOf("a", "c", "b"), HomeRules.move(l, "b", 1))
    @Test fun nao_passa_do_topo() = assertEquals(l, HomeRules.move(l, "a", -1))
    @Test fun nao_passa_do_fim() = assertEquals(l, HomeRules.move(l, "c", 1))
    @Test fun chave_ausente() = assertEquals(l, HomeRules.move(l, "x", 1))
}
