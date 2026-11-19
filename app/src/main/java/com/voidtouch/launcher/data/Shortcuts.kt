package com.voidtouch.launcher.data

import android.content.Context
import android.content.pm.LauncherApps
import android.content.pm.ShortcutInfo
import android.os.Process

/** Um atalho que o próprio app declara, como "nova mensagem" ou "selfie". */
data class AppShortcut(val id: String, val label: String, val info: ShortcutInfo)

/**
 * Atalhos dos apps pelo serviço de launcher do Android. Só funcionam quando o VoidTouch é a tela
 * inicial padrão; fora disso o sistema recusa e a lista vem vazia.
 */
object Shortcuts {

    fun load(context: Context, packageName: String, limit: Int = 4): List<AppShortcut> = runCatching {
        val apps = context.getSystemService(LauncherApps::class.java)
        if (!apps.hasShortcutHost()) return emptyList()
        val query = LauncherApps.ShortcutQuery()
            .setPackage(packageName)
            .setQueryFlags(
                LauncherApps.ShortcutQuery.FLAG_MATCH_DYNAMIC or
                    LauncherApps.ShortcutQuery.FLAG_MATCH_MANIFEST or
                    LauncherApps.ShortcutQuery.FLAG_MATCH_PINNED,
            )
        apps.getShortcuts(query, Process.myUserHandle())
            .orEmpty()
            .filter { it.isEnabled }
            .mapNotNull { info ->
                val label = (info.shortLabel ?: info.longLabel)?.toString()?.takeIf { it.isNotBlank() }
                    ?: return@mapNotNull null
                AppShortcut(info.id, label, info)
            }
            .distinctBy { it.id }
            .take(limit)
    }.getOrDefault(emptyList())

    fun start(context: Context, shortcut: AppShortcut): Boolean = runCatching {
        context.getSystemService(LauncherApps::class.java).startShortcut(shortcut.info, null, null)
        true
    }.getOrDefault(false)
}
