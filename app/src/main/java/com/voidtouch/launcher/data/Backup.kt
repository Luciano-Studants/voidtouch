package com.voidtouch.launcher.data

import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject

/**
 * Backup dos ajustes em um arquivo de texto (JSON): favoritos, pastas, apps do foco, gestos,
 * fundo, hora e fotos ocultas. Fica de fora o que é só do momento (foco ligado, versão vista).
 * O arquivo é salvo e lido pelo seletor de arquivos do Android, sem pedir permissão de armazenamento.
 */
object Backup {
    private const val FORMAT = 1
    private val SKIP = setOf("focus_since", "seen_version", "tips_seen", "last_open_ms")

    fun export(prefs: SharedPreferences): String {
        val data = JSONObject()
        prefs.all.forEach { (key, value) ->
            if (key in SKIP || value == null) return@forEach
            val entry = JSONObject()
            when (value) {
                is String -> entry.put("t", "s").put("v", value)
                is Boolean -> entry.put("t", "b").put("v", value)
                is Int -> entry.put("t", "i").put("v", value)
                is Long -> entry.put("t", "l").put("v", value)
                is Set<*> -> entry.put("t", "set").put("v", JSONArray(value.map { it.toString() }))
                else -> return@forEach
            }
            data.put(key, entry)
        }
        return JSONObject().put("app", "voidtouch").put("format", FORMAT).put("data", data).toString(2)
    }

    /** Aplica o backup. Devolve falso se o arquivo não for um backup do VoidTouch. */
    fun import(prefs: SharedPreferences, text: String): Boolean = runCatching {
        val root = JSONObject(text)
        if (root.optString("app") != "voidtouch") return false
        val data = root.getJSONObject("data")
        val edit = prefs.edit()
        data.keys().forEach { key ->
            if (key in SKIP) return@forEach
            val e = data.getJSONObject(key)
            when (e.getString("t")) {
                "s" -> edit.putString(key, e.getString("v"))
                "b" -> edit.putBoolean(key, e.getBoolean("v"))
                "i" -> edit.putInt(key, e.getInt("v"))
                "l" -> edit.putLong(key, e.getLong("v"))
                "set" -> {
                    val arr = e.getJSONArray("v")
                    edit.putStringSet(key, (0 until arr.length()).map { arr.getString(it) }.toSet())
                }
            }
        }
        edit.apply()
        true
    }.getOrDefault(false)
}
