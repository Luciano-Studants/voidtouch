package com.voidtouch.launcher.data

/** Janela de horário do modo noite. Aceita virada de dia, como das 22h às 6h. */
object NightMode {
    const val DEFAULT_START = 22
    const val DEFAULT_END = 6

    /** Verdadeiro se [hour] (0 a 23) está dentro da janela [start, end). Início igual ao fim = nunca. */
    fun isNight(hour: Int, start: Int, end: Int): Boolean = when {
        start == end -> false
        start < end -> hour in start until end
        else -> hour >= start || hour < end
    }
}
