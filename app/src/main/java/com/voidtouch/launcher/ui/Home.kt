package com.voidtouch.launcher.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.coerceIn
import androidx.compose.ui.unit.dp
import com.voidtouch.launcher.data.AppEntry
import com.voidtouch.launcher.data.ClockStyle
import com.voidtouch.launcher.data.DeviceStats
import com.voidtouch.launcher.data.IconLoader
import com.voidtouch.launcher.data.IconResult
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
    onEnableNotifications: () -> Unit,
    focusSinceMillis: Long,
    favorites: List<AppEntry>,
    onLaunch: (AppEntry) -> Unit,
    onUnpin: (AppEntry) -> Unit,
    onOpenDrawer: () -> Unit,
    onOpenSearch: () -> Unit,
    onOpenHealth: () -> Unit,
    onToggleFocus: () -> Unit,
    onOpenSettings: () -> Unit,
    onSwipeLeft: () -> Unit,
    onSwipeRight: () -> Unit,
) {
    val focusOn = focusSinceMillis != 0L
    val pagerState = rememberPagerState(pageCount = { CAROUSEL_PAGES })
    // Só rola se a tela for tão baixa que os favoritos não caibam nem no tamanho mínimo.
    val favoritesScroll = rememberScrollState()
    val haptic = LocalHapticFeedback.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            // Deslizar para cima abre a lista completa (a menos que os favoritos estejam rolando).
            .onVerticalSwipe(
                up = true,
                threshold = 96.dp,
                enabled = { !favoritesScroll.canScrollForward },
                onSwipe = onOpenDrawer,
            ),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Spacer(Modifier.height(12.dp))
            HomeCarousel(
                nowMillis = nowMillis,
                clockStyle = clockStyle,
                pagerState = pagerState,
                notificationsEnabled = notificationsEnabled,
                onEnableNotifications = onEnableNotifications,
                onLongPress = onOpenSettings,
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
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
                if (!focusOn) {
                    InfoBlock(
                        stats = stats,
                        notifications = notifications,
                        notificationsEnabled = notificationsEnabled,
                        onNotificationClick = onNotificationClick,
                        onNotificationDismiss = onNotificationDismiss,
                        onEnableNotifications = onEnableNotifications,
                        onStatusClick = onOpenHealth,
                    )
                }
                FocusLine(
                    focusSinceMillis = focusSinceMillis,
                    nowMillis = nowMillis,
                    onClick = onToggleFocus,
                )

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
                            favorites.forEach { app ->
                                FavoriteRow(
                                    app = app,
                                    height = rowHeight,
                                    onLaunch = onLaunch,
                                    onUnpin = onUnpin,
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
            .height(52.dp)
            .width(168.dp)
            .clip(CircleShape)
            .border(width = 1.dp, color = VoidColors.TextDim, shape = CircleShape)
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp),
    ) {
        Text(text = "buscar app", style = VoidType.Caption, color = VoidColors.TextDim)
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun FavoriteRow(
    app: AppEntry,
    height: Dp,
    onLaunch: (AppEntry) -> Unit,
    onUnpin: (AppEntry) -> Unit,
) {
    val haptic = LocalHapticFeedback.current
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .combinedClickable(
                onClick = { onLaunch(app) },
                onLongClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onUnpin(app)
                },
            )
            .padding(horizontal = 24.dp),
    ) {
        AppIcon(app = app, size = 24.dp)
        Spacer(Modifier.width(18.dp))
        Text(
            text = app.label,
            style = VoidType.App,
            color = VoidColors.Text,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/** Ícone branco e fino do app; sem logotipo nem camada monocromática, vira a letra inicial em um círculo. */
@Composable
private fun AppIcon(app: AppEntry, size: Dp) {
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
            .clickable(onClick = onClick),
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
