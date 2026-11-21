package com.voidtouch.launcher.ui

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.voidtouch.launcher.data.HeavyAppsReader
import com.voidtouch.launcher.data.UsageReader
import com.voidtouch.launcher.data.UsageReport
import com.voidtouch.launcher.data.UsageRules
import com.voidtouch.launcher.ui.theme.VoidColors
import com.voidtouch.launcher.ui.theme.VoidType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Relatório de tempo de tela por app, hoje e nos últimos 7 dias. Usa o acesso ao uso do Android,
 * lê só com esta tela aberta e não conta o tempo na tela inicial. Deslizar para baixo volta.
 */
@Composable
fun UsageScreen(refreshKey: Int, onClose: () -> Unit) {
    val context = LocalContext.current
    val appContext = context.applicationContext
    val scroll = rememberScrollState()
    var week by remember { mutableStateOf(false) }
    val access = remember(refreshKey) { HeavyAppsReader.hasUsageAccess(context) }
    val loaded = produceState<Pair<Boolean, UsageReport?>>(false to null, refreshKey, access) {
        value = false to null
        val report = if (access) withContext(Dispatchers.Default) { UsageReader.read(appContext) } else null
        value = true to report
    }
    val (done, report) = loaded.value

    Column(
        modifier = Modifier
            .fillMaxSize()
            .onVerticalSwipe(up = false, threshold = 96.dp, enabled = { !scroll.canScrollBackward }, onSwipe = onClose)
            .verticalScroll(scroll)
            .padding(horizontal = 24.dp),
    ) {
        Column(modifier = Modifier.padding(top = 28.dp, bottom = 12.dp)) {
            Text("relatório de uso", style = VoidType.App, color = VoidColors.Text)
            Text("tempo de tela por app · deslize para baixo para voltar", style = VoidType.Caption, color = VoidColors.TextDim)
        }

        if (!access) {
            Text(
                "para medir o tempo de tela, libere o acesso ao uso para o VoidTouch. Ele só lê quando esta tela está aberta e nada sai do aparelho.",
                style = VoidType.Small,
                color = VoidColors.TextDim,
            )
            Text(
                "liberar acesso ao uso ›",
                style = VoidType.App,
                color = VoidColors.Text,
                modifier = Modifier
                    .clickable(role = androidx.compose.ui.semantics.Role.Button) { runCatching { context.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) } }
                    .padding(vertical = 14.dp),
            )
            return@Column
        }

        Row {
            Text(
                "hoje",
                style = VoidType.App,
                color = if (!week) VoidColors.Text else VoidColors.TextDim,
                modifier = Modifier.clickable(role = androidx.compose.ui.semantics.Role.Button) { week = false }.heightIn(min = 44.dp).padding(vertical = 8.dp),
            )
            Spacer(Modifier.width(20.dp))
            Text(
                "7 dias",
                style = VoidType.App,
                color = if (week) VoidColors.Text else VoidColors.TextDim,
                modifier = Modifier.clickable(role = androidx.compose.ui.semantics.Role.Button) { week = true }.heightIn(min = 44.dp).padding(vertical = 8.dp),
            )
        }

        if (!done) {
            LoadingLine(modifier = Modifier.padding(top = 12.dp))
            Text("medindo…", style = VoidType.Caption, color = VoidColors.TextDim, modifier = Modifier.padding(top = 6.dp))
        } else if (report == null) {
            Text("não foi possível ler o uso agora", style = VoidType.Small, color = VoidColors.TextDim)
        } else if (!week) {
            Total(UsageRules.duration(report.todayTotal), "de tela hoje")
            AppBars(report.todayApps, report)
        } else {
            val average = UsageRules.dailyAverage(report.days.map { it.totalMs })
            Total(UsageRules.duration(report.weekTotal), "em 7 dias · média ${UsageRules.duration(average)} por dia")
            val max = report.days.maxOfOrNull { it.totalMs } ?: 0L
            report.days.forEach { d ->
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp)) {
                    Text(UsageRules.weekday(d.dayOfWeek), style = VoidType.Small, color = VoidColors.TextDim, modifier = Modifier.width(44.dp))
                    Text(UsageRules.bar(UsageRules.fraction(d.totalMs, max)), style = VoidType.Small, color = VoidColors.Text, modifier = Modifier.weight(1f))
                    Text(UsageRules.duration(d.totalMs), style = VoidType.Caption, color = VoidColors.TextDim)
                }
            }
            Spacer(Modifier.height(18.dp))
            Text("mais usados na semana", style = VoidType.Caption, color = VoidColors.TextDim)
            AppBars(report.weekApps, report)
        }

        Spacer(Modifier.height(20.dp))
        Text(
            "não conta o tempo na tela inicial. O Android guarda poucos dias de histórico, então o começo do uso pode estar incompleto.",
            style = VoidType.Caption,
            color = VoidColors.TextDim,
        )
        Spacer(Modifier.height(32.dp))
    }
}

@Composable
private fun Total(value: String, caption: String) {
    Column(modifier = Modifier.padding(top = 6.dp, bottom = 14.dp)) {
        Text(value, style = VoidType.App, color = VoidColors.Text)
        Text(caption, style = VoidType.Caption, color = VoidColors.TextDim)
    }
}

@Composable
private fun AppBars(apps: List<Pair<String, Long>>, report: UsageReport) {
    if (apps.isEmpty()) {
        Text("sem uso medido ainda", style = VoidType.Small, color = VoidColors.TextDim, modifier = Modifier.padding(top = 8.dp))
        return
    }
    val max = apps.first().second
    apps.forEach { (pkg, ms) ->
        Column(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
            Row {
                Text(
                    report.labels[pkg] ?: pkg,
                    style = VoidType.App,
                    color = VoidColors.Text,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                Text(UsageRules.duration(ms), style = VoidType.Caption, color = VoidColors.TextDim)
            }
            Text(UsageRules.bar(UsageRules.fraction(ms, max), 20), style = VoidType.Caption, color = VoidColors.TextDim)
        }
    }
}
