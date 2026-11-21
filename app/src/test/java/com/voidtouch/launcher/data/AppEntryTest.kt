package com.voidtouch.launcher.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test

class AppEntryTest {
    @Test fun chave_junta_pacote_e_atividade() =
        assertEquals("com.exemplo/com.exemplo.Main", AppEntry("Exemplo", "com.exemplo", "com.exemplo.Main").key)

    @Test fun chave_e_calculada_uma_vez() {
        val app = AppEntry("Exemplo", "com.exemplo", "com.exemplo.Main")
        assertSame(app.key, app.key)
    }

    @Test fun igualdade_continua_pelos_tres_campos() =
        assertEquals(AppEntry("A", "p", "a"), AppEntry("A", "p", "a"))

    @Test fun busca_sem_acento_e_ordenada_por_relevancia() {
        val apps = listOf(
            AppEntry("Câmera", "p1", "a"), AppEntry("Minha Câmera", "p2", "a"), AppEntry("Relógio", "p3", "a"),
        )
        assertEquals(listOf("p1", "p2"), Search.filter(apps, "camera").map { it.packageName })
    }
}
