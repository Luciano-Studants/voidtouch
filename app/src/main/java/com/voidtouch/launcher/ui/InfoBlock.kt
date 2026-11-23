package com.voidtouch.launcher.ui

import android.content.Context
import android.text.format.Formatter
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
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
import androidx.compose.ui.unit.sp
import com.voidtouch.launcher.data.AppEntry
import com.voidtouch.launcher.data.DeviceStats
import com.voidtouch.launcher.data.FocusSession
import com.voidtouch.launcher.data.NetworkKind
import com.voidtouch.launcher.data.Note
import com.voidtouch.launcher.data.NoteGroups
import com.voidtouch.launcher.notify.NoteItem
import androidx.compose.ui.text.style.TextOverflow
import com.voidtouch.launcher.data.RotinaTokens
import com.voidtouch.launcher.ui.theme.VoidColors
import com.voidtouch.launcher.ui.theme.card
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
        NotificationsPanel(
            notifications = notifications,
            notificationsEnabled = notificationsEnabled,
            onNotificationClick = onNotificationClick,
            onNotificationDismiss = onNotificationDismiss,
            onNotificationReply = onNotificationReply,
            onNotificationLater = onNotificationLater,
            laterCount = laterCount,
            onOpenLater = onOpenLater,
            onEnableNotifications = onEnableNotifications,
            modifier = Modifier.weight(1f).padding(end = 12.dp),
        )
        // Só o bloco de texto abre a saúde do aparelho; o espaço vazio em volta fica livre para os gestos.
        Box(modifier = Modifier.weight(1f)) {
            Column(modifier = Modifier.clickable(role = androidx.compose.ui.semantics.Role.Button, onClick = onStatusClick)) {
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

/** Lista de notificações agrupadas, com o painel de opções (segurar). Usada nos dois designs. */
@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun NotificationsPanel(
    notifications: List<NoteItem>,
    notificationsEnabled: Boolean,
    onNotificationClick: (NoteItem) -> Unit,
    onNotificationDismiss: (NoteItem) -> Unit,
    onNotificationReply: (NoteItem, String) -> Unit,
    onNotificationLater: (NoteItem) -> Unit,
    laterCount: Int,
    onOpenLater: () -> Unit,
    onEnableNotifications: () -> Unit,
    modifier: Modifier = Modifier,
    maxGroups: Int = 3,
) {
    Column(modifier = modifier) {
        Text(text = "notificações", style = VoidType.Caption, color = VoidColors.TextDim)
        val haptic = LocalHapticFeedback.current
        if (!notificationsEnabled) {
            Text(
                text = "ativar acesso ›",
                style = VoidType.Small,
                color = VoidColors.Text,
                modifier = Modifier.clickable(role = androidx.compose.ui.semantics.Role.Button, onClick = onEnableNotifications).padding(top = 3.dp, bottom = 3.dp),
            )
        } else if (notifications.isEmpty()) {
            Text("nenhuma", style = VoidType.Small, color = VoidColors.TextDim, modifier = Modifier.padding(top = 3.dp))
        } else {
            val groups = remember(notifications) { NoteGroups.group(notifications) { it.packageName + "|" + it.title } }
            var panelFor by remember { mutableStateOf<String?>(null) }
            groups.take(maxGroups).forEach { g ->
                val n = g.first
                Column(
                    modifier = Modifier
                        .combinedClickable(
            role = androidx.compose.ui.semantics.Role.Button,
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
            if (groups.size > maxGroups) {
                Text("+${groups.size - maxGroups}", style = VoidType.Small, color = VoidColors.TextDim)
            }
        }
        if (laterCount > 0) {
            Text(
                text = "ler depois ($laterCount) ›",
                style = VoidType.Caption,
                color = VoidColors.Text,
                modifier = Modifier.clickable(role = androidx.compose.ui.semantics.Role.Button, onClick = onOpenLater).padding(top = 6.dp, bottom = 2.dp),
            )
        }
    }
}

/** Recuo lateral do texto das linhas da home: menor dentro dos cartões do design Rotina. */
internal val LineInset: androidx.compose.ui.unit.Dp get() = if (VoidColors.isRotina) 14.dp else 24.dp

private data class StatTileData(val label: String, val value: String)

private fun statTiles(context: Context, s: DeviceStats): List<StatTileData> = buildList {
    if (s.batteryPercent >= 0) add(StatTileData(if (s.charging) "carregando" else "bateria", "${s.batteryPercent}%"))
    if (s.ramTotalMb > 0) add(StatTileData("ram livre", String.format(PtBr, "%.1f GB", s.ramFreeMb / 1024f)))
    add(StatTileData("disco livre", Formatter.formatShortFileSize(context, s.storageFreeBytes.coerceAtLeast(0L))))
    val net = when (s.network) {
        NetworkKind.WiFi -> "wi-fi"
        NetworkKind.Mobile -> "dados"
        NetworkKind.Other -> "ativa"
        NetworkKind.None -> "desligada"
    }
    add(StatTileData(s.temperatureC?.let { String.format(PtBr, "rede · %.0f °C", it) } ?: "rede", net))
}

/** Design Rotina: o status do celular em quatro cartões de número. Toque abre a saúde do aparelho. */
@Composable
internal fun StatGrid(stats: DeviceStats?, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val tiles = remember(stats) { stats?.let { statTiles(context, it) }.orEmpty() }
    Column(
        modifier = modifier.clickable(role = androidx.compose.ui.semantics.Role.Button, onClick = onClick),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        tiles.chunked(2).forEach { pair ->
            Row(modifier = Modifier.weight(1f).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                pair.forEach { StatTile(it, Modifier.weight(1f).fillMaxHeight()) }
                if (pair.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun StatTile(tile: StatTileData, modifier: Modifier = Modifier) {
    Column(
        verticalArrangement = Arrangement.Center,
        modifier = modifier.card(14.dp).padding(horizontal = 12.dp, vertical = 6.dp),
    ) {
        Text(tile.label, style = VoidType.Caption.copy(fontSize = 11.sp), color = VoidColors.TextDim, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(
            tile.value,
            style = VoidType.Day.copy(fontSize = 20.sp, lineHeight = 24.sp, letterSpacing = 0.sp),
            color = VoidColors.Text,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
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
 * Linha do modo foco. Toque liga e desliga; segurar (com o foco desligado) oferece uma sessão
 * com hora para acabar: 25, 50, 90 minutos ou sem fim. Ligado, a home fica mais calma: some o
 * bloco de notificações e status e somem os apps marcados como distração.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FocusLine(
    focusSinceMillis: Long,
    focusUntilMillis: Long,
    nowMillis: Long,
    onClick: () -> Unit,
    onStartSession: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val minute = nowMillis / 60_000L
    val focusOn = focusSinceMillis != 0L
    val label = remember(minute, focusSinceMillis, focusUntilMillis) {
        FocusSession.label(focusSinceMillis, focusUntilMillis, nowMillis)
    }
    val haptic = LocalHapticFeedback.current
    var choosing by remember { mutableStateOf(false) }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.padding(horizontal = LineInset, vertical = 2.dp),
    ) {
        if (choosing && !focusOn) {
            Text("foco por", style = VoidType.Caption, color = VoidColors.TextDim)
            FocusSession.CHOICES.forEach { minutes ->
                Text(
                    text = FocusSession.choiceLabel(minutes),
                    style = VoidType.Caption,
                    color = VoidColors.Text,
                    modifier = Modifier
                        .clickable(role = androidx.compose.ui.semantics.Role.Button) {
                            choosing = false
                            onStartSession(minutes)
                        }
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                )
            }
            Text(
                text = "×",
                style = VoidType.Caption,
                color = VoidColors.TextDim,
                modifier = Modifier.clickable(role = androidx.compose.ui.semantics.Role.Button) { choosing = false }.padding(horizontal = 8.dp, vertical = 8.dp),
            )
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .combinedClickable(
                role = androidx.compose.ui.semantics.Role.Button,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onClick()
                        },
                        onLongClick = {
                            if (!focusOn) {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                choosing = true
                            }
                        },
                    )
                    .padding(vertical = 6.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .then(
                            if (focusOn) {
                                Modifier.background(VoidColors.Text, CircleShape)
                            } else {
                                Modifier.border(VoidColors.Stroke, VoidColors.TextDim, CircleShape)
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
    }
}

/** Resumo de uma sessão de foco que acabou. Toque dispensa. */
@Composable
fun FocusSummaryLine(text: String, onDismiss: () -> Unit) {
    Text(
        text = "$text ×",
        style = VoidType.Caption,
        color = VoidColors.Text,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(role = androidx.compose.ui.semantics.Role.Button, onClick = onDismiss)
            .padding(horizontal = LineInset, vertical = 8.dp),
    )
}

/** Os apps mais prováveis para este horário, em uma linha. Só o texto responde ao toque. */
@Composable
fun NowLine(apps: List<AppEntry>, onLaunch: (AppEntry) -> Unit, modifier: Modifier = Modifier) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.fillMaxWidth().padding(horizontal = LineInset, vertical = 2.dp),
    ) {
        Text("agora", style = VoidType.Caption, color = VoidColors.TextDim)
        apps.forEach { app ->
            Spacer(Modifier.width(14.dp))
            Text(
                text = app.label,
                style = VoidType.Small,
                color = VoidColors.Text,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false).clickable(role = androidx.compose.ui.semantics.Role.Button) { onLaunch(app) }.heightIn(min = 44.dp).wrapContentHeight(Alignment.CenterVertically).padding(vertical = 6.dp),
            )
        }
    }
}

/** A tarefa do foco na home. Toque marca como feita; segurar abre as anotações. Só o texto responde. */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TaskLine(note: Note, onDone: (Note) -> Unit, onOpen: () -> Unit) {
    val haptic = LocalHapticFeedback.current
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().padding(horizontal = LineInset, vertical = 2.dp),
    ) {
        Text(
            text = "● ${note.text}",
            style = VoidType.Small,
            color = VoidColors.Text,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .weight(1f, fill = false)
                .combinedClickable(
                role = androidx.compose.ui.semantics.Role.Button,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onDone(note)
                    },
                    onLongClick = onOpen,
                )
                .padding(vertical = 6.dp),
        )
    }
}

/** Opções de uma notificação (segurar): responder sem abrir o app, guardar para depois ou dispensar. */
@Composable
private fun NotePanel(item: NoteItem, onReply: (String) -> Unit, onLater: () -> Unit, onDismiss: () -> Unit) {
    var replying by remember { mutableStateOf(false) }
    var text by remember { mutableStateOf("") }
    Column(modifier = Modifier.padding(bottom = 6.dp)) {
        @OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
        androidx.compose.foundation.layout.FlowRow(horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(14.dp)) {
            if (item.replyInput != null) PanelButton("responder") { replying = !replying }
            PanelButton("ler depois", onLater)
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
        maxLines = 1,
        softWrap = false,
        style = VoidType.Caption,
        color = VoidColors.Text,
        modifier = Modifier.clickable(role = androidx.compose.ui.semantics.Role.Button, onClick = onClick).heightIn(min = 44.dp).padding(vertical = 6.dp),
    )
}
