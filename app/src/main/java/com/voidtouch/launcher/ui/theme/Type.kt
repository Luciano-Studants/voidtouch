package com.voidtouch.launcher.ui.theme

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.voidtouch.launcher.R

/** Fonte monoespaçada fina, usada na lista e nos textos. */
val JetBrainsMono = FontFamily(Font(R.font.jetbrains_mono_light, FontWeight.Light))

/** Fonte quadrada e fina, usada só no relógio. */
val Oxanium = FontFamily(Font(R.font.oxanium_extralight, FontWeight.ExtraLight))

/** Fontes do design Rotina (as mesmas do app Rotina Noturna). */
val Inter = FontFamily(
    Font(R.font.inter_regular, FontWeight.Normal),
    Font(R.font.inter_semibold, FontWeight.SemiBold),
)
val SpaceGrotesk = FontFamily(Font(R.font.space_grotesk_medium, FontWeight.Medium))

/** Letra manuscrita elegante, usada só no nome do mês. */
val Allura = FontFamily(Font(R.font.allura_regular, FontWeight.Normal))

/** Estilos de texto do app. A cor é aplicada em cada Text. */
object VoidType {
    /** No modo de alta visibilidade as letras ficam em negrito. */
    private fun hv(style: TextStyle): TextStyle {
        val base = if (VoidColors.isRotina) rotina(style) else style
        return if (VoidColors.highVis) base.copy(fontWeight = FontWeight.Bold) else base
    }

    /** Design Rotina: Inter no texto e Space Grotesk no relógio e nos títulos grandes. */
    private fun rotina(style: TextStyle): TextStyle = when (style.fontFamily) {
        JetBrainsMono -> style.copy(fontFamily = Inter, fontWeight = FontWeight.Normal, letterSpacing = 0.sp)
        Oxanium, Allura -> style.copy(fontFamily = SpaceGrotesk, fontWeight = FontWeight.Medium)
        else -> style
    }

    /** Linhas do bloco de status e notificações: menores, para caber duas colunas. */
    val Small: TextStyle get() = hv(TextStyle(
        fontFamily = JetBrainsMono,
        fontWeight = FontWeight.Light,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.3.sp,
    ))

    /** Dia da semana grande, na página "hoje é" do carrossel. */
    val Day: TextStyle get() = hv(TextStyle(
        fontFamily = Oxanium,
        fontWeight = FontWeight.ExtraLight,
        fontSize = 40.sp,
        lineHeight = 44.sp,
        letterSpacing = 1.sp,
    ))

    val Month: TextStyle get() = if (VoidColors.highVis) TextStyle(
        fontFamily = JetBrainsMono,
        fontWeight = FontWeight.Bold,
        fontSize = 34.sp,
        lineHeight = 40.sp,
    ) else (TextStyle(
        fontFamily = Allura,
        fontWeight = FontWeight.Normal,
        fontSize = 46.sp,
        lineHeight = 52.sp,
    ))

    val Caption: TextStyle get() = hv(TextStyle(
        fontFamily = JetBrainsMono,
        fontWeight = FontWeight.Light,
        fontSize = 12.sp,
        letterSpacing = 1.5.sp,
    ))

    val App: TextStyle get() = hv(TextStyle(
        fontFamily = JetBrainsMono,
        fontWeight = FontWeight.Light,
        fontSize = 16.sp,
        letterSpacing = 0.3.sp,
    ))

    val Date: TextStyle get() = hv(TextStyle(
        fontFamily = JetBrainsMono,
        fontWeight = FontWeight.Light,
        fontSize = 13.sp,
        letterSpacing = 0.8.sp,
    ))

    val Clock: TextStyle get() = hv(TextStyle(
        fontFamily = Oxanium,
        fontWeight = FontWeight.ExtraLight,
        fontSize = 64.sp,
        lineHeight = 68.sp,
        letterSpacing = 2.sp,
    ))
}
