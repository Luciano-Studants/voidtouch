package com.voidtouch.launcher.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class WallToneTest {
    private fun fill(argb: Int) = IntArray(100) { argb }

    @Test fun vermelho_tem_matiz_zero() {
        val t = WallTone.fromPixels(fill(0xFFCC2020.toInt()))
        assertNotNull(t)
        val h = t!!.hue
        assertTrue(h < 5f || h > 355f)
    }
    @Test fun azul_tem_matiz_perto_de_240() {
        val t = WallTone.fromPixels(fill(0xFF2030CC.toInt()))!!
        assertEquals(236f, t.hue, 8f)
    }
    @Test fun cinza_nao_tem_tom() = assertNull(WallTone.fromPixels(fill(0xFF808080.toInt())))
    @Test fun preto_nao_tem_tom() = assertNull(WallTone.fromPixels(fill(0xFF000000.toInt())))
    @Test fun vazio_nao_tem_tom() = assertNull(WallTone.fromPixels(IntArray(0)))
    @Test fun hsl_branco_e_preto() {
        assertEquals(0xFFFFFFFF.toInt(), WallTone.hslToArgb(0f, 0f, 1f))
        assertEquals(0xFF000000.toInt(), WallTone.hslToArgb(120f, 1f, 0f))
    }
    @Test fun sem_tom_vira_neutro() = assertEquals(WallTone.NEUTRAL, WallTone.palette(null))
    @Test fun texto_continua_claro_com_qualquer_matiz() {
        for (h in 0..350 step 10) {
            val p = WallTone.palette(Tone(h.toFloat(), 1f))
            val r = (p.text shr 16) and 0xFF
            val g = (p.text shr 8) and 0xFF
            val b = p.text and 0xFF
            assertTrue("h=$h", minOf(r, g, b) >= 0xD0)
            val veilRgb = p.veil and 0xFFFFFF
            assertTrue("h=$h", maxOf((veilRgb shr 16) and 0xFF, (veilRgb shr 8) and 0xFF, veilRgb and 0xFF) < 0x40)
        }
    }
}

class WallToneColorsTest {
    @Test fun cores_do_sistema_viram_tom() {
        val t = WallTone.fromPixels(intArrayOf(0xFF2030CC.toInt(), 0xFF3040D0.toInt()))
        assertNotNull(t)
        assertEquals(236f, t!!.hue, 10f)
    }
    @Test fun so_cores_neutras_nao_dao_tom() =
        assertNull(WallTone.fromPixels(intArrayOf(0xFF808080.toInt(), 0xFFFFFFFF.toInt())))
}
