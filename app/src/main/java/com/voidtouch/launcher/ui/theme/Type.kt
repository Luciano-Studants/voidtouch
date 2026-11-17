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

/** Estilos de texto do app. A cor é aplicada em cada Text. */
object VoidType {
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
        fontSize = 84.sp,
        lineHeight = 88.sp,
        letterSpacing = 2.sp,
    )
}
