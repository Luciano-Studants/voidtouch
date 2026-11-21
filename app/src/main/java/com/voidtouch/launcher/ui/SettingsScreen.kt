package com.voidtouch.launcher.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
    parallax: Boolean,
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
            .padding(horizontal = 24.dp, vertical = 28.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("ajustes", style = VoidType.App, color = VoidColors.Text, modifier = Modifier.weight(1f))
            Text(
                "fechar",
                style = VoidType.Caption,
                color = VoidColors.Text,
                modifier = Modifier.clickable(onClick = onClose).padding(vertical = 8.dp),
            )
        }

        Section("fundo")
        Options(
            items = Backdrop.entries.toList(),
            selected = backdrop,
            label = {
                when (it) {
                    Backdrop.Void -> "preto"
                    Backdrop.Graphite -> "grafite"
                    Backdrop.Wallpaper -> "papel de parede"
                }
            },
            onSelect = onBackdrop,
        )

        Section("hora")
        Options(
            items = ClockStyle.entries.toList(),
            selected = clockStyle,
            label = { it.label },
            onSelect = onClockStyle,
        )

        Section("gestos da home")
        SwipeSide.entries.forEach { side ->
            Text(gestureName(side), style = VoidType.Caption, color = VoidColors.TextDim, modifier = Modifier.padding(top = 8.dp))
            ValueRow(targetLabel(gestures[side], byKey)) { picking = side }
        }
        Text(
            "os gestos valem na home, abaixo do carrossel. Cada um abre um app ou executa uma ação (lanterna, painel de notificações, busca...)",
            style = VoidType.Small.copy(fontSize = 12.sp, lineHeight = 17.sp),
            color = VoidColors.TextDim,
            modifier = Modifier.padding(top = 10.dp),
        )

        Section("favoritos")
        ValueRow("ordem dos favoritos", onOpenFavoritesOrder)

        Section("papel de parede")
        Text(
            "conjunto de cores · toque para trocar; cada nome já aparece na cor dele",
            style = VoidType.Small.copy(fontSize = 12.sp, lineHeight = 17.sp),
            color = VoidColors.TextDim,
            modifier = Modifier.padding(top = 8.dp),
        )
        ColorSets.all.forEach { set ->
            val pal = ColorSets.palette(set, WallTone.fromPixels(IntArray(0)) ?: Tone(30f, 0.8f), true)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().clickable { onColorSet(set.key) }.padding(vertical = 12.dp),
            ) {
                Text(
                    set.name,
                    style = VoidType.App,
                    color = androidx.compose.ui.graphics.Color(pal.text),
                    modifier = Modifier.weight(1f),
                )
                Text(
                    "texto",
                    style = VoidType.Small,
                    color = androidx.compose.ui.graphics.Color(pal.dim),
                )
                Spacer(Modifier.width(12.dp))
                Text(if (set.key == colorSet) "●" else "○", style = VoidType.App, color = VoidColors.Text)
            }
        }
        Text(
            "o automático segue o papel de parede e só vale com o fundo em papel de parede. A luminosidade é fixa, então a leitura é a mesma em todos.",
            style = VoidType.Small.copy(fontSize = 12.sp, lineHeight = 17.sp),
            color = VoidColors.TextDim,
            modifier = Modifier.padding(top = 4.dp, bottom = 6.dp),
        )
        ValueRow(if (parallax) "paralaxe: ligada" else "paralaxe: desligada", onParallax)
        Text(
            "com o fundo em papel de parede, a imagem se move de leve conforme você inclina o celular. Só funciona com a home visível e usa o acelerômetro apenas nesse momento.",
            style = VoidType.Small.copy(fontSize = 12.sp, lineHeight = 17.sp),
            color = VoidColors.TextDim,
            modifier = Modifier.padding(top = 10.dp),
        )

        Section("abrir apps")
        ValueRow(if (openAnim) "animação a partir do toque: ligada" else "animação a partir do toque: desligada", onOpenAnim)
        ValueRow(if (openHaptic) "vibração leve ao abrir: ligada" else "vibração leve ao abrir: desligada", onOpenHaptic)
        Text(
            "o app se abre crescendo do ponto onde você tocou, e os itens da home e da lista afundam e voltam com um pequeno quique ao toque.",
            style = VoidType.Small.copy(fontSize = 12.sp, lineHeight = 17.sp),
            color = VoidColors.TextDim,
            modifier = Modifier.padding(top = 4.dp),
        )

        Section("modo carregando")
        ValueRow(if (dockOn) "ligado" else "desligado", onDockToggle)
        if (dockOn) ValueRow(if (dockAwake) "tela acesa enquanto aparece: sim" else "tela acesa enquanto aparece: não", onDockAwake)
        Text(
            "com o celular carregando e a home parada por 15 segundos, aparece um relógio grande de cabeceira com a bateria e a música. Um toque sai.",
            style = VoidType.Small.copy(fontSize = 12.sp, lineHeight = 17.sp),
            color = VoidColors.TextDim,
            modifier = Modifier.padding(top = 4.dp),
        )

        Section("linha agora")
        ValueRow(if (predictOn) "ligada" else "desligada", onPredictToggle)
        if (predictOn) ValueRow("apagar o histórico de horários", onPredictClear)
        Text(
            "mostra na home os apps que você costuma abrir neste horário, só pelo seu histórico no aparelho. Nada é enviado e nada roda em segundo plano.",
            style = VoidType.Small.copy(fontSize = 12.sp, lineHeight = 17.sp),
            color = VoidColors.TextDim,
            modifier = Modifier.padding(top = 4.dp),
        )

        Section("cenas")
        ValueRow(if (sceneAuto) "troca sozinha pelo horário" else "troca só quando eu escolho", onSceneAuto)
        var openScene by remember { mutableStateOf<String?>(null) }
        scenes.forEach { sc ->
            val on = sc.id == sceneActive
            ValueRow((if (on) "● " else "○ ") + "${sc.name} · ${sc.start}h às ${sc.end}h") {
                openScene = if (openScene == sc.id) null else sc.id
            }
            if (openScene == sc.id) {
                ValueRow(if (on) "em uso · voltar para sem cena" else "usar agora") { onSceneUse(if (on) null else sc.id) }
                ValueRow("começa às ${sc.start}h") { onSceneEdit(sc.copy(start = (sc.start + 1) % 24)) }
                ValueRow("termina às ${sc.end}h") { onSceneEdit(sc.copy(end = (sc.end + 1) % 24)) }
                ValueRow("modo foco: ${if (sc.focus) "liga" else "não mexe"}") { onSceneEdit(sc.copy(focus = !sc.focus)) }
                ValueRow("home calma: ${if (sc.quiet) "sim" else "não"}") { onSceneEdit(sc.copy(quiet = !sc.quiet)) }
                ValueRow("excluir cena") { onSceneDelete(sc.id) }
            }
        }
        ValueRow("+ nova cena", onSceneAdd)
        Text(
            "cada cena lembra os próprios favoritos: ao sair dela, os favoritos de agora ficam guardados. A home calma esconde status, notificações e foto, como o modo noite.",
            style = VoidType.Small.copy(fontSize = 12.sp, lineHeight = 17.sp),
            color = VoidColors.TextDim,
            modifier = Modifier.padding(top = 4.dp),
        )

        Section("foco automático")
        ValueRow(if (focusAutoOn) "ligado" else "desligado", onFocusAutoToggle)
        if (focusAutoOn) {
            ValueRow("liga às ${focusAutoStart}h", onFocusAutoStart)
            ValueRow("desliga às ${focusAutoEnd}h", onFocusAutoEnd)
        }
        Text(
            "liga e desliga o modo foco nesse horário. Precisa de apps marcados em \"apps do foco\". A troca acontece quando a home está aberta; você pode ligar ou desligar à mão a qualquer momento.",
            style = VoidType.Small.copy(fontSize = 12.sp, lineHeight = 17.sp),
            color = VoidColors.TextDim,
            modifier = Modifier.padding(top = 4.dp),
        )

        Section("modo noite")
        ValueRow(if (nightEnabled) "ligado" else "desligado", onNightToggle)
        if (nightEnabled) {
            ValueRow("começa às ${nightStart}h", onNightStart)
            ValueRow("termina às ${nightEnd}h", onNightEnd)
        }
        Text(
            "no horário escolhido a home fica mais escura e mais calma: some o bloco de status e notificações e a foto do dia. Toque nos horários para mudar de hora em hora.",
            style = VoidType.Small.copy(fontSize = 12.sp, lineHeight = 17.sp),
            color = VoidColors.TextDim,
            modifier = Modifier.padding(top = 4.dp),
        )

        Section("memórias do dia")
        ValueRow(memoryMode.label, onMemoryMode)
        Text(
            "mostra, ao lado da hora, uma foto da galeria (menos capturas de tela) tirada por volta de hoje em anos ou meses atrás. As fotos não saem do aparelho.",
            style = VoidType.Small.copy(fontSize = 12.sp, lineHeight = 17.sp),
            color = VoidColors.TextDim,
            modifier = Modifier.padding(top = 4.dp),
        )
        if (hiddenPhotos > 0) ValueRow("$hiddenPhotos foto(s) oculta(s) · restaurar", onRestorePhotos)

        Section("celular")
        ValueRow("otimizar", onOpenOptimize)
        ValueRow("relatório de uso", onOpenUsage)
        ValueRow("permissões", onOpenPermissions)
        ValueRow("trocar tela inicial padrão", onOpenHomeSettings)
        Text(
            "se o VoidTouch travar várias vezes seguidas, ele abre um modo seguro sozinho. Aqui você também pode voltar para outra tela inicial a qualquer momento.",
            style = VoidType.Small.copy(fontSize = 12.sp, lineHeight = 17.sp),
            color = VoidColors.TextDim,
            modifier = Modifier.padding(top = 4.dp),
        )

        Section("notificações e música")
        ValueRow(if (notificationsEnabled) "acesso ligado" else "acesso desligado", onOpenNotificationAccess)
        ValueRow("liberar configurações restritas", onOpenAppInfo)
        Text(
            "se o acesso aparecer bloqueado: info do app > ⋮ > permitir configurações restritas",
            style = VoidType.Small,
            color = VoidColors.TextDim,
            modifier = Modifier.padding(top = 4.dp),
        )

        Section("backup dos ajustes")
        ValueRow("salvar backup em arquivo", onBackupSave)
        ValueRow("restaurar backup de arquivo", onBackupRestore)
        Text(
            "guarda favoritos, pastas, apps do foco, gestos e ajustes. Útil antes de trocar de celular.",
            style = VoidType.Small.copy(fontSize = 12.sp, lineHeight = 17.sp),
            color = VoidColors.TextDim,
            modifier = Modifier.padding(top = 4.dp),
        )

        Section("sobre")
        Text("VoidTouch $versionName", style = VoidType.App, color = VoidColors.TextDim)
        ValueRow("novidades", onOpenChangelog)
        ValueRow("dicas de uso", onOpenTips)
        if (openTimeMs > 0L) {
            Text(
                "última abertura: $openTimeMs ms",
                style = VoidType.Small.copy(fontSize = 12.sp, lineHeight = 17.sp),
                color = VoidColors.TextDim,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
        UpdateRow(versionName)
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

    Text(
        text = status,
        style = VoidType.App,
        color = VoidColors.Text,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !busy) {
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
                            is UpdateCheck.Failed -> status = "não deu: ${r.reason}. toque para tentar"
                        }
                        busy = false
                    }
                }
            }
            .padding(vertical = 12.dp),
    )
    // Barra enquanto algo acontece: indeterminada ao verificar, com avanço ao baixar.
    if (busy) LoadingLine(progress = progress)
}

@Composable
private fun Section(title: String) {
    Spacer(Modifier.height(28.dp))
    Text(title, style = VoidType.Caption, color = VoidColors.TextDim)
    Spacer(Modifier.height(6.dp))
}

@Composable
private fun <T> Options(items: List<T>, selected: T, label: (T) -> String, onSelect: (T) -> Unit) {
    items.forEach { item ->
        val on = item == selected
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onSelect(item) }
                .padding(vertical = 12.dp),
        ) {
            Text(
                text = (if (on) "● " else "○ ") + label(item),
                style = VoidType.App,
                color = if (on) VoidColors.Text else VoidColors.TextDim,
            )
        }
    }
}

@Composable
private fun ValueRow(value: String, onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 12.dp),
    ) {
        Text(value, style = VoidType.App, color = VoidColors.Text, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
        Spacer(Modifier.width(12.dp))
        Text("›", style = VoidType.App, color = VoidColors.TextDim)
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
                    modifier = Modifier.fillMaxWidth().clickable { onPick(null) }.padding(horizontal = 24.dp, vertical = 14.dp),
                )
            }
            items(actions, key = { "action:${it.id}" }) { action ->
                Text(
                    "◆ ${action.label}",
                    style = VoidType.App,
                    color = VoidColors.Text,
                    modifier = Modifier.fillMaxWidth().clickable { onPick(GestureTarget.encode(action)) }.padding(horizontal = 24.dp, vertical = 14.dp),
                )
            }
            items(results, key = { it.key }) { app ->
                Text(
                    app.label,
                    style = VoidType.App,
                    color = VoidColors.Text,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth().clickable { onPick(app.key) }.padding(horizontal = 24.dp, vertical = 14.dp),
                )
            }
        }
    }
}
