package com.voidtouch.launcher.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import com.voidtouch.launcher.data.DeviceStats
import com.voidtouch.launcher.ui.theme.VoidColors
import com.voidtouch.launcher.ui.theme.VoidType
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val PtBr: Locale = Locale.forLanguageTag("pt-BR")
private val TimeFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm", PtBr)
private val MonthFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("MMMM", PtBr)
private val WeekdayFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("EEEE", PtBr)

/** Dia do mês por extenso, de 1 a 31. */
private val DayWords = listOf(
    "um", "dois", "três", "quatro", "cinco", "seis", "sete", "oito", "nove", "dez",
    "onze", "doze", "treze", "quatorze", "quinze", "dezesseis", "dezessete", "dezoito", "dezenove",
    "vinte", "vinte e um", "vinte e dois", "vinte e três", "vinte e quatro", "vinte e cinco",
    "vinte e seis", "vinte e sete", "vinte e oito", "vinte e nove", "trinta", "trinta e um",
)

private data class HeaderText(val time: String, val month: String, val dateLine: String)

/**
 * Cabeçalho da home: hora em 24 h, mês em letra manuscrita, dia da semana e dia do mês por
 * extenso, e uma linha com bateria e RAM. O horário e os números vêm de fora (a Activity os
 * atualiza só enquanto a tela está visível), então este componente não agenda nada sozinho.
 *
 * Segurar o dedo troca o fundo (provisório, até existir a tela de Ajustes).
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ClockHeader(
    nowMillis: Long,
    stats: DeviceStats?,
    onLongPress: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val minute = nowMillis / 60_000L
    val text = remember(minute) {
        val zoned = Instant.ofEpochMilli(nowMillis).atZone(ZoneId.systemDefault())
        val day = DayWords.getOrElse(zoned.dayOfMonth - 1) { zoned.dayOfMonth.toString() }
        HeaderText(
            time = zoned.format(TimeFormat),
            month = zoned.format(MonthFormat).replaceFirstChar { it.uppercase() },
            dateLine = "${zoned.format(WeekdayFormat)} · $day",
        )
    }
    val statsLine = remember(stats) { stats?.let(::formatStats) }
    val haptic = LocalHapticFeedback.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {},
                onLongClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onLongPress()
                },
            )
            .padding(start = 24.dp, end = 24.dp, top = 32.dp, bottom = 20.dp),
    ) {
        Text(text = text.time, style = VoidType.Clock, color = VoidColors.Text)
        Text(text = text.month, style = VoidType.Month, color = VoidColors.Text)
        Text(text = text.dateLine, style = VoidType.Caption, color = VoidColors.TextDim)
        if (statsLine != null) {
            Spacer(Modifier.height(12.dp))
            Text(text = statsLine, style = VoidType.Caption, color = VoidColors.TextDim)
        }
    }
}

private fun formatStats(stats: DeviceStats): String {
    val parts = mutableListOf<String>()
    if (stats.batteryPercent >= 0) {
        val charging = if (stats.charging) " (carregando)" else ""
        parts += "bateria ${stats.batteryPercent}%$charging"
    }
    if (stats.ramTotalMb > 0) {
        parts += String.format(PtBr, "ram %.1f GB livre", stats.ramFreeMb / 1024f)
    }
    return parts.joinToString("  ·  ")
}
