package com.voidtouch.launcher.ui

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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.voidtouch.launcher.ui.theme.VoidColors
import com.voidtouch.launcher.ui.theme.VoidType

/** Uma linha da tela de ordem: a chave do favorito e o nome mostrado. */
class OrderRow(val key: String, val label: String)

/** Muda a ordem dos favoritos da home com setas. A mudança vale na hora. */
@Composable
fun FavoritesOrderScreen(
    rows: List<OrderRow>,
    onMove: (key: String, delta: Int) -> Unit,
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
            Text("ordem dos favoritos", style = VoidType.App, color = VoidColors.Text)
            Text("deslize para baixo para voltar", style = VoidType.Caption, color = VoidColors.TextDim)
        }
        if (rows.isEmpty()) {
            Text("nenhum favorito ainda. Segure um app na lista para fixar.", style = VoidType.Small, color = VoidColors.TextDim)
        }
        rows.forEachIndexed { i, row ->
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                Text(
                    row.label,
                    style = VoidType.App,
                    color = VoidColors.Text,
                    maxLines = 1,
                    modifier = Modifier.weight(1f).padding(vertical = 10.dp),
                )
                Text(
                    "↑",
                    style = VoidType.App,
                    color = if (i > 0) VoidColors.Text else VoidColors.TextDim,
                    modifier = Modifier.clickable(enabled = i > 0, role = androidx.compose.ui.semantics.Role.Button) { onMove(row.key, -1) }.padding(horizontal = 16.dp, vertical = 10.dp),
                )
                Text(
                    "↓",
                    style = VoidType.App,
                    color = if (i < rows.lastIndex) VoidColors.Text else VoidColors.TextDim,
                    modifier = Modifier.clickable(enabled = i < rows.lastIndex, role = androidx.compose.ui.semantics.Role.Button) { onMove(row.key, 1) }.padding(start = 16.dp, top = 10.dp, bottom = 10.dp),
                )
            }
        }
        Spacer(Modifier.height(32.dp))
    }
}
