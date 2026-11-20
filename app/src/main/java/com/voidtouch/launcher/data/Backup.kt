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
    private val SKIP = setOf("focus_since", "focus_until", "seen_version", "tips_seen", "last_open_ms", "read_later", "predict_history")

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
        return JSONObject().put("app", "voidtouch").put("format", FORMAT).put("exportedAt", System.currentTimeMillis()).put("data", data).toString(2)
    }

    private const val MAX_BYTES = 1_000_000
    private const val MAX_ENTRIES = 500
    private const val MAX_SET = 2000
    private const val MAX_TEXT = 200_000
    private val KEY_RE = Regex("^[a-z0-9_:.-]{1,64}$")

    /** Um item validado, pronto para gravar. */
    class Item(val key: String, val type: String, val value: Any)

    sealed class Result {
        class Ok(val items: List<Item>) : Result()
        class Invalid(val reason: String) : Result()
    }

    /**
     * Confere o arquivo inteiro antes de tocar nos ajustes: formato, versão, tamanhos e tipos.
     * Nada é gravado aqui, então um arquivo ruim nunca deixa os ajustes pela metade.
     */
    fun parse(text: String): Result {
        if (text.length > MAX_BYTES) return Result.Invalid("arquivo grande demais para ser um backup")
        try {
            val root = JSONObject(text)
            if (root.optString("app") != "voidtouch") return Result.Invalid("esse arquivo não é um backup do VoidTouch")
            val format = root.optInt("format", -1)
            if (format < 1) return Result.Invalid("backup sem versão de formato válida")
            if (format > FORMAT) return Result.Invalid("backup feito por uma versão mais nova do VoidTouch")
            val data = root.optJSONObject("data") ?: return Result.Invalid("backup sem dados")
            if (data.length() > MAX_ENTRIES) return Result.Invalid("backup com itens demais")
            val items = ArrayList<Item>()
            for (key in data.keys()) {
                if (key in SKIP) continue
                if (!KEY_RE.matches(key)) return Result.Invalid("backup com item inválido")
                val e = data.optJSONObject(key) ?: return Result.Invalid("backup corrompido")
                val value: Any = when (e.optString("t")) {
                    "s" -> e.getString("v").also { if (it.length > MAX_TEXT) return Result.Invalid("backup corrompido") }
                    "b" -> e.getBoolean("v")
                    "i" -> e.getInt("v")
                    "l" -> e.getLong("v")
                    "set" -> {
                        val arr = e.getJSONArray("v")
                        if (arr.length() > MAX_SET) return Result.Invalid("backup corrompido")
                        (0 until arr.length()).map { arr.getString(it) }.toSet()
                    }
                    else -> return Result.Invalid("backup com tipo desconhecido")
                }
                items += Item(key, e.getString("t"), value)
            }
            if (items.isEmpty()) return Result.Invalid("backup vazio")
            return Result.Ok(items)
        } catch (ex: Exception) {
            return Result.Invalid("arquivo corrompido ou ilegível")
        }
    }

    /** Valida e só então grava, de uma vez. Devolve nulo se deu certo, ou o motivo da recusa. */
    fun import(prefs: SharedPreferences, text: String): String? {
        val parsed = parse(text)
        if (parsed !is Result.Ok) return (parsed as Result.Invalid).reason
        val edit = prefs.edit()
        for (it in parsed.items) {
            @Suppress("UNCHECKED_CAST")
            when (it.type) {
                "s" -> edit.putString(it.key, it.value as String)
                "b" -> edit.putBoolean(it.key, it.value as Boolean)
                "i" -> edit.putInt(it.key, it.value as Int)
                "l" -> edit.putLong(it.key, it.value as Long)
                else -> edit.putStringSet(it.key, it.value as Set<String>)
            }
        }
        return if (edit.commit()) null else "não foi possível gravar os ajustes"
    }
}
