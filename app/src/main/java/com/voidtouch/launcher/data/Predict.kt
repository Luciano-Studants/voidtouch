package com.voidtouch.launcher.data

import org.json.JSONObject

/**
 * Sugestão de apps pelo horário, só com o histórico de aberturas guardado no aparelho. Nada roda
 * em segundo plano: o histórico muda apenas quando você abre um app. O dia é dividido em 8 blocos
 * de 3 horas; cada bloco guarda os [KEEP] apps mais abertos.
 */
object Predict {
    const val KEEP = 15
    const val MIN_SCORE = 2.0
    const val LIMIT = 3

    fun block(hour: Int): Int = (hour.coerceIn(0, 23)) / 3

    fun record(history: Map<Int, Map<String, Int>>, hour: Int, key: String): Map<Int, Map<String, Int>> {
        val b = block(hour)
        val current = history[b].orEmpty()
        var next = current + (key to ((current[key] ?: 0) + 1))
        if (next.size > KEEP) next = next.entries.sortedByDescending { it.value }.take(KEEP).associate { it.key to it.value }
        return history + (b to next)
    }

    /**
     * Os apps mais prováveis agora: o bloco atual pesa inteiro e os vizinhos pela metade, para a
     * sugestão não "pular" na virada do bloco. Só entra quem passou de [MIN_SCORE] e não está em [exclude].
     */
    fun suggest(history: Map<Int, Map<String, Int>>, hour: Int, exclude: Set<String>, limit: Int = LIMIT): List<String> {
        val b = block(hour)
        val scores = HashMap<String, Double>()
        fun add(block: Int, weight: Double) {
            history[((block % 8) + 8) % 8]?.forEach { (k, n) -> scores[k] = (scores[k] ?: 0.0) + n * weight }
        }
        add(b, 1.0)
        add(b - 1, 0.5)
        add(b + 1, 0.5)
        return scores.entries
            .filter { it.value >= MIN_SCORE && it.key !in exclude }
            .sortedWith(compareByDescending<Map.Entry<String, Double>> { it.value }.thenBy { it.key })
            .take(limit)
            .map { it.key }
    }

    fun encode(history: Map<Int, Map<String, Int>>): String {
        val root = JSONObject()
        history.forEach { (b, apps) -> root.put(b.toString(), JSONObject(apps as Map<*, *>)) }
        return root.toString()
    }

    fun decode(text: String): Map<Int, Map<String, Int>> = runCatching {
        val root = JSONObject(text)
        root.keys().asSequence().mapNotNull { k ->
            val b = k.toIntOrNull()?.takeIf { it in 0..7 } ?: return@mapNotNull null
            val o = root.getJSONObject(k)
            b to o.keys().asSequence().associateWith { o.getInt(it) }
        }.toMap()
    }.getOrDefault(emptyMap())
}
