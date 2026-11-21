package com.voidtouch.launcher.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import kotlin.coroutines.cancellation.CancellationException

class ErrorLogTest {
    @Before fun limpa() = ErrorLog.clear()

    @Test fun guarded_devolve_o_resultado_quando_tudo_ok() = assertEquals(5, guarded("x", 0) { 5 })

    @Test fun guarded_devolve_o_reserva_e_registra_o_erro() {
        val r = guarded("agenda", emptyList<String>()) { error("falhou") }
        assertEquals(emptyList<String>(), r)
        assertEquals(listOf("agenda: IllegalStateException (falhou)"), ErrorLog.recent())
    }

    @Test fun guarded_pega_ate_falta_de_memoria() {
        assertEquals(-1, guarded("img", -1) { throw OutOfMemoryError("sem espaço") })
        assertTrue(ErrorLog.recent().first().startsWith("img: OutOfMemoryError"))
    }

    @Test fun guarded_nao_engole_cancelamento() {
        try {
            guarded("x", 0) { throw CancellationException("parou") }
            fail("deveria repassar o cancelamento")
        } catch (_: CancellationException) {
        }
        assertTrue(ErrorLog.recent().isEmpty())
    }

    @Test fun guarda_no_maximo_vinte_os_mais_novos_primeiro() {
        repeat(25) { ErrorLog.note("t$it", null) }
        val r = ErrorLog.recent()
        assertEquals(ErrorLog.MAX, r.size)
        assertEquals("t24: erro", r.first())
    }

    @Test fun mensagem_longa_e_cortada() {
        assertTrue(ErrorLog.format("t", RuntimeException("x".repeat(500))).length < 130)
    }
}

class UpdateUrlRulesTest {
    private val ok = "https://github.com/Luciano-Studants/voidtouch/releases/download/v1/voidtouch.apk"
    @Test fun aceita_o_repositorio() = assertTrue(UpdateCheckRules.urlAllowed(ok))
    @Test fun recusa_http() = assertEquals(false, UpdateCheckRules.urlAllowed(ok.replace("https", "http")))
    @Test fun recusa_outro_repositorio() =
        assertEquals(false, UpdateCheckRules.urlAllowed("https://github.com/outro/app/releases/download/v1/a.apk"))
    @Test fun recusa_outro_servidor() =
        assertEquals(false, UpdateCheckRules.urlAllowed("https://evil.example.com/Luciano-Studants/voidtouch/a.apk"))
    @Test fun recusa_host_enganoso() =
        assertEquals(false, UpdateCheckRules.urlAllowed("https://github.com.evil.com/Luciano-Studants/voidtouch/a.apk"))
    @Test fun recusa_usuario_na_url() =
        assertEquals(false, UpdateCheckRules.urlAllowed("https://github.com@evil.com/Luciano-Studants/voidtouch/a.apk"))
    @Test fun redirecionamento_para_cdn_do_github_vale() =
        assertTrue(UpdateCheckRules.urlAllowed("https://objects.githubusercontent.com/abc/def", checkPath = false))
    @Test fun redirecionamento_para_fora_nao_vale() =
        assertEquals(false, UpdateCheckRules.urlAllowed("https://cdn.evil.com/x", checkPath = false))
    @Test fun lixo_nao_vale() = assertEquals(false, UpdateCheckRules.urlAllowed("não é url"))
}
