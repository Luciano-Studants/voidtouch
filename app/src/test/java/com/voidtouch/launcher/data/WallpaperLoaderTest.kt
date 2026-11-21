package com.voidtouch.launcher.data

import org.junit.Assert.assertEquals
import org.junit.Test

class WallpaperLoaderTest {
    @Test fun imagem_do_tamanho_da_tela_nao_reduz() = assertEquals(1, WallpaperLoader.sampleSize(720, 1600, 720, 1600))
    @Test fun imagem_grande_reduz_por_potencia_de_dois() = assertEquals(4, WallpaperLoader.sampleSize(4000, 8000, 720, 1600))
    @Test fun nunca_fica_menor_que_o_alvo() {
        val s = WallpaperLoader.sampleSize(1500, 3200, 720, 1600)
        assertEquals(2, s)
        assertEquals(true, 1500 / s >= 720 && 3200 / s >= 1600)
    }
    @Test fun valores_invalidos_devolvem_um() = assertEquals(1, WallpaperLoader.sampleSize(0, 0, 720, 1600))
}

class CoverRectTest {
    @Test fun imagem_larga_corta_nas_laterais() {
        val r = WallpaperLoader.coverRect(2000, 1000, 720, 1600)
        assertEquals(1600f, r[3] - r[1], 0.01f)
        assertEquals(true, r[2] - r[0] >= 720f)
        assertEquals(0f, r[1], 0.01f)
        assertEquals(360f, (r[0] + r[2]) / 2f, 0.01f)
    }
    @Test fun imagem_alta_corta_em_cima_e_embaixo() {
        val r = WallpaperLoader.coverRect(1000, 4000, 720, 1600)
        assertEquals(720f, r[2] - r[0], 0.01f)
        assertEquals(true, r[3] - r[1] >= 1600f)
        assertEquals(800f, (r[1] + r[3]) / 2f, 0.01f)
    }
    @Test fun sempre_cobre_a_tela() {
        for ((w, h) in listOf(100 to 100, 4000 to 3000, 500 to 9000)) {
            val r = WallpaperLoader.coverRect(w, h, 720, 1600)
            assertEquals(true, r[0] <= 0.01f && r[1] <= 0.01f && r[2] >= 719.99f && r[3] >= 1599.99f)
        }
    }
    @Test fun origem_invalida_cobre_a_tela() {
        val r = WallpaperLoader.coverRect(0, 0, 720, 1600)
        assertEquals(720f, r[2], 0.01f)
    }
}
