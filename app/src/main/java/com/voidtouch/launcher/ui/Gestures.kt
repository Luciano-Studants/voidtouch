package com.voidtouch.launcher.ui

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.AwaitPointerEventScope
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.unit.Dp
import kotlin.math.abs

/**
 * Acompanha um gesto sem consumir o toque, então toques e segurar nos filhos continuam
 * funcionando. Devolve quando o gesto termina ou quando [onDelta] retorna verdadeiro
 * (o deslize foi reconhecido). [armed] é lido uma vez, quando o dedo encosta: assim o gesto
 * não dispara no meio de uma rolagem que acabou de chegar ao fim da lista.
 */
private suspend fun AwaitPointerEventScope.watchSwipe(
    excludeEndEdgePx: Float,
    armed: () -> Boolean,
    onDelta: (dx: Float, dy: Float) -> Boolean,
) {
    val down = awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
    // Toques na faixa da direita (a régua de rolagem da gaveta) não contam como deslize.
    if (excludeEndEdgePx > 0f && down.position.x > size.width - excludeEndEdgePx) return
    if (!armed()) return

    var dx = 0f
    var dy = 0f
    while (true) {
        val event = awaitPointerEvent(PointerEventPass.Initial)
        val change = event.changes.firstOrNull { it.id == down.id } ?: return
        if (!change.pressed) return
        val delta = change.positionChange()
        dx += delta.x
        dy += delta.y
        if (onDelta(dx, dy)) return
    }
}

/**
 * Deslize vertical longo, para cima ou para baixo. Só vale quando o movimento é mais vertical
 * que horizontal.
 */
@Composable
fun Modifier.onVerticalSwipe(
    up: Boolean,
    threshold: Dp,
    enabled: () -> Boolean = { true },
    excludeEndEdge: Dp = Dp.Zero,
    onSwipe: () -> Unit,
): Modifier {
    val currentOnSwipe by rememberUpdatedState(onSwipe)
    val currentEnabled by rememberUpdatedState(enabled)

    return pointerInput(up) {
        val limit = threshold.toPx()
        val edge = excludeEndEdge.toPx()
        awaitEachGesture {
            watchSwipe(edge, { currentEnabled() }) { dx, dy ->
                val reached = (if (up) dy < -limit else dy > limit) && abs(dy) > abs(dx)
                if (reached) currentOnSwipe()
                reached
            }
        }
    }
}

/**
 * Deslize horizontal longo. Só vale quando o movimento é bem mais horizontal que vertical,
 * para não confundir com a rolagem de uma lista.
 */
@Composable
fun Modifier.onHorizontalSwipe(
    threshold: Dp,
    onSwipeLeft: () -> Unit,
    onSwipeRight: () -> Unit,
): Modifier {
    val currentLeft by rememberUpdatedState(onSwipeLeft)
    val currentRight by rememberUpdatedState(onSwipeRight)

    return pointerInput(Unit) {
        val limit = threshold.toPx()
        awaitEachGesture {
            watchSwipe(0f, { true }) { dx, dy ->
                val reached = abs(dx) > limit && abs(dx) > abs(dy) * 1.5f
                if (reached) {
                    if (dx < 0f) currentLeft() else currentRight()
                }
                reached
            }
        }
    }
}
