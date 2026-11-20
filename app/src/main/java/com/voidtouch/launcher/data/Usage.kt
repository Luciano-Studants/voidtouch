package com.voidtouch.launcher.data

import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import java.util.Calendar

/** Regras do relatório de uso, puras para poderem ser testadas. */
object UsageRules {
    private const val MIN = 60_000L

    /** "<1 min", "35 min" ou "2 h 05 min". */
    fun duration(ms: Long): String {
        val minutes = ms / MIN
        return when {
            minutes < 1L -> "<1 min"
            minutes < 60L -> "$minutes min"
            else -> "%d h %02d min".format(minutes / 60L, minutes % 60L)
        }
    }

    /** Os apps mais usados primeiro; some quem ficou abaixo de [minMs] (ruído). */
    fun top(totals: Map<String, Long>, limit: Int = 5, minMs: Long = MIN): List<Pair<String, Long>> =
        totals.entries.filter { it.value >= minMs }.sortedByDescending { it.value }.take(limit).map { it.key to it.value }

    /** Parte de 0 a 1 que [value] ocupa de [max]. */
    fun fraction(value: Long, max: Long): Float = if (max <= 0) 0f else (value.toFloat() / max).coerceIn(0f, 1f)

    /** Barra de texto: "█████·······". */
    fun bar(fraction: Float, width: Int = 12): String {
        val filled = Math.round(fraction.coerceIn(0f, 1f) * width)
        return "█".repeat(filled) + "·".repeat(width - filled)
    }

    /** Dia da semana curto a partir de Calendar.DAY_OF_WEEK (1 = domingo). */
    fun weekday(dayOfWeek: Int): String = listOf("dom", "seg", "ter", "qua", "qui", "sex", "sáb").getOrElse(dayOfWeek - 1) { "?" }

    /** Média por dia, ignorando dias sem nenhum uso medido (por exemplo, antes de ligar o acesso). */
    fun dailyAverage(days: List<Long>): Long {
        val used = days.filter { it > 0 }
        return if (used.isEmpty()) 0 else used.sum() / used.size
    }
}

/** Um dia do relatório: início do dia e tempo de tela. */
class UsageDay(val startMillis: Long, val dayOfWeek: Int, val totalMs: Long)

class UsageReport(
    val todayTotal: Long,
    val todayApps: List<Pair<String, Long>>,
    val days: List<UsageDay>,
    val weekTotal: Long,
    val weekApps: List<Pair<String, Long>>,
    /** Nome legível de cada pacote que aparece nas listas. */
    val labels: Map<String, String>,
)

/** Lê o tempo de tela por app. Só roda com a tela de relatório aberta e fora da thread principal. */
object UsageReader {

    fun read(context: Context): UsageReport? {
        if (!HeavyAppsReader.hasUsageAccess(context)) return null
        val usm = context.getSystemService(UsageStatsManager::class.java) ?: return null
        val skip = excludedPackages(context)
        val now = System.currentTimeMillis()

        val perDay = ArrayList<UsageDay>()
        val weekByApp = HashMap<String, Long>()
        var todayByApp: Map<String, Long> = emptyMap()
        for (back in 6 downTo 0) {
            val start = startOfDay(back)
            val end = if (back == 0) now else startOfDay(back - 1)
            val stats = runCatching { usm.queryAndAggregateUsageStats(start, end) }.getOrDefault(emptyMap())
            val byApp = stats.filter { it.key !in skip && it.value.totalTimeInForeground > 0 }
                .mapValues { it.value.totalTimeInForeground }
            byApp.forEach { (pkg, ms) -> weekByApp[pkg] = (weekByApp[pkg] ?: 0L) + ms }
            if (back == 0) todayByApp = byApp
            val cal = Calendar.getInstance().apply { timeInMillis = start }
            perDay += UsageDay(start, cal.get(Calendar.DAY_OF_WEEK), byApp.values.sum())
        }

        val todayTop = UsageRules.top(todayByApp)
        val weekTop = UsageRules.top(weekByApp)
        val pm = context.packageManager
        val labels = (todayTop + weekTop).map { it.first }.toSet().associateWith { pkg ->
            runCatching { pm.getApplicationLabel(pm.getApplicationInfo(pkg, 0)).toString() }.getOrDefault(pkg)
        }
        return UsageReport(todayByApp.values.sum(), todayTop, perDay, perDay.sumOf { it.totalMs }, weekTop, labels)
    }

    /** O próprio VoidTouch e as telas iniciais: o tempo na home não é "uso" de um app. */
    private fun excludedPackages(context: Context): Set<String> {
        val home = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
        val launchers = context.packageManager.queryIntentActivities(home, 0).map { it.activityInfo.packageName }
        return (launchers + context.packageName).toSet()
    }

    private fun startOfDay(daysBack: Int): Long = Calendar.getInstance().apply {
        add(Calendar.DAY_OF_YEAR, -daysBack)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis
}
