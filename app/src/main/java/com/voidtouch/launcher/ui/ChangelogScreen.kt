package com.voidtouch.launcher.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.voidtouch.launcher.data.ReleaseNote
import com.voidtouch.launcher.ui.theme.VoidColors
import com.voidtouch.launcher.ui.theme.VoidType

/** Lista de novidades: depois de atualizar mostra só a versão nova; em Ajustes mostra o histórico todo. */
@Composable
fun ChangelogScreen(title: String, notes: List<ReleaseNote>, onClose: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .onVerticalSwipe(up = false, threshold = 96.dp, onSwipe = onClose)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 28.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(title, style = VoidType.App, color = VoidColors.Text, modifier = Modifier.weight(1f))
            Text(
                "fechar",
                style = VoidType.Caption,
                color = VoidColors.Text,
                modifier = Modifier.clickable(onClick = onClose).padding(vertical = 8.dp),
            )
        }
        notes.forEach { note ->
            Spacer(Modifier.height(28.dp))
            Text("v${note.version}", style = VoidType.Caption, color = VoidColors.TextDim)
            Spacer(Modifier.height(8.dp))
            note.items.forEach { item ->
                Text(
                    "· $item",
                    style = VoidType.Small.copy(fontSize = 13.sp, lineHeight = 19.sp),
                    color = VoidColors.Text,
                    modifier = Modifier.padding(bottom = 8.dp),
                )
            }
        }
    }
}
