package com.voidtouch.launcher.data

import org.json.JSONArray
import org.json.JSONObject

/** Uma anotação rápida. A "tarefa do foco" é a única marcada com [focus] e aparece na home. */
data class Note(
    val id: String,
    val text: String,
    val createdAt: Long,
    val done: Boolean = false,
    val focus: Boolean = false,
)

/** Regras das anotações, puras para poderem ser testadas. Mais novas primeiro. */
object Notes {
    const val MAX = 50
    const val MAX_TEXT = 200

    /** No máximo uma tarefa do foco, sempre pendente; se não há, a pendente mais antiga assume. */
    fun normalize(list: List<Note>): List<Note> {
        val focusId = list.firstOrNull { it.focus && !it.done }?.id
            ?: list.filter { !it.done }.minByOrNull { it.createdAt }?.id
        return list.map { it.copy(focus = it.id == focusId) }
    }

    /** Texto vazio não entra; passou do limite, sai a mais antiga (as feitas primeiro). */
    fun add(list: List<Note>, id: String, text: String, now: Long): List<Note> {
        val clean = text.trim().take(MAX_TEXT)
        if (clean.isEmpty()) return list
        var next = listOf(Note(id, clean, now)) + list
        while (next.size > MAX) {
            val drop = next.filter { it.done }.minByOrNull { it.createdAt } ?: next.minBy { it.createdAt }
            next = next.filterNot { it.id == drop.id }
        }
        return normalize(next)
    }

    fun toggleDone(list: List<Note>, id: String): List<Note> =
        normalize(list.map { if (it.id == id) it.copy(done = !it.done, focus = false) else it })

    /** Vira a tarefa do foco, se estiver pendente. */
    fun setFocus(list: List<Note>, id: String): List<Note> {
        val target = list.firstOrNull { it.id == id } ?: return list
        if (target.done) return list
        return list.map { it.copy(focus = it.id == id) }
    }

    fun remove(list: List<Note>, id: String): List<Note> = normalize(list.filterNot { it.id == id })

    fun clearDone(list: List<Note>): List<Note> = normalize(list.filterNot { it.done })

    fun focusNote(list: List<Note>): Note? = list.firstOrNull { it.focus && !it.done }

    fun pending(list: List<Note>): Int = list.count { !it.done }

    fun encode(list: List<Note>): String {
        val arr = JSONArray()
        list.forEach { arr.put(JSONObject().put("id", it.id).put("text", it.text).put("at", it.createdAt).put("done", it.done).put("focus", it.focus)) }
        return arr.toString()
    }

    fun decode(text: String): List<Note> = runCatching {
        val arr = JSONArray(text)
        normalize((0 until minOf(arr.length(), MAX)).map { i ->
            val o = arr.getJSONObject(i)
            Note(o.getString("id"), o.getString("text").take(MAX_TEXT), o.optLong("at"), o.optBoolean("done"), o.optBoolean("focus"))
        })
    }.getOrDefault(emptyList())
}
