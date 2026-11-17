package com.voidtouch.launcher.ui

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.unit.Dp

/**
 * Detecta um deslize vertical longo sem consumir o toque, então toques e segurar nos filhos
 * continuam funcionando. [enabled] é lido uma vez, quando o dedo encosta: assim o gesto não
 * dispara no meio de uma rolagem que acabou de chegar ao fim da lista.
 */
@Composable
fun Modifier.onVerticalSwipe(
    up: Boolean,
    threshold: Dp,
    enabled: () -> Boolean = { true },
    onSwipe: () -> Unit,
): Modifier {
    val currentOnSwipe by rememberUpdatedState(onSwipe)
    val currentEnabled by rememberUpdatedState(enabled)

    return pointerInput(up) {
        val limit = threshold.toPx()
        awaitEachGesture {
            val down = awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
            val armed = currentEnabled()
            var total = 0f
            while (true) {
                val event = awaitPointerEvent(PointerEventPass.Initial)
                val change = event.changes.firstOrNull { it.id == down.id } ?: break
                if (!change.pressed) break
                total += change.positionChange().y
                val reached = if (up) total < -limit else total > limit
                if (armed && reached) {
                    currentOnSwipe()
                    break
                }
            }
        }
    }
}
