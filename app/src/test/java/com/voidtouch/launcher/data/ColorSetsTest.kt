package com.voidtouch.launcher.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ColorSetsTest {
    @Test fun chave_desconhecida_vira_automatico() = assertEquals("auto", ColorSets.fromKey("zzz").key)
    @Test fun neutro_e_neutro() =
        assertEquals(WallTone.NEUTRAL, ColorSets.palette(ColorSets.fromKey("neutral"), Tone(10f, 1f), true))
    @Test fun automatico_sem_papel_de_parede_e_neutro() =
        assertEquals(WallTone.NEUTRAL, ColorSets.palette(ColorSets.fromKey("auto"), Tone(10f, 1f), false))
    @Test fun automatico_usa_o_tom_da_imagem() {
        val p = ColorSets.palette(ColorSets.fromKey("auto"), Tone(200f, 0.9f), true)
        assertNotEquals(WallTone.NEUTRAL, p)
    }
    @Test fun fixo_vale_em_qualquer_fundo() =
        assertNotEquals(WallTone.NEUTRAL, ColorSets.palette(ColorSets.fromKey("amber"), null, false))
    @Test fun chaves_unicas_e_texto_sempre_claro() {
        assertEquals(ColorSets.all.size, ColorSets.all.map { it.key }.toSet().size)
        ColorSets.all.forEach {
            val p = ColorSets.palette(it, Tone(90f, 1f), true)
            assertTrue(it.key, minOf((p.text shr 16) and 0xFF, (p.text shr 8) and 0xFF, p.text and 0xFF) >= 0xD0)
        }
    }
}
