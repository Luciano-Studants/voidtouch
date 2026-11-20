package com.voidtouch.launcher.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.voidtouch.launcher.data.ClockStyle
import com.voidtouch.launcher.data.DeviceStats
import com.voidtouch.launcher.data.Dock
import com.voidtouch.launcher.notify.MediaInfo
import com.voidtouch.launcher.ui.theme.VoidColors
import com.voidtouch.launcher.ui.theme.VoidType

/**
 * Modo carregando: relógio grande de cabeceira, com a bateria e a música. Aparece sozinho depois
 * de uns segundos parado na home com o celular carregando; um toque sai. O conteúdo muda de
 * lugar um pouco a cada minuto para não marcar a tela.
 */
@Composable
fun DockScreen(
    nowMillis: Long,
    clockStyle: ClockStyle,
    stats: DeviceStats?,
    media: MediaInfo?,
    onExit: () -> Unit,
) {
    val minute = nowMillis / 60_000L
    val shift = Dock.shift(minute)
    val time = remember(minute, clockStyle) { Dock.clockText(nowMillis, clockStyle) }
    val date = remember(minute) { Dock.dateText(nowMillis) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onExit),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.offset(x = shift.dp, y = shift.dp).padding(horizontal = 24.dp),
        ) {
            Text(
                text = time,
                style = VoidType.Clock.copy(fontSize = 96.sp, lineHeight = 100.sp),
                color = VoidColors.Text,
                textAlign = TextAlign.Center,
            )
            Text(text = date, style = VoidType.Date, color = VoidColors.TextDim, textAlign = TextAlign.Center)
            Spacer(Modifier.height(24.dp))
            stats?.takeIf { it.batteryPercent >= 0 }?.let {
                Text(text = "carregando ${it.batteryPercent}%", style = VoidType.Small, color = VoidColors.TextDim)
            }
            if (media != null && media.playing) {
                Spacer(Modifier.height(16.dp))
                Text(
                    text = if (media.artist.isBlank()) media.title else "${media.title} · ${media.artist}",
                    style = VoidType.Small,
                    color = VoidColors.Text,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                )
            }
            Spacer(Modifier.height(28.dp))
            Text(text = "toque para sair", style = VoidType.Caption, color = VoidColors.TextDim.copy(alpha = 0.6f))
        }
    }
}
