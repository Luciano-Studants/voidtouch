package com.voidtouch.launcher.data

import android.app.usage.StorageStatsManager
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.net.Uri
import android.os.Process
import android.os.storage.StorageManager
import android.provider.Settings

/** App que não é aberto faz tempo e ocupa espaço. */
data class IdleApp(val app: AppEntry, val daysIdle: Int, val totalBytes: Long)

sealed interface OptimizeData {
    data object NeedsPermission : OptimizeData
    data class Ready(
        val cacheTotalBytes: Long,
        val topCache: List<AppSize>,
        val idle: List<IdleApp>,
    ) : OptimizeData
}

/**
 * Lê o que dá para otimizar. O Android não deixa um launcher apagar o cache dos outros apps nem
 * fechá-los, então aqui só se mede e se leva a pessoa direto à tela certa do sistema.
 */
object Optimizer {

    private const val IDLE_DAYS = 30

    /** Uma chamada ao sistema por app: rodar fora da thread principal e só com a tela aberta. */
    fun read(context: Context, apps: List<AppEntry>): OptimizeData {
        if (!HeavyAppsReader.hasUsageAccess(context)) return OptimizeData.NeedsPermission
        val stats = context.getSystemService(StorageStatsManager::class.java) ?: return OptimizeData.NeedsPermission
        val usage = context.getSystemService(UsageStatsManager::class.java)
        val user = Process.myUserHandle()
        val now = System.currentTimeMillis()
        val lastUsed = runCatching {
            usage?.queryAndAggregateUsageStats(now - 90L * DAY, now)
        }.getOrNull().orEmpty()

        val sizes = apps.distinctBy { it.packageName }.mapNotNull { app ->
            runCatching {
                val s = stats.queryStatsForPackage(StorageManager.UUID_DEFAULT, app.packageName, user)
                AppSize(app, s.appBytes + s.dataBytes, s.cacheBytes)
            }.getOrNull()
        }

        val pm = context.packageManager
        val idle = sizes.mapNotNull { size ->
            val info = runCatching { pm.getApplicationInfo(size.app.packageName, 0) }.getOrNull() ?: return@mapNotNull null
            // Só apps instalados pelo usuário: apps de sistema não devem ser sugeridos para remoção.
            if (info.flags and ApplicationInfo.FLAG_SYSTEM != 0) return@mapNotNull null
            val last = lastUsed[size.app.packageName]?.lastTimeUsed ?: 0L
            val days = if (last <= 0L) 90 else ((now - last) / DAY).toInt()
            if (days >= IDLE_DAYS) IdleApp(size.app, days, size.totalBytes) else null
        }.sortedByDescending { it.totalBytes }

        return OptimizeData.Ready(
            cacheTotalBytes = sizes.sumOf { it.cacheBytes },
            topCache = sizes.filter { it.cacheBytes > 0 }.sortedByDescending { it.cacheBytes }.take(10),
            idle = idle.take(10),
        )
    }

    private const val DAY = 24L * 60L * 60L * 1000L

    /**
     * Tenta o pedido do sistema para limpar o cache de todos os apps. Em muitos aparelhos o Android
     * recusa esse pedido para apps comuns; devolve falso e quem chamou abre o armazenamento do sistema.
     */
    fun requestClearAllCache(context: Context): Boolean = runCatching {
        context.startActivity(
            Intent(StorageManager.ACTION_CLEAR_APP_CACHE).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
        )
        true
    }.getOrDefault(false)

    fun openStorage(context: Context) = open(context, Intent(Settings.ACTION_INTERNAL_STORAGE_SETTINGS))

    fun openAppInfo(context: Context, pkg: String) =
        open(context, Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", pkg, null)))

    fun openBatteryOptimization(context: Context) =
        open(context, Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS))

    fun openDeveloperOptions(context: Context) =
        open(context, Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS))

    private fun deviceCareIntent() = Intent().setClassName(
        "com.samsung.android.lool",
        "com.samsung.android.sm.ui.cstyleboard.SmartManagerDashBoardActivity",
    )

    /** Verdadeiro só em aparelhos Samsung com "Cuidados com o dispositivo" (One UI). */
    fun hasDeviceCare(context: Context): Boolean = runCatching {
        deviceCareIntent().resolveActivity(context.packageManager) != null
    }.getOrDefault(false)

    /** Cuidados com o dispositivo da Samsung (One UI), se existir; senão a tela de bateria do sistema. */
    fun openDeviceCare(context: Context) {
        if (!open(context, deviceCareIntent())) open(context, Intent(Intent.ACTION_POWER_USAGE_SUMMARY))
    }

    private fun open(context: Context, intent: Intent): Boolean = runCatching {
        context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        true
    }.getOrDefault(false)
}
