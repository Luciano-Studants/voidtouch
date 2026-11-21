package com.voidtouch.launcher.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ContrastTest {
    private val black = 0xFF000000.toInt()
    private val graphite = 0xFF121212.toInt()

    @Test fun preto_no_branco_e_21() = assertEquals(21.0, Contrast.ratio(black, 0xFFFFFFFF.toInt()), 0.01)

    @Test fun mesma_cor_e_um() = assertEquals(1.0, Contrast.ratio(graphite, graphite), 0.001)

    @Test fun texto_do_tema_neutro_passa_em_todos_os_fundos() {
        for (bg in listOf(black, graphite)) {
            assertTrue(Contrast.ratio(WallTone.NEUTRAL.text, bg) >= Contrast.AA)
            assertTrue(Contrast.ratio(WallTone.NEUTRAL.dim, bg) >= Contrast.AA)
        }
    }

    @Test fun todos_os_conjuntos_de_cores_passam_no_contraste_AA() {
        for (set in ColorSets.all) {
            for (h in listOf(0f, 38f, 120f, 205f, 270f, 335f)) {
                val p = ColorSets.palette(set, Tone(h, 1f), true)
                for (bg in listOf(black, graphite)) {
                    assertTrue("${set.key} h=$h texto", Contrast.ratio(p.text, bg) >= Contrast.AA)
                    assertTrue("${set.key} h=$h secundário", Contrast.ratio(p.dim, bg) >= Contrast.AA)
                }
            }
        }
    }
}
