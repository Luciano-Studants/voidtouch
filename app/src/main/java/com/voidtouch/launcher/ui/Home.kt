package com.voidtouch.launcher.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.unit.dp
import com.voidtouch.launcher.data.AppEntry
import com.voidtouch.launcher.data.DeviceStats
import com.voidtouch.launcher.data.IconLoader
import com.voidtouch.launcher.data.IconResult
import com.voidtouch.launcher.ui.theme.VoidColors
import com.voidtouch.launcher.ui.theme.VoidType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Home: cabeçalho, favoritos com ícone e o botão redondo que abre a lista completa. */
@Composable
fun HomeContent(
    nowMillis: Long,
    stats: DeviceStats?,
    favorites: List<AppEntry>,
    onLaunch: (AppEntry) -> Unit,
    onUnpin: (AppEntry) -> Unit,
    onOpenDrawer: () -> Unit,
    onOpenSearch: () -> Unit,
    onOpenHealth: () -> Unit,
    onClockLongPress: () -> Unit,
) {
    // A rolagem aqui é só uma rede de segurança para telas pequenas; a home cabe sem rolar.
    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            // Deslizar para cima abre a lista completa (só quando a home não está rolando).
            .onVerticalSwipe(
                up = true,
                threshold = 96.dp,
                enabled = { !scrollState.canScrollForward },
                onSwipe = onOpenDrawer,
            ),
    ) {
        Column(modifier = Modifier.fillMaxSize().verticalScroll(scrollState)) {
            ClockHeader(
                nowMillis = nowMillis,
                stats = stats,
                onStatsClick = onOpenHealth,
                onLongPress = onClockLongPress,
            )
            if (favorites.isEmpty()) {
                Text(
                    text = "deslize para cima, segure um app\npara fixar aqui",
                    style = VoidType.Caption,
                    color = VoidColors.TextDim,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                )
            } else {
                favorites.forEach { app ->
                    FavoriteRow(app = app, onLaunch = onLaunch, onUnpin = onUnpin)
                }
            }
            // Espaço para o botão redondo não cobrir o último favorito.
            Spacer(Modifier.height(88.dp))
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
    onLaunch: (AppEntry) -> Unit,
    onUnpin: (AppEntry) -> Unit,
) {
    val haptic = LocalHapticFeedback.current
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = { onLaunch(app) },
                onLongClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onUnpin(app)
                },
            )
            .padding(horizontal = 24.dp, vertical = 10.dp),
    ) {
        AppIcon(app = app, size = 26.dp)
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

/** Ícone branco e fino do app; sem camada monocromática, vira a letra inicial em um círculo. */
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
