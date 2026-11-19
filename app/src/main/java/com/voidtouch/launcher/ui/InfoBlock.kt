package com.voidtouch.launcher.ui

import android.content.Context
import android.text.format.Formatter
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import com.voidtouch.launcher.data.DeviceStats
import com.voidtouch.launcher.data.NetworkKind
import com.voidtouch.launcher.data.NoteGroups
import com.voidtouch.launcher.notify.NoteItem
import androidx.compose.ui.text.style.TextOverflow
import com.voidtouch.launcher.ui.theme.VoidColors
import com.voidtouch.launcher.ui.theme.VoidType
import java.util.Locale

private val PtBr: Locale = Locale.forLanguageTag("pt-BR")

/**
 * Duas colunas sob o carrossel, como na prancheta de referência: notificações à esquerda e
 * status do celular à direita. Tocar no status abre a tela "saúde do aparelho".
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun InfoBlock(
    stats: DeviceStats?,
    notifications: List<NoteItem>,
    notificationsEnabled: Boolean,
    onNotificationClick: (NoteItem) -> Unit,
    onNotificationDismiss: (NoteItem) -> Unit,
    onNotificationReply: (NoteItem, String) -> Unit,
    onNotificationLater: (NoteItem) -> Unit,
    laterCount: Int,
    onOpenLater: () -> Unit,
    onEnableNotifications: () -> Unit,
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
            Text(text = "notificações", style = VoidType.Caption, color = VoidColors.TextDim)
            val haptic = LocalHapticFeedback.current
            if (!notificationsEnabled) {
                Text(
                    text = "ativar acesso ›",
                    style = VoidType.Small,
                    color = VoidColors.Text,
                    modifier = Modifier.clickable(onClick = onEnableNotifications).padding(top = 3.dp, bottom = 3.dp),
                )
            } else if (notifications.isEmpty()) {
                Text("nenhuma", style = VoidType.Small, color = VoidColors.TextDim, modifier = Modifier.padding(top = 3.dp))
            } else {
                val groups = remember(notifications) { NoteGroups.group(notifications) { it.packageName + "|" + it.title } }
                var panelFor by remember { mutableStateOf<String?>(null) }
                groups.take(3).forEach { g ->
                    val n = g.first
                    Column(
                        modifier = Modifier
                            .combinedClickable(
                                onClick = { onNotificationClick(n) },
                                onLongClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    panelFor = if (panelFor == n.key) null else n.key
                                },
                            )
                            .padding(vertical = 4.dp),
                    ) {
                        Text(
                            text = n.appLabel,
                            style = VoidType.Caption,
                            color = VoidColors.TextDim,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = if (g.count > 1) "${n.title} (${g.count})" else n.title,
                            style = VoidType.Small,
                            color = VoidColors.Text,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        if (n.text.isNotBlank()) {
                            Text(
                                text = n.text,
                                style = VoidType.Caption,
                                color = VoidColors.TextDim,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                    if (panelFor == n.key) {
                        NotePanel(
                            item = n,
                            onReply = { text -> onNotificationReply(n, text); panelFor = null },
                            onLater = { onNotificationLater(n); panelFor = null },
                            onDismiss = { g.items.forEach(onNotificationDismiss); panelFor = null },
                        )
                    }
                }
                if (groups.size > 3) {
                    Text("+${groups.size - 3}", style = VoidType.Small, color = VoidColors.TextDim)
                }
            }
            if (laterCount > 0) {
                Text(
                    text = "ler depois ($laterCount) ›",
                    style = VoidType.Caption,
                    color = VoidColors.Text,
                    modifier = Modifier.clickable(onClick = onOpenLater).padding(top = 6.dp, bottom = 2.dp),
                )
            }
        }
        // Só o bloco de texto abre a saúde do aparelho; o espaço vazio em volta fica livre para os gestos.
        Box(modifier = Modifier.weight(1f)) {
            Column(modifier = Modifier.clickable(onClick = onStatusClick)) {
                Text(text = "status do celular ›", style = VoidType.Date, color = VoidColors.TextDim)
                lines.forEach { line ->
                    Text(
                        text = line,
                        style = VoidType.Small,
                        color = VoidColors.Text,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
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

/** Opções de uma notificação (segurar): responder sem abrir o app, guardar para depois ou dispensar. */
@Composable
private fun NotePanel(item: NoteItem, onReply: (String) -> Unit, onLater: () -> Unit, onDismiss: () -> Unit) {
    var replying by remember { mutableStateOf(false) }
    var text by remember { mutableStateOf("") }
    Column(modifier = Modifier.padding(bottom = 6.dp)) {
        Row {
            if (item.replyInput != null) {
                PanelButton("responder") { replying = !replying }
                Spacer(Modifier.width(14.dp))
            }
            PanelButton("ler depois", onLater)
            Spacer(Modifier.width(14.dp))
            PanelButton("dispensar", onDismiss)
        }
        if (replying) {
            SearchField(
                query = text,
                onQueryChange = { text = it },
                requestFocus = true,
                onGo = { if (text.isNotBlank()) onReply(text.trim()) },
                placeholder = "responder…",
            )
            if (text.isNotBlank()) PanelButton("enviar") { onReply(text.trim()) }
        }
    }
}

@Composable
private fun PanelButton(label: String, onClick: () -> Unit) {
    Text(
        text = label,
        style = VoidType.Caption,
        color = VoidColors.Text,
        modifier = Modifier.clickable(onClick = onClick).padding(vertical = 6.dp),
    )
}
