package com.voidtouch.launcher.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.text.format.Formatter
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
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
import com.voidtouch.launcher.data.AppEntry
import com.voidtouch.launcher.data.AppSize
import com.voidtouch.launcher.data.HealthReader
import com.voidtouch.launcher.data.ErrorLog
import com.voidtouch.launcher.data.HealthSnapshot
import com.voidtouch.launcher.data.HeavyApps
import com.voidtouch.launcher.data.HeavyAppsReader
import com.voidtouch.launcher.data.Perf
import com.voidtouch.launcher.ui.theme.VoidColors
import com.voidtouch.launcher.ui.theme.VoidType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

private val PtBr: Locale = Locale.forLanguageTag("pt-BR")

/**
 * Saúde do aparelho: RAM, armazenamento e bateria com barras finas, e os apps que mais ocupam
 * espaço. Nada roda em segundo plano: os dados são lidos só enquanto esta tela está aberta.
 * Deslizar para baixo, com a tela no topo, volta para a home.
 */
@Composable
fun HealthScreen(
    apps: List<AppEntry>,
    nowMillis: Long,
    refreshKey: Int,
    onClose: () -> Unit,
) {
    val context = LocalContext.current
    val appContext = context.applicationContext
    val scroll = rememberScrollState()

    // Relido a cada minuto (nowMillis muda no ACTION_TIME_TICK) enquanto a tela está visível.
    val snapshot by produceState<HealthSnapshot?>(null, nowMillis) {
        value = withContext(Dispatchers.Default) { HealthReader.read(appContext) }
    }
    // Relido quando a lista de apps é recarregada, por exemplo ao voltar da tela de limpar cache.
    val heavy by produceState<HeavyApps>(HeavyApps.Loading, apps, refreshKey) {
        value = withContext(Dispatchers.Default) { HeavyAppsReader.read(appContext, apps) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .onVerticalSwipe(
                up = false,
                threshold = 96.dp,
                enabled = { !scroll.canScrollBackward },
                onSwipe = onClose,
            )
            .verticalScroll(scroll)
            .padding(horizontal = 24.dp),
    ) {
        Column(modifier = Modifier.padding(top = 28.dp, bottom = 8.dp)) {
            Text(text = "saúde do aparelho", style = VoidType.App, color = VoidColors.Text)
            Text(
                text = "deslize para baixo para voltar",
                style = VoidType.Caption,
                color = VoidColors.TextDim,
            )
        }

        snapshot?.let { Meters(context, it) }

        Spacer(Modifier.height(20.dp))
        Text(text = "apps mais pesados", style = VoidType.App, color = VoidColors.Text)
        Spacer(Modifier.height(4.dp))

        when (val state = heavy) {
            HeavyApps.Loading -> {
                LoadingLine()
                Spacer(Modifier.height(8.dp))
                Hint("medindo os apps…")
            }
            HeavyApps.NeedsPermission -> {
                Hint(
                    "para medir o tamanho dos apps, o Android pede a permissão \"acesso ao uso\". " +
                        "O VoidTouch só lê os tamanhos e não envia nada.",
                )
                Spacer(Modifier.height(14.dp))
                TextPill(text = "permitir acesso", onClick = { openUsageAccess(context) })
            }
            is HeavyApps.Ready -> HeavyList(context, state)
        }

        Spacer(Modifier.height(28.dp))
        PerfSection(refreshKey = refreshKey, nowMillis = nowMillis)

        Spacer(Modifier.height(28.dp))
        val handled = ErrorLog.recent()
        Text(text = "erros tratados", style = VoidType.App, color = VoidColors.Text)
        Spacer(Modifier.height(4.dp))
        Hint(
            if (handled.isEmpty()) "nenhum nesta sessão. Quando algo falha sem fechar o app, aparece aqui."
            else "o app se recuperou sozinho destes (os mais novos primeiro):",
        )
        handled.forEach { line -> Hint(line) }

        Spacer(Modifier.height(28.dp))
        TextPill(text = "abrir armazenamento do sistema", onClick = { openStorageSettings(context) })
        Spacer(Modifier.height(40.dp))
    }
}

@Composable
private fun Meters(context: Context, s: HealthSnapshot) {
    val ramUsed = s.ramTotalBytes - s.ramFreeBytes
    Meter(
        label = "ram",
        value = "${size(context, s.ramFreeBytes)} livre de ${size(context, s.ramTotalBytes)}",
        fraction = ratio(ramUsed, s.ramTotalBytes),
        note = if (s.lowMemory) "memória baixa: o sistema pode fechar apps em segundo plano" else null,
    )

    val storageUsed = s.storageTotalBytes - s.storageFreeBytes
    Meter(
        label = "armazenamento",
        value = "${size(context, s.storageFreeBytes)} livre de ${size(context, s.storageTotalBytes)}",
        fraction = ratio(storageUsed, s.storageTotalBytes),
    )

    if (s.batteryPercent >= 0) {
        val details = listOfNotNull(
            s.temperatureC?.let { String.format(PtBr, "%.1f °C", it) },
            s.batteryHealth?.let { "saúde $it" },
        ).joinToString(" · ").ifEmpty { null }
        Meter(
            label = "bateria",
            value = "${s.batteryPercent}%" + if (s.charging) " · carregando" else "",
            fraction = s.batteryPercent / 100f,
            note = details,
        )
    }
}

/** Rótulo e valor na mesma linha, uma barra fina embaixo e uma nota opcional. */
@Composable
private fun Meter(label: String, value: String, fraction: Float, note: String? = null) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(text = label, style = VoidType.Caption, color = VoidColors.TextDim)
            Text(text = value, style = VoidType.Caption, color = VoidColors.Text)
        }
        Spacer(Modifier.height(8.dp))
        Box(modifier = Modifier.fillMaxWidth().height(2.dp).background(VoidColors.Line)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction.coerceIn(0f, 1f))
                    .height(2.dp)
                    .background(VoidColors.Text),
            )
        }
        if (note != null) {
            Spacer(Modifier.height(6.dp))
            Text(text = note, style = VoidType.Caption, color = VoidColors.TextDim)
        }
    }
}

@Composable
private fun HeavyList(context: Context, state: HeavyApps.Ready) {
    if (state.top.isEmpty()) {
        Hint("nenhum app encontrado")
        return
    }
    Hint("cache total dos apps: ${size(context, state.cacheTotalBytes)}. Toque num app, depois em Armazenamento e Limpar cache.")
    Spacer(Modifier.height(8.dp))
    state.top.forEach { item ->
        HeavyRow(
            item = item,
            total = size(context, item.totalBytes),
            cache = size(context, item.cacheBytes),
            onClick = { openAppInfo(context, item.app.packageName) },
        )
    }
}

@Composable
private fun HeavyRow(item: AppSize, total: String, cache: String, onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(role = androidx.compose.ui.semantics.Role.Button, onClick = onClick)
            .padding(vertical = 10.dp),
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.app.label,
                style = VoidType.App,
                color = VoidColors.Text,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(text = "cache $cache", style = VoidType.Caption, color = VoidColors.TextDim)
        }
        Text(text = total, style = VoidType.Caption, color = VoidColors.Text)
    }
}

@Composable
private fun Hint(text: String) {
    Text(text = text, style = VoidType.Caption, color = VoidColors.TextDim)
}

/** Pílula fina de texto, no mesmo estilo da busca da home. */
@Composable
private fun TextPill(text: String, onClick: () -> Unit) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .heightIn(min = 48.dp)
            .clip(CircleShape)
            .border(width = 1.dp, color = VoidColors.TextDim, shape = CircleShape)
            .clickable(role = androidx.compose.ui.semantics.Role.Button, onClick = onClick)
            .padding(horizontal = 24.dp),
    ) {
        Text(text = text, style = VoidType.Caption, color = VoidColors.Text)
    }
}

private fun size(context: Context, bytes: Long): String =
    Formatter.formatShortFileSize(context, bytes.coerceAtLeast(0L))

private fun ratio(part: Long, whole: Long): Float =
    if (whole <= 0L) 0f else (part.toFloat() / whole.toFloat()).coerceIn(0f, 1f)

private fun start(context: Context, intent: Intent) {
    runCatching { context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
}

/** Tela de informações do app, onde fica Armazenamento > Limpar cache. */
private fun openAppInfo(context: Context, packageName: String) {
    start(
        context,
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", packageName, null)),
    )
}

/** Tela de acesso ao uso; no Android 10+ já abre direto na linha do VoidTouch. */
private fun openUsageAccess(context: Context) {
    val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        intent.data = Uri.fromParts("package", context.packageName, null)
    }
    start(context, intent)
}

private fun openStorageSettings(context: Context) {
    start(context, Intent(Settings.ACTION_INTERNAL_STORAGE_SETTINGS))
}

/** Tempos medidos nesta sessão do app, em milissegundos. Só mostra o que já foi medido. */
@Composable
private fun PerfSection(refreshKey: Int, nowMillis: Long) {
    // Lê a cada atualização da tela; é só um acesso à memória.
    @Suppress("UNUSED_VARIABLE") val tick = refreshKey + nowMillis
    Text(text = "desempenho", style = VoidType.App, color = VoidColors.Text)
    Spacer(Modifier.height(4.dp))
    var any = false
    Perf.ALL.forEach { name ->
        val s = Perf.stats.summary(name) ?: return@forEach
        any = true
        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
            Text(name, style = VoidType.Caption, color = VoidColors.TextDim, modifier = Modifier.weight(1f))
            Text(
                if (s.count > 1) "${s.last} ms · média ${s.average} · pior ${s.worst}" else "${s.last} ms",
                style = VoidType.Caption,
                color = VoidColors.Text,
            )
        }
    }
    if (!any) Hint("ainda sem medidas: abra a lista de apps e use a busca")
    Hint("medido nesta sessão, sem rodar nada em segundo plano")
}
