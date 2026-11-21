package com.voidtouch.launcher.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.voidtouch.launcher.data.TonePalette
import com.voidtouch.launcher.data.WallTone
import androidx.compose.ui.graphics.Color

/** Paleta única do VoidTouch: preto, cinzas e branco. Sem cor de destaque. */
object VoidColors {
    val Void = Color(0xFF000000)
    val Graphite = Color(0xFF121212)

    // Texto e linhas podem ganhar um tom do papel de parede (veja WallTone); por padrão são neutros.
    private var palette by mutableStateOf(WallTone.NEUTRAL)

    val Line: Color get() = Color(palette.line)
    val TextDim: Color get() = Color(palette.dim)
    val Text: Color get() = Color(palette.text)

    /** Camada escura sobre o papel de parede (ARGB). */
    val Veil: Int get() = palette.veil

    fun apply(p: TonePalette) {
        if (p != palette) palette = p
    }
}

/**
 * Fundo da tela inicial. É aplicado como fundo da janela (e não desenhado pelo Compose),
 * para não gastar nada de GPU em desenho extra.
 */
enum class Backdrop(val key: String, val argb: Int) {
    /** Preto puro. */
    Void("void", 0xFF000000.toInt()),

    /** Cinza bem escuro. */
    Graphite("graphite", 0xFF121212.toInt()),

    /** Papel de parede do sistema, com uma camada escura (55%) para o texto continuar legível. */
    Wallpaper("wallpaper", 0x8C000000.toInt());

    fun next(): Backdrop = entries[(ordinal + 1) % entries.size]

    companion object {
        fun fromKey(key: String?): Backdrop = entries.firstOrNull { it.key == key } ?: Void
    }
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
    val density = androidx.compose.ui.platform.LocalDensity.current
    val capped = androidx.compose.ui.unit.Density(density.density, com.voidtouch.launcher.data.FontScale.cap(density.fontScale))
    androidx.compose.runtime.CompositionLocalProvider(androidx.compose.ui.platform.LocalDensity provides capped) {
        MaterialTheme(
            colorScheme = VoidScheme,
            content = content,
        )
    }
}
