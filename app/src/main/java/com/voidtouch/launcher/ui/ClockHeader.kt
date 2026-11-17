package com.voidtouch.launcher.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
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
    focusSinceMillis: Long,
    onStatsClick: () -> Unit,
    onFocusClick: () -> Unit,
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
    val focusOn = focusSinceMillis != 0L
    val focusLine = remember(minute, focusSinceMillis) { formatFocus(focusSinceMillis, nowMillis) }
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
            // Tocar na linha abre a tela "saúde do aparelho"; o ">" indica que é tocável.
            Text(
                text = "$statsLine  ›",
                style = VoidType.Caption,
                color = VoidColors.TextDim,
                modifier = Modifier
                    .padding(top = 4.dp)
                    .clickable(onClick = onStatsClick)
                    .padding(vertical = 8.dp),
            )
        }
        // Liga e desliga o modo foco com um toque. Ligado, a home fica mais calma: some a linha
        // de bateria e RAM e os apps marcados como distração.
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clickable {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onFocusClick()
                }
                .padding(vertical = 8.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .then(
                        if (focusOn) {
                            Modifier.background(VoidColors.Text, CircleShape)
                        } else {
                            Modifier.border(1.dp, VoidColors.TextDim, CircleShape)
                        },
                    ),
            )
            Spacer(Modifier.width(10.dp))
            Text(
                text = focusLine,
                style = VoidType.Caption,
                color = if (focusOn) VoidColors.Text else VoidColors.TextDim,
            )
        }
    }
}

/** "foco desligado" ou "foco ligado · 23 min" (e "1 h 05 min" depois da primeira hora). */
private fun formatFocus(sinceMillis: Long, nowMillis: Long): String {
    if (sinceMillis == 0L) return "foco desligado"
    val minutes = ((nowMillis - sinceMillis) / 60_000L).coerceAtLeast(0L)
    val elapsed = when {
        minutes < 1L -> "agora"
        minutes < 60L -> "$minutes min"
        else -> String.format(PtBr, "%d h %02d min", minutes / 60L, minutes % 60L)
    }
    return "foco ligado · $elapsed"
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
