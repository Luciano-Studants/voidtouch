package com.voidtouch.launcher.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.clickable
import com.voidtouch.launcher.data.ClockStyle
import com.voidtouch.launcher.notify.NotificationHub
import androidx.compose.ui.text.style.TextOverflow
import com.voidtouch.launcher.ui.theme.VoidColors
import com.voidtouch.launcher.ui.theme.VoidType
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/** Quantas páginas o carrossel tem: hora, dia da semana e música. */
const val CAROUSEL_PAGES = 3

private val PtBr: Locale = Locale.forLanguageTag("pt-BR")
private val MonthFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("MMMM", PtBr)
private val WeekdayFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("EEEE", PtBr)
private val ShortDateFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("d 'de' MMMM", PtBr)

/** Dia do mês por extenso, de 1 a 31. */
private val DayWords = listOf(
    "um", "dois", "três", "quatro", "cinco", "seis", "sete", "oito", "nove", "dez",
    "onze", "doze", "treze", "quatorze", "quinze", "dezesseis", "dezessete", "dezoito", "dezenove",
    "vinte", "vinte e um", "vinte e dois", "vinte e três", "vinte e quatro", "vinte e cinco",
    "vinte e seis", "vinte e sete", "vinte e oito", "vinte e nove", "trinta", "trinta e um",
)

private data class CarouselText(
    val time: String,
    val month: String,
    val weekday: String,
    val weekdayAndDay: String,
    val shortDate: String,
)

/** Altura fixa do carrossel; as três páginas cabem nela. */
private val CarouselHeight = 136.dp

/**
 * Carrossel do topo da home, passado com o dedo para os lados. Cada página mostra uma
 * informação: a hora, o dia da semana e a música. O horário vem de fora (a Activity o atualiza
 * só enquanto a tela está visível), então nada aqui agenda trabalho em segundo plano.
 *
 * Segurar o dedo abre os Ajustes.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeCarousel(
    nowMillis: Long,
    clockStyle: ClockStyle,
    pagerState: PagerState,
    notificationsEnabled: Boolean,
    onEnableNotifications: () -> Unit,
    onLongPress: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val minute = nowMillis / 60_000L
    val text = remember(minute, clockStyle) {
        val zoned = Instant.ofEpochMilli(nowMillis).atZone(ZoneId.systemDefault())
        val day = DayWords.getOrElse(zoned.dayOfMonth - 1) { zoned.dayOfMonth.toString() }
        val weekday = zoned.format(WeekdayFormat)
        CarouselText(
            time = zoned.format(DateTimeFormatter.ofPattern(clockStyle.pattern, PtBr)),
            month = zoned.format(MonthFormat).replaceFirstChar { it.uppercase() },
            weekday = weekday,
            weekdayAndDay = "$weekday · $day",
            shortDate = zoned.format(ShortDateFormat),
        )
    }
    val haptic = LocalHapticFeedback.current

    Column(modifier = modifier.fillMaxWidth()) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth().height(CarouselHeight),
        ) { page ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .combinedClickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {},
                        onLongClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onLongPress()
                        },
                    )
                    .padding(horizontal = 24.dp),
            ) {
                when (page) {
                    0 -> TimePage(text)
                    1 -> DayPage(text)
                    else -> MusicPage(notificationsEnabled, onEnableNotifications)
                }
            }
        }
        PagerDashes(count = CAROUSEL_PAGES, current = pagerState.currentPage)
    }
}

/** Página 1: hora grande, mês em letra manuscrita e o dia por extenso (a home aprovada na v0.3). */
@Composable
private fun TimePage(text: CarouselText) {
    Column {
        Text(text = text.time, style = VoidType.Clock, color = VoidColors.Text)
        Text(text = text.month, style = VoidType.Month, color = VoidColors.Text)
        Text(text = text.weekdayAndDay, style = VoidType.Caption, color = VoidColors.TextDim)
    }
}

/** Página 2: "hoje é" e o dia da semana grande, como na prancheta de referência. */
@Composable
private fun DayPage(text: CarouselText) {
    Column {
        Text(text = "hoje é", style = VoidType.Caption, color = VoidColors.TextDim)
        Text(text = text.weekday.uppercase(), style = VoidType.Day, color = VoidColors.Text)
        Spacer(Modifier.height(6.dp))
        Text(text = text.shortDate, style = VoidType.Caption, color = VoidColors.TextDim)
    }
}

/** Página 3: música tocando agora, com anterior, tocar/pausar e próxima. */
@Composable
private fun MusicPage(enabled: Boolean, onEnable: () -> Unit) {
    val media = NotificationHub.media
    Column {
        Text(text = "música", style = VoidType.Caption, color = VoidColors.TextDim)
        Spacer(Modifier.height(8.dp))
        when {
            !enabled -> Text(
                text = "ativar acesso às notificações ›",
                style = VoidType.App,
                color = VoidColors.Text,
                modifier = Modifier.clickable(onClick = onEnable).padding(vertical = 4.dp),
            )
            media == null -> Text("nenhuma música tocando", style = VoidType.App, color = VoidColors.TextDim)
            else -> {
                Text(media.title, style = VoidType.App, color = VoidColors.Text, maxLines = 1, overflow = TextOverflow.Ellipsis)
                if (media.artist.isNotEmpty()) {
                    Text(media.artist, style = VoidType.Caption, color = VoidColors.TextDim, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MediaButton("‹‹", NotificationHub::previous)
                    MediaButton(if (media.playing) "❚❚" else "▶", NotificationHub::togglePlay)
                    MediaButton("››", NotificationHub::next)
                }
            }
        }
    }
}

@Composable
private fun MediaButton(label: String, onClick: () -> Unit) {
    Text(
        text = label,
        style = VoidType.App,
        color = VoidColors.Text,
        modifier = Modifier.clickable(onClick = onClick).padding(horizontal = 12.dp, vertical = 10.dp),
    )
}

/** Traços finos que mostram em que página do carrossel você está. */
@Composable
private fun PagerDashes(count: Int, current: Int) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.padding(start = 24.dp, top = 6.dp, bottom = 4.dp),
    ) {
        repeat(count) { index ->
            Box(
                modifier = Modifier
                    .width(18.dp)
                    .height(2.dp)
                    .background(if (index == current) VoidColors.Text else VoidColors.Line),
            )
        }
    }
}
