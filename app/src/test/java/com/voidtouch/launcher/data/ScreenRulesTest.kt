package com.voidtouch.launcher.data

import org.junit.Assert.assertEquals
import org.junit.Test

class ScreenRulesTest {
    @Test fun abrir_substitui_a_tela_atual() =
        assertEquals(Screen.Optimize, ScreenRules.open(Screen.Settings, Screen.Optimize))

    @Test fun fechar_a_tela_aberta_volta_para_a_home() =
        assertEquals(Screen.Home, ScreenRules.close(Screen.Health, Screen.Health))

    @Test fun fechar_outra_tela_nao_mexe_na_atual() =
        assertEquals(Screen.Settings, ScreenRules.close(Screen.Settings, Screen.Drawer))

    @Test fun abrir_a_partir_de_ajustes_fecha_ajustes_e_abre_a_outra() {
        var s = Screen.Settings
        s = ScreenRules.close(s, Screen.Settings)
        s = ScreenRules.open(s, Screen.Usage)
        assertEquals(Screen.Usage, s)
    }

    @Test fun home_sempre_volta() = assertEquals(Screen.Home, ScreenRules.home())

    @Test fun chaves_sao_unicas() =
        assertEquals(Screen.entries.size, Screen.entries.map { it.key }.toSet().size)

    @Test fun a_home_tem_chave_home() = assertEquals("home", Screen.Home.key)
}
