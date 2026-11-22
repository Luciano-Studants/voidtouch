package com.voidtouch.launcher.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HighVisTest {
    @Test fun texto_tem_contraste_maximo_sobre_o_preto() =
        assertEquals(21.0, Contrast.ratio(HighVis.BACKGROUND, HighVis.TEXT), 0.01)

    @Test fun texto_secundario_passa_no_AAA() =
        assertTrue(Contrast.ratio(HighVis.BACKGROUND, HighVis.DIM) >= 7.0)

    @Test fun contorno_passa_no_AAA() =
        assertTrue(Contrast.ratio(HighVis.BACKGROUND, HighVis.LINE) >= 7.0)

    @Test fun contorno_fica_mais_grosso() {
        assertEquals(1f, HighVis.strokeDp(false), 0f)
        assertTrue(HighVis.strokeDp(true) > HighVis.strokeDp(false))
    }

    @Test fun fonte_sobe_para_o_minimo_no_modo() {
        assertEquals(HighVis.FONT_SCALE, FontScale.cap(1f, true), 0f)
        assertEquals(FontScale.MAX, FontScale.cap(2f, true), 0f)
    }

    @Test fun fonte_normal_nao_muda_fora_do_modo() = assertEquals(1f, FontScale.cap(1f, false), 0f)

    @Test fun acao_rapida_existe_e_tem_id_unico() {
        assertEquals(QuickAction.HighVis, QuickAction.fromId("highvis"))
        assertEquals(QuickAction.entries.size, QuickAction.entries.map { it.id }.toSet().size)
    }
}
