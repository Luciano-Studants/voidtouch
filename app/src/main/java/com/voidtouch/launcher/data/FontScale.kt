package com.voidtouch.launcher.data

/**
 * A home tem muitas linhas fixas na mesma tela; com a fonte do Android muito grande elas se
 * sobrepõem. Por isso o app limita o aumento da fonte (como a maioria dos launchers faz).
 */
object FontScale {
    const val MAX = 1.15f

    fun cap(scale: Float, highVis: Boolean = false): Float {
        val base = when {
            scale.isNaN() || scale <= 0f -> 1f
            else -> scale.coerceAtMost(MAX)
        }
        return if (highVis) base.coerceAtLeast(HighVis.FONT_SCALE) else base
    }
}
