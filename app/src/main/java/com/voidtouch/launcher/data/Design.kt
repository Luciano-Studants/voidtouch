package com.voidtouch.launcher.data

/** Designs do app: o minimalista de sempre ("Void") e o visual de cartões do app Rotina Noturna. */
enum class Design(val key: String, val label: String, val note: String) {
    Void("void", "void", "minimalista: lista solta, fonte monoespaçada fina, sem cartões"),
    Rotina("rotina", "rotina", "cartões arredondados, fontes Inter e Space Grotesk, como no Rotina Noturna");

    companion object {
        fun fromKey(key: String?): Design = entries.firstOrNull { it.key == key } ?: Void
    }
}

/** Cores do design Rotina, tiradas do app Rotina Noturna (tema escuro, sem cor de destaque). */
object RotinaTokens {
    const val BACKGROUND = 0xFF0A0A0A.toInt()
    const val SURFACE = 0xFF19191B.toInt()
    const val LINE = 0xFF2E2E30.toInt()
    const val TEXT = 0xFFF5F5F5.toInt()
    const val MUTED = 0xFF8F8E94.toInt()

    /** Item selecionado: pílula clara com texto escuro. */
    const val SELECTED = 0xFFF2F2F2.toInt()
    const val ON_SELECTED = 0xFF0A0A0A.toInt()
    const val RADIUS_DP = 20
    const val MARGIN_DP = 12
}
