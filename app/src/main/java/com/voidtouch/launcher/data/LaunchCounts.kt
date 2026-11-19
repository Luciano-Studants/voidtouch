package com.voidtouch.launcher.data

import org.json.JSONObject

/**
 * Quantas vezes cada app foi aberto, para a busca mostrar primeiro o que você mais usa.
 * Só é atualizado quando você abre um app; guarda no máximo [KEEP] apps, os mais usados.
 */
object LaunchCounts {
    const val KEEP = 100

    fun increment(counts: Map<String, Int>, key: String): Map<String, Int> {
        val next = counts + (key to ((counts[key] ?: 0) + 1))
        if (next.size <= KEEP) return next
        return next.entries.sortedByDescending { it.value }.take(KEEP).associate { it.key to it.value }
    }

    fun encode(counts: Map<String, Int>): String = JSONObject(counts as Map<*, *>).toString()

    fun decode(text: String): Map<String, Int> = runCatching {
        val o = JSONObject(text)
        o.keys().asSequence().associateWith { o.getInt(it) }
    }.getOrDefault(emptyMap())
}
