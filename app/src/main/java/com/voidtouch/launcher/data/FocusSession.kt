package com.voidtouch.launcher.data

/** Regras da sessão de foco (foco com hora para acabar), puras para poderem ser testadas. */
object FocusSession {
    /** Durações oferecidas, em minutos; 0 é "sem fim". */
    val CHOICES = listOf(25, 50, 90, 0)

    fun choiceLabel(minutes: Int): String = if (minutes == 0) "sem fim" else "$minutes min"

    /** Chegou a hora de acabar? Sessão sem fim (until = 0) nunca acaba sozinha. */
    fun expired(untilMillis: Long, now: Long): Boolean = untilMillis > 0 && now >= untilMillis

    /** Quanto falta, em milissegundos, nunca negativo. */
    fun remainingMs(untilMillis: Long, now: Long): Long = (untilMillis - now).coerceAtLeast(0)

    private fun minutesText(minutes: Long): String = when {
        minutes < 1L -> "agora"
        minutes < 60L -> "$minutes min"
        else -> "%d h %02d min".format(minutes / 60L, minutes % 60L)
    }

    /** "foco desligado", "foco ligado · 23 min" ou, com hora para acabar, "foco ligado · restam 12 min". */
    fun label(sinceMillis: Long, untilMillis: Long, now: Long): String {
        if (sinceMillis == 0L) return "foco desligado"
        if (untilMillis > 0) {
            val left = (remainingMs(untilMillis, now) + 59_999L) / 60_000L // arredonda para cima
            return "foco ligado · restam ${minutesText(left)}"
        }
        return "foco ligado · ${minutesText(((now - sinceMillis) / 60_000L).coerceAtLeast(0L))}"
    }

    /** Resumo ao terminar; nulo se durou menos de 1 minuto (não vale mostrar). */
    fun summary(durationMs: Long, waiting: Int): String? {
        val minutes = durationMs / 60_000L
        if (minutes < 1L) return null
        val notes = when (waiting) {
            0 -> "nenhuma notificação esperando"
            1 -> "1 notificação esperando"
            else -> "$waiting notificações esperando"
        }
        return "você focou ${minutesText(minutes)} · $notes"
    }
}
