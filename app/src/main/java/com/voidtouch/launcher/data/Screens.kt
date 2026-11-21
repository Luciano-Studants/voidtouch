package com.voidtouch.launcher.data

/** Telas internas do VoidTouch (a home é a tela base). Só uma fica aberta por vez. */
enum class Screen(val key: String) {
    Home("home"),
    Drawer("drawer"),
    Health("health"),
    Settings("settings"),
    Optimize("optimize"),
    Permissions("permissions"),
    Later("later"),
    Capture("capture"),
    Usage("usage"),
    Order("order"),
    Tips("tips"),
}

/** Regras puras da navegação entre telas, separadas da interface para poderem ser testadas. */
object ScreenRules {
    /** Abrir uma tela substitui a que estava aberta. */
    fun open(current: Screen, target: Screen): Screen = target

    /** Fechar só vale se a tela ainda for a aberta; fechar outra tela não tira a atual do lugar. */
    fun close(current: Screen, target: Screen): Screen = if (current == target) Screen.Home else current

    /** Botão voltar e botão home levam sempre de volta para a home. */
    fun home(): Screen = Screen.Home
}
