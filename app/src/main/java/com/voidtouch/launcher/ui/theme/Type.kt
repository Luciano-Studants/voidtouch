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

/** Letra manuscrita elegante, usada só no nome do mês. */
val Allura = FontFamily(Font(R.font.allura_regular, FontWeight.Normal))

/** Estilos de texto do app. A cor é aplicada em cada Text. */
object VoidType {
    val Month = TextStyle(
        fontFamily = Allura,
        fontWeight = FontWeight.Normal,
        fontSize = 46.sp,
        lineHeight = 52.sp,
    )

    val Caption = TextStyle(
        fontFamily = JetBrainsMono,
        fontWeight = FontWeight.Light,
        fontSize = 12.sp,
        letterSpacing = 1.5.sp,
    )

    val App = TextStyle(
        fontFamily = JetBrainsMono,
        fontWeight = FontWeight.Light,
        fontSize = 16.sp,
        letterSpacing = 0.3.sp,
    )

    val Date = TextStyle(
        fontFamily = JetBrainsMono,
        fontWeight = FontWeight.Light,
        fontSize = 13.sp,
        letterSpacing = 0.8.sp,
    )

    val Clock = TextStyle(
        fontFamily = Oxanium,
        fontWeight = FontWeight.ExtraLight,
        fontSize = 64.sp,
        lineHeight = 68.sp,
        letterSpacing = 2.sp,
    )
}
