package com.voidtouch.launcher.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.voidtouch.launcher.data.AppEntry
import com.voidtouch.launcher.ui.theme.VoidColors
import com.voidtouch.launcher.ui.theme.VoidType

/**
 * Lista completa de apps, em texto. Tocar abre o app; segurar fixa ou desafixa na home
 * (o ponto à direita marca os fixados).
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppList(
    apps: List<AppEntry>,
    pinnedKeys: List<String>,
    onLaunch: (AppEntry) -> Unit,
    onTogglePin: (AppEntry) -> Unit,
) {
    val haptic = LocalHapticFeedback.current

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp),
    ) {
        item(key = "title", contentType = "title") {
            Column(modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 32.dp, bottom = 16.dp)) {
                Text(text = "todos os apps", style = VoidType.App, color = VoidColors.Text)
                Text(
                    text = "segure um app para fixar na home",
                    style = VoidType.Caption,
                    color = VoidColors.TextDim,
                )
            }
        }
        items(
            items = apps,
            key = { it.key },
            contentType = { "app" },
        ) { app ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .combinedClickable(
                        onClick = { onLaunch(app) },
                        onLongClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onTogglePin(app)
                        },
                    )
                    .padding(horizontal = 24.dp, vertical = 14.dp),
            ) {
                Text(
                    text = app.label,
                    style = VoidType.App,
                    color = VoidColors.Text,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                if (app.key in pinnedKeys) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(VoidColors.Text, CircleShape),
                    )
                }
            }
        }
    }
}
