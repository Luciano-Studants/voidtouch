package com.voidtouch.launcher.data

/** Um resultado da busca que não é um app. */
sealed interface Hit {
    val title: String
    val hint: String

    /** Resposta de conta ou conversão; tocar copia. */
    data class Answer(override val title: String) : Hit {
        override val hint = "toque para copiar"
    }

    data class Action(val action: QuickAction) : Hit {
        override val title = action.label
        override val hint = "ação"
    }

    /** Tela de configurações do Android, aberta por uma intent (nome da ação). */
    data class Setting(override val title: String, val intentAction: String) : Hit {
        override val hint = "configuração do sistema"
    }

    data class Contact(override val title: String, val uri: String) : Hit {
        override val hint = "contato"
    }
}

/** Resultados universais da busca, tudo puro e sem Android para poder ser testado. */
object UniversalSearch {
    private class Entry(val hit: Hit, val words: List<String>)

    private val actions = listOf(
        Entry(Hit.Action(QuickAction.Torch), listOf("lanterna", "flash", "luz")),
        Entry(Hit.Action(QuickAction.NotificationPanel), listOf("painel", "notificacoes")),
        Entry(Hit.Action(QuickAction.Focus), listOf("foco", "concentrar")),
        Entry(Hit.Action(QuickAction.Capture), listOf("anotar", "anotacao", "nota", "tarefa", "ideia", "lembrete")),
        Entry(Hit.Action(QuickAction.Optimize), listOf("otimizar", "cache", "limpar")),
        Entry(Hit.Action(QuickAction.Health), listOf("saude", "ram", "bateria", "armazenamento")),
        Entry(Hit.Action(QuickAction.Settings), listOf("ajustes", "configurar", "voidtouch")),
    )

    private val settings = listOf(
        Entry(Hit.Setting("wi-fi", "android.settings.WIFI_SETTINGS"), listOf("wifi", "wi-fi", "internet", "rede")),
        Entry(Hit.Setting("bluetooth", "android.settings.BLUETOOTH_SETTINGS"), listOf("bluetooth")),
        Entry(Hit.Setting("bateria", "android.settings.BATTERY_SAVER_SETTINGS"), listOf("bateria", "economia")),
        Entry(Hit.Setting("tela e brilho", "android.settings.DISPLAY_SETTINGS"), listOf("tela", "brilho")),
        Entry(Hit.Setting("som e volume", "android.settings.SOUND_SETTINGS"), listOf("som", "volume", "toque")),
        Entry(Hit.Setting("localização", "android.settings.LOCATION_SOURCE_SETTINGS"), listOf("localizacao", "gps")),
        Entry(Hit.Setting("armazenamento", "android.settings.INTERNAL_STORAGE_SETTINGS"), listOf("armazenamento", "espaco")),
        Entry(Hit.Setting("apps instalados", "android.settings.APPLICATION_SETTINGS"), listOf("apps", "aplicativos")),
        Entry(Hit.Setting("data e hora", "android.settings.DATE_SETTINGS"), listOf("data", "hora", "fuso")),
        Entry(Hit.Setting("idioma", "android.settings.LOCALE_SETTINGS"), listOf("idioma", "lingua")),
        Entry(Hit.Setting("acessibilidade", "android.settings.ACCESSIBILITY_SETTINGS"), listOf("acessibilidade")),
        Entry(Hit.Setting("NFC", "android.settings.NFC_SETTINGS"), listOf("nfc", "aproximacao")),
        Entry(Hit.Setting("modo avião", "android.settings.AIRPLANE_MODE_SETTINGS"), listOf("aviao", "voo")),
        Entry(Hit.Setting("configurações do Android", "android.settings.SETTINGS"), listOf("configuracoes", "sistema")),
    )

    private const val MAX = 4

    /** Resposta de conta ou conversão, ações e configurações que combinam com o texto. */
    fun hits(query: String): List<Hit> {
        val q = Search.normalize(query.trim())
        if (q.length < 2) return emptyList()
        val out = ArrayList<Hit>()
        (Units.answer(query) ?: Calc.answer(query))?.let { out += Hit.Answer(it) }
        for (e in actions + settings) {
            if (out.size >= MAX) break
            if (e.words.any { it.startsWith(q) }) out += e.hit
        }
        return out
    }
}
