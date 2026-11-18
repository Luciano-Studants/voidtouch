package com.voidtouch.launcher.ui

import android.content.Context
import android.os.Build
import android.provider.Settings
import android.net.Uri
import android.content.Intent
import android.text.format.Formatter
import android.widget.Toast
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
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
fun OptimizeScreen(apps: List<AppEntry>, onClose: () -> Unit) {
    val context = LocalContext.current
    val appContext = context.applicationContext
    val scroll = rememberScrollState()
    val data by produceState<OptimizeData?>(null, apps) {
        value = withContext(Dispatchers.Default) { Optimizer.read(appContext, apps) }
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
                modifier = Modifier.clickable(onClick = onClose).padding(vertical = 8.dp),
            )
        }
        Text(
            "o Android não deixa um launcher apagar o cache dos outros apps nem fechá-los. Aqui você vê o que pesa e vai direto à tela certa do sistema.",
            style = VoidType.Small.copy(fontSize = 12.sp, lineHeight = 17.sp),
            color = VoidColors.TextDim,
            modifier = Modifier.padding(top = 6.dp),
        )

        when (val d = data) {
            null -> Hint("calculando...")
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
                Spacer(Modifier.height(10.dp))
                Pill("limpar cache de todos") {
                    if (!Optimizer.requestClearAllCache(context)) {
                        Toast.makeText(
                            context,
                            "O sistema não aceita limpar tudo de uma vez aqui. Abrindo o armazenamento.",
                            Toast.LENGTH_LONG,
                        ).show()
                        Optimizer.openStorage(context)
                    }
                }
                Spacer(Modifier.height(10.dp))
                Hint("ou toque num app abaixo > Armazenamento > Limpar cache")
                d.topCache.forEach { item ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { Optimizer.openAppInfo(context, item.app.packageName) }
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
                                .clickable { Optimizer.openAppInfo(context, item.app.packageName) }
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
        Shortcut("cuidados com o dispositivo", "otimização automática da Samsung") { Optimizer.openDeviceCare(context) }
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
    Column(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 12.dp)) {
        Text("$title ›", style = VoidType.App, color = VoidColors.Text)
        Text(note, style = VoidType.Small.copy(fontSize = 12.sp, lineHeight = 17.sp), color = VoidColors.TextDim)
    }
}

@Composable
private fun Pill(text: String, onClick: () -> Unit) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .height(48.dp)
            .clip(CircleShape)
            .border(1.dp, VoidColors.TextDim, CircleShape)
            .clickable(onClick = onClick)
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
