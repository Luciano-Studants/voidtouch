package com.voidtouch.launcher.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FoldersTest {
    @Test
    fun create_blankNameGetsDefault() {
        val list = Folders.create(emptyList(), "  ", "a/b")
        assertEquals(1, list.size)
        assertEquals("Pasta 1", list[0].name)
        assertEquals(listOf("a/b"), list[0].keys)
    }

    @Test
    fun create_idsAreUnique() {
        var list = Folders.create(emptyList(), "A", "x/1")
        list = Folders.create(list, "B", "x/2")
        assertEquals(2, list.map { it.id }.toSet().size)
    }

    @Test
    fun addApp_movesBetweenFolders() {
        var list = Folders.create(emptyList(), "A", "x/1")
        list = Folders.create(list, "B", "x/2")
        val a = list[0].id
        val b = list[1].id
        list = Folders.addApp(list, b, "x/1")
        // A ficou vazia e foi apagada; B tem os dois apps.
        assertEquals(1, list.size)
        assertEquals(b, list[0].id)
        assertEquals(listOf("x/2", "x/1"), list[0].keys)
        assertTrue(list.none { it.id == a })
    }

    @Test
    fun removeApp_dropsEmptyFolder() {
        val list = Folders.create(emptyList(), "A", "x/1")
        assertEquals(emptyList<Folder>(), Folders.removeApp(list, "x/1"))
    }

    @Test
    fun rename_ignoresBlankAndLimitsLength() {
        val list = Folders.create(emptyList(), "A", "x/1")
        val id = list[0].id
        assertEquals("A", Folders.rename(list, id, "   ")[0].name)
        val long = Folders.rename(list, id, "a".repeat(50))[0].name
        assertEquals(Folders.MAX_NAME, long.length)
    }

    @Test
    fun encodeDecode_roundTrip() {
        var list = Folders.create(emptyList(), "Jogos", "com.a/com.a.Main")
        list = Folders.addApp(list, list[0].id, "com.b/com.b.Main\$Inner")
        list = Folders.create(list, "Banco", "com.c/com.c.Main")
        assertEquals(list, Folders.decode(Folders.encode(list)))
    }

    @Test
    fun decode_ignoresBrokenLines() {
        assertEquals(emptyList<Folder>(), Folders.decode("lixo\n\n"))
    }

    @Test
    fun delete_removesOnlyThatFolder() {
        var list = Folders.create(emptyList(), "A", "x/1")
        list = Folders.create(list, "B", "x/2")
        val result = Folders.delete(list, list[0].id)
        assertEquals(listOf("B"), result.map { it.name })
    }
}

class FavoriteKeysTest {
    @Test
    fun folderKeyRoundTrip() {
        assertEquals("7", FavoriteKeys.folderId(FavoriteKeys.folder("7")))
        assertEquals(null, FavoriteKeys.folderId("com.a/com.a.Main"))
    }

    @Test
    fun missingFoldersAreDropped() {
        val folders = Folders.create(emptyList(), "A", "x/1")
        val keys = listOf("com.a/Main", FavoriteKeys.folder(folders[0].id), FavoriteKeys.folder("999"))
        assertEquals(
            listOf("com.a/Main", FavoriteKeys.folder(folders[0].id)),
            FavoriteKeys.withoutMissingFolders(keys, folders),
        )
    }
}
