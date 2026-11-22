package com.voidtouch.launcher.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.voidtouch.launcher.data.Design
import com.voidtouch.launcher.data.HighVis
import com.voidtouch.launcher.data.RotinaTokens
import com.voidtouch.launcher.data.TonePalette
import com.voidtouch.launcher.data.WallTone
import androidx.compose.ui.graphics.Color

/** Paleta única do VoidTouch: preto, cinzas e branco. Sem cor de destaque. */
object VoidColors {
    val Void = Color(0xFF000000)
    val Graphite = Color(0xFF121212)

    // Texto e linhas podem ganhar um tom do papel de parede (veja WallTone); por padrão são neutros.
    private var palette by mutableStateOf(WallTone.NEUTRAL)

    /** Modo de alta visibilidade: sobrepõe as cores, o peso das letras e a espessura dos traços. */
    var highVis by mutableStateOf(false)

    /** Design escolhido (Void ou Rotina). */
    var design by mutableStateOf(Design.Void)

    val isRotina: Boolean get() = design == Design.Rotina

    val Line: Color get() = when {
        highVis -> Color(HighVis.LINE)
        isRotina -> Color(RotinaTokens.LINE)
        else -> Color(palette.line)
    }
    val TextDim: Color get() = when {
        highVis -> Color(HighVis.DIM)
        isRotina -> Color(RotinaTokens.MUTED)
        else -> Color(palette.dim)
    }
    val Text: Color get() = when {
        highVis -> Color(HighVis.TEXT)
        isRotina -> Color(RotinaTokens.TEXT)
        else -> Color(palette.text)
    }

    /** Fundo dos cartões: só existe no design Rotina. */
    val Surface: Color get() = when {
        !isRotina -> Color.Transparent
        highVis -> Color(HighVis.BACKGROUND)
        else -> Color(RotinaTokens.SURFACE)
    }

    /** Cor do item selecionado (pílula clara) e do texto sobre ela. */
    val Selected: Color get() = if (isRotina) Color(RotinaTokens.SELECTED) else Text
    val OnSelected: Color get() = Color(RotinaTokens.ON_SELECTED)

    /** Fundo da tela no design Rotina (quando o fundo não é o papel de parede). */
    val RotinaBackground: Int get() = if (highVis) HighVis.BACKGROUND else RotinaTokens.BACKGROUND

    /** Espessura dos contornos de botões e pílulas. */
    val Stroke: Dp get() = HighVis.strokeDp(highVis).dp

    /** Camada escura sobre o papel de parede (ARGB). No modo de alta visibilidade, preto sólido. */
    val Veil: Int get() = if (highVis) HighVis.BACKGROUND else palette.veil

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
    val capped = androidx.compose.ui.unit.Density(density.density, com.voidtouch.launcher.data.FontScale.cap(density.fontScale, VoidColors.highVis))
    androidx.compose.runtime.CompositionLocalProvider(androidx.compose.ui.platform.LocalDensity provides capped) {
        MaterialTheme(
            colorScheme = VoidScheme,
            content = content,
        )
    }
}
