package com.voidtouch.launcher.ui

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.voidtouch.launcher.data.Live
import com.voidtouch.launcher.data.LiveKind
import com.voidtouch.launcher.notify.LiveItem
import com.voidtouch.launcher.ui.theme.VoidColors
import com.voidtouch.launcher.ui.theme.VoidType
import kotlinx.coroutines.delay

/**
 * Ilha dinâmica da home: uma pílula com o que está acontecendo agora (chamada, navegação,
 * cronômetro, downloads). Toque abre a lista; tocar numa linha abre o app. O relógio dos
 * cronômetros só anda com a home visível e só enquanto há um cronômetro na ilha.
 */
@Composable
fun LivePill(items: List<LiveItem>, active: Boolean, onOpen: (LiveItem) -> Unit) {
    if (items.isEmpty()) return
    var expanded by remember { mutableStateOf(false) }
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    val ticking = items.any { it.chrono }
    LaunchedEffect(ticking, active) {
        while (ticking && active) {
            now = System.currentTimeMillis()
            delay(1000)
        }
    }

    val shape = RoundedCornerShape(20.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 4.dp)
            .clip(shape)
            .border(1.dp, VoidColors.TextDim.copy(alpha = 0.45f), shape)
            .animateContentSize()
            .clickable(role = androidx.compose.ui.semantics.Role.Button) { expanded = !expanded }
            .padding(horizontal = 14.dp, vertical = 8.dp),
    ) {
        if (!expanded) {
            LiveRow(items.first(), now, extra = items.size - 1, onClick = null)
        } else {
            items.forEachIndexed { i, item ->
                if (i > 0) Spacer(Modifier.height(8.dp))
                LiveRow(item, now, extra = 0, onClick = { onOpen(item) })
            }
        }
    }
}

@Composable
private fun LiveRow(item: LiveItem, now: Long, extra: Int, onClick: (() -> Unit)?) {
    val value = Live.valueText(item.chrono, item.whenMs, item.countDown, item.percent, now)
    Column(modifier = Modifier.fillMaxWidth().then(if (onClick != null) Modifier.clickable(role = androidx.compose.ui.semantics.Role.Button, onClick = onClick) else Modifier)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "● ${item.appLabel} · ${item.title}",
                style = VoidType.Caption,
                color = VoidColors.Text,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            Spacer(Modifier.width(10.dp))
            if (value.isNotEmpty()) Text(value, style = VoidType.Caption, color = VoidColors.Text, maxLines = 1)
            if (extra > 0) {
                Spacer(Modifier.width(10.dp))
                Text("+$extra", style = VoidType.Caption, color = VoidColors.TextDim, maxLines = 1)
            }
        }
        val pct = item.percent
        if (pct != null && !item.chrono) {
            Spacer(Modifier.height(5.dp))
            Box(modifier = Modifier.fillMaxWidth().height(2.dp).background(VoidColors.TextDim.copy(alpha = 0.3f))) {
                Box(modifier = Modifier.fillMaxWidth(pct / 100f).height(2.dp).background(VoidColors.Text))
            }
        }
        if (item.kind == LiveKind.Call && !item.chrono) {
            Text("em chamada", style = VoidType.Caption, color = VoidColors.TextDim)
        }
    }
}
