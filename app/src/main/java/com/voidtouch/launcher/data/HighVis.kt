package com.voidtouch.launcher.data

/**
 * Modo de alta visibilidade, para usar o celular sob sol forte ou em lugares difíceis de ver:
 * fundo preto puro, texto branco, letras em negrito, traços mais grossos e brilho no máximo
 * (só enquanto a home está na tela).
 */
object HighVis {
    const val TEXT = 0xFFFFFFFF.toInt()

    /** Texto secundário: ainda bem claro (contraste acima de 15 para 1 sobre o preto). */
    const val DIM = 0xFFE6E6E6.toInt()
    const val LINE = 0xFFFFFFFF.toInt()
    const val BACKGROUND = 0xFF000000.toInt()

    /** Aumento de fonte mínimo no modo (o limite da home continua valendo). */
    const val FONT_SCALE = 1.15f

    fun strokeDp(on: Boolean): Float = if (on) 2f else 1f
}
