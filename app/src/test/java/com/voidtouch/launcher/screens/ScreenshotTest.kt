package com.voidtouch.launcher.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.github.takahirom.roborazzi.captureRoboImage
import com.voidtouch.launcher.data.Design
import com.voidtouch.launcher.notify.NoteItem
import com.voidtouch.launcher.ui.MonthPage
import com.voidtouch.launcher.ui.NotificationsPanel
import com.voidtouch.launcher.ui.PauseScreen
import com.voidtouch.launcher.ui.theme.VoidColors
import java.io.File
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Capturas dos pedaços de tela que já quebraram (meia largura, fonte grande, alta visibilidade).
 * Rodam só com `-Pscreens`; as imagens vão para app/build/screens e o CI as guarda como artefato.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = "w360dp-h780dp-xxhdpi")
class ScreenshotTest {
    @get:Rule val rule = createComposeRule()

    private val note = NoteItem(
        key = "k", packageName = "com.whatsapp", appLabel = "WhatsApp", title = "Jessyca Marmitaria",
        text = "Bom dia, tudo bem? Seu pedido saiu para entrega", intent = null,
    )

    private fun shot(name: String, design: Design, highVis: Boolean, fontScale: Float, widthDp: Int, content: @Composable () -> Unit) {
        VoidColors.design = design
        VoidColors.highVis = highVis
        rule.setContent {
            val d = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(d.density, fontScale)) {
                Box(Modifier.width(widthDp.dp).height(420.dp).background(Color.Black)) { content() }
            }
        }
        File("build/screens").mkdirs()
        rule.onRoot().captureRoboImage("build/screens/$name.png")
    }

    private fun withActions(widthDp: Int, name: String, design: Design, hv: Boolean, scale: Float) {
        shot(name, design, hv, scale, widthDp) {
            NotificationsPanel(
                notifications = listOf(note), notificationsEnabled = true,
                onNotificationClick = {}, onNotificationDismiss = {}, onNotificationReply = { _, _ -> },
                onNotificationLater = {}, laterCount = 0, onOpenLater = {}, onEnableNotifications = {},
                maxGroups = 2,
            )
        }
        rule.onNodeWithText("Jessyca Marmitaria").performTouchInput { longClick() }
        rule.waitForIdle()
        rule.onRoot().captureRoboImage("build/screens/$name-aberto.png")
    }

    @Test fun notificacoesMeiaLarguraVoid() = withActions(170, "notif-void-170", Design.Void, false, 1f)
    @Test fun notificacoesMeiaLarguraFonteGrande() = withActions(170, "notif-void-170-fonte115", Design.Void, true, 1.15f)
    @Test fun notificacoesRotina() = withActions(170, "notif-rotina-170", Design.Rotina, false, 1f)

    @Test fun mesVoid() = shot("mes-void", Design.Void, false, 1f, 360) { MonthPage(System.currentTimeMillis(), {}, setOf(2, 15, 19, 20)) }
    @Test fun mesFonteGrande() = shot("mes-void-fonte115", Design.Void, true, 1.15f, 320) { MonthPage(System.currentTimeMillis(), {}, setOf(2, 15, 19, 20)) }
    @Test fun mesRotina() = shot("mes-rotina", Design.Rotina, false, 1f, 360) { MonthPage(System.currentTimeMillis(), {}, setOf(2, 15)) }

    @Test fun pausa() = shot("pausa", Design.Void, false, 1f, 360) {
        Box(Modifier.fillMaxSize()) { PauseScreen("TikTok", "você abriu 4 vezes hoje", {}, {}) }
    }
}
