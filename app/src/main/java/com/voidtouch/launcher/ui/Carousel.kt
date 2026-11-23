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
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.sp
import com.voidtouch.launcher.data.MemoryShown
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import android.media.session.PlaybackState
import android.os.SystemClock
import com.voidtouch.launcher.notify.MediaInfo
import kotlinx.coroutines.delay
import com.voidtouch.launcher.data.Agenda
import com.voidtouch.launcher.data.AgendaItem
import com.voidtouch.launcher.data.ClockStyle
import com.voidtouch.launcher.data.MonthGrid
import com.voidtouch.launcher.notify.NotificationHub
import androidx.compose.ui.text.style.TextOverflow
import com.voidtouch.launcher.ui.theme.VoidColors
import com.voidtouch.launcher.ui.theme.VoidType
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/** Quantas páginas o carrossel tem: hora, dia da semana, música e agenda. */
const val CAROUSEL_PAGES = 4

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
private val CarouselHeight = 176.dp

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
    active: Boolean,
    agenda: List<AgendaItem>,
    eventDays: Set<Int> = emptySet(),
    agendaEnabled: Boolean,
    onEnableAgenda: () -> Unit,
    onAgendaClick: () -> Unit,
    memory: MemoryShown?,
    onMemoryClick: () -> Unit,
    onMemoryHide: () -> Unit,
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
                role = androidx.compose.ui.semantics.Role.Button,
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
                    0 -> TimePage(text, memory, onMemoryClick, onMemoryHide)
                    1 -> DayPage(text)
                    2 -> MusicPage(notificationsEnabled, onEnableNotifications, active)
                    else -> AgendaPage(agenda, agendaEnabled, nowMillis, onEnableAgenda, onAgendaClick, eventDays)
                }
            }
        }
        PagerDashes(count = CAROUSEL_PAGES, current = pagerState.currentPage)
    }
}

/** Página 1: hora grande, mês em letra manuscrita e o dia por extenso; à direita, a foto "neste dia". */
@Composable
private fun TimePage(text: CarouselText, memory: MemoryShown?, onMemoryClick: () -> Unit, onMemoryHide: () -> Unit) {
    Row(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = text.time, style = VoidType.Clock, color = VoidColors.Text)
            Text(text = text.month, style = VoidType.Month, color = VoidColors.Text)
            Text(text = text.weekdayAndDay, style = VoidType.App, color = VoidColors.TextDim)
        }
        AnimatedVisibility(
            visible = memory != null,
            enter = fadeIn(tween(400)),
            exit = fadeOut(tween(200)),
        ) {
            // Guarda a última foto para ela não sumir de uma vez durante o fade de saída.
            var last by remember { mutableStateOf<MemoryShown?>(null) }
            if (memory != null) last = memory
            last?.let { MemoryCard(it, onMemoryClick, onMemoryHide) }
        }
    }
}

@Composable
private fun MemoryCard(shown: MemoryShown, onClick: () -> Unit, onHide: () -> Unit) {
    Box(
        modifier = Modifier
            .padding(start = 12.dp, top = 6.dp)
            .size(width = 104.dp, height = 132.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(VoidColors.Graphite)
            .clickable(role = androidx.compose.ui.semantics.Role.Button, onClick = onClick),
    ) {
        Image(
            bitmap = shown.bitmap,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            alpha = 0.92f,
            modifier = Modifier.fillMaxSize(),
        )
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f))))
                .padding(start = 8.dp, end = 8.dp, top = 18.dp, bottom = 6.dp),
        ) {
            Text(shown.memory.label, style = VoidType.Small.copy(fontSize = 12.sp, lineHeight = 15.sp), color = VoidColors.Text)
        }
        // Botão de esconder: some com esta foto para sempre (dá para restaurar em Ajustes).
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(4.dp)
                .size(30.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.55f))
                .clickable(role = androidx.compose.ui.semantics.Role.Button, onClick = onHide),
        ) {
            Canvas(modifier = Modifier.size(10.dp)) {
                val w = size.width
                drawLine(Color.White, Offset(0f, 0f), Offset(w, w), strokeWidth = 1.6.dp.toPx(), cap = StrokeCap.Round)
                drawLine(Color.White, Offset(w, 0f), Offset(0f, w), strokeWidth = 1.6.dp.toPx(), cap = StrokeCap.Round)
            }
        }
    }
}

/** Página 2: "hoje é" e o dia da semana grande, como na prancheta de referência. */
@Composable
private fun DayPage(text: CarouselText) {
    // O dia da semana cresce até ocupar toda a largura da página.
    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center) {
        Text(text = "hoje é", style = VoidType.App, color = VoidColors.TextDim)
        FitText(
            text = text.weekday.uppercase(),
            style = VoidType.Day,
            color = VoidColors.Text,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(6.dp))
        Text(text = text.shortDate, style = VoidType.App, color = VoidColors.TextDim)
    }
}

/** Página 4: próximos compromissos do calendário do celular. */
@Composable
private fun AgendaPage(
    agenda: List<AgendaItem>,
    enabled: Boolean,
    nowMillis: Long,
    onEnable: () -> Unit,
    onOpen: () -> Unit,
    eventDays: Set<Int> = emptySet(),
) {
    when {
        !enabled -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = "ativar agenda ›",
                style = VoidType.App,
                color = VoidColors.Text,
                modifier = Modifier.clickable(role = androidx.compose.ui.semantics.Role.Button, onClick = onEnable).padding(12.dp),
            )
        }
        agenda.isEmpty() -> MonthPage(nowMillis, onOpen, eventDays)
        else -> Column(modifier = Modifier.fillMaxSize().clickable(role = androidx.compose.ui.semantics.Role.Button, onClick = onOpen), verticalArrangement = Arrangement.Center) {
            Text("agenda", style = VoidType.Caption, color = VoidColors.TextDim)
            Spacer(Modifier.height(6.dp))
            val zone = ZoneId.systemDefault()
            agenda.take(3).forEach { item ->
                Column(modifier = Modifier.padding(vertical = 3.dp)) {
                    Text(
                        Agenda.label(item, nowMillis, zone),
                        style = VoidType.Caption,
                        color = VoidColors.TextDim,
                    )
                    Text(
                        item.title,
                        style = VoidType.App,
                        color = VoidColors.Text,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

/** Sem compromissos nos próximos dias, a agenda mostra o mês com o dia de hoje marcado. */
@Composable
private fun MonthPage(nowMillis: Long, onOpen: () -> Unit, eventDays: Set<Int>) {
    val zone = ZoneId.systemDefault()
    val today = Instant.ofEpochMilli(nowMillis).atZone(zone).toLocalDate()
    val month = java.time.YearMonth.from(today)
    val weeks = remember(month) { MonthGrid.weeks(month) }
    val name = month.month.getDisplayName(java.time.format.TextStyle.FULL, Locale.forLanguageTag("pt-BR"))
    androidx.compose.foundation.layout.BoxWithConstraints(
        modifier = Modifier.fillMaxSize().clickable(role = androidx.compose.ui.semantics.Role.Button, onClick = onOpen),
    ) {
        // Cabeçalho + letras dos dias + semanas dividem a altura da página.
        val rowH = ((maxHeight - 40.dp) / weeks.size).coerceIn(16.dp, 26.dp)
        Column(verticalArrangement = Arrangement.Center, modifier = Modifier.fillMaxSize()) {
            Text(
                "$name de ${month.year} · nada nos próximos 7 dias" + if (eventDays.isNotEmpty()) " · ponto = compromisso" else "",
                style = VoidType.Caption,
                color = VoidColors.TextDim,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(6.dp))
            Row(Modifier.fillMaxWidth()) {
                MonthGrid.WEEKDAYS.forEach { d ->
                    Box(Modifier.weight(1f).height(18.dp), contentAlignment = Alignment.Center) {
                        Text(d, style = VoidType.Caption.copy(fontSize = 11.sp), color = VoidColors.TextDim)
                    }
                }
            }
            weeks.forEach { week ->
                Row(Modifier.fillMaxWidth()) {
                    week.forEach { day ->
                        Box(Modifier.weight(1f).height(rowH), contentAlignment = Alignment.Center) {
                            if (day != null) {
                                val isToday = day == today.dayOfMonth
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(rowH.coerceAtMost(24.dp))
                                        .then(if (isToday) Modifier.clip(CircleShape).background(VoidColors.Selected) else Modifier),
                                ) {
                                    Text(
                                        day.toString(),
                                        style = VoidType.Small.copy(fontSize = 12.sp, lineHeight = 14.sp),
                                        color = if (isToday) VoidColors.OnSelected else if (day < today.dayOfMonth) VoidColors.TextDim else VoidColors.Text,
                                    )
                                }
                                if (day in eventDays) {
                                    Box(
                                        Modifier
                                            .align(Alignment.BottomCenter)
                                            .size(4.dp)
                                            .clip(CircleShape)
                                            .background(VoidColors.Text),
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/** Página 3: player no estilo do Android, centralizado: capa ao fundo, título, artista, barra e controles. */
@Composable
private fun MusicPage(enabled: Boolean, onEnable: () -> Unit, active: Boolean) {
    val media = NotificationHub.media
    when {
        !enabled -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = "ativar acesso às notificações ›",
                style = VoidType.App,
                color = VoidColors.Text,
                modifier = Modifier.clickable(role = androidx.compose.ui.semantics.Role.Button, onClick = onEnable).padding(12.dp),
            )
        }
        media == null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("nenhuma música tocando", style = VoidType.App, color = VoidColors.TextDim)
        }
        else -> PlayerCard(media, active)
    }
}

@Composable
private fun PlayerCard(media: MediaInfo, active: Boolean) {
    // O relógio do player só corre com a tela visível e a música tocando.
    var tick by remember { mutableLongStateOf(SystemClock.elapsedRealtime()) }
    LaunchedEffect(active, media.playing, media.title) {
        tick = SystemClock.elapsedRealtime()
        while (active && media.playing) {
            delay(1000)
            tick = SystemClock.elapsedRealtime()
        }
    }
    val position = remember(tick, media) { currentPosition(media) }
    val art = remember(media.art) { media.art?.asImageBitmap() }
    val shape = RoundedCornerShape(20.dp)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 4.dp)
            .clip(shape)
            .background(VoidColors.Graphite),
    ) {
        if (art != null) {
            Image(
                bitmap = art,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                alpha = 0.3f,
                modifier = Modifier.fillMaxSize(),
            )
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        ) {
            Text(
                media.title,
                style = VoidType.App,
                color = VoidColors.Text,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth(),
            )
            if (media.artist.isNotEmpty()) {
                Text(
                    media.artist,
                    style = VoidType.Date,
                    color = VoidColors.TextDim,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            if (media.durationMs > 0L) {
                ProgressBar(position = position, duration = media.durationMs)
            } else {
                Spacer(Modifier.height(8.dp))
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                MediaButton(MediaIcon.Previous, NotificationHub::previous)
                MediaButton(if (media.playing) MediaIcon.Pause else MediaIcon.Play, NotificationHub::togglePlay)
                MediaButton(MediaIcon.Next, NotificationHub::next)
            }
        }
    }
}

private fun currentPosition(media: MediaInfo): Long {
    val state = media.controller.playbackState ?: return 0L
    val moved = if (state.state == PlaybackState.STATE_PLAYING) {
        ((SystemClock.elapsedRealtime() - state.lastPositionUpdateTime) * state.playbackSpeed).toLong()
    } else {
        0L
    }
    return (state.position + moved).coerceIn(0L, media.durationMs.takeIf { it > 0L } ?: Long.MAX_VALUE)
}

/** Barra fina com os tempos nas pontas; tocar nela pula para aquele ponto da música. */
@Composable
private fun ProgressBar(position: Long, duration: Long) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
    ) {
        Text(formatTime(position), style = VoidType.Caption, color = VoidColors.TextDim)
        Box(
            contentAlignment = Alignment.CenterStart,
            modifier = Modifier
                .weight(1f)
                .height(24.dp)
                .padding(horizontal = 10.dp)
                .pointerInput(duration) {
                    detectTapGestures { offset ->
                        NotificationHub.seekTo((offset.x / size.width * duration).toLong())
                    }
                },
        ) {
            Box(Modifier.fillMaxWidth().height(2.dp).background(VoidColors.Line))
            Box(
                Modifier
                    .fillMaxWidth((position.toFloat() / duration).coerceIn(0f, 1f))
                    .height(2.dp)
                    .background(VoidColors.Text),
            )
        }
        Text(formatTime(duration), style = VoidType.Caption, color = VoidColors.TextDim)
    }
}

private fun formatTime(ms: Long): String {
    val total = ms / 1000
    return String.format(PtBr, "%02d:%02d", total / 60, total % 60)
}

private enum class MediaIcon { Previous, Play, Pause, Next }

/** Botão com ícone desenhado, sem fonte de símbolos (evita caracteres que faltam em alguns aparelhos). */
@Composable
private fun MediaButton(icon: MediaIcon, onClick: () -> Unit) {
    Canvas(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .clickable(role = androidx.compose.ui.semantics.Role.Button, onClick = onClick)
            .padding(14.dp),
    ) {
        val w = size.width
        val h = size.height
        val color = VoidColors.Text
        fun triangle(left: Float, right: Float, pointRight: Boolean) {
            val path = Path().apply {
                if (pointRight) {
                    moveTo(left, 0f); lineTo(right, h / 2); lineTo(left, h)
                } else {
                    moveTo(right, 0f); lineTo(left, h / 2); lineTo(right, h)
                }
                close()
            }
            drawPath(path, color)
        }
        when (icon) {
            MediaIcon.Play -> triangle(w * 0.1f, w, true)
            MediaIcon.Pause -> {
                drawRect(color, Offset(w * 0.12f, 0f), Size(w * 0.28f, h))
                drawRect(color, Offset(w * 0.60f, 0f), Size(w * 0.28f, h))
            }
            MediaIcon.Next -> {
                triangle(0f, w * 0.8f, true)
                drawRect(color, Offset(w * 0.86f, 0f), Size(w * 0.14f, h))
            }
            MediaIcon.Previous -> {
                drawRect(color, Offset(0f, 0f), Size(w * 0.14f, h))
                triangle(w * 0.2f, w, false)
            }
        }
    }
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
