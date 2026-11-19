package com.voidtouch.launcher.data

import android.content.SharedPreferences

/** Uma pasta da lista de apps: nome e as chaves ("pacote/atividade") dos apps dentro dela. */
data class Folder(val id: String, val name: String, val keys: List<String>)

/**
 * Regras das pastas, todas puras (sem Android) para poderem ser testadas. Um app está em no
 * máximo uma pasta, e pasta vazia deixa de existir.
 */
object Folders {

    const val MAX_NAME = 20

    fun cleanName(name: String): String =
        name.replace(Regex("[\\t\\r\\n]+"), " ").trim().take(MAX_NAME).trim()

    /** Cria uma pasta já com o app. Nome em branco vira "Pasta N". */
    fun create(list: List<Folder>, name: String, appKey: String): List<Folder> {
        val id = ((list.mapNotNull { it.id.toIntOrNull() }.maxOrNull() ?: 0) + 1).toString()
        val finalName = cleanName(name).ifEmpty { "Pasta ${list.size + 1}" }
        return removeApp(list, appKey) + Folder(id, finalName, listOf(appKey))
    }

    /** Põe o app na pasta, tirando-o de qualquer outra. */
    fun addApp(list: List<Folder>, folderId: String, appKey: String): List<Folder> =
        removeApp(list, appKey).let { cleaned ->
            // Se a pasta de destino ficou vazia e foi apagada, o app volta para a lista solta.
            cleaned.map { if (it.id == folderId) it.copy(keys = it.keys + appKey) else it }
        }

    /** Tira o app de qualquer pasta; pastas que ficam vazias somem. */
    fun removeApp(list: List<Folder>, appKey: String): List<Folder> =
        list.map { it.copy(keys = it.keys.filter { k -> k != appKey }) }.filter { it.keys.isNotEmpty() }

    fun rename(list: List<Folder>, folderId: String, name: String): List<Folder> {
        val clean = cleanName(name)
        if (clean.isEmpty()) return list
        return list.map { if (it.id == folderId) it.copy(name = clean) else it }
    }

    fun delete(list: List<Folder>, folderId: String): List<Folder> = list.filter { it.id != folderId }

    fun encode(list: List<Folder>): String =
        list.joinToString("\n") { "${it.id}\t${cleanName(it.name)}\t${it.keys.joinToString(",")}" }

    fun decode(text: String): List<Folder> = text.lines().mapNotNull { line ->
        val parts = line.split('\t')
        if (parts.size < 3 || parts[0].isBlank()) null
        else Folder(parts[0], parts[1], parts[2].split(',').filter { it.isNotBlank() })
    }
}

/** Guarda as pastas nas preferências. */
class FolderStore(private val prefs: SharedPreferences) {
    fun load(): List<Folder> = Folders.decode(prefs.getString(KEY, "").orEmpty())

    fun save(list: List<Folder>) {
        prefs.edit().putString(KEY, Folders.encode(list)).apply()
    }

    private companion object {
        const val KEY = "folders"
    }
}
