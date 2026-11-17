package com.voidtouch.launcher.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.voidtouch.launcher.data.AppEntry
import com.voidtouch.launcher.data.Search
import com.voidtouch.launcher.ui.theme.VoidColors
import com.voidtouch.launcher.ui.theme.VoidType

/**
 * Lista completa de apps, em texto, com busca no topo. Tocar abre o app; segurar fixa ou
 * desafixa na home (o ponto à direita marca os fixados). O botão de confirmar do teclado abre
 * o primeiro resultado. Deslizar para baixo, com a lista no topo, volta para a home.
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

        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f).fillMaxWidth(),
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
                        .padding(horizontal = 24.dp, vertical = 14.dp),
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
    }
}

/** Campo de busca simples: texto monoespaçado, linha fina embaixo, sem os componentes pesados do Material. */
@Composable
private fun SearchField(
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
