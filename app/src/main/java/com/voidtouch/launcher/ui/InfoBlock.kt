package com.voidtouch.launcher.ui

import android.content.Context
import android.text.format.Formatter
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import com.voidtouch.launcher.data.DeviceStats
import com.voidtouch.launcher.data.NetworkKind
import com.voidtouch.launcher.ui.theme.VoidColors
import com.voidtouch.launcher.ui.theme.VoidType
import java.util.Locale

private val PtBr: Locale = Locale.forLanguageTag("pt-BR")

/**
 * Duas colunas sob o carrossel, como na prancheta de referência: notificações à esquerda e
 * status do celular à direita. Tocar no status abre a tela "saúde do aparelho".
 */
@Composable
fun InfoBlock(
    stats: DeviceStats?,
    onStatusClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val lines = remember(stats) { stats?.let { statusLines(context, it) }.orEmpty() }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 10.dp),
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
            Text(text = "notificações", style = VoidType.Small, color = VoidColors.TextDim)
            // As notificações chegam na v0.9, junto com a permissão de acesso a elas.
            Text(
                text = "em breve (v0.9)",
                style = VoidType.Small,
                color = VoidColors.TextDim,
                modifier = Modifier.padding(top = 3.dp),
            )
        }
        Column(modifier = Modifier.weight(1f).clickable(onClick = onStatusClick)) {
            Text(text = "status do celular ›", style = VoidType.Small, color = VoidColors.TextDim)
            lines.forEach { line ->
                Text(
                    text = line,
                    style = VoidType.Small,
                    color = VoidColors.Text,
                    modifier = Modifier.padding(top = 3.dp),
                )
            }
        }
    }
}

private fun statusLines(context: Context, s: DeviceStats): List<String> = buildList {
    if (s.batteryPercent >= 0) {
        add(if (s.charging) "carregando ${s.batteryPercent}%" else "bateria ${s.batteryPercent}%")
    }
    if (s.ramTotalMb > 0) {
        add(String.format(PtBr, "ram %.1f GB livre", s.ramFreeMb / 1024f))
    }
    add("disco ${Formatter.formatShortFileSize(context, s.storageFreeBytes.coerceAtLeast(0L))} livre")
    add(
        "rede " + when (s.network) {
            NetworkKind.WiFi -> "wi-fi"
            NetworkKind.Mobile -> "dados"
            NetworkKind.Other -> "ativa"
            NetworkKind.None -> "desligada"
        },
    )
    s.temperatureC?.let { add(String.format(PtBr, "temp %.1f °C", it)) }
}

/**
 * Liga e desliga o modo foco com um toque. Ligado, a home fica mais calma: some o bloco de
 * notificações e status e somem os apps marcados como distração.
 */
@Composable
fun FocusLine(
    focusSinceMillis: Long,
    nowMillis: Long,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val minute = nowMillis / 60_000L
    val focusOn = focusSinceMillis != 0L
    val label = remember(minute, focusSinceMillis) { formatFocus(focusSinceMillis, nowMillis) }
    val haptic = LocalHapticFeedback.current

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .clickable {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onClick()
            }
            .padding(horizontal = 24.dp, vertical = 8.dp),
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
            text = label,
            style = VoidType.Caption,
            color = if (focusOn) VoidColors.Text else VoidColors.TextDim,
        )
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
