package com.voidtouch.launcher.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.voidtouch.launcher.ui.theme.VoidColors
import com.voidtouch.launcher.ui.theme.VoidType
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/** Mensagem curta na tela. O [id] muda a cada aviso, para o mesmo texto poder aparecer de novo. */
data class Banner(val id: Int, val text: String)

/** Aviso flutuante discreto, no lugar do Toast do sistema: entra de baixo e some sozinho. */
@Composable
fun BannerHost(banner: Banner?, modifier: Modifier = Modifier) {
    // Guarda o último texto para ele não sumir de repente durante a animação de saída.
    var last by remember { mutableStateOf("") }
    if (banner != null) last = banner.text
    AnimatedVisibility(
        visible = banner != null,
        enter = fadeIn(tween(160)) + slideInVertically(tween(200)) { it / 2 },
        exit = fadeOut(tween(160)) + slideOutVertically(tween(160)) { it / 2 },
        modifier = modifier,
    ) {
        Box(
            modifier = Modifier
                .background(VoidColors.Graphite, RoundedCornerShape(24.dp))
                .border(VoidColors.Stroke, VoidColors.Line, RoundedCornerShape(24.dp))
                .padding(horizontal = 20.dp, vertical = 12.dp),
        ) {
            Text(text = last, style = VoidType.Caption, color = VoidColors.Text)
        }
    }
}

/** Linha fina de progresso: com [progress] mostra o avanço; sem ele, um trecho vai e volta (carregando). */
@Composable
fun LoadingLine(modifier: Modifier = Modifier, progress: Float? = null) {
    val t = if (progress == null) {
        val transition = rememberInfiniteTransition(label = "loading")
        transition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(tween(1100, easing = LinearEasing), RepeatMode.Restart),
            label = "loading-x",
        ).value
    } else {
        0f
    }
    val shown by animateFloatAsState(progress ?: 0f, tween(200), label = "progress")
    Canvas(modifier = modifier.fillMaxWidth().height(2.dp)) {
        drawRect(VoidColors.Line, Offset.Zero, size)
        if (progress == null) {
            val seg = size.width * 0.3f
            val x = t * (size.width + seg) - seg
            val left = x.coerceAtLeast(0f)
            val right = (x + seg).coerceAtMost(size.width)
            if (right > left) drawRect(VoidColors.Text, Offset(left, 0f), Size(right - left, size.height))
        } else {
            drawRect(VoidColors.Text, Offset.Zero, Size(size.width * shown.coerceIn(0f, 1f), size.height))
        }
    }
}

/** Ao tocar, o item encolhe um pouco e escurece: o toque é sentido na hora. */
@Composable
fun Modifier.pressFeedback(source: InteractionSource): Modifier {
    val pressed by source.collectIsPressedAsState()
    // Mola: encolhe rápido ao tocar e volta com um pequeno quique, em vez de uma transição seca.
    val scale by animateFloatAsState(
        if (pressed) 0.93f else 1f,
        spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "press",
    )
    return graphicsLayer {
        scaleX = scale
        scaleY = scale
        alpha = if (pressed) 0.7f else 1f
    }
}

/** Engrenagem desenhada (anel, dentes e miolo), sem depender de fonte de ícones. */
@Composable
fun GearButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(44.dp)
            .clip(CircleShape)
            .clickable(role = androidx.compose.ui.semantics.Role.Button, onClick = onClick),
    ) {
        Canvas(modifier = Modifier.size(24.dp)) {
            val c = center
            val ring = 6.dp.toPx()
            drawCircle(VoidColors.Text, ring, c, style = Stroke(width = 1.6.dp.toPx()))
            drawCircle(VoidColors.Text, 2.dp.toPx(), c, style = Stroke(width = 1.4.dp.toPx()))
            for (i in 0 until 8) {
                val a = i * PI / 4
                val from = Offset(c.x + cos(a).toFloat() * (ring + 1.dp.toPx()), c.y + sin(a).toFloat() * (ring + 1.dp.toPx()))
                val to = Offset(c.x + cos(a).toFloat() * (ring + 4.dp.toPx()), c.y + sin(a).toFloat() * (ring + 4.dp.toPx()))
                drawLine(VoidColors.Text, from, to, strokeWidth = 2.4.dp.toPx(), cap = StrokeCap.Round)
            }
        }
    }
}
