package com.voidtouch.launcher

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import com.voidtouch.launcher.data.CrashRules

/**
 * Guarda os travamentos do app (em arquivo próprio, fora do backup) e decide quando entrar no modo seguro.
 * O registro é síncrono e protegido: se ele mesmo falhar, o erro original segue o caminho normal.
 */
object CrashGuard {
    private const val FILE = "voidtouch_crash"
    private const val KEY_TIMES = "times"
    private const val KEY_SAFE = "safe"
    private const val KEY_LAST = "last_error"
    private const val KEY_LAST_MS = "last_error_ms"
    private const val KEY_OK_CODE = "ok_code"

    private fun prefs(context: Context): SharedPreferences = context.getSharedPreferences(FILE, Context.MODE_PRIVATE)

    fun install(app: Application) {
        val previous = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, error ->
            runCatching { record(app, error) }
            previous?.uncaughtException(thread, error)
        }
    }

    @Suppress("DEPRECATION")
    private fun versionCode(context: Context): Long = runCatching {
        context.packageManager.getPackageInfo(context.packageName, 0).longVersionCode
    }.getOrDefault(0L)

    /** Verdadeiro até a versão instalada provar que roda bem por um tempo (veja [markHealthy]). */
    fun afterUpdate(context: Context): Boolean = prefs(context).getLong(KEY_OK_CODE, 0L) != versionCode(context)

    private fun record(context: Context, error: Throwable) {
        val p = prefs(context)
        val now = System.currentTimeMillis()
        val times = CrashRules.add(CrashRules.decode(p.getString(KEY_TIMES, null)), now)
        val summary = CrashRules.summary(error.javaClass.name, error.message, error.stackTrace.map { it.toString() })
        p.edit()
            .putString(KEY_TIMES, CrashRules.encode(times))
            .putString(KEY_LAST, summary)
            .putLong(KEY_LAST_MS, now)
            .apply { if (CrashRules.shouldEnterSafeMode(times, now, afterUpdate(context))) putBoolean(KEY_SAFE, true) }
            .commit()
    }

    fun inSafeMode(context: Context): Boolean = runCatching { prefs(context).getBoolean(KEY_SAFE, false) }.getOrDefault(false)

    fun lastError(context: Context): String? = prefs(context).getString(KEY_LAST, null)

    fun lastErrorMs(context: Context): Long = prefs(context).getLong(KEY_LAST_MS, 0L)

    /** A versão atual ficou de pé tempo suficiente: zera a contagem e deixa de tratá-la como recém-instalada. */
    fun markHealthy(context: Context) {
        prefs(context).edit().putString(KEY_TIMES, "").putLong(KEY_OK_CODE, versionCode(context)).apply()
    }

    /** Sai do modo seguro. Quem chama decide se desliga os recursos arriscados antes. */
    fun leaveSafeMode(context: Context) {
        prefs(context).edit().putBoolean(KEY_SAFE, false).putString(KEY_TIMES, "").commit()
    }
}
