package com.voidtouch.launcher.data

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build

/** Um app instalado que pode ser aberto pelo launcher. */
data class AppEntry(
    val label: String,
    val packageName: String,
    val activityName: String,
) {
    /** Identificador estável do app, usado nos favoritos. */
    val key: String get() = "$packageName/$activityName"

    /** Nome sem acento e em minúsculas, calculado uma vez para a busca ser instantânea. */
    val searchLabel: String = Search.normalize(label)
}

object AppRepository {

    /** Lista os apps com ícone de launcher, em ordem alfabética, sem o próprio VoidTouch. */
    fun loadApps(context: Context): List<AppEntry> {
        val pm = context.packageManager
        val query = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)

        val resolved = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            pm.queryIntentActivities(query, PackageManager.ResolveInfoFlags.of(0))
        } else {
            @Suppress("DEPRECATION")
            pm.queryIntentActivities(query, 0)
        }

        return resolved
            .asSequence()
            .filter { it.activityInfo.packageName != context.packageName }
            .map {
                AppEntry(
                    label = it.loadLabel(pm).toString(),
                    packageName = it.activityInfo.packageName,
                    activityName = it.activityInfo.name,
                )
            }
            .sortedBy { it.label.lowercase() }
            .toList()
    }

    /**
     * Abre o app. Ignora silenciosamente se ele foi desinstalado nesse meio tempo. Com [origin],
     * a tela do app se abre a partir desse ponto, como nos launchers de ponta.
     */
    fun launch(context: Context, app: AppEntry, origin: OpenAnim.Rect? = null) {
        val intent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
            component = ComponentName(app.packageName, app.activityName)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
        }
        val options = origin?.let { r ->
            (context as? android.app.Activity)?.window?.decorView?.let { view ->
                runCatching { android.app.ActivityOptions.makeClipRevealAnimation(view, r.x, r.y, r.w, r.h).toBundle() }.getOrNull()
            }
        }
        runCatching { context.startActivity(intent, options) }
    }
}
