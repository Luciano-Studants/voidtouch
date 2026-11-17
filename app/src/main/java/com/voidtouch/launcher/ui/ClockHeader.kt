package com.voidtouch.launcher.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import com.voidtouch.launcher.ui.theme.VoidColors
import com.voidtouch.launcher.ui.theme.VoidType
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val PtBr: Locale = Locale.forLanguageTag("pt-BR")
private val TimeFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm", PtBr)
private val DateFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM", PtBr)

/**
 * Relógio 24 h e data em português. O horário vem de fora (atualizado pela Activity só enquanto
 * a tela está visível), então este componente não agenda nada por conta própria.
 *
 * Segurar o dedo troca o fundo (provisório, até existir a tela de Ajustes).
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ClockHeader(
    nowMillis: Long,
    onLongPress: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val minute = nowMillis / 60_000L
    val (time, date) = remember(minute) {
        val zoned = Instant.ofEpochMilli(nowMillis).atZone(ZoneId.systemDefault())
        zoned.format(TimeFormat) to zoned.format(DateFormat)
    }
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
            .padding(start = 24.dp, end = 24.dp, top = 40.dp, bottom = 32.dp),
    ) {
        Text(text = time, style = VoidType.Clock, color = VoidColors.Text)
        Text(text = date, style = VoidType.Date, color = VoidColors.TextDim)
    }
}
