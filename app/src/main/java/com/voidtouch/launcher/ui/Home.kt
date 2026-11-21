package com.voidtouch.launcher.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.coerceIn
import androidx.compose.ui.unit.dp
import com.voidtouch.launcher.data.AgendaItem
import com.voidtouch.launcher.data.AppEntry
import com.voidtouch.launcher.data.ClockStyle
import com.voidtouch.launcher.data.Favorite
import com.voidtouch.launcher.data.Folder
import com.voidtouch.launcher.data.DeviceStats
import com.voidtouch.launcher.data.IconLoader
import com.voidtouch.launcher.data.IconResult
import com.voidtouch.launcher.data.MemoryShown
import com.voidtouch.launcher.data.Note
import com.voidtouch.launcher.notify.LiveItem
import com.voidtouch.launcher.notify.NoteItem
import com.voidtouch.launcher.ui.theme.VoidColors
import com.voidtouch.launcher.ui.theme.VoidType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Home, seguindo as pranchetas de referência: carrossel no topo (hora, dia, música), duas
 * colunas de notificações e status, a linha do modo foco e a lista de favoritos.
 *
 * Gestos: deslizar para cima abre a lista de apps; deslizar para os lados **abaixo do
 * carrossel** abre os apps configurados nos Ajustes. Dentro do carrossel, deslizar para os
 * lados troca de página (por isso os dois gestos não se misturam).
 */
@Composable
fun HomeContent(
    nowMillis: Long,
    stats: DeviceStats?,
    clockStyle: ClockStyle,
    notifications: List<NoteItem>,
    notificationsEnabled: Boolean,
    onNotificationClick: (NoteItem) -> Unit,
    onNotificationDismiss: (NoteItem) -> Unit,
    onNotificationReply: (NoteItem, String) -> Unit,
    onNotificationLater: (NoteItem) -> Unit,
    laterCount: Int,
    onOpenLater: () -> Unit,
    onEnableNotifications: () -> Unit,
    active: Boolean,
    agenda: List<AgendaItem>,
    agendaEnabled: Boolean,
    onEnableAgenda: () -> Unit,
    onAgendaClick: () -> Unit,
    memory: MemoryShown?,
    onMemoryClick: () -> Unit,
    onMemoryHide: () -> Unit,
    focusSinceMillis: Long,
    focusUntilMillis: Long,
    onStartFocus: (Int) -> Unit,
    focusSummary: String?,
    onDismissFocusSummary: () -> Unit,
    favorites: List<Favorite>,
    onLaunch: (AppEntry) -> Unit,
    onUnpin: (Favorite) -> Unit,
    onOpenFolder: (Folder) -> Unit,
    onOpenDrawer: () -> Unit,
    onOpenSearch: () -> Unit,
    onOpenHealth: () -> Unit,
    onToggleFocus: () -> Unit,
    onOpenSettings: () -> Unit,
    onSwipeLeft: () -> Unit,
    onSwipeRight: () -> Unit,
    onSwipeDown: () -> Unit,
    onDoubleTap: () -> Unit,
    night: Boolean,
    live: List<LiveItem>,
    suggestions: List<AppEntry>,
    focusNote: Note?,
    onNoteDone: (Note) -> Unit,
    onOpenCapture: () -> Unit,
    onLiveOpen: (LiveItem) -> Unit,
) {
    val focusOn = focusSinceMillis != 0L
    val pagerState = rememberPagerState(pageCount = { CAROUSEL_PAGES })
    // Só rola se a tela for tão baixa que os favoritos não caibam nem no tamanho mínimo.
    val favoritesScroll = rememberScrollState()
    val haptic = LocalHapticFeedback.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            // À noite a home inteira fica mais escura para cansar menos a vista.
            .graphicsLayer { alpha = if (night) 0.65f else 1f }
            // Deslizar para cima abre a lista completa (a menos que os favoritos estejam rolando).
            .onVerticalSwipe(
                up = true,
                threshold = 96.dp,
                enabled = { !favoritesScroll.canScrollForward },
                onSwipe = onOpenDrawer,
            )
            // Deslizar para baixo executa o gesto configurado (por padrão, o painel de notificações).
            .onVerticalSwipe(
                up = false,
                threshold = 96.dp,
                enabled = { !favoritesScroll.canScrollBackward },
                onSwipe = onSwipeDown,
            ),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp), horizontalArrangement = Arrangement.End) {
                GearButton(onClick = onOpenSettings)
            }
            // Ilha dinâmica: some na home calma (noite) para não puxar o olhar.
            if (!night) LivePill(items = live, active = active, onOpen = onLiveOpen)
            HomeCarousel(
                nowMillis = nowMillis,
                clockStyle = clockStyle,
                pagerState = pagerState,
                notificationsEnabled = notificationsEnabled,
                onEnableNotifications = onEnableNotifications,
                active = active,
                agenda = agenda,
                agendaEnabled = agendaEnabled,
                onEnableAgenda = onEnableAgenda,
                onAgendaClick = onAgendaClick,
                memory = memory,
                onMemoryClick = onMemoryClick,
                onMemoryHide = onMemoryHide,
                onLongPress = onOpenSettings,
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    // Toque duplo numa área vazia (os itens clicáveis consomem o toque antes).
                    .pointerInput(Unit) {
                        detectTapGestures(onDoubleTap = { onDoubleTap() })
                    }
                    .onHorizontalSwipe(
                        threshold = 80.dp,
                        onSwipeLeft = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onSwipeLeft()
                        },
                        onSwipeRight = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onSwipeRight()
                        },
                    ),
            ) {
                // Com o foco ligado a home fica mais calma: some o bloco de notificações e status.
                if (!focusOn && !night) {
                    InfoBlock(
                        stats = stats,
                        notifications = notifications,
                        notificationsEnabled = notificationsEnabled,
                        onNotificationClick = onNotificationClick,
                        onNotificationDismiss = onNotificationDismiss,
                        onNotificationReply = onNotificationReply,
                        onNotificationLater = onNotificationLater,
                        laterCount = laterCount,
                        onOpenLater = onOpenLater,
                        onEnableNotifications = onEnableNotifications,
                        onStatusClick = onOpenHealth,
                    )
                }
                if (focusSummary != null && !night) FocusSummaryLine(focusSummary, onDismissFocusSummary)
                FocusLine(
                    focusSinceMillis = focusSinceMillis,
                    focusUntilMillis = focusUntilMillis,
                    nowMillis = nowMillis,
                    onClick = onToggleFocus,
                    onStartSession = onStartFocus,
                )
                // "Agora": apps prováveis para este horário; some na home calma.
                if (!night && suggestions.isNotEmpty()) NowLine(suggestions, onLaunch)
                // Tarefa do foco: toque marca como feita; segurar abre as anotações.
                if (!night && focusNote != null) TaskLine(focusNote, onNoteDone, onOpenCapture)

                // Os favoritos dividem o espaço que sobra: a altura de cada linha se ajusta
                // (de 32 a 44 dp) para os 10 caberem sem rolar.
                BoxWithConstraints(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    val rowHeight = (maxHeight / favorites.size.coerceAtLeast(1)).coerceIn(32.dp, 44.dp)
                    Column(modifier = Modifier.fillMaxSize().verticalScroll(favoritesScroll)) {
                        if (favorites.isEmpty()) {
                            Text(
                                text = if (focusOn) {
                                    "modo foco ligado: nenhum favorito liberado"
                                } else {
                                    "deslize para cima, segure um app\npara fixar aqui"
                                },
                                style = VoidType.Caption,
                                color = VoidColors.TextDim,
                                modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
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

                // Espaço para a pílula de busca e o botão redondo não cobrirem o último favorito.
                Spacer(Modifier.height(88.dp))
            }
        }

        SearchPill(
            onClick = onOpenSearch,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(24.dp),
        )

        DrawerButton(
            onClick = onOpenDrawer,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp),
        )
    }
}

/** Pílula fina "buscar app": abre a lista completa já com o teclado. */
@Composable
private fun SearchPill(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        contentAlignment = Alignment.CenterStart,
        modifier = modifier
            .heightIn(min = 52.dp)
            .width(168.dp)
            .clip(CircleShape)
            .border(width = 1.dp, color = VoidColors.TextDim, shape = CircleShape)
            .clickable(role = androidx.compose.ui.semantics.Role.Button, onClick = onClick)
            .padding(horizontal = 20.dp),
    ) {
        Text(text = "buscar app", style = VoidType.Caption, color = VoidColors.TextDim)
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun FavoriteRow(
    item: Favorite,
    height: Dp,
    onOpen: () -> Unit,
    onUnpin: () -> Unit,
) {
    val haptic = LocalHapticFeedback.current
    val source = remember { MutableInteractionSource() }
    // A linha ocupa a largura toda, mas só o conteúdo (ícone e nome) responde ao toque: o espaço
    // vazio ao lado fica livre para o toque duplo e os deslizes da home.
    Box(modifier = Modifier.fillMaxWidth().heightIn(min = height).padding(start = 24.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxHeight()
                .pressFeedback(source)
                .combinedClickable(
                role = androidx.compose.ui.semantics.Role.Button,
                    interactionSource = source,
                    indication = null,
                    onClick = onOpen,
                    onLongClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onUnpin()
                    },
                )
                .padding(end = 20.dp),
        ) {
            when (item) {
                is Favorite.App -> {
                    AppIcon(app = item.app, size = 24.dp)
                    Spacer(Modifier.width(18.dp))
                    Text(
                        text = item.app.label,
                        style = VoidType.App,
                        color = VoidColors.Text,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                is Favorite.FolderItem -> {
                    FolderGlyph(width = 24.dp, height = 20.dp)
                    Spacer(Modifier.width(18.dp))
                    Text(
                        text = item.folder.name,
                        style = VoidType.App,
                        color = VoidColors.Text,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false),
                    )
                    Text(
                        text = "  ${item.count}",
                        style = VoidType.Caption,
                        color = VoidColors.TextDim,
                    )
                }
            }
        }
    }
}

/** Ícone branco e fino do app; sem logotipo nem camada monocromática, vira a letra inicial em um círculo. */
@Composable
internal fun AppIcon(app: AppEntry, size: Dp) {
    val context = LocalContext.current
    var icon by remember(app.key) { mutableStateOf(IconLoader.peek(app.key)) }
    LaunchedEffect(app.key) {
        if (icon is IconResult.Loading) {
            icon = withContext(Dispatchers.Default) { IconLoader.load(context, app) }
        }
    }

    when (val state = icon) {
        IconResult.Loading -> Box(Modifier.size(size))
        is IconResult.Ready -> {
            val bitmap = state.bitmap
            if (bitmap != null) {
                Image(bitmap = bitmap, contentDescription = null, modifier = Modifier.size(size))
            } else {
                LetterBadge(letter = app.label.firstOrNull()?.uppercaseChar() ?: '?', size = size)
            }
        }
    }
}

@Composable
private fun LetterBadge(letter: Char, size: Dp) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(size)
            .border(width = 1.dp, color = VoidColors.TextDim, shape = CircleShape),
    ) {
        Text(
            text = letter.toString(),
            style = VoidType.Caption,
            color = VoidColors.Text,
        )
    }
}

/** Botão redondo discreto: círculo fino com quatro pontos, que abre a lista de todos os apps. */
@Composable
private fun DrawerButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(52.dp)
            .clip(CircleShape)
            .clickable(role = androidx.compose.ui.semantics.Role.Button, onClick = onClick),
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                color = VoidColors.TextDim,
                style = Stroke(width = 1.dp.toPx()),
            )
            val spread = 5.dp.toPx()
            val dot = 1.6.dp.toPx()
            for (dx in listOf(-spread, spread)) {
                for (dy in listOf(-spread, spread)) {
                    drawCircle(
                        color = VoidColors.Text,
                        radius = dot,
                        center = Offset(center.x + dx, center.y + dy),
                    )
                }
            }
        }
    }
}
