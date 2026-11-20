package com.voidtouch.launcher.data

/** Regras da animação de abrir app a partir do ponto tocado, puras para poderem ser testadas. */
object OpenAnim {
    /** Depois disso o último toque já não vale como origem da animação. */
    const val RECENT_MS = 1_500L

    /** Retângulo de origem, em pixels, relativo à janela. */
    data class Rect(val x: Int, val y: Int, val w: Int, val h: Int)

    /**
     * Um quadrado de [sizePx] centrado no toque e mantido dentro da janela. Nulo se não há toque
     * recente (por exemplo, o app foi aberto por outro caminho) ou se a janela ainda não tem tamanho.
     */
    fun rect(touchX: Float, touchY: Float, sizePx: Int, viewW: Int, viewH: Int, sinceTouchMs: Long): Rect? {
        if (sinceTouchMs !in 0..RECENT_MS || viewW <= 0 || viewH <= 0 || sizePx <= 0) return null
        val size = minOf(sizePx, viewW, viewH)
        val x = (touchX - size / 2f).toInt().coerceIn(0, viewW - size)
        val y = (touchY - size / 2f).toInt().coerceIn(0, viewH - size)
        return Rect(x, y, size, size)
    }
}
