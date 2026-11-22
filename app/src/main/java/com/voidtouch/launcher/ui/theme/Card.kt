package com.voidtouch.launcher.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.voidtouch.launcher.data.RotinaTokens

/** Cartão arredondado com contorno fino. No design Void não muda nada. */
fun Modifier.card(radius: Dp = RotinaTokens.RADIUS_DP.dp): Modifier {
    if (!VoidColors.isRotina) return this
    val shape = RoundedCornerShape(radius)
    return this
        .clip(shape)
        .background(VoidColors.Surface)
        .border(VoidColors.Stroke, VoidColors.Line, shape)
}

/** Margem lateral dos cartões (0 no design Void). */
val cardMargin: Dp get() = if (VoidColors.isRotina) RotinaTokens.MARGIN_DP.dp else 0.dp
