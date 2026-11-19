package com.voidtouch.launcher.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GestureTargetTest {
    @Test
    fun encodeAndDecodeAction() {
        QuickAction.entries.forEach { action ->
            assertEquals(action, GestureTarget.action(GestureTarget.encode(action)))
        }
    }

    @Test
    fun appKeyIsNotAnAction() {
        assertNull(GestureTarget.action("com.a/com.a.Main"))
        assertTrue(GestureTarget.isApp("com.a/com.a.Main"))
    }

    @Test
    fun emptyOrUnknownTargets() {
        assertNull(GestureTarget.action(null))
        assertNull(GestureTarget.action("action:inexistente"))
        assertFalse(GestureTarget.isApp(null))
        assertFalse(GestureTarget.isApp(""))
        assertFalse(GestureTarget.isApp(GestureTarget.encode(QuickAction.Torch)))
    }

    @Test
    fun actionIdsAreUnique() {
        assertEquals(QuickAction.entries.size, QuickAction.entries.map { it.id }.toSet().size)
    }
}
