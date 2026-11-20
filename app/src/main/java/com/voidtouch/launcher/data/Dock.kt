package com.voidtouch.launcher.data

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/** Regras do modo carregando (relógio de cabeceira), puras para poderem ser testadas. */
object Dock {
    /** Tempo parado na home, carregando, até o relógio de cabeceira aparecer. */
    const val IDLE_MS = 15_000L

    private val ptBr: Locale = Locale.forLanguageTag("pt-BR")

    /** Só aparece ligado, carregando, na home, sem ter sido dispensado e depois de um tempo sem toque. */
    fun shouldShow(on: Boolean, charging: Boolean, onHome: Boolean, dismissed: Boolean, idleMs: Long): Boolean =
        on && charging && onHome && !dismissed && idleMs >= IDLE_MS

    /** Deslocamento em dp (0, 6 ou 12) que muda a cada minuto, para o relógio não ficar parado no mesmo ponto. */
    fun shift(minute: Long): Int = (Math.floorMod(minute, 3L)).toInt() * 6

    fun clockText(millis: Long, style: ClockStyle, zone: ZoneId = ZoneId.systemDefault()): String =
        DateTimeFormatter.ofPattern(style.pattern).format(Instant.ofEpochMilli(millis).atZone(zone))

    fun dateText(millis: Long, zone: ZoneId = ZoneId.systemDefault()): String =
        DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM", ptBr).format(Instant.ofEpochMilli(millis).atZone(zone))
}
