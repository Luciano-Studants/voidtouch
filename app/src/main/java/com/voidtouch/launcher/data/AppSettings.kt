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
enum class SwipeSide { Left, Right }

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

    fun loadSwipeApp(side: SwipeSide): String? =
        prefs.getString(swipeKey(side), null)?.takeIf { it.isNotBlank() }

    fun saveSwipeApp(side: SwipeSide, appKey: String?) {
        prefs.edit().putString(swipeKey(side), appKey.orEmpty()).apply()
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
    }

    private companion object {
        // Mesma chave usada desde a v0.2, para o fundo escolhido não se perder.
        const val KEY_BACKDROP = "backdrop"
        const val KEY_CLOCK_STYLE = "clock_style"
        const val KEY_MEMORY_MODE = "memory_mode"
        const val KEY_HIDDEN_PHOTOS = "hidden_photos"
        const val KEY_SWIPE_LEFT = "swipe_left_app"
        const val KEY_SWIPE_RIGHT = "swipe_right_app"
    }
}
