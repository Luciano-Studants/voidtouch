package com.voidtouch.launcher

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.voidtouch.launcher.data.Screen
import com.voidtouch.launcher.data.ScreenRules

/** Guarda qual tela interna está aberta; as regras ficam em [ScreenRules]. */
class Navigator(initial: Screen = Screen.Home) {
    var current by mutableStateOf(initial)
        private set

    fun isOpen(screen: Screen): Boolean = current == screen

    fun open(screen: Screen) {
        current = ScreenRules.open(current, screen)
    }

    fun close(screen: Screen) {
        current = ScreenRules.close(current, screen)
    }

    fun home() {
        current = ScreenRules.home()
    }
}
