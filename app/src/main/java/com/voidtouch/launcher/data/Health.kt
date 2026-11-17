package com.voidtouch.launcher.data

import android.app.ActivityManager
import android.app.AppOpsManager
import android.app.usage.StorageStatsManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import android.os.Environment
import android.os.Process
import android.os.StatFs
import android.os.storage.StorageManager

/** Retrato do aparelho mostrado na tela "saúde do aparelho". Tudo em bytes, bateria em %. */
data class HealthSnapshot(
    val ramFreeBytes: Long,
    val ramTotalBytes: Long,
    val lowMemory: Boolean,
    val storageFreeBytes: Long,
    val storageTotalBytes: Long,
    val batteryPercent: Int,
    val charging: Boolean,
    val temperatureC: Float?,
    val batteryHealth: String?,
)

object HealthReader {

    /** Leitura pontual e barata: não registra nada nem fica rodando em segundo plano. */
    fun read(context: Context): HealthSnapshot {
        val memory = ActivityManager.MemoryInfo()
        context.getSystemService(ActivityManager::class.java)?.getMemoryInfo(memory)

        val (storageFree, storageTotal) = readStorage(context)

        // Com receiver nulo, o Android só devolve o último estado da bateria.
        val battery = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val level = battery?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = battery?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        val status = battery?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val tempTenths = battery?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, Int.MIN_VALUE) ?: Int.MIN_VALUE
        val health = battery?.getIntExtra(BatteryManager.EXTRA_HEALTH, -1) ?: -1

        return HealthSnapshot(
            ramFreeBytes = memory.availMem,
            ramTotalBytes = memory.totalMem,
            lowMemory = memory.lowMemory,
            storageFreeBytes = storageFree,
            storageTotalBytes = storageTotal,
            batteryPercent = if (level >= 0 && scale > 0) level * 100 / scale else -1,
            charging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL,
            temperatureC = if (tempTenths == Int.MIN_VALUE) null else tempTenths / 10f,
            batteryHealth = healthLabel(health),
        )
    }

    /** Livre e total, como a tela de Armazenamento do sistema mostra; se falhar, usa a partição de dados. */
    private fun readStorage(context: Context): Pair<Long, Long> = runCatching {
        val stats = context.getSystemService(StorageStatsManager::class.java)
        stats.getFreeBytes(StorageManager.UUID_DEFAULT) to stats.getTotalBytes(StorageManager.UUID_DEFAULT)
    }.getOrElse {
        val fs = StatFs(Environment.getDataDirectory().path)
        fs.availableBytes to fs.totalBytes
    }

    private fun healthLabel(code: Int): String? = when (code) {
        BatteryManager.BATTERY_HEALTH_GOOD -> "boa"
        BatteryManager.BATTERY_HEALTH_OVERHEAT -> "superaquecida"
        BatteryManager.BATTERY_HEALTH_DEAD -> "ruim"
        BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> "sobretensão"
        BatteryManager.BATTERY_HEALTH_COLD -> "fria"
        BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE -> "com falha"
        else -> null
    }
}

/** Espaço que um app ocupa: o app em si mais os dados (o cache já está contado nos dados). */
data class AppSize(
    val app: AppEntry,
    val totalBytes: Long,
    val cacheBytes: Long,
)

sealed interface HeavyApps {
    data object Loading : HeavyApps

    /** O Android só informa o tamanho dos outros apps com a permissão especial de acesso ao uso. */
    data object NeedsPermission : HeavyApps

    data class Ready(val top: List<AppSize>, val cacheTotalBytes: Long) : HeavyApps
}

object HeavyAppsReader {

    fun hasUsageAccess(context: Context): Boolean {
        val ops = context.getSystemService(AppOpsManager::class.java) ?: return false
        val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ops.unsafeCheckOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), context.packageName)
        } else {
            @Suppress("DEPRECATION")
            ops.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), context.packageName)
        }
        return mode == AppOpsManager.MODE_ALLOWED
    }

    /**
     * Mede os apps instalados (só os que têm ícone de launcher). É uma chamada ao sistema por
     * app, então deve rodar fora da thread principal e só com a tela de saúde aberta.
     */
    fun read(context: Context, apps: List<AppEntry>, limit: Int = 10): HeavyApps {
        if (!hasUsageAccess(context)) return HeavyApps.NeedsPermission

        val stats = context.getSystemService(StorageStatsManager::class.java)
            ?: return HeavyApps.NeedsPermission
        val user = Process.myUserHandle()

        val sizes = apps
            .distinctBy { it.packageName }
            .mapNotNull { app ->
                runCatching {
                    val s = stats.queryStatsForPackage(StorageManager.UUID_DEFAULT, app.packageName, user)
                    AppSize(app = app, totalBytes = s.appBytes + s.dataBytes, cacheBytes = s.cacheBytes)
                }.getOrNull()
            }

        return HeavyApps.Ready(
            top = sizes.sortedByDescending { it.totalBytes }.take(limit),
            cacheTotalBytes = sizes.sumOf { it.cacheBytes },
        )
    }
}
