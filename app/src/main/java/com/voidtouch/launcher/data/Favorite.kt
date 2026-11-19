package com.voidtouch.launcher.data

/** Um item da lista de favoritos da home: um app ou uma pasta fixada. */
sealed interface Favorite {
    data class App(val app: AppEntry) : Favorite
    data class FolderItem(val folder: Folder, val count: Int) : Favorite
}

/** Favoritos guardam apps por "pacote/atividade" e pastas por "folder:<id>". */
object FavoriteKeys {
    private const val FOLDER_PREFIX = "folder:"

    fun folder(id: String): String = FOLDER_PREFIX + id

    fun folderId(key: String): String? = key.takeIf { it.startsWith(FOLDER_PREFIX) }?.removePrefix(FOLDER_PREFIX)

    /** Tira dos favoritos as pastas que não existem mais. */
    fun withoutMissingFolders(keys: List<String>, folders: List<Folder>): List<String> {
        val ids = folders.map { it.id }.toSet()
        return keys.filter { k -> folderId(k)?.let { it in ids } ?: true }
    }
}
