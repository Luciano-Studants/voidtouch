package com.voidtouch.launcher

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.net.Uri
import android.provider.Settings
import com.voidtouch.launcher.data.AppEntry
import com.voidtouch.launcher.data.Hit
import com.voidtouch.launcher.data.QuickAction

/**
 * Ações da home que só falam com o sistema (abrir telas, lanterna, painel). Nenhuma mexe no estado
 * da tela; cada falha vira um aviso em vez de derrubar o app.
 */
internal class SystemActions(private val context: Context, private val say: (String) -> Unit) {
    private var torchOn = false

    private val torchCallback = object : CameraManager.TorchCallback() {
        override fun onTorchModeChanged(cameraId: String, enabled: Boolean) {
            torchOn = enabled
        }
    }

    fun startTorchWatch() {
        runCatching { context.getSystemService(CameraManager::class.java).registerTorchCallback(torchCallback, null) }
    }

    fun stopTorchWatch() {
        runCatching { context.getSystemService(CameraManager::class.java).unregisterTorchCallback(torchCallback) }
    }

    fun openNotificationAccess() {
        runCatching { context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)) }
    }

    fun openAppInfo() = openPackageInfo(context.packageName)

    fun openPackageInfo(pkg: String) {
        runCatching {
            context.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", pkg, null)))
        }
    }

    fun uninstall(app: AppEntry) {
        runCatching {
            context.startActivity(Intent(Intent.ACTION_DELETE, Uri.fromParts("package", app.packageName, null)))
        }.onFailure { say("não foi possível desinstalar ${app.label}") }
    }

    /** Executa um resultado da busca que não é app. */
    fun openHit(hit: Hit, runAction: (QuickAction) -> Unit) {
        when (hit) {
            is Hit.Answer -> {
                val text = hit.title.substringAfter("= ", hit.title)
                runCatching {
                    (context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager)
                        .setPrimaryClip(ClipData.newPlainText("resultado", text))
                }.onSuccess { say("copiado: $text") }.onFailure { say("não foi possível copiar") }
            }
            is Hit.Action -> runAction(hit.action)
            is Hit.Setting -> runCatching { context.startActivity(Intent(hit.intentAction).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
                .onFailure { say("não foi possível abrir ${hit.title}") }
            is Hit.Contact -> runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(hit.uri)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
                .onFailure { say("não foi possível abrir o contato") }
        }
    }

    fun toggleTorch() {
        runCatching {
            val manager = context.getSystemService(CameraManager::class.java)
            val id = manager.cameraIdList.firstOrNull { cam ->
                val c = manager.getCameraCharacteristics(cam)
                c.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true &&
                    c.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_BACK
            } ?: return say("este aparelho não tem lanterna")
            manager.setTorchMode(id, !torchOn)
            say(if (torchOn) "lanterna desligada" else "lanterna ligada")
        }.onFailure { say("não foi possível usar a lanterna") }
    }

    /** Abre onde se troca a tela inicial padrão; tenta caminhos mais genéricos se o aparelho não tiver o primeiro. */
    fun openHomeSettings() {
        val options = listOf(
            Intent(Settings.ACTION_HOME_SETTINGS),
            Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS),
            Intent(Settings.ACTION_SETTINGS),
        )
        for (i in options) if (runCatching { context.startActivity(i) }.isSuccess) return
        say("não foi possível abrir as configurações")
    }

    /** Abre o painel de notificações do sistema (método interno do Android; pode não existir em todo aparelho). */
    fun expandNotificationPanel() {
        runCatching {
            val service = context.getSystemService("statusbar")
            Class.forName("android.app.StatusBarManager").getMethod("expandNotificationsPanel").invoke(service)
        }.onFailure { say("este aparelho não permite abrir o painel por aqui") }
    }
}
