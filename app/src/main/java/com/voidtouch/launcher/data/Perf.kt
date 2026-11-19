package com.voidtouch.launcher.data

/** Resumo de um tipo de medida: última, média, pior e quantas vezes foi medida. */
data class PerfSummary(val last: Long, val average: Long, val worst: Long, val count: Int)

/**
 * Medidas de desempenho guardadas só na memória, enquanto o app vive. Nada é medido em segundo
 * plano: cada ponto é registrado no momento em que o usuário faz a ação (abrir, buscar...).
 */
class PerfStats(private val keep: Int = 20) {
    private val samples = LinkedHashMap<String, ArrayDeque<Long>>()

    @Synchronized
    fun record(name: String, ms: Long) {
        val list = samples.getOrPut(name) { ArrayDeque() }
        list.addLast(ms.coerceAtLeast(0))
        while (list.size > keep) list.removeFirst()
    }

    @Synchronized
    fun has(name: String): Boolean = samples[name]?.isNotEmpty() == true

    @Synchronized
    fun summary(name: String): PerfSummary? {
        val list = samples[name]?.takeIf { it.isNotEmpty() } ?: return null
        return PerfSummary(list.last(), list.sum() / list.size, list.max(), list.size)
    }
}

/** Instância única usada pelo app. */
object Perf {
    val stats = PerfStats()
    const val OPEN = "abertura da home"
    const val READY = "home pronta (apps carregados)"
    const val DRAWER = "abrir lista de apps"
    const val SEARCH = "busca"
    val ALL = listOf(OPEN, READY, DRAWER, SEARCH)
}
