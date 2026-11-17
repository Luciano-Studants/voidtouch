package com.voidtouch.launcher.data

import android.content.SharedPreferences

/**
 * Guarda o modo foco: quais apps distraem (chaves "pacote/atividade") e desde quando o foco
 * está ligado. Horário zero significa desligado.
 */
class FocusStore(private val prefs: SharedPreferences) {

    fun loadApps(): Set<String> =
        prefs.getString(KEY_APPS, "").orEmpty().split('\n').filter { it.isNotBlank() }.toSet()

    fun saveApps(apps: Set<String>) {
        prefs.edit().putString(KEY_APPS, apps.joinToString("\n")).apply()
    }

    fun loadSince(): Long = prefs.getLong(KEY_SINCE, 0L)

    fun saveSince(sinceMillis: Long) {
        prefs.edit().putLong(KEY_SINCE, sinceMillis).apply()
    }

    private companion object {
        const val KEY_APPS = "focus_apps"
        const val KEY_SINCE = "focus_since"
    }
}
