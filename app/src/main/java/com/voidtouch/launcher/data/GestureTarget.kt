package com.voidtouch.launcher.data

/** Ações rápidas que um gesto pode executar, além de abrir um app. */
enum class QuickAction(val id: String, val label: String) {
    Torch("torch", "lanterna"),
    NotificationPanel("panel", "painel de notificações"),
    Search("search", "buscar app"),
    Drawer("drawer", "lista de apps"),
    Focus("focus", "ligar ou desligar o foco"),
    Capture("capture", "anotação rápida"),
    Optimize("optimize", "otimizar o celular"),
    Health("health", "saúde do aparelho"),
    Usage("usage", "relatório de uso"),
    Settings("settings", "ajustes");

    companion object {
        fun fromId(id: String): QuickAction? = entries.firstOrNull { it.id == id }
    }
}

/**
 * O destino de um gesto é guardado como texto: ou a chave de um app ("pacote/atividade") ou
 * "action:<id>" para uma [QuickAction]. Nulo significa "nenhum".
 */
object GestureTarget {
    private const val PREFIX = "action:"

    fun encode(action: QuickAction): String = PREFIX + action.id

    /** A ação do destino, ou nulo se ele for um app (ou vazio). */
    fun action(key: String?): QuickAction? =
        key?.takeIf { it.startsWith(PREFIX) }?.removePrefix(PREFIX)?.let(QuickAction::fromId)

    fun isApp(key: String?): Boolean = !key.isNullOrBlank() && !key.startsWith(PREFIX)
}
