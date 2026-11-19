package com.voidtouch.launcher.data

/** Texto do resumo do painel de permissões, puro para ser testado. */
object PermissionRules {
    fun summary(on: Int, total: Int): String = when {
        total <= 0 -> ""
        on == total -> "todas liberadas"
        on == 0 -> "nenhuma liberada"
        else -> "$on de $total liberadas"
    }
}
