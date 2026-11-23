package com.voidtouch.launcher.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.voidtouch.launcher.ui.theme.VoidColors
import com.voidtouch.launcher.ui.theme.VoidType
import kotlinx.coroutines.delay

/** Pausa antes de abrir um app do foco: espera 3 s e pergunta se quer abrir mesmo assim. */
@Composable
fun PauseScreen(
    appLabel: String,
    phrase: String,
    onOpen: () -> Unit,
    onBack: () -> Unit,
) {
    var left by remember { mutableIntStateOf(3) }
    LaunchedEffect(Unit) {
        while (left > 0) {
            delay(1000)
            left--
        }
    }
    Box(
        Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.94f))
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {},
        contentAlignment = Alignment.Center,
    ) {
        Column(Modifier.padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(appLabel, style = VoidType.Day, color = VoidColors.Text, textAlign = TextAlign.Center)
            Spacer(Modifier.height(12.dp))
            Text("abrir mesmo assim?", style = VoidType.App, color = VoidColors.Text)
            Spacer(Modifier.height(6.dp))
            Text(phrase, style = VoidType.App, color = VoidColors.TextDim)
            Spacer(Modifier.height(36.dp))
            Text(
                "voltar",
                style = VoidType.App,
                color = VoidColors.Text,
                modifier = Modifier.clickable(onClick = onBack).padding(horizontal = 24.dp, vertical = 14.dp),
            )
            Text(
                if (left > 0) "abrir ($left)" else "abrir mesmo assim",
                style = VoidType.App,
                color = if (left > 0) VoidColors.TextDim else VoidColors.Text,
                modifier = Modifier
                    .clickable(enabled = left == 0, onClick = onOpen)
                    .padding(horizontal = 24.dp, vertical = 14.dp),
            )
        }
    }
}
