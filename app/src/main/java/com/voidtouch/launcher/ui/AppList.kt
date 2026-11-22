package com.voidtouch.launcher.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.Path
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.ui.text.style.TextAlign
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
import androidx.compose.animation.core.tween
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import com.voidtouch.launcher.data.AppEntry
import com.voidtouch.launcher.data.AppShortcut
import com.voidtouch.launcher.data.Perf
import com.voidtouch.launcher.data.Shortcuts
import androidx.compose.runtime.produceState
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.voidtouch.launcher.data.FavoriteKeys
import com.voidtouch.launcher.data.ContactsReader
import com.voidtouch.launcher.data.Folder
import com.voidtouch.launcher.data.Hit
import com.voidtouch.launcher.data.UniversalSearch
import com.voidtouch.launcher.data.Search
import com.voidtouch.launcher.ui.theme.VoidColors
import com.voidtouch.launcher.ui.theme.card
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
    folders: List<Folder>,
    onFolderCreate: (AppEntry, String) -> Unit,
    onFolderAdd: (String, AppEntry) -> Unit,
    onFolderRemoveApp: (AppEntry) -> Unit,
    onFolderRename: (String, String) -> Unit,
    onFolderDelete: (String) -> Unit,
    onTogglePinFolder: (Folder) -> Unit,
    startFolderId: String?,
    onAppInfo: (AppEntry) -> Unit,
    onUninstall: (AppEntry) -> Unit,
    onStartShortcut: (AppShortcut) -> Unit,
    onOpenSettings: () -> Unit,
    opens: Map<String, Int>,
    onHit: (Hit) -> Unit,
    onClose: () -> Unit,
) {
    val haptic = LocalHapticFeedback.current
    val focusManager = LocalFocusManager.current
    val listState = rememberLazyGridState()

    // Enquanto se marca os apps do foco, todos aparecem para poder desmarcar.
    val hiding = focusModeOn && !markingFocusApps
    val visible = remember(apps, focusApps, hiding) {
        if (hiding) apps.filter { it.key !in focusApps } else apps
    }
    val hiddenCount = apps.size - visible.size
    var openFolderId by remember { mutableStateOf(startFolderId) }
    var sheet by remember { mutableStateOf<Sheet?>(null) }
    var lastSheet by remember { mutableStateOf<Sheet?>(null) }
    if (sheet != null) lastSheet = sheet

    val byKey = remember(visible) { visible.associateBy { it.key } }
    val folderKeys = remember(folders) { folders.flatMap { it.keys }.toSet() }
    val openFolder = folders.firstOrNull { it.id == openFolderId }
    // Busca vasculha tudo; dentro de uma pasta só os apps dela; na lista principal, pastas primeiro
    // e depois só os apps que não estão em nenhuma pasta.
    val entries = remember(visible, folders, query, openFolderId, byKey, folderKeys) {
        when {
            query.isNotBlank() -> {
                val t0 = System.nanoTime()
                val found = Search.filter(visible, query, opens).map { Entry.AppItem(it) }
                Perf.stats.record(Perf.SEARCH, (System.nanoTime() - t0) / 1_000_000)
                found
            }
            openFolder != null ->
                openFolder.keys.mapNotNull { byKey[it] }.sortedBy { it.searchLabel }.map { Entry.AppItem(it) }
            else -> folders.sortedBy { Search.normalize(it.name) }.mapNotNull { f ->
                val n = f.keys.count { it in byKey }
                if (n > 0) Entry.FolderItem(f, n) else null
            } + visible.filter { it.key !in folderKeys }.map { Entry.AppItem(it) }
        }
    }
    // Resultados que não são apps: conta, conversão, ações, configurações e (com permissão) contatos.
    val context = LocalContext.current
    val extras = remember(query) { if (openFolder == null) UniversalSearch.hits(query) else emptyList() }
    val contacts by produceState(emptyList<Hit.Contact>(), query) {
        if (query.isBlank() || openFolderId != null) {
            value = emptyList()
        } else {
            delay(150)
            value = withContext(Dispatchers.IO) { ContactsReader.search(context, query) }
        }
    }
    val allHits = extras + contacts
    val showRail = entries.size > RAIL_MIN_ITEMS
    var railLetter by remember { mutableStateOf<Char?>(null) }

    // Voltar fecha primeiro a janela de opções, depois a pasta aberta.
    BackHandler(enabled = sheet != null || openFolder != null) {
        if (sheet != null) sheet = null else openFolderId = null
    }

    fun launch(app: AppEntry) {
        focusManager.clearFocus()
        onLaunch(app)
    }

    val caption = when {
        markingFocusApps -> "toque nos apps que distraem: eles somem no modo foco"
        query.isNotBlank() -> "${entries.size} encontrado(s)"
        openFolder != null -> "pasta ${openFolder.name} · ${entries.size} app(s)"
        hiding -> "modo foco ligado: $hiddenCount app(s) escondido(s)"
        else -> "segure um app para ver as opções"
    }

    Box(modifier = Modifier.fillMaxSize()) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .onVerticalSwipe(
                up = false,
                threshold = 96.dp,
                enabled = { !listState.canScrollBackward && sheet == null },
                // A faixa da régua é só da régua: arrastar nela não fecha a lista.
                excludeEndEdge = 44.dp,
                onSwipe = {
                    focusManager.clearFocus()
                    if (openFolderId != null) openFolderId = null else onClose()
                },
            ),
    ) {
        Column(modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 28.dp, bottom = 8.dp)) {
            SearchField(
                query = query,
                onQueryChange = onQueryChange,
                requestFocus = focusSearch,
                onGo = {
                    when (val first = entries.firstOrNull()) {
                        is Entry.AppItem -> launch(first.app)
                        is Entry.FolderItem -> openFolderId = first.folder.id
                        null -> Unit
                    }
                },
            )
            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (openFolder != null && query.isBlank()) {
                    Text(
                        text = "‹",
                        style = VoidType.App,
                        color = VoidColors.Text,
                        modifier = Modifier
                            .clickable(role = androidx.compose.ui.semantics.Role.Button) { openFolderId = null }
                            .padding(end = 12.dp, top = 8.dp, bottom = 8.dp),
                    )
                }
                Text(
                    text = caption,
                    style = VoidType.Caption,
                    color = VoidColors.TextDim,
                    modifier = Modifier.weight(1f).padding(end = 12.dp),
                )
                if (!markingFocusApps) {
                    GearButton(
                        onClick = {
                            focusManager.clearFocus()
                            onOpenSettings()
                        },
                    )
                    Spacer(Modifier.width(8.dp))
                }
                Text(
                    text = if (markingFocusApps) "concluir" else "apps do foco",
                    style = VoidType.Caption,
                    color = VoidColors.Text,
                    modifier = Modifier
                        .clickable(role = androidx.compose.ui.semantics.Role.Button) {
                            focusManager.clearFocus()
                            onMarkingChange(!markingFocusApps)
                        }
                        .padding(vertical = 8.dp),
                )
            }
        }

        if (entries.isEmpty() && allHits.isEmpty()) {
            Text(
                text = "nenhum app encontrado",
                style = VoidType.App,
                color = VoidColors.TextDim,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 20.dp),
            )
        }

        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 12.dp,
                    end = if (showRail) 40.dp else 12.dp,
                    bottom = 24.dp,
                ),
            ) {
                if (allHits.isNotEmpty()) {
                    items(items = allHits, key = { "hit:$it" }, span = { GridItemSpan(maxLineSpan) }) { hit ->
                        HitRow(hit, onClick = {
                            focusManager.clearFocus()
                            onHit(hit)
                        })
                    }
                }
                items(
                    items = entries,
                    key = { it.key },
                    contentType = { if (it is Entry.FolderItem) "folder" else "app" },
                ) { entry ->
                    when (entry) {
                        is Entry.FolderItem -> FolderCell(
                            modifier = Modifier.animateItem(),
                            folder = entry.folder,
                            count = entry.count,
                            pinned = FavoriteKeys.folder(entry.folder.id) in pinnedKeys,
                            onClick = { openFolderId = entry.folder.id },
                            onLongClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                sheet = Sheet.FolderActions(entry.folder)
                            },
                        )
                        is Entry.AppItem -> {
                            val app = entry.app
                            val marked = app.key in focusApps
                            AppCell(
                                modifier = Modifier.animateItem(),
                                app = app,
                                marked = marked,
                                pinned = app.key in pinnedKeys,
                                dimmed = markingFocusApps && marked,
                                onClick = { if (markingFocusApps) onToggleFocusApp(app) else launch(app) },
                                onLongClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    if (markingFocusApps) onToggleFocusApp(app) else sheet = Sheet.AppActions(app)
                                },
                            )
                        }
                    }
                }
            }

            if (showRail) {
                ScrollRail(
                    listState = listState,
                    itemCount = entries.size,
                    letterAt = { index -> letterOf(entries.getOrNull(index)) },
                    onLetterChange = { letter ->
                        if (letter != null && letter != railLetter) {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        }
                        railLetter = letter
                    },
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
                        .border(VoidColors.Stroke, VoidColors.Line, RoundedCornerShape(16.dp)),
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

    AnimatedVisibility(
        visible = sheet != null,
        enter = fadeIn(tween(160)),
        exit = fadeOut(tween(120)),
    ) {
        val current = lastSheet
        if (current != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.6f))
                    .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { sheet = null },
            ) {
                SheetPanel(
                    sheet = current,
                    folders = folders,
                    pinned = current.appOrNull()?.let { it.key in pinnedKeys } == true,
                    folderPinned = (current as? Sheet.FolderActions)?.let { FavoriteKeys.folder(it.folder.id) in pinnedKeys } == true,
                    onTogglePinFolder = onTogglePinFolder,
                    onSheet = { sheet = it },
                    onTogglePin = onTogglePin,
                    onFolderCreate = onFolderCreate,
                    onFolderAdd = onFolderAdd,
                    onFolderRemoveApp = onFolderRemoveApp,
                    onFolderRename = onFolderRename,
                    onFolderDelete = { id ->
                        if (openFolderId == id) openFolderId = null
                        onFolderDelete(id)
                    },
                    onAppInfo = onAppInfo,
                    onUninstall = onUninstall,
                    onStartShortcut = onStartShortcut,
                    modifier = Modifier.align(Alignment.BottomCenter),
                )
            }
        }
    }
    }
}

/** Itens da grade: um app ou uma pasta. */
private sealed interface Entry {
    val key: String

    data class AppItem(val app: AppEntry) : Entry {
        override val key: String get() = app.key
    }

    data class FolderItem(val folder: Folder, val count: Int) : Entry {
        override val key: String get() = "folder:${folder.id}"
    }
}

/** Janelas de opções que sobem de baixo. */
private sealed interface Sheet {
    data class AppActions(val app: AppEntry) : Sheet
    data class PickFolder(val app: AppEntry) : Sheet
    data class NewFolder(val app: AppEntry) : Sheet
    data class FolderActions(val folder: Folder) : Sheet
    data class RenameFolder(val folder: Folder) : Sheet

    fun appOrNull(): AppEntry? = when (this) {
        is AppActions -> app
        is PickFolder -> app
        is NewFolder -> app
        else -> null
    }
}

@Composable
private fun SheetPanel(
    sheet: Sheet,
    folders: List<Folder>,
    pinned: Boolean,
    folderPinned: Boolean,
    onTogglePinFolder: (Folder) -> Unit,
    onSheet: (Sheet?) -> Unit,
    onTogglePin: (AppEntry) -> Unit,
    onFolderCreate: (AppEntry, String) -> Unit,
    onFolderAdd: (String, AppEntry) -> Unit,
    onFolderRemoveApp: (AppEntry) -> Unit,
    onFolderRename: (String, String) -> Unit,
    onFolderDelete: (String) -> Unit,
    onAppInfo: (AppEntry) -> Unit,
    onUninstall: (AppEntry) -> Unit,
    onStartShortcut: (AppShortcut) -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(VoidColors.Graphite, shape)
            .border(VoidColors.Stroke, VoidColors.Line, shape)
            // Absorve o toque para não fechar a janela ao tocar dentro dela.
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {}
            .padding(horizontal = 24.dp, vertical = 20.dp),
    ) {
        when (sheet) {
            is Sheet.AppActions -> {
                val app = sheet.app
                val inFolder = folders.firstOrNull { f -> app.key in f.keys }
                SheetTitle(app.label)
                // Atalhos do próprio app (nova mensagem, selfie...), lidos fora da thread principal.
                val context = LocalContext.current
                val shortcuts by produceState(emptyList<AppShortcut>(), app.key) {
                    value = withContext(Dispatchers.IO) { Shortcuts.load(context.applicationContext, app.packageName) }
                }
                shortcuts.forEach { sc ->
                    SheetRow("↗ ${sc.label}") {
                        onStartShortcut(sc)
                        onSheet(null)
                    }
                }
                SheetRow(if (pinned) "tirar da home" else "fixar na home") {
                    onTogglePin(app)
                    onSheet(null)
                }
                if (inFolder != null) {
                    SheetRow("tirar da pasta ${inFolder.name}") {
                        onFolderRemoveApp(app)
                        onSheet(null)
                    }
                } else {
                    SheetRow("adicionar à pasta ›") { onSheet(Sheet.PickFolder(app)) }
                }
                SheetRow("informações do app") {
                    onAppInfo(app)
                    onSheet(null)
                }
                SheetRow("desinstalar") {
                    onUninstall(app)
                    onSheet(null)
                }
            }
            is Sheet.PickFolder -> {
                SheetTitle("adicionar ${sheet.app.label} a…")
                folders.sortedBy { Search.normalize(it.name) }.forEach { f ->
                    SheetRow("${f.name} · ${f.keys.size}") {
                        onFolderAdd(f.id, sheet.app)
                        onSheet(null)
                    }
                }
                SheetRow("+ nova pasta") { onSheet(Sheet.NewFolder(sheet.app)) }
            }
            is Sheet.NewFolder -> {
                var name by remember { mutableStateOf("") }
                SheetTitle("nome da nova pasta")
                SearchField(
                    query = name,
                    onQueryChange = { name = it },
                    requestFocus = true,
                    placeholder = "ex.: jogos",
                    onGo = {
                        onFolderCreate(sheet.app, name)
                        onSheet(null)
                    },
                )
                SheetRow("criar pasta") {
                    onFolderCreate(sheet.app, name)
                    onSheet(null)
                }
            }
            is Sheet.FolderActions -> {
                SheetTitle("pasta ${sheet.folder.name}")
                SheetRow(if (folderPinned) "tirar da home" else "fixar na home") {
                    onTogglePinFolder(sheet.folder)
                    onSheet(null)
                }
                SheetRow("renomear") { onSheet(Sheet.RenameFolder(sheet.folder)) }
                SheetRow("excluir pasta (os apps voltam à lista)") {
                    onFolderDelete(sheet.folder.id)
                    onSheet(null)
                }
            }
            is Sheet.RenameFolder -> {
                var name by remember { mutableStateOf(sheet.folder.name) }
                SheetTitle("renomear pasta")
                SearchField(
                    query = name,
                    onQueryChange = { name = it },
                    requestFocus = true,
                    placeholder = "nome da pasta",
                    onGo = {
                        onFolderRename(sheet.folder.id, name)
                        onSheet(null)
                    },
                )
                SheetRow("salvar") {
                    onFolderRename(sheet.folder.id, name)
                    onSheet(null)
                }
            }
        }
    }
}

@Composable
private fun SheetTitle(text: String) {
    Text(text, style = VoidType.Caption, color = VoidColors.TextDim, modifier = Modifier.padding(bottom = 6.dp))
}

@Composable
private fun SheetRow(text: String, onClick: () -> Unit) {
    Text(
        text = text,
        style = VoidType.App,
        color = VoidColors.Text,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(role = androidx.compose.ui.semantics.Role.Button, onClick = onClick)
            .padding(vertical = 14.dp),
    )
}

/** Contorno de pasta desenhado (aba e corpo), usado na grade e nos favoritos. */
@Composable
internal fun FolderGlyph(width: androidx.compose.ui.unit.Dp, height: androidx.compose.ui.unit.Dp) {
    Canvas(modifier = Modifier.size(width, height)) {
        val unit = size.height / 28f
        val stroke = Stroke(width = 1.6.dp.toPx().coerceAtMost(2f * unit * 1.dp.toPx()))
        val top = 5.dp.toPx() * unit
        val tab = Path().apply {
            moveTo(0f, top)
            lineTo(0f, 2.dp.toPx() * unit)
            lineTo(12.dp.toPx() * unit, 2.dp.toPx() * unit)
            lineTo(15.dp.toPx() * unit, top)
        }
        drawPath(tab, VoidColors.TextDim, style = stroke)
        drawRoundRect(
            color = VoidColors.Text,
            topLeft = Offset(0f, top),
            size = Size(size.width, size.height - top),
            cornerRadius = CornerRadius(4.dp.toPx() * unit),
            style = stroke,
        )
    }
}

/** Pasta na grade: contorno de pasta com a quantidade de apps dentro, e o nome embaixo. */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun FolderCell(
    folder: Folder,
    count: Int,
    pinned: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val source = remember { MutableInteractionSource() }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxWidth()
            .then(if (VoidColors.isRotina) Modifier.padding(4.dp).card(16.dp) else Modifier)
            .pressFeedback(source)
            .combinedClickable(
                role = androidx.compose.ui.semantics.Role.Button,
                interactionSource = source,
                indication = null,
                onClick = onClick,
                onLongClick = onLongClick,
            )
            .padding(horizontal = 4.dp, vertical = 12.dp),
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(44.dp)) {
            FolderGlyph(width = 34.dp, height = 28.dp)
            if (pinned) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(6.dp)
                        .background(VoidColors.Text, CircleShape),
                )
            }
            Text(
                text = count.toString(),
                style = VoidType.Caption,
                color = VoidColors.Text,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text = folder.name,
            style = VoidType.Small.copy(fontSize = 12.sp, lineHeight = 15.sp),
            color = VoidColors.Text,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/** Célula da grade: ícone em cima, nome embaixo. Ponto cheio = fixado na home; círculo = app do foco. */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun AppCell(
    app: AppEntry,
    marked: Boolean,
    pinned: Boolean,
    dimmed: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val source = remember { MutableInteractionSource() }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxWidth()
            .then(if (VoidColors.isRotina) Modifier.padding(4.dp).card(16.dp) else Modifier)
            .pressFeedback(source)
            .combinedClickable(
                role = androidx.compose.ui.semantics.Role.Button,
                interactionSource = source,
                indication = null,
                onClick = onClick,
                onLongClick = onLongClick,
            )
            .padding(horizontal = 4.dp, vertical = 12.dp),
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(44.dp)) {
            AppIcon(app = app, size = 36.dp)
            if (pinned) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(6.dp)
                        .background(VoidColors.Text, CircleShape),
                )
            }
            if (marked) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .size(9.dp)
                        .border(VoidColors.Stroke, VoidColors.TextDim, CircleShape),
                )
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text = app.label,
            style = VoidType.Small.copy(fontSize = 12.sp, lineHeight = 15.sp),
            color = if (dimmed) VoidColors.TextDim else VoidColors.Text,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/** Letra inicial do app, sem acento; qualquer coisa que não seja letra vira "#". */
private fun letterOf(entry: Entry?): Char = when (entry) {
    is Entry.AppItem -> entry.app.searchLabel.firstOrNull()?.takeIf { it.isLetter() }?.uppercaseChar() ?: '#'
    else -> '#'
}

/**
 * Régua de rolagem: uma linha tracejada na lateral com um traço cheio que mostra onde a lista
 * está. Arrastar nela rola a lista na hora e mostra a letra do ponto.
 */
@Composable
private fun ScrollRail(
    listState: LazyGridState,
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
    placeholder: String = "buscar app",
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
                        Text(text = placeholder, style = VoidType.App, color = VoidColors.TextDim)
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

/** Uma linha de resultado que não é app: resposta, ação, configuração ou contato. */
@Composable
private fun HitRow(hit: Hit, onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(role = androidx.compose.ui.semantics.Role.Button, onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
    ) {
        Text(
            text = if (hit is Hit.Answer) "= ${hit.title}" else hit.title,
            style = VoidType.App,
            color = VoidColors.Text,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(Modifier.width(12.dp))
        Text(text = hit.hint, style = VoidType.Caption, color = VoidColors.TextDim, maxLines = 1)
    }
}
