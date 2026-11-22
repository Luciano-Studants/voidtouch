package com.voidtouch.launcher.ui

import android.content.Context
import android.os.Build
import android.provider.Settings
import android.net.Uri
import android.content.Intent
import android.text.format.Formatter
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.animation.AnimatedVisibility
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.voidtouch.launcher.data.AppEntry
import com.voidtouch.launcher.data.OptimizeData
import com.voidtouch.launcher.data.Optimizer
import com.voidtouch.launcher.ui.theme.VoidColors
import com.voidtouch.launcher.ui.theme.VoidType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Otimizar: cache de todos os apps, apps parados e atalhos para os ajustes do sistema que mais
 * pesam no processamento. Nada roda sozinho: só mede com a tela aberta.
 */
@Composable
fun OptimizeScreen(apps: List<AppEntry>, refreshKey: Int, onMessage: (String) -> Unit, onClose: () -> Unit) {
    val context = LocalContext.current
    val appContext = context.applicationContext
    val scroll = rememberScrollState()
    var data by remember { mutableStateOf<OptimizeData?>(null) }
    var measuring by remember { mutableStateOf(true) }
    // Cache total medido antes de pedir a limpeza; depois da volta, a diferença vira o "liberado".
    var before by remember { mutableLongStateOf(-1L) }
    var waitingSystem by remember { mutableStateOf(false) }
    var freed by remember { mutableStateOf<Long?>(null) }
    var noChange by remember { mutableStateOf(false) }
    var skipped by remember { mutableStateOf(emptySet<String>()) }

    // Mede de novo sempre que a tela volta a ser vista (por exemplo, depois de limpar um cache).
    LaunchedEffect(apps, refreshKey) {
        measuring = true
        val result = withContext(Dispatchers.Default) { Optimizer.read(appContext, apps) }
        if (result is OptimizeData.Ready && before >= 0L) {
            if (result.cacheTotalBytes < before) freed = before - result.cacheTotalBytes else noChange = true
            before = -1L
        }
        data = result
        measuring = false
        waitingSystem = false
    }
    LaunchedEffect(freed, noChange) {
        if (freed != null || noChange) {
            delay(5000)
            freed = null
            noChange = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .onVerticalSwipe(up = false, threshold = 96.dp, enabled = { !scroll.canScrollBackward }, onSwipe = onClose)
            .verticalScroll(scroll)
            .padding(horizontal = 24.dp, vertical = 28.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("otimizar", style = VoidType.App, color = VoidColors.Text, modifier = Modifier.weight(1f))
            Text(
                "fechar",
                style = VoidType.Caption,
                color = VoidColors.Text,
                modifier = Modifier.clickable(role = androidx.compose.ui.semantics.Role.Button, onClick = onClose).heightIn(min = 44.dp).padding(vertical = 8.dp),
            )
        }
        Text(
            "o Android não deixa um launcher apagar o cache dos outros apps nem fechá-los. Aqui você vê o que pesa e vai direto à tela certa do sistema.",
            style = VoidType.Small.copy(fontSize = 12.sp, lineHeight = 17.sp),
            color = VoidColors.TextDim,
            modifier = Modifier.padding(top = 6.dp),
        )

        // Feedback do que está acontecendo: medindo, aguardando o sistema ou resultado da limpeza.
        Spacer(Modifier.height(16.dp))
        when {
            measuring -> {
                LoadingLine()
                Spacer(Modifier.height(8.dp))
                Hint(if (waitingSystem) "conferindo o que foi liberado…" else "medindo ${apps.map { it.packageName }.distinct().size} apps…")
            }
            waitingSystem -> {
                LoadingLine()
                Spacer(Modifier.height(8.dp))
                Hint("aguardando o sistema… volte aqui quando terminar")
            }
        }
        AnimatedVisibility(visible = freed != null) {
            ResultCard("✓ liberado ${size(context, freed ?: 0L)} de cache")
        }
        AnimatedVisibility(visible = noChange) {
            ResultCard("nenhum cache foi liberado desde a última medição")
        }

        when (val d = data) {
            null -> Unit
            OptimizeData.NeedsPermission -> {
                Section("medição")
                Hint("para medir cache e uso dos apps o Android pede a permissão \"acesso ao uso\". O VoidTouch só lê tamanhos e datas, nada sai do aparelho.")
                Spacer(Modifier.height(14.dp))
                Pill("permitir acesso") { openUsageAccess(context) }
            }
            is OptimizeData.Ready -> {
                Section("cache dos apps")
                Text(
                    size(context, d.cacheTotalBytes),
                    style = VoidType.Day,
                    color = VoidColors.Text,
                )
                Spacer(Modifier.height(6.dp))
                Hint("o Android não deixa um app comum apagar o cache dos outros sozinho. Há dois caminhos que funcionam:")

                // 1) Limpeza guiada: o app mostra o maior cache e leva direto à tela do app no sistema.
                val next = d.topCache.firstOrNull { it.app.packageName !in skipped && it.cacheBytes >= 1_000_000L }
                Spacer(Modifier.height(14.dp))
                Text("limpeza guiada", style = VoidType.Caption, color = VoidColors.TextDim)
                Spacer(Modifier.height(8.dp))
                if (next == null) {
                    ResultCard("✓ nenhum app com cache relevante")
                } else {
                    Text(
                        "${next.app.label} · ${size(context, next.cacheBytes)}",
                        style = VoidType.App,
                        color = VoidColors.Text,
                    )
                    Spacer(Modifier.height(4.dp))
                    Hint("toque em Armazenamento > Limpar cache e volte: o próximo aparece sozinho")
                    Spacer(Modifier.height(10.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Pill("abrir ${next.app.label}") {
                            before = d.cacheTotalBytes
                            Optimizer.openAppInfo(context, next.app.packageName)
                        }
                        Spacer(Modifier.width(16.dp))
                        Text(
                            "pular",
                            style = VoidType.Caption,
                            color = VoidColors.TextDim,
                            modifier = Modifier
                                .clickable(role = androidx.compose.ui.semantics.Role.Button) { skipped = skipped + next.app.packageName }
                                .padding(12.dp),
                        )
                    }
                }

                // 2) Limpeza de todos de uma vez: só o app de manutenção do próprio fabricante consegue.
                Spacer(Modifier.height(20.dp))
                Text("todos de uma vez", style = VoidType.Caption, color = VoidColors.TextDim)
                Spacer(Modifier.height(8.dp))
                val hasCare = remember { Optimizer.hasDeviceCare(context) }
                Pill(
                    if (waitingSystem) "aguardando o sistema…"
                    else if (hasCare) "abrir a limpeza da Samsung"
                    else "abrir o armazenamento do sistema",
                ) {
                    if (waitingSystem) return@Pill
                    before = d.cacheTotalBytes
                    waitingSystem = true
                    if (hasCare) {
                        onMessage("abrindo Cuidados com o dispositivo")
                        Optimizer.openDeviceCare(context)
                    } else {
                        onMessage("abrindo o armazenamento")
                        Optimizer.openStorage(context)
                    }
                }
                Spacer(Modifier.height(6.dp))
                Hint(
                    if (hasCare) "em Cuidados com o dispositivo > Armazenamento, use o botão de limpar. Ao voltar, o VoidTouch confere quanto foi liberado."
                    else "no armazenamento do sistema, procure \"dados em cache\" ou \"liberar espaço\" (o nome muda por marca). Ao voltar, o VoidTouch confere quanto foi liberado.",
                )
                Spacer(Modifier.height(18.dp))
                Text("maiores caches", style = VoidType.Caption, color = VoidColors.TextDim)
                Spacer(Modifier.height(4.dp))
                d.topCache.forEach { item ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(role = androidx.compose.ui.semantics.Role.Button) { Optimizer.openAppInfo(context, item.app.packageName) }
                            .padding(vertical = 12.dp),
                    ) {
                        Text(item.app.label, style = VoidType.App, color = VoidColors.Text, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                        Spacer(Modifier.width(12.dp))
                        Text(size(context, item.cacheBytes), style = VoidType.Caption, color = VoidColors.Text)
                    }
                }

                Section("apps parados há 30 dias ou mais")
                if (d.idle.isEmpty()) {
                    Hint("nenhum app instalado por você está parado")
                } else {
                    Hint("toque para abrir o app no sistema e desativar ou desinstalar")
                    d.idle.forEach { item ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(role = androidx.compose.ui.semantics.Role.Button) { Optimizer.openAppInfo(context, item.app.packageName) }
                                .padding(vertical = 12.dp),
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(item.app.label, style = VoidType.App, color = VoidColors.Text, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text(
                                    if (item.daysIdle >= 90) "sem uso há mais de 90 dias" else "sem uso há ${item.daysIdle} dias",
                                    style = VoidType.Caption,
                                    color = VoidColors.TextDim,
                                )
                            }
                            Text(size(context, item.totalBytes), style = VoidType.Caption, color = VoidColors.Text)
                        }
                    }
                }
            }
        }

        Section("o que mais pesa no processamento")
        val careAvailable = remember { Optimizer.hasDeviceCare(context) }
        Shortcut(
            if (careAvailable) "cuidados com o dispositivo" else "uso da bateria",
            if (careAvailable) "otimização automática da Samsung" else "veja o que mais gasta bateria e processamento",
        ) { Optimizer.openDeviceCare(context) }
        Shortcut("bateria: apps em segundo plano", "restrinja os que acordam o celular à toa") { Optimizer.openBatteryOptimization(context) }
        Shortcut(
            "animações mais rápidas",
            "opções do desenvolvedor > escala de animação 0,5x (ou desativada) deixa tudo mais ágil",
        ) { Optimizer.openDeveloperOptions(context) }
        Shortcut("armazenamento do sistema", "arquivos grandes e lixeiras") { Optimizer.openStorage(context) }
        Spacer(Modifier.height(32.dp))
    }
}

@Composable
private fun ResultCard(text: String) {
    Box(
        modifier = Modifier
            .padding(top = 12.dp)
            .fillMaxWidth()
            .border(VoidColors.Stroke, VoidColors.TextDim, androidx.compose.foundation.shape.RoundedCornerShape(16.dp))
            .padding(16.dp),
    ) {
        Text(text, style = VoidType.App, color = VoidColors.Text)
    }
}

@Composable
private fun Section(title: String) {
    Spacer(Modifier.height(28.dp))
    Text(title, style = VoidType.Caption, color = VoidColors.TextDim)
    Spacer(Modifier.height(8.dp))
}

@Composable
private fun Hint(text: String) {
    Text(text, style = VoidType.Small.copy(fontSize = 12.sp, lineHeight = 17.sp), color = VoidColors.TextDim)
}

@Composable
private fun Shortcut(title: String, note: String, onClick: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().clickable(role = androidx.compose.ui.semantics.Role.Button, onClick = onClick).padding(vertical = 12.dp)) {
        Text("$title ›", style = VoidType.App, color = VoidColors.Text)
        Text(note, style = VoidType.Small.copy(fontSize = 12.sp, lineHeight = 17.sp), color = VoidColors.TextDim)
    }
}

@Composable
private fun Pill(text: String, onClick: () -> Unit) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .heightIn(min = 48.dp)
            .clip(CircleShape)
            .border(VoidColors.Stroke, VoidColors.TextDim, CircleShape)
            .clickable(role = androidx.compose.ui.semantics.Role.Button, onClick = onClick)
            .padding(horizontal = 24.dp),
    ) {
        Text(text, style = VoidType.Caption, color = VoidColors.Text)
    }
}

private fun size(context: Context, bytes: Long): String =
    Formatter.formatShortFileSize(context, bytes.coerceAtLeast(0L))

private fun openUsageAccess(context: Context) {
    val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        intent.data = Uri.fromParts("package", context.packageName, null)
    }
    runCatching { context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
}
