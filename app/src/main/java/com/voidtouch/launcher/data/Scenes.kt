package com.voidtouch.launcher.data

import org.json.JSONArray
import org.json.JSONObject

/**
 * Uma cena é um jeito de a home ficar: horário em que vale, se liga o foco, se deixa a home calma
 * (visual do modo noite) e quais favoritos mostra. Cada cena lembra os próprios favoritos.
 */
data class Scene(
    val id: String,
    val name: String,
    val start: Int,
    val end: Int,
    val focus: Boolean = false,
    val quiet: Boolean = false,
    /** Favoritos da cena; nulo = ainda não salvou, então usa os favoritos de quando entrar. */
    val pins: List<String>? = null,
)

/** Resultado de trocar de cena: cenas e favoritos padrão atualizados e os favoritos a mostrar. */
data class SceneSwitch(val scenes: List<Scene>, val defaultPins: List<String>?, val pins: List<String>)

/** Regras das cenas, puras (sem Android) para poderem ser testadas. */
object Scenes {
    const val MAX = 8

    /** Nomes e horários sugeridos para novas cenas. */
    val PRESETS = listOf(
        Triple("manhã", 6, 12),
        Triple("trabalho", 12, 18),
        Triple("noite", 18, 0),
        Triple("madrugada", 0, 6),
    )

    /** A primeira cena cujo horário contém [hour]; nulo se nenhuma. */
    fun pick(scenes: List<Scene>, hour: Int): Scene? =
        scenes.firstOrNull { NightMode.isNight(hour, it.start, it.end) }

    /** Nova cena a partir do próximo modelo ainda não usado. Nulo se já chegou ao limite. */
    fun create(scenes: List<Scene>, newId: String): Scene? {
        if (scenes.size >= MAX) return null
        val used = scenes.map { it.name }.toSet()
        val preset = PRESETS.firstOrNull { it.first !in used }
        return if (preset != null) {
            Scene(newId, preset.first, preset.second, preset.third, focus = preset.first == "noite", quiet = preset.first == "madrugada")
        } else {
            Scene(newId, "cena ${scenes.size + 1}", 9, 18)
        }
    }

    /**
     * Troca da cena [fromId] para [toId] (nulo = sem cena). Os favoritos de agora ficam guardados
     * na cena que sai; a que entra mostra os dela, ou mantém os de agora se ainda não tem.
     */
    fun switch(
        scenes: List<Scene>,
        defaultPins: List<String>?,
        fromId: String?,
        toId: String?,
        currentPins: List<String>,
    ): SceneSwitch {
        if (fromId == toId) return SceneSwitch(scenes, defaultPins, currentPins)
        var newScenes = scenes
        var newDefault = defaultPins
        if (fromId == null) newDefault = currentPins
        else newScenes = scenes.map { if (it.id == fromId) it.copy(pins = currentPins) else it }
        val target = toId?.let { id -> newScenes.firstOrNull { it.id == id } }
        val pins = if (target != null) target.pins ?: currentPins else newDefault ?: currentPins
        return SceneSwitch(newScenes, newDefault, pins)
    }

    fun encode(scenes: List<Scene>): String {
        val arr = JSONArray()
        scenes.forEach { s ->
            val o = JSONObject().put("id", s.id).put("name", s.name).put("start", s.start).put("end", s.end)
                .put("focus", s.focus).put("quiet", s.quiet)
            s.pins?.let { o.put("pins", JSONArray(it)) }
            arr.put(o)
        }
        return arr.toString()
    }

    /** Lê as cenas guardadas; texto inválido vira lista vazia, e valores fora do limite são corrigidos. */
    fun decode(text: String): List<Scene> = runCatching {
        val arr = JSONArray(text)
        (0 until minOf(arr.length(), MAX)).map { i ->
            val o = arr.getJSONObject(i)
            Scene(
                id = o.getString("id"),
                name = o.getString("name").take(24),
                start = o.getInt("start").coerceIn(0, 23),
                end = o.getInt("end").coerceIn(0, 23),
                focus = o.optBoolean("focus"),
                quiet = o.optBoolean("quiet"),
                pins = o.optJSONArray("pins")?.let { p -> (0 until p.length()).map { p.getString(it) } },
            )
        }
    }.getOrDefault(emptyList())

    fun encodePins(pins: List<String>?): String = pins?.let { JSONArray(it).toString() }.orEmpty()

    fun decodePins(text: String): List<String>? = if (text.isBlank()) null else runCatching {
        val a = JSONArray(text)
        (0 until a.length()).map { a.getString(it) }
    }.getOrNull()
}
