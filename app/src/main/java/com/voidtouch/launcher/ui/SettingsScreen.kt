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
import com.voidtouch.launcher.data.AppEntry
import com.voidtouch.launcher.data.ClockStyle
import com.voidtouch.launcher.data.Search
import com.voidtouch.launcher.data.SwipeSide
import com.voidtouch.launcher.ui.theme.Backdrop
import com.voidtouch.launcher.ui.theme.VoidColors
import com.voidtouch.launcher.ui.theme.VoidType

/**
 * Ajustes: fundo, formato da hora e o app de cada deslize horizontal da home.
 * Tudo se escolhe com um toque; deslizar para baixo ou voltar fecha.
 */
@Composable
fun SettingsScreen(
    apps: List<AppEntry>,
    backdrop: Backdrop,
    clockStyle: ClockStyle,
    swipeLeftKey: String?,
    swipeRightKey: String?,
    versionName: String,
    notificationsEnabled: Boolean,
    onOpenNotificationAccess: () -> Unit,
    onOpenAppInfo: () -> Unit,
    onOpenChangelog: () -> Unit,
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
            title = if (side == SwipeSide.Left) "app do deslize para a esquerda" else "app do deslize para a direita",
            apps = apps,
            onPick = {
                onSwipeApp(side, it?.key)
                picking = null
            },
        )
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .onVerticalSwipe(up = false, threshold = 96.dp, onSwipe = onClose)
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

        Section("deslizar para a esquerda")
        ValueRow(byKey[swipeLeftKey]?.label ?: "nenhum") { picking = SwipeSide.Left }

        Section("deslizar para a direita")
        ValueRow(byKey[swipeRightKey]?.label ?: "nenhum") { picking = SwipeSide.Right }

        Text(
            "os deslizes valem na home, abaixo do carrossel",
            style = VoidType.Small,
            color = VoidColors.TextDim,
            modifier = Modifier.padding(top = 10.dp),
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

        Section("sobre")
        Text("VoidTouch $versionName", style = VoidType.App, color = VoidColors.TextDim)
        ValueRow("novidades", onOpenChangelog)
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

    fun download(info: UpdateInfo) {
        if (!Updater.canInstall(context)) {
            status = "libere \"instalar apps desconhecidos\" e toque de novo"
            Updater.openInstallPermission(context)
            return
        }
        busy = true
        scope.launch {
            val apk = Updater.download(context, info) { status = "baixando… $it%" }
            busy = false
            if (apk == null) {
                status = "falha ao baixar, toque para tentar de novo"
            } else {
                status = "pronto: confirme a instalação"
                Updater.install(context, apk)
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

/** Escolha de app com busca; "nenhum" limpa o gesto. */
@Composable
private fun AppPicker(title: String, apps: List<AppEntry>, onPick: (AppEntry?) -> Unit) {
    var query by remember { mutableStateOf("") }
    val results = remember(apps, query) { Search.filter(apps, query) }
    Column(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 28.dp, bottom = 8.dp)) {
            Text(title, style = VoidType.Caption, color = VoidColors.TextDim)
            Spacer(Modifier.height(10.dp))
            SearchField(query = query, onQueryChange = { query = it }, requestFocus = false, onGo = {
                results.firstOrNull()?.let(onPick)
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
            items(results, key = { it.key }) { app ->
                Text(
                    app.label,
                    style = VoidType.App,
                    color = VoidColors.Text,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth().clickable { onPick(app) }.padding(horizontal = 24.dp, vertical = 14.dp),
                )
            }
        }
    }
}
