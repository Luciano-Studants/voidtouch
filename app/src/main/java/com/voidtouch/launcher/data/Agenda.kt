package com.voidtouch.launcher.data

import android.Manifest
import android.content.ContentUris
import android.content.Context
import android.content.pm.PackageManager
import android.provider.CalendarContract
import androidx.core.content.ContextCompat
import java.time.DayOfWeek
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

/** Um compromisso do calendário do celular. */
data class AgendaItem(val title: String, val begin: Long, val end: Long, val allDay: Boolean)

/**
 * Próximos compromissos para a página "agenda" do carrossel. Lê só o calendário do aparelho, só
 * quando você liberou a permissão, e nada sai do celular.
 */
object Agenda {
    private val PtBr: Locale = Locale.forLanguageTag("pt-BR")
    private val Hour: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    private const val DAY = 24L * 60L * 60L * 1000L

    fun hasAccess(context: Context): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALENDAR) == PackageManager.PERMISSION_GRANTED

    /** Compromissos dos próximos 7 dias que ainda não terminaram, do mais próximo ao mais distante. */
    fun read(context: Context, nowMillis: Long, limit: Int = 4): List<AgendaItem> = runCatching {
        if (!hasAccess(context)) return emptyList()
        val uri = CalendarContract.Instances.CONTENT_URI.buildUpon().also {
            ContentUris.appendId(it, nowMillis)
            ContentUris.appendId(it, nowMillis + 7 * DAY)
        }.build()
        val items = ArrayList<AgendaItem>()
        context.contentResolver.query(
            uri,
            arrayOf(
                CalendarContract.Instances.TITLE,
                CalendarContract.Instances.BEGIN,
                CalendarContract.Instances.END,
                CalendarContract.Instances.ALL_DAY,
            ),
            null,
            null,
            "${CalendarContract.Instances.BEGIN} ASC",
        )?.use { c ->
            while (c.moveToNext() && items.size < limit * 3) {
                val end = c.getLong(2)
                if (end <= nowMillis) continue
                items += AgendaItem(
                    title = c.getString(0)?.takeIf { it.isNotBlank() } ?: "(sem título)",
                    begin = c.getLong(1),
                    end = end,
                    allDay = c.getInt(3) == 1,
                )
            }
        }
        items.take(limit)
    }.getOrDefault(emptyList())

    /** "hoje 14:30", "amanhã 09:00", "sex 10:00", "agora até 15:00" ou "hoje · dia todo". */
    fun label(item: AgendaItem, nowMillis: Long, zone: ZoneId): String {
        val today = Instant.ofEpochMilli(nowMillis).atZone(zone).toLocalDate()
        // Eventos de dia inteiro vêm em UTC; os outros, no fuso do aparelho.
        val start = Instant.ofEpochMilli(item.begin).atZone(if (item.allDay) ZoneOffset.UTC else zone)
        val dayWord = when (start.toLocalDate().toEpochDay() - today.toEpochDay()) {
            0L -> "hoje"
            1L -> "amanhã"
            else -> start.dayOfWeek.shortName() + " " + start.dayOfMonth
        }
        return when {
            item.allDay -> "$dayWord · dia todo"
            item.begin <= nowMillis -> "agora até " + Instant.ofEpochMilli(item.end).atZone(zone).format(Hour)
            else -> "$dayWord " + start.format(Hour)
        }
    }

    private fun DayOfWeek.shortName(): String = getDisplayName(TextStyle.SHORT, PtBr).trimEnd('.')
}
