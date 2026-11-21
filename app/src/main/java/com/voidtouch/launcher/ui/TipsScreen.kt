package com.voidtouch.launcher.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.voidtouch.launcher.ui.theme.VoidColors
import com.voidtouch.launcher.ui.theme.VoidType

private val Tips = listOf(
    "deslizar para cima" to "abre a lista de apps (grade de 3 colunas; segure um app para ver as opções)",
    "deslizar para os lados" to "abre os apps ou ações que você escolher em Ajustes > gestos da home",
    "deslizar para baixo" to "abre o painel de notificações (muda em Ajustes)",
    "toque duplo" to "num espaço vazio da home, executa o gesto configurado",
    "carrossel do topo" to "passe para os lados: hora, dia, música e agenda; segure para abrir Ajustes",
    "favoritos" to "toque abre; segure para tirar da home. Apps e pastas se fixam pelo menu da lista",
    "notificações" to "toque abre o app; segure para responder sem abrir, guardar em \"ler depois\" ou dispensar",
    "anotação rápida" to "defina um gesto para \"anotação rápida\" (ou busque \"anotar\"): a primeira anotação vira a tarefa do foco na home; toque nela para marcar como feita",
    "foco" to "toque na linha \"foco\" para esconder os apps que distraem; segure para uma sessão de 25, 50 ou 90 minutos",
    "status do celular" to "toque no bloco de status para ver a saúde do aparelho e otimizar",
)

/** Dicas rápidas dos gestos; aparece uma vez no primeiro uso e fica em Ajustes > dicas de uso. */
@Composable
fun TipsScreen(onClose: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .onVerticalSwipe(up = false, threshold = 96.dp, onSwipe = onClose)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 28.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("como usar", style = VoidType.App, color = VoidColors.Text, modifier = Modifier.weight(1f))
            Text(
                "fechar",
                style = VoidType.Caption,
                color = VoidColors.Text,
                modifier = Modifier.clickable(role = androidx.compose.ui.semantics.Role.Button, onClick = onClose).heightIn(min = 44.dp).padding(vertical = 8.dp),
            )
        }
        Tips.forEach { (title, text) ->
            Spacer(Modifier.height(22.dp))
            Text(title, style = VoidType.Caption, color = VoidColors.TextDim)
            Spacer(Modifier.height(4.dp))
            Text(text, style = VoidType.Small.copy(fontSize = 14.sp, lineHeight = 20.sp), color = VoidColors.Text)
        }
        Spacer(Modifier.width(1.dp))
    }
}
