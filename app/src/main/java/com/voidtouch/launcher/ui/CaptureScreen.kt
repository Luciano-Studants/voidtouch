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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.voidtouch.launcher.data.Note
import com.voidtouch.launcher.data.Notes
import com.voidtouch.launcher.ui.theme.VoidColors
import com.voidtouch.launcher.ui.theme.VoidType

/**
 * Captura rápida: o campo já abre com o teclado; escrever e confirmar guarda a anotação. A
 * "tarefa do foco" (●) aparece na home e some quando marcada como feita. Tocar no texto marca ou
 * desmarca como feita. Deslizar para baixo, com a tela no topo, volta para a home.
 */
@Composable
fun CaptureScreen(
    notes: List<Note>,
    onAdd: (String) -> Unit,
    onToggleDone: (Note) -> Unit,
    onSetFocus: (Note) -> Unit,
    onRemove: (Note) -> Unit,
    onClearDone: () -> Unit,
    onClose: () -> Unit,
) {
    var text by remember { mutableStateOf("") }
    val scroll = rememberScrollState()
    val pending = notes.filter { !it.done }
    val done = notes.filter { it.done }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .onVerticalSwipe(up = false, threshold = 96.dp, enabled = { !scroll.canScrollBackward }, onSwipe = onClose)
            .verticalScroll(scroll)
            .padding(horizontal = 24.dp),
    ) {
        Column(modifier = Modifier.padding(top = 28.dp, bottom = 8.dp)) {
            SearchField(
                query = text,
                onQueryChange = { text = it },
                requestFocus = true,
                onGo = {
                    if (text.isNotBlank()) {
                        onAdd(text)
                        text = ""
                    }
                },
                placeholder = "anotar ideia ou tarefa…",
            )
            Text(
                "${Notes.pending(notes)} pendente(s) · confirme para guardar · deslize para baixo para voltar",
                style = VoidType.Caption,
                color = VoidColors.TextDim,
                modifier = Modifier.padding(top = 6.dp),
            )
        }
        pending.forEach { NoteRow(it, onToggleDone, onSetFocus, onRemove) }
        if (done.isNotEmpty()) {
            Spacer(Modifier.height(14.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("feitas", style = VoidType.Caption, color = VoidColors.TextDim, modifier = Modifier.weight(1f))
                Text(
                    "limpar",
                    style = VoidType.Caption,
                    color = VoidColors.Text,
                    modifier = Modifier.clickable(onClick = onClearDone).padding(vertical = 6.dp),
                )
            }
            done.forEach { NoteRow(it, onToggleDone, onSetFocus, onRemove) }
        }
        if (notes.isEmpty()) {
            Text(
                "nada anotado ainda. A primeira anotação vira a tarefa do foco, que aparece na home.",
                style = VoidType.Small,
                color = VoidColors.TextDim,
                modifier = Modifier.padding(top = 12.dp),
            )
        }
        Spacer(Modifier.height(32.dp))
    }
}

@Composable
private fun NoteRow(note: Note, onToggleDone: (Note) -> Unit, onSetFocus: (Note) -> Unit, onRemove: (Note) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Text(
            text = (if (note.done) "✓ " else if (note.focus) "● " else "○ ") + note.text,
            style = VoidType.App.copy(textDecoration = if (note.done) TextDecoration.LineThrough else TextDecoration.None),
            color = if (note.done) VoidColors.TextDim else VoidColors.Text,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f).clickable { onToggleDone(note) }.padding(vertical = 10.dp),
        )
        if (!note.done && !note.focus) {
            Text(
                "foco",
                style = VoidType.Caption,
                color = VoidColors.Text,
                modifier = Modifier.clickable { onSetFocus(note) }.padding(horizontal = 8.dp, vertical = 10.dp),
            )
        }
        Spacer(Modifier.width(4.dp))
        Text(
            "×",
            style = VoidType.App,
            color = VoidColors.TextDim,
            modifier = Modifier.clickable { onRemove(note) }.padding(horizontal = 8.dp, vertical = 6.dp),
        )
    }
}
