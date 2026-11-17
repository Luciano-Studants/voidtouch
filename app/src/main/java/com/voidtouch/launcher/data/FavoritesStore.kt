package com.voidtouch.launcher.data

import android.content.SharedPreferences

/** Guarda a lista de favoritos (chaves "pacote/atividade") na ordem em que foram fixados. */
class FavoritesStore(private val prefs: SharedPreferences) {

    fun load(): List<String> =
        prefs.getString(KEY, "").orEmpty().split('\n').filter { it.isNotBlank() }

    fun save(favorites: List<String>) {
        prefs.edit().putString(KEY, favorites.joinToString("\n")).apply()
    }

    companion object {
        const val MAX = 10
        private const val KEY = "favorites"
    }
}
