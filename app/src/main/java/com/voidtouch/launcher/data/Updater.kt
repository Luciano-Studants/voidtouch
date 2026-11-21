package com.voidtouch.launcher.data

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
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
                val json = JSONObject(readLimited(conn.inputStream, UpdateCheckRules.MAX_JSON_BYTES))
                val version = json.getString("tag_name").removePrefix("v")
                val assets = json.getJSONArray("assets")
                var url: String? = null
                for (i in 0 until assets.length()) {
                    val a = assets.getJSONObject(i)
                    if (a.getString("name").endsWith(".apk")) url = a.getString("browser_download_url")
                }
                if (url != null && !UpdateCheckRules.urlAllowed(url)) {
                    return@withContext UpdateCheck.Failed("endereço de download não confiável")
                }
                when {
                    url == null -> UpdateCheck.NoRelease
                    Version.isNewer(version, installedVersion) -> UpdateCheck.Available(UpdateInfo(version, url))
                    else -> UpdateCheck.UpToDate
                }
            } finally {
                conn.disconnect()
            }
        } catch (e: Exception) {
            UpdateCheck.Failed("sem conexão")
        }
    }

    /** Resultado do download: o arquivo conferido, ou o motivo de não ser seguro instalar. */
    sealed interface Download {
        data class Ready(val file: File) : Download
        data class Failed(val reason: String) : Download
    }

    /**
     * Baixa o APK para o cache, avisando o progresso (0 a 100). Grava em arquivo temporário e só
     * troca pelo final depois de conferir o tamanho, o pacote, a versão e a assinatura.
     */
    suspend fun download(context: Context, info: UpdateInfo, onProgress: (Int) -> Unit): Download =
        withContext(Dispatchers.IO) {
            val dir = File(context.cacheDir, "updates").apply { mkdirs() }
            val part = File(dir, "voidtouch.apk.part")
            val target = File(dir, "voidtouch.apk")
            try {
                part.delete()
                target.delete()
                if (!UpdateCheckRules.urlAllowed(info.apkUrl)) return@withContext Download.Failed("endereço de download não confiável")
                val conn = open(info.apkUrl)
                try {
                    if (conn.responseCode != 200) return@withContext Download.Failed("o servidor respondeu ${conn.responseCode}")
                    // Depois dos redirecionamentos, o arquivo ainda precisa vir de um servidor do GitHub.
                    if (!UpdateCheckRules.urlAllowed(conn.url.toString(), checkPath = false)) {
                        return@withContext Download.Failed("o download foi desviado para um servidor não confiável")
                    }
                    val total = conn.contentLengthLong
                    if (total > UpdateCheckRules.MAX_APK_BYTES) return@withContext Download.Failed("arquivo grande demais para ser o app")
                    var done = 0L
                    var last = -1
                    conn.inputStream.use { input ->
                        part.outputStream().use { out ->
                            val buf = ByteArray(32 * 1024)
                            while (true) {
                                val n = input.read(buf)
                                if (n < 0) break
                                out.write(buf, 0, n)
                                done += n
                                if (done > UpdateCheckRules.MAX_APK_BYTES) {
                                    part.delete()
                                    return@withContext Download.Failed("arquivo grande demais para ser o app")
                                }
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
                    if (!UpdateCheckRules.sizeOk(done, total)) {
                        part.delete()
                        return@withContext Download.Failed("download incompleto, tente de novo")
                    }
                } finally {
                    conn.disconnect()
                }
                val problem = verify(context, part, info)
                if (problem != null) {
                    part.delete()
                    return@withContext Download.Failed(problem)
                }
                if (!part.renameTo(target)) return@withContext Download.Failed("não foi possível preparar o arquivo")
                Download.Ready(target)
            } catch (e: Exception) {
                part.delete()
                Download.Failed("falha no download, tente de novo")
            }
        }

    /** Confere o APK baixado contra o app instalado. Devolve o motivo da recusa, ou nulo se estiver ok. */
    @Suppress("DEPRECATION")
    private fun verify(context: Context, apk: File, info: UpdateInfo): String? {
        val pm = context.packageManager
        val flags = PackageManager.GET_SIGNING_CERTIFICATES
        val archive = pm.getPackageArchiveInfo(apk.absolutePath, flags) ?: return "o arquivo baixado não é um app válido"
        if (archive.packageName != context.packageName) return "o arquivo baixado é de outro app"
        val installed = pm.getPackageInfo(context.packageName, flags)
        if (archive.longVersionCode <= installed.longVersionCode) return "o arquivo baixado não é mais novo que o instalado"
        if (!UpdateCheckRules.versionMatches(archive.versionName, info.version)) return "a versão do arquivo não confere com a anunciada"
        val newSigs = archive.signingInfo?.apkContentsSigners?.map { it.toByteArray().contentHashCode() }?.toSet().orEmpty()
        val oldSigs = installed.signingInfo?.apkContentsSigners?.map { it.toByteArray().contentHashCode() }?.toSet().orEmpty()
        if (newSigs.isEmpty() || newSigs != oldSigs) return "a assinatura do arquivo não confere com a do app instalado"
        return null
    }

    /** Verdadeiro se o sistema já deixa o VoidTouch instalar apps. */
    fun canInstall(context: Context): Boolean = context.packageManager.canRequestPackageInstalls()

    /** Abre a tela do Android onde se libera "instalar apps desconhecidos" para o VoidTouch. */
    fun openInstallPermission(context: Context): Boolean = runCatching {
        context.startActivity(
            Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, Uri.parse("package:${context.packageName}"))
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
        )
        true
    }.getOrDefault(false)

    /** Abre o instalador do Android com o APK baixado. */
    fun install(context: Context, apk: File): Boolean = runCatching {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.updates", apk)
        context.startActivity(
            Intent(Intent.ACTION_VIEW)
                .setDataAndType(uri, "application/vnd.android.package-archive")
                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK),
        )
        true
    }.getOrDefault(false)

    private fun readLimited(input: java.io.InputStream, max: Int): String {
        val buf = ByteArray(max + 1)
        var n = 0
        while (n < buf.size) {
            val r = input.read(buf, n, buf.size - n)
            if (r < 0) break
            n += r
        }
        require(n <= max) { "resposta grande demais" }
        return String(buf, 0, n, Charsets.UTF_8)
    }

    private fun open(url: String): HttpURLConnection =
        (URL(url).openConnection() as HttpURLConnection).apply {
            connectTimeout = 10_000
            readTimeout = 20_000
            instanceFollowRedirects = true
            setRequestProperty("Accept", "application/vnd.github+json")
        }
}

/** Regras puras da conferência do download, separadas para poderem ser testadas. */
object UpdateCheckRules {
    const val MAX_JSON_BYTES = 1_000_000
    const val MAX_APK_BYTES = 80L * 1024 * 1024
    private const val REPO_PATH = "/Luciano-Studants/voidtouch/"
    private val HOSTS = setOf("github.com", "objects.githubusercontent.com", "release-assets.githubusercontent.com")

    /** Só https e só servidores do GitHub; o endereço inicial ainda precisa ser deste repositório. */
    fun urlAllowed(url: String, checkPath: Boolean = true): Boolean {
        val uri = runCatching { java.net.URI(url) }.getOrNull() ?: return false
        if (uri.scheme != "https" || uri.userInfo != null) return false
        val host = uri.host?.lowercase() ?: return false
        if (host !in HOSTS) return false
        return !checkPath || (host == "github.com" && uri.path.orEmpty().startsWith(REPO_PATH))
    }

    /** O tamanho baixado precisa bater com o anunciado; sem tamanho anunciado, só exige algo baixado. */
    fun sizeOk(downloaded: Long, announced: Long): Boolean =
        downloaded > 0 && (announced <= 0 || downloaded == announced)

    fun versionMatches(apkVersion: String?, announced: String): Boolean =
        apkVersion != null && apkVersion.removePrefix("v") == announced.removePrefix("v")
}
