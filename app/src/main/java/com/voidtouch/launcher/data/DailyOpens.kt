package com.voidtouch.launcher.data

import org.json.JSONObject

/** Quantas vezes cada app foi aberto hoje; zera sozinho quando o dia muda. */
object DailyOpens {
    /** Estado: o dia ("2026-11-23") e a contagem por app. */
    data class State(val day: String, val counts: Map<String, Int>)

    fun empty(day: String) = State(day, emptyMap())

    /** Se o dia mudou, começa uma contagem nova. */
    fun forDay(state: State, day: String): State = if (state.day == day) state else empty(day)

    fun count(state: State, day: String, key: String): Int = forDay(state, day).counts[key] ?: 0

    fun increment(state: State, day: String, key: String): State {
        val s = forDay(state, day)
        return s.copy(counts = s.counts + (key to ((s.counts[key] ?: 0) + 1)))
    }

    fun encode(state: State): String = JSONObject().apply {
        put("day", state.day)
        put("c", JSONObject(state.counts as Map<*, *>))
    }.toString()

    fun decode(text: String, day: String): State = runCatching {
        val o = JSONObject(text)
        val c = o.getJSONObject("c")
        State(o.getString("day"), c.keys().asSequence().associateWith { c.getInt(it) })
    }.getOrElse { empty(day) }

    /** Frase da tela de pausa. */
    fun phrase(opened: Int): String = when (opened) {
        0 -> "você ainda não abriu hoje"
        1 -> "você abriu 1 vez hoje"
        else -> "você abriu $opened vezes hoje"
    }
}
