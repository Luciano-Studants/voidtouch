package com.voidtouch.launcher.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BackupTest {
    private fun ok(data: String, format: Int = 1) = """{"app":"voidtouch","format":$format,"data":$data}"""

    @Test fun validBackupParses() {
        val r = Backup.parse(ok("""{"backdrop":{"t":"s","v":"black"},"flag":{"t":"b","v":true},"tags":{"t":"set","v":["a","b"]}}"""))
        assertTrue(r is Backup.Result.Ok)
        assertEquals(3, (r as Backup.Result.Ok).items.size)
    }

    @Test fun skipsMomentaryKeys() {
        val r = Backup.parse(ok("""{"focus_since":{"t":"l","v":5},"backdrop":{"t":"s","v":"x"}}"""))
        assertEquals(1, (r as Backup.Result.Ok).items.size)
    }

    @Test fun rejectsForeignFile() {
        assertTrue(Backup.parse("""{"app":"other","format":1,"data":{}}""") is Backup.Result.Invalid)
    }

    @Test fun rejectsNewerFormat() {
        assertTrue(Backup.parse(ok("""{"a":{"t":"b","v":true}}""", format = 99)) is Backup.Result.Invalid)
    }

    @Test fun rejectsCorruptedJson() {
        assertTrue(Backup.parse("{not json") is Backup.Result.Invalid)
    }

    @Test fun rejectsUnknownTypeAndBadKey() {
        assertTrue(Backup.parse(ok("""{"a":{"t":"x","v":1}}""")) is Backup.Result.Invalid)
        assertTrue(Backup.parse(ok("""{"../etc":{"t":"b","v":true}}""")) is Backup.Result.Invalid)
    }

    @Test fun oneBadItemRejectsAll() {
        val r = Backup.parse(ok("""{"good":{"t":"b","v":true},"bad":{"t":"i","v":"texto"}}"""))
        assertTrue(r is Backup.Result.Invalid)
    }

    @Test fun rejectsEmpty() {
        assertTrue(Backup.parse(ok("{}")) is Backup.Result.Invalid)
    }
}
