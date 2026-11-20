package com.voidtouch.launcher.data

/**
 * Regras puras da proteção contra travamentos. Se o VoidTouch fecha sozinho várias vezes seguidas
 * (por exemplo, por um erro numa atualização), ele deixa de abrir a home normal e mostra o modo seguro,
 * para o celular nunca ficar sem uma tela inicial utilizável.
 */
object CrashRules {
    const val WINDOW_MS = 120_000L
    const val LIMIT = 3
    const val LIMIT_AFTER_UPDATE = 2
    private const val KEEP = 10
    private const val MAX_TRACE = 700

    /** Só os travamentos da janela recente contam. */
    fun recent(times: List<Long>, now: Long): List<Long> = times.filter { now - it in 0..WINDOW_MS }

    fun add(times: List<Long>, now: Long): List<Long> = (recent(times, now) + now).takeLast(KEEP)

    /** Logo depois de uma atualização o limite é menor: o erro provavelmente veio com ela. */
    fun shouldEnterSafeMode(times: List<Long>, now: Long, afterUpdate: Boolean): Boolean =
        recent(times, now).size >= if (afterUpdate) LIMIT_AFTER_UPDATE else LIMIT

    fun encode(times: List<Long>): String = times.joinToString(",")

    fun decode(text: String?): List<Long> =
        text.orEmpty().split(',').mapNotNull { it.trim().toLongOrNull() }

    /** Resumo curto do erro (tipo, mensagem e as primeiras linhas da pilha), para mostrar e copiar. */
    fun summary(className: String, message: String?, frames: List<String>): String {
        val head = if (message.isNullOrBlank()) className else "$className: $message"
        val text = (listOf(head) + frames.take(6).map { "  em $it" }).joinToString("\n")
        return if (text.length <= MAX_TRACE) text else text.take(MAX_TRACE - 1) + "…"
    }

    /** Ajustes que ligam recursos mais novos ou mais arriscados; o modo seguro os desliga. Chave -> valor seguro. */
    val RISKY_BOOLEANS: Map<String, Boolean> = mapOf(
        "parallax" to false,
        "dock_on" to false,
        "open_anim" to false,
        "predict_on" to false,
        "scene_auto" to false,
    )
    const val SAFE_COLOR_SET = "neutral"
    const val SAFE_BACKDROP = "void"
}
