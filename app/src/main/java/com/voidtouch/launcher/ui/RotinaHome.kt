package com.voidtouch.launcher.ui

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.voidtouch.launcher.data.AppEntry
import com.voidtouch.launcher.data.DeviceStats
import com.voidtouch.launcher.data.Favorite
import com.voidtouch.launcher.data.Folder
import com.voidtouch.launcher.data.Note
import com.voidtouch.launcher.data.RotinaTokens
import com.voidtouch.launcher.notify.NoteItem
import com.voidtouch.launcher.ui.theme.VoidColors
import com.voidtouch.launcher.ui.theme.VoidType
import com.voidtouch.launcher.ui.theme.card

/**
 * Corpo da home no design Rotina, abaixo do carrossel. Duas colunas: à esquerda os favoritos em
 * lista (ícone na lateral, como no design void); à direita os cartões de notificações, status,
 * foco e "agora". A tarefa do foco e o resumo da sessão ficam em cartões de largura inteira.
 */
@Composable
fun RotinaHomeBody(
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
    onOpenHealth: () -> Unit,
    focusOn: Boolean,
    night: Boolean,
    focusSummary: String?,
    onDismissFocusSummary: () -> Unit,
    focusSinceMillis: Long,
    focusUntilMillis: Long,
    nowMillis: Long,
    onToggleFocus: () -> Unit,
    onStartFocus: (Int) -> Unit,
    suggestions: List<AppEntry>,
    onLaunch: (AppEntry) -> Unit,
    focusNote: Note?,
    onNoteDone: (Note) -> Unit,
    onOpenCapture: () -> Unit,
    favorites: List<Favorite>,
    favoritesScroll: ScrollState,
    onOpenFolder: (Folder) -> Unit,
    onUnpin: (Favorite) -> Unit,
    modifier: Modifier = Modifier,
) {
    val margin = RotinaTokens.MARGIN_DP.dp
    val fullCard = Modifier.fillMaxWidth().padding(horizontal = margin, vertical = 4.dp).card()
    Column(modifier = modifier.fillMaxSize()) {
        if (focusSummary != null && !night) Box(fullCard) { FocusSummaryLine(focusSummary, onDismissFocusSummary) }
        if (!night && focusNote != null) Box(fullCard) { TaskLine(focusNote, onNoteDone, onOpenCapture) }

        Row(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = margin - 3.dp, vertical = 2.dp),
        ) {
            // Esquerda: favoritos em lista.
            BoxWithConstraints(modifier = Modifier.weight(1f).fillMaxHeight()) {
                val rowHeight = (maxHeight / favorites.size.coerceAtLeast(1)).coerceIn(36.dp, 52.dp)
                Column(modifier = Modifier.fillMaxSize().verticalScroll(favoritesScroll)) {
                    if (favorites.isEmpty()) {
                        Text(
                            text = if (focusOn) "modo foco ligado: nenhum favorito liberado" else "deslize para cima, segure um app\npara fixar aqui",
                            style = VoidType.Caption,
                            color = VoidColors.TextDim,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
                        )
                    } else {
                        favorites.forEach { item ->
                            FavoriteRow(
                                item = item,
                                height = rowHeight,
                                onOpen = {
                                    when (item) {
                                        is Favorite.App -> onLaunch(item.app)
                                        is Favorite.FolderItem -> onOpenFolder(item.folder)
                                    }
                                },
                                onUnpin = { onUnpin(item) },
                            )
                        }
                    }
                }
            }

            // Direita: cartões de informação.
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .weight(1.2f)
                    .fillMaxHeight()
                    .padding(start = 3.dp, top = 3.dp, bottom = 3.dp, end = 3.dp)
                    .verticalScroll(rememberScrollState()),
            ) {
                if (!focusOn && !night) {
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
                        maxGroups = 2,
                        modifier = Modifier.fillMaxWidth().card().padding(12.dp),
                    )
                    StatGrid(stats, onOpenHealth, Modifier.fillMaxWidth().height(124.dp))
                }
                FocusLine(
                    focusSinceMillis = focusSinceMillis,
                    focusUntilMillis = focusUntilMillis,
                    nowMillis = nowMillis,
                    onClick = onToggleFocus,
                    onStartSession = onStartFocus,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).card(),
                )
                if (!night && suggestions.isNotEmpty()) {
                    NowLine(
                        suggestions.take(2),
                        onLaunch,
                        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp).card(),
                    )
                }
            }
        }
    }
}
