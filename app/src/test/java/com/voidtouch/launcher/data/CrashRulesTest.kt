package com.voidtouch.launcher.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CrashRulesTest {
    private val now = 1_000_000L

    @Test fun tres_travamentos_na_janela_entram_no_modo_seguro() =
        assertTrue(CrashRules.shouldEnterSafeMode(listOf(now - 90_000, now - 40_000, now), now, false))

    @Test fun dois_travamentos_nao_bastam_fora_de_atualizacao() =
        assertFalse(CrashRules.shouldEnterSafeMode(listOf(now - 40_000, now), now, false))

    @Test fun apos_atualizacao_dois_bastam() =
        assertTrue(CrashRules.shouldEnterSafeMode(listOf(now - 40_000, now), now, true))

    @Test fun travamento_antigo_nao_conta() {
        val old = now - CrashRules.WINDOW_MS - 1
        assertFalse(CrashRules.shouldEnterSafeMode(listOf(old, old + 1, now), now, false))
    }

    @Test fun add_descarta_antigos_e_limita_a_dez() {
        var t = emptyList<Long>()
        repeat(15) { t = CrashRules.add(t, now + it) }
        assertEquals(10, t.size)
        assertEquals(listOf(now + 14), CrashRules.add(listOf(now - CrashRules.WINDOW_MS - 5), now + 14).takeLast(1))
    }

    @Test fun encode_decode_ida_e_volta_e_ignora_lixo() {
        assertEquals(listOf(1L, 2L, 3L), CrashRules.decode(CrashRules.encode(listOf(1L, 2L, 3L))))
        assertEquals(listOf(5L), CrashRules.decode("x,5,,y"))
        assertEquals(emptyList<Long>(), CrashRules.decode(null))
    }

    @Test fun resumo_limita_tamanho_e_linhas() {
        val frames = List(20) { "com.exemplo.Classe$it.metodo(Arquivo.kt:$it)" }
        val s = CrashRules.summary("java.lang.IllegalStateException", "falhou", frames)
        assertTrue(s.startsWith("java.lang.IllegalStateException: falhou"))
        assertEquals(7, s.lines().size)
        val big = CrashRules.summary("E", "x".repeat(5000), emptyList())
        assertTrue(big.length <= 700)
    }

    @Test fun resumo_sem_mensagem_usa_so_o_tipo() =
        assertEquals("java.lang.Error", CrashRules.summary("java.lang.Error", null, emptyList()))

    @Test fun ajustes_seguros_desligam_recursos_arriscados() {
        assertTrue(CrashRules.RISKY_BOOLEANS.values.none { it })
        assertEquals("neutral", CrashRules.SAFE_COLOR_SET)
        assertEquals("void", CrashRules.SAFE_BACKDROP)
    }
}
