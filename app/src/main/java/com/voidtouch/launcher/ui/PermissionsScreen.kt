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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.voidtouch.launcher.data.Agenda
import com.voidtouch.launcher.data.HeavyAppsReader
import com.voidtouch.launcher.data.MemoryReader
import com.voidtouch.launcher.data.PermissionRules
import com.voidtouch.launcher.data.Updater
import com.voidtouch.launcher.ui.theme.VoidColors
import com.voidtouch.launcher.ui.theme.VoidType

private class PermissionItem(
    val name: String,
    val why: String,
    val granted: Boolean,
    val onClick: () -> Unit,
)

/**
 * Painel com o estado de cada permissão, para que serve e um toque que leva à configuração certa.
 * O estado é lido ao abrir a tela e ao voltar das configurações (refreshKey).
 */
@Composable
fun PermissionsScreen(
    refreshKey: Int,
    notificationAccess: Boolean,
    onOpenNotificationAccess: () -> Unit,
    onRequestPhotos: () -> Unit,
    onRequestCalendar: () -> Unit,
    onOpenAppInfo: () -> Unit,
    onClose: () -> Unit,
) {
    val context = LocalContext.current
    val scroll = rememberScrollState()

    val photos = remember(refreshKey) { MemoryReader.hasAccess(context) }
    val calendar = remember(refreshKey) { Agenda.hasAccess(context) }
    val usage = remember(refreshKey) { HeavyAppsReader.hasUsageAccess(context) }
    val install = remember(refreshKey) { Updater.canInstall(context) }

    val items = listOf(
        PermissionItem(
            "acesso às notificações",
            "mostra as notificações na home e controla o player de música",
            notificationAccess,
            onOpenNotificationAccess,
        ),
        PermissionItem(
            "fotos",
            "memórias do dia: só uma miniatura é lida, nada sai do aparelho",
            photos,
            { if (photos) onOpenAppInfo() else onRequestPhotos() },
        ),
        PermissionItem(
            "calendário",
            "agenda no carrossel: só leitura dos próximos compromissos",
            calendar,
            { if (calendar) onOpenAppInfo() else onRequestCalendar() },
        ),
        PermissionItem(
            "acesso ao uso",
            "mede o tamanho e o cache dos apps na tela de saúde e otimizar",
            usage,
            { runCatching { context.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) } },
        ),
        PermissionItem(
            "instalar apps desconhecidos",
            "instala a atualização baixada do GitHub",
            install,
            { Updater.openInstallPermission(context) },
        ),
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .onVerticalSwipe(up = false, threshold = 96.dp, enabled = { !scroll.canScrollBackward }, onSwipe = onClose)
            .verticalScroll(scroll)
            .padding(horizontal = 24.dp),
    ) {
        Column(modifier = Modifier.padding(top = 28.dp, bottom = 12.dp)) {
            Text("permissões", style = VoidType.App, color = VoidColors.Text)
            Text(
                "${PermissionRules.summary(items.count { it.granted }, items.size)} · deslize para baixo para voltar",
                style = VoidType.Caption,
                color = VoidColors.TextDim,
            )
        }
        items.forEach { item ->
            Row(
                verticalAlignment = Alignment.Top,
                modifier = Modifier.fillMaxWidth().clickable(onClick = item.onClick).padding(vertical = 12.dp),
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(item.name, style = VoidType.App, color = VoidColors.Text)
                    Spacer(Modifier.height(2.dp))
                    Text(item.why, style = VoidType.Caption, color = VoidColors.TextDim)
                }
                Spacer(Modifier.width(12.dp))
                Text(
                    if (item.granted) "ligada" else "desligada",
                    style = VoidType.Caption,
                    color = if (item.granted) VoidColors.Text else VoidColors.TextDim,
                )
            }
        }
        Spacer(Modifier.height(12.dp))
        Text(
            "o VoidTouch só pede uma permissão quando você liga a função que precisa dela. Toque em um item para ligar ou ajustar.",
            style = VoidType.Caption,
            color = VoidColors.TextDim,
        )
        Spacer(Modifier.height(32.dp))
    }
}
