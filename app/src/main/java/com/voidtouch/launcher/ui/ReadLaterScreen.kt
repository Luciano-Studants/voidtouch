package com.voidtouch.launcher.ui

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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.voidtouch.launcher.data.LaterItem
import com.voidtouch.launcher.ui.theme.VoidColors
import com.voidtouch.launcher.ui.theme.VoidType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val dayFormat = SimpleDateFormat("dd/MM HH:mm", Locale.forLanguageTag("pt-BR"))

/**
 * Notificações guardadas para ler depois. Ficam só no aparelho (30 no máximo) e fora do backup.
 * Deslizar para baixo, com a tela no topo, volta para a home.
 */
@Composable
fun ReadLaterScreen(
    items: List<LaterItem>,
    onOpenApp: (LaterItem) -> Unit,
    onRemove: (LaterItem) -> Unit,
    onClose: () -> Unit,
) {
    val scroll = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .onVerticalSwipe(up = false, threshold = 96.dp, enabled = { !scroll.canScrollBackward }, onSwipe = onClose)
            .verticalScroll(scroll)
            .padding(horizontal = 24.dp),
    ) {
        Column(modifier = Modifier.padding(top = 28.dp, bottom = 12.dp)) {
            Text("ler depois", style = VoidType.App, color = VoidColors.Text)
            Text("deslize para baixo para voltar", style = VoidType.Caption, color = VoidColors.TextDim)
        }
        if (items.isEmpty()) {
            Text(
                "nada guardado. Segure uma notificação na home e escolha \"ler depois\".",
                style = VoidType.Small,
                color = VoidColors.TextDim,
            )
        }
        items.forEach { item ->
            Column(modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp)) {
                Text(
                    "${item.app} · ${dayFormat.format(Date(item.savedAt))}",
                    style = VoidType.Caption,
                    color = VoidColors.TextDim,
                )
                Text(item.title, style = VoidType.App, color = VoidColors.Text)
                if (item.text.isNotBlank()) Text(item.text, style = VoidType.Small, color = VoidColors.TextDim)
                Spacer(Modifier.height(4.dp))
                Row {
                    Text(
                        "abrir app",
                        style = VoidType.Caption,
                        color = VoidColors.Text,
                        modifier = Modifier.clickable(role = androidx.compose.ui.semantics.Role.Button) { onOpenApp(item) }.heightIn(min = 44.dp).padding(vertical = 6.dp),
                    )
                    Spacer(Modifier.width(16.dp))
                    Text(
                        "remover",
                        style = VoidType.Caption,
                        color = VoidColors.Text,
                        modifier = Modifier.clickable(role = androidx.compose.ui.semantics.Role.Button) { onRemove(item) }.heightIn(min = 44.dp).padding(vertical = 6.dp),
                    )
                }
            }
        }
        Spacer(Modifier.height(32.dp))
    }
}
