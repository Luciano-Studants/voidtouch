package com.voidtouch.launcher.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DesignTest {
    @Test fun chave_desconhecida_vira_void() {
        assertEquals(Design.Void, Design.fromKey(null))
        assertEquals(Design.Void, Design.fromKey("outro"))
    }

    @Test fun chaves_ida_e_volta() {
        for (d in Design.entries) assertEquals(d, Design.fromKey(d.key))
    }

    @Test fun texto_do_rotina_passa_no_AAA_sobre_fundo_e_cartao() {
        for (bg in listOf(RotinaTokens.BACKGROUND, RotinaTokens.SURFACE)) {
            assertTrue(Contrast.ratio(bg, RotinaTokens.TEXT) >= 7.0)
        }
    }

    @Test fun texto_secundario_do_rotina_passa_no_AA() {
        for (bg in listOf(RotinaTokens.BACKGROUND, RotinaTokens.SURFACE)) {
            assertTrue(Contrast.ratio(bg, RotinaTokens.MUTED) >= Contrast.AA)
        }
    }

    @Test fun item_selecionado_tem_contraste_alto() =
        assertTrue(Contrast.ratio(RotinaTokens.SELECTED, RotinaTokens.ON_SELECTED) >= 7.0)
}
