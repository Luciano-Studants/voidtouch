package com.voidtouch.launcher.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.draw.drawBehind
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import com.voidtouch.launcher.data.UpdateCheck
import com.voidtouch.launcher.data.UpdateInfo
import com.voidtouch.launcher.data.Updater
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.voidtouch.launcher.data.AppEntry
import com.voidtouch.launcher.data.ClockStyle
import com.voidtouch.launcher.data.Design
import com.voidtouch.launcher.data.MemoryMode
import com.voidtouch.launcher.data.GestureTarget
import com.voidtouch.launcher.data.QuickAction
import com.voidtouch.launcher.data.Search
import com.voidtouch.launcher.data.Scene
import com.voidtouch.launcher.data.SwipeSide
import com.voidtouch.launcher.ui.theme.Backdrop
import com.voidtouch.launcher.data.ColorSets
import com.voidtouch.launcher.data.Tone
import com.voidtouch.launcher.data.WallTone
import com.voidtouch.launcher.ui.theme.VoidColors
import com.voidtouch.launcher.ui.theme.VoidType

/**
 * Ajustes: fundo, formato da hora e o app de cada deslize horizontal da home.
 * Tudo se escolhe com um toque; "fechar" ou voltar sai. Deslizar para baixo só rola a lista.
 */
@Composable
fun SettingsScreen(
    apps: List<AppEntry>,
    backdrop: Backdrop,
    clockStyle: ClockStyle,
    gestures: Map<SwipeSide, String?>,
    versionName: String,
    notificationsEnabled: Boolean,
    onOpenNotificationAccess: () -> Unit,
    onOpenAppInfo: () -> Unit,
    onOpenChangelog: () -> Unit,
    onOpenTips: () -> Unit,
    focusAutoOn: Boolean,
    focusAutoStart: Int,
    focusAutoEnd: Int,
    onFocusAutoToggle: () -> Unit,
    onFocusAutoStart: () -> Unit,
    onFocusAutoEnd: () -> Unit,
    colorSet: String,
    onColorSet: (String) -> Unit,
    design: Design,
    onDesign: (Design) -> Unit,
    highVis: Boolean,
    onHighVis: () -> Unit,
    parallax: Boolean,
    parallaxAvailable: Boolean,
    ownImage: Boolean,
    onPickImage: () -> Unit,
    onRemoveImage: () -> Unit,
    onParallax: () -> Unit,
    openAnim: Boolean,
    onOpenAnim: () -> Unit,
    openHaptic: Boolean,
    onOpenHaptic: () -> Unit,
    dockOn: Boolean,
    onDockToggle: () -> Unit,
    dockAwake: Boolean,
    onDockAwake: () -> Unit,
    predictOn: Boolean,
    onPredictToggle: () -> Unit,
    onPredictClear: () -> Unit,
    scenes: List<Scene>,
    sceneActive: String?,
    sceneAuto: Boolean,
    onSceneAuto: () -> Unit,
    onSceneUse: (String?) -> Unit,
    onSceneEdit: (Scene) -> Unit,
    onSceneAdd: () -> Unit,
    onSceneDelete: (String) -> Unit,
    nightEnabled: Boolean,
    nightStart: Int,
    nightEnd: Int,
    onNightToggle: () -> Unit,
    onNightStart: () -> Unit,
    onNightEnd: () -> Unit,
    onBackupSave: () -> Unit,
    onBackupRestore: () -> Unit,
    openTimeMs: Long,
    onOpenOptimize: () -> Unit,
    onOpenPermissions: () -> Unit,
    onOpenUsage: () -> Unit,
    onOpenHomeSettings: () -> Unit,
    onOpenFavoritesOrder: () -> Unit,
    memoryMode: MemoryMode,
    hiddenPhotos: Int,
    onMemoryMode: () -> Unit,
    onRestorePhotos: () -> Unit,
    onBackdrop: (Backdrop) -> Unit,
    onClockStyle: (ClockStyle) -> Unit,
    onSwipeApp: (SwipeSide, String?) -> Unit,
    onClose: () -> Unit,
) {
    var picking by remember { mutableStateOf<SwipeSide?>(null) }
    val byKey = remember(apps) { apps.associateBy { it.key } }

    BackHandler(enabled = picking != null) { picking = null }

    picking?.let { side ->
        AppPicker(
            title = "o que o gesto \"${gestureName(side)}\" abre",
            apps = apps,
            onPick = {
                onSwipeApp(side, it)
                picking = null
            },
        )
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 28.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("ajustes", style = VoidType.App.copy(fontSize = 24.sp, lineHeight = 30.sp), color = VoidColors.Text, modifier = Modifier.weight(1f))
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .clip(CircleShape)
                    .border(VoidColors.Stroke, VoidColors.TextDim, CircleShape)
                    .clickable(role = Role.Button, onClick = onClose)
                    .heightIn(min = 44.dp)
                    .padding(horizontal = 20.dp),
            ) {
                Text("fechar", style = VoidType.Caption, color = VoidColors.Text)
            }
        }
        Note("Toque numa linha para mudar. Linhas com › abrem outra tela, as chaves ligam e desligam, o círculo marca a escolha e os textos pequenos só explicam.")

        Section("aparência")
        ChoiceGroup(items = Design.entries.toList(), selected = design, label = { "design ${it.label}" }, onSelect = onDesign)
        Note(design.note)
        ChoiceGroup(
            items = Backdrop.entries.toList(),
            selected = backdrop,
            label = {
                when (it) {
                    Backdrop.Void -> "fundo preto"
                    Backdrop.Graphite -> "fundo grafite"
                    Backdrop.Wallpaper -> "fundo com papel de parede"
                }
            },
            onSelect = onBackdrop,
        )
        Spacer(Modifier.height(10.dp))
        ChoiceGroup(items = ClockStyle.entries.toList(), selected = clockStyle, label = { "hora ${it.label}" }, onSelect = onClockStyle)
        Spacer(Modifier.height(10.dp))
        Group { ToggleRow("alta visibilidade", highVis, onHighVis) }
        Note("Alta visibilidade é para sol forte ou lugares difíceis de ver: fundo preto, texto branco em negrito, contornos mais grossos, fonte maior e brilho da tela no máximo enquanto a home está aberta. Também dá para ligar por um gesto da home.")

        Section("papel de parede")
        Note("Conjunto de cores: toque para trocar. Cada nome já aparece na cor dele.", top = 0.dp)
        Spacer(Modifier.height(6.dp))
        Group {
            ColorSets.all.forEach { set ->
                val pal = ColorSets.palette(set, WallTone.fromPixels(IntArray(0)) ?: Tone(30f, 0.8f), true)
                ChoiceRow(
                    label = set.name,
                    selected = set.key == colorSet,
                    onClick = { onColorSet(set.key) },
                    labelColor = Color(pal.text),
                    sample = "texto",
                    sampleColor = Color(pal.dim),
                )
            }
        }
        Note("O automático segue o papel de parede e só vale com o fundo em papel de parede. A luminosidade é fixa, então a leitura é a mesma em todos.")
        Group {
            ActionRow(if (ownImage) "trocar a imagem de fundo própria" else "escolher imagem de fundo própria", onPickImage)
            if (ownImage) ActionRow("remover a imagem própria", onRemoveImage)
            ToggleRow("paralaxe", parallax, onParallax, unavailable = !parallaxAvailable)
        }
        Note("A imagem escolhida fica só neste aparelho, reduzida ao tamanho da tela. Funciona em qualquer Android e permite a paralaxe mesmo quando o sistema não deixa ler o papel de parede.")
        Note("Paralaxe: com o fundo em papel de parede, a imagem se move de leve conforme você inclina o celular. Só funciona com a home visível e usa o acelerômetro apenas nesse momento. Em alguns aparelhos o Android não deixa apps lerem o papel de parede; aí fica indisponível, mas as cores continuam funcionando.", top = 4.dp)

        Section("home")
        Group {
            SwipeSide.entries.forEach { side ->
                ValueRow(gestureName(side), targetLabel(gestures[side], byKey)) { picking = side }
            }
        }
        Note("Os gestos valem na home, abaixo do carrossel. Cada um abre um app ou executa uma ação (lanterna, painel de notificações, busca...).")
        Group { ActionRow("ordem dos favoritos", onOpenFavoritesOrder) }
        Spacer(Modifier.height(10.dp))
        Group {
            ToggleRow("animação a partir do toque", openAnim, onOpenAnim)
            ToggleRow("vibração leve ao abrir", openHaptic, onOpenHaptic)
        }
        Note("O app se abre crescendo do ponto onde você tocou, e os itens da home e da lista afundam e voltam com um pequeno quique ao toque.")
        Group {
            ToggleRow("linha \"agora\"", predictOn, onPredictToggle)
            if (predictOn) ActionRow("apagar o histórico de horários", onPredictClear)
        }
        Note("Mostra na home os apps que você costuma abrir neste horário, só pelo seu histórico no aparelho. Nada é enviado e nada roda em segundo plano.")
        Group {
            ValueRow("memórias do dia", memoryMode.label, onMemoryMode)
            if (hiddenPhotos > 0) ActionRow("$hiddenPhotos foto(s) oculta(s) · restaurar", onRestorePhotos)
        }
        Note("Mostra, ao lado da hora, uma foto da galeria (menos capturas de tela) tirada por volta de hoje em anos ou meses atrás. As fotos não saem do aparelho.")

        Section("modos")
        Group {
            ToggleRow("modo carregando", dockOn, onDockToggle)
            if (dockOn) ToggleRow("manter a tela acesa", dockAwake, onDockAwake)
        }
        Note("Com o celular carregando e a home parada por 15 segundos, aparece um relógio grande de cabeceira com a bateria e a música. Um toque sai.")
        Group {
            ToggleRow("foco automático", focusAutoOn, onFocusAutoToggle)
            if (focusAutoOn) {
                ValueRow("liga às", "${focusAutoStart}h", onFocusAutoStart)
                ValueRow("desliga às", "${focusAutoEnd}h", onFocusAutoEnd)
            }
        }
        Note("Liga e desliga o modo foco nesse horário. Precisa de apps marcados em \"apps do foco\". A troca acontece quando a home está aberta; você pode ligar ou desligar à mão a qualquer momento.")
        Group {
            ToggleRow("modo noite", nightEnabled, onNightToggle)
            if (nightEnabled) {
                ValueRow("começa às", "${nightStart}h", onNightStart)
                ValueRow("termina às", "${nightEnd}h", onNightEnd)
            }
        }
        Note("No horário escolhido a home fica mais escura e mais calma: some o bloco de status e notificações e a foto do dia. Toque nos horários para mudar de hora em hora.")

        Section("cenas")
        var openScene by remember { mutableStateOf<String?>(null) }
        Group {
            ToggleRow("trocar sozinha pelo horário", sceneAuto, onSceneAuto)
            scenes.forEach { sc ->
                val on = sc.id == sceneActive
                ValueRow((if (on) "● " else "") + sc.name, "${sc.start}h às ${sc.end}h") {
                    openScene = if (openScene == sc.id) null else sc.id
                }
                if (openScene == sc.id) {
                    ActionRow(if (on) "em uso · voltar para sem cena" else "usar esta cena agora") { onSceneUse(if (on) null else sc.id) }
                    ValueRow("começa às", "${sc.start}h") { onSceneEdit(sc.copy(start = (sc.start + 1) % 24)) }
                    ValueRow("termina às", "${sc.end}h") { onSceneEdit(sc.copy(end = (sc.end + 1) % 24)) }
                    ToggleRow("liga o modo foco", sc.focus, onToggle = { onSceneEdit(sc.copy(focus = !sc.focus)) })
                    ToggleRow("home calma", sc.quiet, onToggle = { onSceneEdit(sc.copy(quiet = !sc.quiet)) })
                    ActionRow("excluir esta cena") { onSceneDelete(sc.id) }
                }
            }
            ActionRow("+ nova cena", onSceneAdd)
        }
        Note("Cada cena lembra os próprios favoritos: ao sair dela, os favoritos de agora ficam guardados. A home calma esconde status, notificações e foto, como o modo noite. Toque numa cena para ver as opções dela.")

        Section("celular")
        Group {
            ActionRow("otimizar", onOpenOptimize)
            ActionRow("relatório de uso", onOpenUsage)
            ActionRow("permissões", onOpenPermissions)
            ActionRow("trocar tela inicial padrão", onOpenHomeSettings)
        }
        Note("Se o VoidTouch travar várias vezes seguidas, ele abre um modo seguro sozinho. Aqui você também pode voltar para outra tela inicial a qualquer momento.")

        Section("notificações e música")
        Group {
            ValueRow("acesso às notificações", if (notificationsEnabled) "ligado" else "desligado", onOpenNotificationAccess)
            ActionRow("liberar configurações restritas", onOpenAppInfo)
        }
        Note("Se o acesso aparecer bloqueado: info do app > ⋮ > permitir configurações restritas.")

        Section("backup")
        Group {
            ActionRow("salvar backup em arquivo", onBackupSave)
            ActionRow("restaurar backup de arquivo", onBackupRestore)
        }
        Note("Guarda favoritos, pastas, apps do foco, gestos e ajustes. Útil antes de trocar de celular.")

        Section("sobre")
        Group {
            VersionLine(versionName)
            ActionRow("novidades desta versão", onOpenChangelog)
            ActionRow("dicas de uso", onOpenTips)
            UpdateRow(versionName)
        }
        if (openTimeMs > 0L) Note("Última abertura: $openTimeMs ms.")
    }
}

/** Linha "verificar atualização": consulta o GitHub, baixa o APK e abre o instalador. */
@Composable
private fun UpdateRow(versionName: String) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var status by remember { mutableStateOf("verificar atualização") }
    var found by remember { mutableStateOf<UpdateInfo?>(null) }
    var busy by remember { mutableStateOf(false) }
    var progress by remember { mutableStateOf<Float?>(null) }

    fun download(info: UpdateInfo) {
        if (!Updater.canInstall(context)) {
            status = "libere \"instalar apps desconhecidos\" e toque de novo"
            Updater.openInstallPermission(context)
            return
        }
        busy = true
        scope.launch {
            val result = Updater.download(context, info) { status = "baixando… $it%"; progress = it / 100f }
            busy = false
            progress = null
            when (result) {
                is Updater.Download.Failed -> status = "${result.reason}; nada foi instalado. Toque para tentar de novo"
                is Updater.Download.Ready -> {
                    status = "arquivo conferido: confirme a instalação"
                    if (!Updater.install(context, result.file)) status = "não foi possível abrir o instalador do Android"
                }
            }
        }
    }

    RowBase(tap = Modifier.clickable(enabled = !busy, role = Role.Button) {
                val info = found
                if (info != null) {
                    download(info)
                } else {
                    busy = true
                    progress = null
                    status = "verificando…"
                    scope.launch {
                        when (val r = Updater.check(versionName)) {
                            is UpdateCheck.Available -> {
                                found = r.info
                                status = "v${r.info.version} disponível · atualizar"
                            }
                            UpdateCheck.UpToDate -> status = "você já está na versão mais recente"
                            UpdateCheck.NoRelease -> status = "nenhuma versão publicada ainda"
                            is UpdateCheck.Failed -> status = "não foi possível verificar: ${r.reason}. toque para tentar"
                        }
                        busy = false
                    }
                }
}) {
        Text(status, style = VoidType.App, color = VoidColors.Text, modifier = Modifier.weight(1f))
        if (!busy) {
            Spacer(Modifier.width(12.dp))
            Text("›", style = VoidType.App.copy(fontSize = 20.sp), color = VoidColors.TextDim)
        }
    }
    // Barra enquanto algo acontece: indeterminada ao verificar, com avanço ao baixar.
    if (busy) LoadingLine(progress = progress)
}

/** Título de um bloco de ajustes. */
@Composable
private fun Section(title: String) {
    Spacer(Modifier.height(26.dp))
    Text(
        title.uppercase(),
        style = VoidType.Caption.copy(fontSize = 11.sp, letterSpacing = 1.4.sp),
        color = VoidColors.TextDim,
        modifier = Modifier.padding(start = 4.dp, bottom = 8.dp),
    )
}

/** Texto de explicação: pequeno, claro e sem toque, para não ser confundido com botão. */
@Composable
private fun Note(text: String, top: Dp = 8.dp) {
    Text(
        text,
        style = VoidType.Small.copy(fontSize = 12.sp, lineHeight = 17.sp),
        color = VoidColors.TextDim,
        modifier = Modifier.padding(start = 4.dp, end = 4.dp, top = top, bottom = 2.dp),
    )
}

/** Contêiner com contorno: tudo o que está dentro dele é uma linha de ajuste que dá para tocar. */
@Composable
private fun Group(content: @Composable ColumnScope.() -> Unit) {
    val shape = RoundedCornerShape(14.dp)
    val fill = if (VoidColors.isRotina) VoidColors.Surface else Color.Black.copy(alpha = 0.35f)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(fill)
            .border(VoidColors.Stroke, VoidColors.Line, shape),
        content = content,
    )
}

/** Base de toda linha: altura mínima de 52 dp e uma divisória fina embaixo. */
@Composable
private fun RowBase(tap: Modifier = Modifier, content: @Composable RowScope.() -> Unit) {
    val line = VoidColors.Line
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .then(tap)
            .heightIn(min = 52.dp)
            .drawBehind {
                drawLine(line, Offset(0f, size.height), Offset(size.width, size.height), strokeWidth = 1.dp.toPx())
            }
            .padding(horizontal = 16.dp, vertical = 10.dp),
        content = content,
    )
}

@Composable
private fun Chevron() {
    Spacer(Modifier.width(12.dp))
    Text("›", style = VoidType.App.copy(fontSize = 20.sp), color = VoidColors.TextDim)
}

/** Abre outra tela ou executa algo: texto e a seta ›. */
@Composable
private fun ActionRow(label: String, onClick: () -> Unit) {
    RowBase(tap = Modifier.clickable(role = Role.Button, onClick = onClick)) {
        Text(label, style = VoidType.App, color = VoidColors.Text, modifier = Modifier.weight(1f))
        Chevron()
    }
}

/** Mostra o valor atual à direita; tocar muda o valor ou abre a escolha. */
@Composable
private fun ValueRow(label: String, value: String, onClick: () -> Unit) {
    RowBase(tap = Modifier.clickable(role = Role.Button, onClick = onClick)) {
        Text(label, style = VoidType.App, color = VoidColors.Text, modifier = Modifier.weight(1f))
        Spacer(Modifier.width(12.dp))
        Text(value, style = VoidType.Small, color = VoidColors.TextDim, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.widthIn(max = 150.dp))
        Chevron()
    }
}

/** Liga e desliga: a chave à direita mostra o estado. */
@Composable
private fun ToggleRow(label: String, on: Boolean, onToggle: () -> Unit, unavailable: Boolean = false) {
    RowBase(tap = Modifier.toggleable(value = on, role = Role.Switch, onValueChange = { onToggle() })) {
        Text(label, style = VoidType.App, color = VoidColors.Text, modifier = Modifier.weight(1f))
        Spacer(Modifier.width(12.dp))
        if (unavailable) {
            Text("indisponível", style = VoidType.Small, color = VoidColors.TextDim)
        } else {
            SwitchMark(on)
        }
    }
}

@Composable
private fun SwitchMark(on: Boolean) {
    val edge = if (on) VoidColors.Selected else VoidColors.TextDim
    Box(
        contentAlignment = if (on) Alignment.CenterEnd else Alignment.CenterStart,
        modifier = Modifier
            .size(width = 46.dp, height = 26.dp)
            .clip(CircleShape)
            .background(if (on) VoidColors.Selected else Color.Transparent)
            .border(VoidColors.Stroke, edge, CircleShape)
            .padding(4.dp),
    ) {
        Box(Modifier.size(18.dp).background(if (on) VoidColors.OnSelected else VoidColors.TextDim, CircleShape))
    }
}

/** Escolha entre várias opções: o círculo à esquerda marca a selecionada. */
@Composable
private fun ChoiceRow(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    labelColor: Color = Color.Unspecified,
    sample: String? = null,
    sampleColor: Color = Color.Unspecified,
) {
    RowBase(tap = Modifier.selectable(selected = selected, role = Role.RadioButton, onClick = onClick)) {
        val ring = if (selected) VoidColors.Selected else VoidColors.TextDim
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(20.dp).border(VoidColors.Stroke, ring, CircleShape),
        ) {
            if (selected) Box(Modifier.size(10.dp).background(VoidColors.Selected, CircleShape))
        }
        Spacer(Modifier.width(14.dp))
        Text(
            label,
            style = VoidType.App,
            color = if (labelColor != Color.Unspecified) labelColor else if (selected) VoidColors.Text else VoidColors.TextDim,
            modifier = Modifier.weight(1f),
        )
        if (sample != null) Text(sample, style = VoidType.Small, color = if (sampleColor != Color.Unspecified) sampleColor else VoidColors.TextDim)
    }
}

@Composable
private fun <T> ChoiceGroup(items: List<T>, selected: T, label: (T) -> String, onSelect: (T) -> Unit) {
    Group {
        items.forEach { item -> ChoiceRow(label(item), item == selected, { onSelect(item) }) }
    }
}

private fun gestureName(side: SwipeSide): String = when (side) {
    SwipeSide.Left -> "deslizar para a esquerda"
    SwipeSide.Right -> "deslizar para a direita"
    SwipeSide.Down -> "deslizar para baixo"
    SwipeSide.DoubleTap -> "toque duplo"
}

private fun targetLabel(key: String?, byKey: Map<String, AppEntry>): String =
    GestureTarget.action(key)?.label ?: byKey[key]?.label ?: "nenhum"

/** Escolha do destino de um gesto: ações rápidas e apps, com busca; "nenhum" limpa o gesto. */
@Composable
private fun AppPicker(title: String, apps: List<AppEntry>, onPick: (String?) -> Unit) {
    var query by remember { mutableStateOf("") }
    val results = remember(apps, query) { Search.filter(apps, query) }
    val actions = remember(query) {
        if (query.isBlank()) QuickAction.entries.toList()
        else QuickAction.entries.filter { Search.normalize(it.label).contains(Search.normalize(query)) }
    }
    Column(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 28.dp, bottom = 8.dp)) {
            Text(title, style = VoidType.Caption, color = VoidColors.TextDim)
            Spacer(Modifier.height(10.dp))
            SearchField(query = query, onQueryChange = { query = it }, requestFocus = false, onGo = {
                actions.firstOrNull()?.let { onPick(GestureTarget.encode(it)) }
                    ?: results.firstOrNull()?.let { onPick(it.key) }
            })
        }
        LazyColumn(contentPadding = PaddingValues(bottom = 24.dp)) {
            item {
                Text(
                    "nenhum",
                    style = VoidType.App,
                    color = VoidColors.TextDim,
                    modifier = Modifier.fillMaxWidth().clickable(role = androidx.compose.ui.semantics.Role.Button) { onPick(null) }.padding(horizontal = 24.dp, vertical = 14.dp),
                )
            }
            items(actions, key = { "action:${it.id}" }) { action ->
                Text(
                    "◆ ${action.label}",
                    style = VoidType.App,
                    color = VoidColors.Text,
                    modifier = Modifier.fillMaxWidth().clickable(role = androidx.compose.ui.semantics.Role.Button) { onPick(GestureTarget.encode(action)) }.padding(horizontal = 24.dp, vertical = 14.dp),
                )
            }
            items(results, key = { it.key }) { app ->
                Text(
                    app.label,
                    style = VoidType.App,
                    color = VoidColors.Text,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth().clickable(role = androidx.compose.ui.semantics.Role.Button) { onPick(app.key) }.padding(horizontal = 24.dp, vertical = 14.dp),
                )
            }
        }
    }
}

/**
 * Versão do app. Segurar o texto mostra o teste do modo seguro, escondido de propósito para ninguém
 * tocar sem querer: cada toque derruba o app de verdade, para provar que a proteção funciona.
 */
@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
private fun VersionLine(versionName: String) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var test by remember { mutableStateOf(false) }
    RowBase(tap = Modifier.combinedClickable(role = Role.Button, onClick = {}, onLongClick = { test = !test })) {
        Text("versão", style = VoidType.App, color = VoidColors.Text, modifier = Modifier.weight(1f))
        Text("VoidTouch $versionName", style = VoidType.Small, color = VoidColors.TextDim)
    }
    val last = remember(test) { com.voidtouch.launcher.CrashGuard.lastError(context)?.lineSequence()?.firstOrNull() }
    if (last != null) {
        RowBase { Text("último travamento: $last", style = VoidType.Small.copy(fontSize = 12.sp, lineHeight = 17.sp), color = VoidColors.TextDim) }
    }
    if (test) {
        ActionRow("simular travamento (teste do modo seguro)") {
            throw IllegalStateException("travamento simulado pelo usuário")
        }
        RowBase {
            Text(
                "Cada toque fecha o VoidTouch de propósito. Depois de 2 toques logo após uma atualização (ou 3 em seguida), a tela inicial abre em modo seguro. Lá, \"tentar a home de novo\" volta ao normal.",
                style = VoidType.Small.copy(fontSize = 12.sp, lineHeight = 17.sp),
                color = VoidColors.TextDim,
            )
        }
    }
}
