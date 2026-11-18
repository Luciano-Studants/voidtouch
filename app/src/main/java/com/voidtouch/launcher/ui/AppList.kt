package com.voidtouch.launcher.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.voidtouch.launcher.data.AppEntry
import com.voidtouch.launcher.data.Search
import com.voidtouch.launcher.ui.theme.VoidColors
import com.voidtouch.launcher.ui.theme.VoidType
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/** Só mostra a régua de rolagem quando a lista é longa o bastante para precisar dela. */
private const val RAIL_MIN_ITEMS = 12

/**
 * Lista completa de apps, em texto, com busca no topo. Tocar abre o app; segurar fixa ou
 * desafixa na home (o ponto à direita marca os fixados). O botão de confirmar do teclado abre
 * o primeiro resultado. Deslizar para baixo, com a lista no topo, volta para a home.
 *
 * A linha tracejada à direita é uma régua de rolagem: arraste nela para ir direto a uma letra.
 *
 * Modo foco: o texto "apps do foco" liga a marcação, em que tocar num app o marca como
 * distração (o círculo vazio à direita). Com o foco ligado, os marcados somem da lista e da busca.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppList(
    apps: List<AppEntry>,
    pinnedKeys: List<String>,
    focusApps: Set<String>,
    focusModeOn: Boolean,
    markingFocusApps: Boolean,
    onMarkingChange: (Boolean) -> Unit,
    onToggleFocusApp: (AppEntry) -> Unit,
    query: String,
    onQueryChange: (String) -> Unit,
    focusSearch: Boolean,
    onLaunch: (AppEntry) -> Unit,
    onTogglePin: (AppEntry) -> Unit,
    onOpenSettings: () -> Unit,
    onClose: () -> Unit,
) {
    val haptic = LocalHapticFeedback.current
    val focusManager = LocalFocusManager.current
    val listState = rememberLazyListState()

    // Enquanto se marca os apps do foco, todos aparecem para poder desmarcar.
    val hiding = focusModeOn && !markingFocusApps
    val visible = remember(apps, focusApps, hiding) {
        if (hiding) apps.filter { it.key !in focusApps } else apps
    }
    val hiddenCount = apps.size - visible.size
    val results = remember(visible, query) { Search.filter(visible, query) }
    val showRail = results.size > RAIL_MIN_ITEMS
    var railLetter by remember { mutableStateOf<Char?>(null) }

    fun launch(app: AppEntry) {
        focusManager.clearFocus()
        onLaunch(app)
    }

    val caption = when {
        markingFocusApps -> "toque nos apps que distraem: eles somem no modo foco"
        query.isNotBlank() -> "${results.size} encontrado(s)"
        hiding -> "modo foco ligado: $hiddenCount app(s) escondido(s)"
        else -> "segure um app para fixar na home"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .onVerticalSwipe(
                up = false,
                threshold = 96.dp,
                enabled = { !listState.canScrollBackward },
                // A faixa da régua é só da régua: arrastar nela não fecha a lista.
                excludeEndEdge = 44.dp,
                onSwipe = {
                    focusManager.clearFocus()
                    onClose()
                },
            ),
    ) {
        Column(modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 28.dp, bottom = 8.dp)) {
            SearchField(
                query = query,
                onQueryChange = onQueryChange,
                requestFocus = focusSearch,
                onGo = { results.firstOrNull()?.let(::launch) },
            )
            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = caption,
                    style = VoidType.Caption,
                    color = VoidColors.TextDim,
                    modifier = Modifier.weight(1f).padding(end = 12.dp),
                )
                if (!markingFocusApps) {
                    Text(
                        text = "ajustes",
                        style = VoidType.Caption,
                        color = VoidColors.Text,
                        modifier = Modifier
                            .clickable {
                                focusManager.clearFocus()
                                onOpenSettings()
                            }
                            .padding(vertical = 8.dp),
                    )
                    Spacer(Modifier.width(16.dp))
                }
                Text(
                    text = if (markingFocusApps) "concluir" else "apps do foco",
                    style = VoidType.Caption,
                    color = VoidColors.Text,
                    modifier = Modifier
                        .clickable {
                            focusManager.clearFocus()
                            onMarkingChange(!markingFocusApps)
                        }
                        .padding(vertical = 8.dp),
                )
            }
        }

        if (results.isEmpty()) {
            Text(
                text = "nenhum app encontrado",
                style = VoidType.App,
                color = VoidColors.TextDim,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 20.dp),
            )
        }

        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 24.dp),
            ) {
                items(
                    items = results,
                    key = { it.key },
                    contentType = { "app" },
                ) { app ->
                    val marked = app.key in focusApps
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .combinedClickable(
                                onClick = {
                                    if (markingFocusApps) onToggleFocusApp(app) else launch(app)
                                },
                                onLongClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    if (markingFocusApps) onToggleFocusApp(app) else onTogglePin(app)
                                },
                            )
                            // Com a régua à direita, as marcas dos apps ficam um pouco mais para dentro.
                            .padding(
                                start = 24.dp,
                                end = if (showRail) 44.dp else 24.dp,
                                top = 14.dp,
                                bottom = 14.dp,
                            ),
                    ) {
                        Text(
                            text = app.label,
                            style = VoidType.App,
                            // Marcados aparecem apagados enquanto se escolhe os apps do foco.
                            color = if (markingFocusApps && marked) VoidColors.TextDim else VoidColors.Text,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f),
                        )
                        if (marked) {
                            // Círculo vazio: app marcado como distração.
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .border(1.dp, VoidColors.TextDim, CircleShape),
                            )
                        }
                        if (app.key in pinnedKeys) {
                            // Ponto cheio: app fixado na home.
                            if (marked) Spacer(Modifier.width(10.dp))
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .background(VoidColors.Text, CircleShape),
                            )
                        }
                    }
                }
            }

            if (showRail) {
                ScrollRail(
                    listState = listState,
                    itemCount = results.size,
                    letterAt = { index -> letterOf(results.getOrNull(index)) },
                    onLetterChange = { railLetter = it },
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 6.dp, top = 8.dp, bottom = 8.dp),
                )
            }

            // Letra grande no meio da tela enquanto se arrasta a régua.
            railLetter?.let { letter ->
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(88.dp)
                        .background(VoidColors.Graphite, RoundedCornerShape(16.dp))
                        .border(1.dp, VoidColors.Line, RoundedCornerShape(16.dp)),
                ) {
                    Text(
                        text = letter.toString(),
                        style = VoidType.Clock.copy(fontSize = 52.sp, lineHeight = 56.sp),
                        color = VoidColors.Text,
                    )
                }
            }
        }
    }
}

/** Letra inicial do app, sem acento; qualquer coisa que não seja letra vira "#". */
private fun letterOf(app: AppEntry?): Char =
    app?.searchLabel?.firstOrNull()?.takeIf { it.isLetter() }?.uppercaseChar() ?: '#'

/**
 * Régua de rolagem: uma linha tracejada na lateral com um traço cheio que mostra onde a lista
 * está. Arrastar nela rola a lista na hora e mostra a letra do ponto.
 */
@Composable
private fun ScrollRail(
    listState: LazyListState,
    itemCount: Int,
    letterAt: (Int) -> Char,
    onLetterChange: (Char?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()
    var heightPx by remember { mutableIntStateOf(1) }

    // Posição do traço cheio, de 0 a 1; lida só ao desenhar, então rolar não recompõe a lista.
    val thumb by remember(itemCount) {
        derivedStateOf {
            if (itemCount <= 1) 0f
            else (listState.firstVisibleItemIndex.toFloat() / (itemCount - 1)).coerceIn(0f, 1f)
        }
    }

    fun scrollToY(y: Float) {
        val fraction = (y / heightPx).coerceIn(0f, 1f)
        val index = (fraction * (itemCount - 1)).roundToInt().coerceIn(0, itemCount - 1)
        onLetterChange(letterAt(index))
        scope.launch { listState.scrollToItem(index) }
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .width(28.dp)
            .fillMaxHeight()
            .onSizeChanged { heightPx = it.height.coerceAtLeast(1) }
            .pointerInput(itemCount) {
                detectVerticalDragGestures(
                    onDragStart = { offset -> scrollToY(offset.y) },
                    onDragEnd = { onLetterChange(null) },
                    onDragCancel = { onLetterChange(null) },
                ) { change, _ ->
                    change.consume()
                    scrollToY(change.position.y)
                }
            },
    ) {
        Canvas(modifier = Modifier.fillMaxHeight().width(12.dp)) {
            val x = size.width / 2f
            drawLine(
                color = VoidColors.TextDim,
                start = Offset(x, 0f),
                end = Offset(x, size.height),
                strokeWidth = 1.5.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(6.dp.toPx(), 6.dp.toPx())),
            )
            val length = 28.dp.toPx().coerceAtMost(size.height)
            val top = thumb * (size.height - length)
            drawLine(
                color = VoidColors.Text,
                start = Offset(x, top),
                end = Offset(x, top + length),
                strokeWidth = 3.dp.toPx(),
                cap = StrokeCap.Round,
            )
        }
    }
}

/** Campo de busca simples: texto monoespaçado, linha fina embaixo, sem os componentes pesados do Material. */
@Composable
internal fun SearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    requestFocus: Boolean,
    onGo: () -> Unit,
) {
    val focusRequester = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current

    LaunchedEffect(requestFocus) {
        if (requestFocus) {
            runCatching { focusRequester.requestFocus() }
            keyboard?.show()
        }
    }

    BasicTextField(
        value = query,
        onValueChange = onQueryChange,
        singleLine = true,
        textStyle = VoidType.App.copy(color = VoidColors.Text),
        cursorBrush = SolidColor(VoidColors.Text),
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.None,
            autoCorrectEnabled = false,
            imeAction = ImeAction.Go,
        ),
        keyboardActions = KeyboardActions(onGo = { onGo() }),
        modifier = Modifier
            .fillMaxWidth()
            .focusRequester(focusRequester),
        decorationBox = { inner ->
            Column {
                Box(modifier = Modifier.padding(vertical = 6.dp)) {
                    if (query.isEmpty()) {
                        Text(text = "buscar app", style = VoidType.App, color = VoidColors.TextDim)
                    }
                    inner()
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(VoidColors.Line),
                )
            }
        },
    )
}
