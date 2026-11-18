package com.voidtouch.launcher.data

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

/** Versão publicada no GitHub, mais nova que a instalada. */
data class UpdateInfo(val version: String, val apkUrl: String)

sealed interface UpdateCheck {
    data class Available(val info: UpdateInfo) : UpdateCheck
    data object UpToDate : UpdateCheck
    data object NoRelease : UpdateCheck
    data class Failed(val reason: String) : UpdateCheck
}

/**
 * Atualização direto do GitHub. Nada roda sozinho: só consulta ou baixa quando o usuário toca.
 * A consulta usa a API pública das Releases, sem login.
 */
object Updater {
    private const val LATEST = "https://api.github.com/repos/Luciano-Studants/voidtouch/releases/latest"

    suspend fun check(installedVersion: String): UpdateCheck = withContext(Dispatchers.IO) {
        try {
            val conn = open(LATEST)
            try {
                when (conn.responseCode) {
                    404 -> return@withContext UpdateCheck.NoRelease
                    200 -> Unit
                    else -> return@withContext UpdateCheck.Failed("erro ${conn.responseCode}")
                }
                val json = JSONObject(conn.inputStream.bufferedReader().use { it.readText() })
                val version = json.getString("tag_name").removePrefix("v")
                val assets = json.getJSONArray("assets")
                var url: String? = null
                for (i in 0 until assets.length()) {
                    val a = assets.getJSONObject(i)
                    if (a.getString("name").endsWith(".apk")) url = a.getString("browser_download_url")
                }
                when {
                    url == null -> UpdateCheck.NoRelease
                    isNewer(version, installedVersion) -> UpdateCheck.Available(UpdateInfo(version, url))
                    else -> UpdateCheck.UpToDate
                }
            } finally {
                conn.disconnect()
            }
        } catch (e: Exception) {
            UpdateCheck.Failed("sem conexão")
        }
    }

    /** Baixa o APK para o cache, avisando o progresso (0 a 100). Devolve o arquivo, ou nulo se falhar. */
    suspend fun download(context: Context, info: UpdateInfo, onProgress: (Int) -> Unit): File? =
        withContext(Dispatchers.IO) {
            try {
                val dir = File(context.cacheDir, "updates").apply { mkdirs() }
                val target = File(dir, "voidtouch.apk")
                val conn = open(info.apkUrl)
                try {
                    if (conn.responseCode != 200) return@withContext null
                    val total = conn.contentLengthLong
                    var done = 0L
                    var last = -1
                    conn.inputStream.use { input ->
                        target.outputStream().use { out ->
                            val buf = ByteArray(32 * 1024)
                            while (true) {
                                val n = input.read(buf)
                                if (n < 0) break
                                out.write(buf, 0, n)
                                done += n
                                if (total > 0) {
                                    val pct = (done * 100 / total).toInt()
                                    if (pct != last) {
                                        last = pct
                                        onProgress(pct)
                                    }
                                }
                            }
                        }
                    }
                } finally {
                    conn.disconnect()
                }
                target
            } catch (e: Exception) {
                null
            }
        }

    /** Verdadeiro se o sistema já deixa o VoidTouch instalar apps. */
    fun canInstall(context: Context): Boolean = context.packageManager.canRequestPackageInstalls()

    /** Abre a tela do Android onde se libera "instalar apps desconhecidos" para o VoidTouch. */
    fun openInstallPermission(context: Context) {
        context.startActivity(
            Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, Uri.parse("package:${context.packageName}"))
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
        )
    }

    /** Abre o instalador do Android com o APK baixado. */
    fun install(context: Context, apk: File) {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.updates", apk)
        context.startActivity(
            Intent(Intent.ACTION_VIEW)
                .setDataAndType(uri, "application/vnd.android.package-archive")
                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK),
        )
    }

    private fun open(url: String): HttpURLConnection =
        (URL(url).openConnection() as HttpURLConnection).apply {
            connectTimeout = 10_000
            readTimeout = 20_000
            instanceFollowRedirects = true
            setRequestProperty("Accept", "application/vnd.github+json")
        }

    /** Compara "0.9.0" com "0.8.1" número a número. */
    internal fun isNewer(candidate: String, installed: String): Boolean {
        val a = candidate.split('.').map { it.toIntOrNull() ?: 0 }
        val b = installed.split('.').map { it.toIntOrNull() ?: 0 }
        for (i in 0 until maxOf(a.size, b.size)) {
            val x = a.getOrElse(i) { 0 }
            val y = b.getOrElse(i) { 0 }
            if (x != y) return x > y
        }
        return false
    }
}
