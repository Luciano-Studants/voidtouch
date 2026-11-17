package com.voidtouch.launcher.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/** Paleta única do VoidTouch: preto, cinzas e branco. Sem cor de destaque. */
object VoidColors {
    val Void = Color(0xFF000000)
    val Graphite = Color(0xFF121212)
    val Line = Color(0xFF2A2A2A)
    val TextDim = Color(0xFF8A8A8A)
    val Text = Color(0xFFF2F2F2)
}

private val VoidScheme = darkColorScheme(
    primary = VoidColors.Text,
    onPrimary = VoidColors.Void,
    background = VoidColors.Void,
    onBackground = VoidColors.Text,
    surface = VoidColors.Graphite,
    onSurface = VoidColors.Text,
    outline = VoidColors.Line,
)

@Composable
fun VoidTouchTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = VoidScheme,
        content = content,
    )
}
