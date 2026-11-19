package com.voidtouch.launcher.data

import org.junit.Assert.assertEquals
import org.junit.Test

class PermissionRulesTest {
    @Test fun summaryCases() {
        assertEquals("todas liberadas", PermissionRules.summary(5, 5))
        assertEquals("nenhuma liberada", PermissionRules.summary(0, 5))
        assertEquals("2 de 5 liberadas", PermissionRules.summary(2, 5))
        assertEquals("", PermissionRules.summary(0, 0))
    }
}
