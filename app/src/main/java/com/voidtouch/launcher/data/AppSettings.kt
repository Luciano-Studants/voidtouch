package com.voidtouch.launcher.data

import android.content.SharedPreferences
import com.voidtouch.launcher.ui.theme.Backdrop

/** Como a hora aparece na página "hora" do carrossel. */
enum class ClockStyle(val key: String, val label: String, val pattern: String) {
    /** Estilo da prancheta de referência: 20H00. */
    H("h", "20H00", "HH'H'mm"),

    /** Dois pontos: 20:00. */
    Colon("colon", "20:00", "HH:mm");

    companion object {
        fun fromKey(key: String?): ClockStyle = entries.firstOrNull { it.key == key } ?: H
    }
}

/** Os dois gestos horizontais da home, cada um com um app configurável. */
/** Gestos da home que abrem um app ou ação: deslizar para os lados e para baixo, e toque duplo. */
enum class SwipeSide { Left, Right, Down, DoubleTap }

/**
 * Ajustes do usuário: fundo, formato da hora e o app aberto por cada deslize horizontal.
 * Os apps dos gestos são guardados pela chave "pacote/atividade"; nulo significa "nenhum".
 */
class SettingsStore(private val prefs: SharedPreferences) {

    fun loadBackdrop(): Backdrop = Backdrop.fromKey(prefs.getString(KEY_BACKDROP, null))

    fun saveBackdrop(backdrop: Backdrop) {
        prefs.edit().putString(KEY_BACKDROP, backdrop.key).apply()
    }

    fun loadClockStyle(): ClockStyle = ClockStyle.fromKey(prefs.getString(KEY_CLOCK_STYLE, null))

    fun saveClockStyle(style: ClockStyle) {
        prefs.edit().putString(KEY_CLOCK_STYLE, style.key).apply()
    }

    fun loadSwipeApp(side: SwipeSide): String? {
        val key = swipeKey(side)
        // Deslizar para baixo vem ligado ao painel de notificações até o usuário mudar.
        if (!prefs.contains(key)) {
            return if (side == SwipeSide.Down) GestureTarget.encode(QuickAction.NotificationPanel) else null
        }
        return prefs.getString(key, null)?.takeIf { it.isNotBlank() }
    }

    fun saveSwipeApp(side: SwipeSide, appKey: String?) {
        prefs.edit().putString(swipeKey(side), appKey.orEmpty()).apply()
    }

    fun loadNightEnabled(): Boolean = prefs.getBoolean(KEY_NIGHT_ON, false)

    fun saveNightEnabled(on: Boolean) {
        prefs.edit().putBoolean(KEY_NIGHT_ON, on).apply()
    }

    fun loadNightStart(): Int = prefs.getInt(KEY_NIGHT_START, NightMode.DEFAULT_START)

    fun loadNightEnd(): Int = prefs.getInt(KEY_NIGHT_END, NightMode.DEFAULT_END)

    fun saveNightHours(start: Int, end: Int) {
        prefs.edit().putInt(KEY_NIGHT_START, start).putInt(KEY_NIGHT_END, end).apply()
    }

    fun loadFocusAutoOn(): Boolean = prefs.getBoolean(KEY_FOCUS_AUTO_ON, false)

    fun saveFocusAutoOn(on: Boolean) {
        prefs.edit().putBoolean(KEY_FOCUS_AUTO_ON, on).apply()
    }

    fun loadFocusAutoStart(): Int = prefs.getInt(KEY_FOCUS_AUTO_START, FocusSchedule.DEFAULT_START)

    fun loadFocusAutoEnd(): Int = prefs.getInt(KEY_FOCUS_AUTO_END, FocusSchedule.DEFAULT_END)

    fun saveFocusAutoHours(start: Int, end: Int) {
        prefs.edit().putInt(KEY_FOCUS_AUTO_START, start).putInt(KEY_FOCUS_AUTO_END, end).apply()
    }

    /** Último estado da janela já aplicado ao foco; nulo = programação recém-ligada. */
    fun loadFocusAutoLast(): Boolean? =
        if (prefs.contains(KEY_FOCUS_AUTO_LAST)) prefs.getBoolean(KEY_FOCUS_AUTO_LAST, false) else null

    fun saveFocusAutoLast(value: Boolean?) {
        prefs.edit().apply {
            if (value == null) remove(KEY_FOCUS_AUTO_LAST) else putBoolean(KEY_FOCUS_AUTO_LAST, value)
        }.apply()
    }

    fun loadScenes(): List<Scene> = Scenes.decode(prefs.getString(KEY_SCENES, "").orEmpty())

    fun saveScenes(list: List<Scene>) {
        prefs.edit().putString(KEY_SCENES, Scenes.encode(list)).apply()
    }

    fun loadSceneAuto(): Boolean = prefs.getBoolean(KEY_SCENE_AUTO, false)

    fun saveSceneAuto(on: Boolean) {
        prefs.edit().putBoolean(KEY_SCENE_AUTO, on).apply()
    }

    fun loadSceneActive(): String? = prefs.getString(KEY_SCENE_ACTIVE, null)

    fun saveSceneActive(id: String?) {
        prefs.edit().apply { if (id == null) remove(KEY_SCENE_ACTIVE) else putString(KEY_SCENE_ACTIVE, id) }.apply()
    }

    /** Última cena que o horário escolheu sozinho; serve para agir só na virada, sem desfazer escolhas manuais. */
    fun loadSceneLastPick(): String? = prefs.getString(KEY_SCENE_LAST, null)

    fun saveSceneLastPick(id: String?) {
        prefs.edit().apply { if (id == null) remove(KEY_SCENE_LAST) else putString(KEY_SCENE_LAST, id) }.apply()
    }

    fun loadSceneDefaultPins(): List<String>? = Scenes.decodePins(prefs.getString(KEY_SCENE_DEFAULT_PINS, "").orEmpty())

    fun saveSceneDefaultPins(pins: List<String>?) {
        prefs.edit().putString(KEY_SCENE_DEFAULT_PINS, Scenes.encodePins(pins)).apply()
    }

    fun loadLaunchCounts(): String = prefs.getString(KEY_LAUNCH_COUNTS, "").orEmpty()

    fun saveLaunchCounts(json: String) {
        prefs.edit().putString(KEY_LAUNCH_COUNTS, json).apply()
    }

    fun loadReadLater(): List<LaterItem> = ReadLater.decode(prefs.getString(KEY_READ_LATER, "").orEmpty())

    fun saveReadLater(list: List<LaterItem>) {
        prefs.edit().putString(KEY_READ_LATER, ReadLater.encode(list)).apply()
    }

    fun loadMemoryMode(): MemoryMode = MemoryMode.fromKey(prefs.getString(KEY_MEMORY_MODE, null))

    fun saveMemoryMode(mode: MemoryMode) {
        prefs.edit().putString(KEY_MEMORY_MODE, mode.key).apply()
    }

    /** Fotos que o usuário mandou esconder; nunca mais aparecem. */
    fun loadHiddenPhotos(): Set<Long> =
        prefs.getStringSet(KEY_HIDDEN_PHOTOS, emptySet()).orEmpty().mapNotNull { it.toLongOrNull() }.toSet()

    fun saveHiddenPhotos(ids: Set<Long>) {
        prefs.edit().putStringSet(KEY_HIDDEN_PHOTOS, ids.map { it.toString() }.toSet()).apply()
    }

    private fun swipeKey(side: SwipeSide) = when (side) {
        SwipeSide.Left -> KEY_SWIPE_LEFT
        SwipeSide.Right -> KEY_SWIPE_RIGHT
        SwipeSide.Down -> KEY_SWIPE_DOWN
        SwipeSide.DoubleTap -> KEY_DOUBLE_TAP
    }

    private companion object {
        // Mesma chave usada desde a v0.2, para o fundo escolhido não se perder.
        const val KEY_BACKDROP = "backdrop"
        const val KEY_CLOCK_STYLE = "clock_style"
        const val KEY_NIGHT_ON = "night_on"
        const val KEY_NIGHT_START = "night_start"
        const val KEY_NIGHT_END = "night_end"
        const val KEY_FOCUS_AUTO_ON = "focus_auto_on"
        const val KEY_FOCUS_AUTO_START = "focus_auto_start"
        const val KEY_FOCUS_AUTO_END = "focus_auto_end"
        const val KEY_FOCUS_AUTO_LAST = "focus_auto_last"
        const val KEY_SCENES = "scenes"
        const val KEY_SCENE_AUTO = "scene_auto"
        const val KEY_SCENE_ACTIVE = "scene_active"
        const val KEY_SCENE_LAST = "scene_last_pick"
        const val KEY_SCENE_DEFAULT_PINS = "scene_default_pins"
        const val KEY_LAUNCH_COUNTS = "launch_counts"
        const val KEY_READ_LATER = "read_later"
        const val KEY_MEMORY_MODE = "memory_mode"
        const val KEY_HIDDEN_PHOTOS = "hidden_photos"
        const val KEY_SWIPE_LEFT = "swipe_left_app"
        const val KEY_SWIPE_RIGHT = "swipe_right_app"
        const val KEY_SWIPE_DOWN = "swipe_down_app"
        const val KEY_DOUBLE_TAP = "double_tap_app"
    }
}
