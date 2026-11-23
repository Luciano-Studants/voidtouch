package com.voidtouch.launcher.ui

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

/** Texto de uma linha que cresce até ocupar toda a largura (e a altura) disponível. */
@Composable
fun FitText(
    text: String,
    style: TextStyle,
    color: Color,
    modifier: Modifier = Modifier,
    minSize: Float = 14f,
    maxSize: Float = 160f,
) {
    val measurer = rememberTextMeasurer()
    BoxWithConstraints(modifier = modifier) {
        val maxW = constraints.maxWidth
        val maxH = if (constraints.hasBoundedHeight) constraints.maxHeight else Int.MAX_VALUE
        val size = remember(text, style, maxW, maxH) {
            var lo = minSize
            var hi = maxSize
            repeat(12) {
                val mid = (lo + hi) / 2f
                val r = measurer.measure(
                    text = text,
                    style = style.copy(fontSize = mid.sp, lineHeight = TextUnit.Unspecified),
                    softWrap = false,
                    maxLines = 1,
                )
                if (r.size.width <= maxW && r.size.height <= maxH) lo = mid else hi = mid
            }
            lo
        }
        Text(
            text = text,
            style = style.copy(fontSize = size.sp, lineHeight = TextUnit.Unspecified),
            color = color,
            maxLines = 1,
            softWrap = false,
        )
    }
}
