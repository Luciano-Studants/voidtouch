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
