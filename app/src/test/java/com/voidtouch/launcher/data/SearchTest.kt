package com.voidtouch.launcher.data

import org.junit.Assert.assertEquals
import org.junit.Test

class SearchTest {
    private fun app(label: String) = AppEntry(label, "pkg.$label", "Main")

    @Test
    fun normalize_removesAccentsAndCase() {
        assertEquals("camera", Search.normalize("  Câmera "))
        assertEquals("configuracoes", Search.normalize("Configurações"))
    }

    @Test
    fun filter_blankQueryReturnsEverything() {
        val apps = listOf(app("Banco"), app("Câmera"))
        assertEquals(apps, Search.filter(apps, "  "))
    }

    @Test
    fun filter_ignoresAccents() {
        val apps = listOf(app("Câmera"), app("Relógio"))
        assertEquals(listOf("Câmera"), Search.filter(apps, "camera").map { it.label })
    }

    @Test
    fun filter_ranksStartThenWordStartThenContains() {
        val apps = listOf(app("Abanco"), app("Meu Banco"), app("Banco"))
        assertEquals(
            listOf("Banco", "Meu Banco", "Abanco"),
            Search.filter(apps, "banco").map { it.label },
        )
    }

    @Test
    fun filter_noMatchReturnsEmpty() {
        assertEquals(emptyList<AppEntry>(), Search.filter(listOf(app("Banco")), "zzz"))
    }
}
