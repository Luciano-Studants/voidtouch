package com.voidtouch.launcher.data

/** Tipos de atividade ao vivo que a ilha mostra, da mais importante para a menos. */
enum class LiveKind(val label: String) {
    Call("chamada"),
    Navigation("navegação"),
    Timer("cronômetro"),
    Progress("andamento"),
}

/** Regras da ilha dinâmica, puras (sem Android) para poderem ser testadas. */
object Live {
    const val MAX = 4

    /**
     * Decide se uma notificação é uma atividade ao vivo. Só vale o que está em andamento; a música
     * fica de fora porque já tem o player. Categorias são os textos de Notification.CATEGORY_*.
     */
    fun classify(category: String?, ongoing: Boolean, hasProgress: Boolean, showChrono: Boolean): LiveKind? = when {
        !ongoing -> null
        category == "transport" -> null
        category == "call" -> LiveKind.Call
        category == "navigation" -> LiveKind.Navigation
        showChrono || category == "stopwatch" -> LiveKind.Timer
        hasProgress || category == "progress" -> LiveKind.Progress
        else -> null
    }

    /** Porcentagem de 0 a 100, ou nulo se o progresso é indeterminado. */
    fun percent(progress: Int, max: Int): Int? = if (max <= 0) null else (progress.toLong() * 100 / max).toInt().coerceIn(0, 100)

    /** "mm:ss" ou "h:mm:ss". */
    fun clock(ms: Long): String {
        val total = (kotlin.math.abs(ms) / 1000).coerceAtLeast(0)
        val h = total / 3600
        val m = (total % 3600) / 60
        val s = total % 60
        return if (h > 0) "%d:%02d:%02d".format(h, m, s) else "%02d:%02d".format(m, s)
    }

    /**
     * O valor mostrado à direita: tempo (contagem regressiva ou crescente) se há cronômetro,
     * senão a porcentagem, senão nada.
     */
    fun valueText(chrono: Boolean, whenMs: Long, countDown: Boolean, percent: Int?, now: Long): String = when {
        chrono && whenMs > 0 -> clock(if (countDown) (whenMs - now).coerceAtLeast(0) else now - whenMs)
        percent != null -> "$percent%"
        else -> ""
    }

    /** As atividades mais importantes primeiro, no máximo [MAX]. */
    fun <T> top(items: List<T>, kind: (T) -> LiveKind): List<T> = items.sortedBy { kind(it).ordinal }.take(MAX)
}
