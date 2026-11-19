package com.voidtouch.launcher.data

import org.json.JSONArray
import org.json.JSONObject

/** Uma notificação guardada para ler depois. */
data class LaterItem(
    val id: String,
    val pkg: String,
    val app: String,
    val title: String,
    val text: String,
    val savedAt: Long,
)

/** Regras de "ler depois", puras para poderem ser testadas. Fica só no aparelho e fora do backup. */
object ReadLater {
    const val MAX = 30

    /** Coloca no começo; o mesmo conteúdo salvo de novo não duplica. */
    fun add(list: List<LaterItem>, item: LaterItem): List<LaterItem> =
        (listOf(item) + list.filterNot { it.pkg == item.pkg && it.title == item.title && it.text == item.text }).take(MAX)

    fun remove(list: List<LaterItem>, id: String): List<LaterItem> = list.filterNot { it.id == id }

    fun encode(list: List<LaterItem>): String {
        val arr = JSONArray()
        list.forEach {
            arr.put(JSONObject().put("id", it.id).put("pkg", it.pkg).put("app", it.app).put("title", it.title).put("text", it.text).put("at", it.savedAt))
        }
        return arr.toString()
    }

    fun decode(text: String): List<LaterItem> = runCatching {
        val arr = JSONArray(text)
        (0 until minOf(arr.length(), MAX)).map { i ->
            val o = arr.getJSONObject(i)
            LaterItem(o.getString("id"), o.getString("pkg"), o.getString("app"), o.getString("title"), o.optString("text"), o.optLong("at"))
        }
    }.getOrDefault(emptyList())
}

/** Junta notificações da mesma conversa (mesmo app e mesmo título) em uma linha só. */
object NoteGroups {
    class Group<T>(val items: List<T>) {
        val first: T get() = items.first()
        val count: Int get() = items.size
    }

    /** Mantém a ordem de chegada: o grupo fica na posição da notificação mais recente dele. */
    fun <T> group(items: List<T>, key: (T) -> String): List<Group<T>> {
        val map = LinkedHashMap<String, MutableList<T>>()
        items.forEach { map.getOrPut(key(it)) { ArrayList() }.add(it) }
        return map.values.map { Group(it) }
    }
}
