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
