package com.voidtouch.launcher.data

/**
 * Regras da home que antes viviam dentro da MainActivity, agora puras (sem Android) para
 * poderem ser testadas a cada versão e não quebrarem sem aviso.
 */
object HomeRules {
    enum class PinResult { Removed, Full, Added }

    /** Fixa, solta ou recusa um favorito. Devolve a nova lista e o que aconteceu. */
    fun togglePin(current: List<String>, key: String, max: Int): Pair<List<String>, PinResult> = when {
        key in current -> (current - key) to PinResult.Removed
        current.size >= max -> current to PinResult.Full
        else -> (current + key) to PinResult.Added
    }

    /** Move um favorito [delta] posições (-1 sobe, +1 desce), sem sair dos limites. */
    fun move(current: List<String>, key: String, delta: Int): List<String> {
        val from = current.indexOf(key)
        if (from < 0) return current
        val to = (from + delta).coerceIn(0, current.lastIndex)
        if (to == from) return current
        val list = current.toMutableList()
        list.removeAt(from)
        list.add(to, key)
        return list
    }

    /** Favoritos visíveis: apps e pastas que existem e, com o foco ligado, não estão escondidos. */
    fun favorites(
        apps: List<AppEntry>,
        pinned: List<String>,
        folders: List<Folder>,
        focusApps: Set<String>,
        focusOn: Boolean,
    ): List<Favorite> {
        val byKey = apps.associateBy { it.key }
        return pinned.mapNotNull { key ->
            val folderId = FavoriteKeys.folderId(key)
            if (folderId != null) {
                folders.firstOrNull { it.id == folderId }?.let { f ->
                    val n = f.keys.count { it in byKey && (!focusOn || it !in focusApps) }
                    if (n > 0) Favorite.FolderItem(f, n) else null
                }
            } else {
                byKey[key]?.takeIf { !focusOn || it.key !in focusApps }?.let { Favorite.App(it) }
            }
        }
    }

    /** Com o foco ligado, notificações de apps marcados ficam escondidas. */
    fun notificationHidden(packageName: String, focusApps: Set<String>, focusOn: Boolean): Boolean =
        focusOn && focusApps.any { it.substringBefore('/') == packageName }

    enum class FocusToggle { TurnedOff, TurnedOn, NeedsApps }

    fun toggleFocus(focusSince: Long, focusApps: Set<String>): FocusToggle = when {
        focusSince != 0L -> FocusToggle.TurnedOff
        focusApps.isEmpty() -> FocusToggle.NeedsApps
        else -> FocusToggle.TurnedOn
    }
}
