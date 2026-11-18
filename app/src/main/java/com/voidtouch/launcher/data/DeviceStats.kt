package com.voidtouch.launcher.data

import android.app.ActivityManager
import android.app.usage.StorageStatsManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.BatteryManager
import android.os.Environment
import android.os.StatFs
import android.os.storage.StorageManager

/** Tipo da conexão ativa, para a linha "rede" do status. */
enum class NetworkKind { WiFi, Mobile, Other, None }

/** Resumo do aparelho mostrado no bloco "status do celular" da home. */
data class DeviceStats(
    val batteryPercent: Int,
    val charging: Boolean,
    val temperatureC: Float?,
    val ramFreeMb: Long,
    val ramTotalMb: Long,
    val storageFreeBytes: Long,
    val network: NetworkKind,
)

object DeviceStatsReader {

    private const val MB = 1024L * 1024L

    /** Leitura rápida e pontual: não registra nada nem fica rodando em segundo plano. */
    fun read(context: Context): DeviceStats {
        // Com receiver nulo, o Android só devolve o último estado da bateria.
        val battery = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val level = battery?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = battery?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        val status = battery?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val tempTenths = battery?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, Int.MIN_VALUE) ?: Int.MIN_VALUE
        val percent = if (level >= 0 && scale > 0) level * 100 / scale else -1
        val charging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
            status == BatteryManager.BATTERY_STATUS_FULL

        val memory = ActivityManager.MemoryInfo()
        context.getSystemService(ActivityManager::class.java)?.getMemoryInfo(memory)

        return DeviceStats(
            batteryPercent = percent,
            charging = charging,
            temperatureC = if (tempTenths == Int.MIN_VALUE) null else tempTenths / 10f,
            ramFreeMb = memory.availMem / MB,
            ramTotalMb = memory.totalMem / MB,
            storageFreeBytes = readStorageFree(context),
            network = readNetwork(context),
        )
    }

    /** Espaço livre como a tela de Armazenamento do sistema mostra; se falhar, usa a partição de dados. */
    private fun readStorageFree(context: Context): Long = runCatching {
        context.getSystemService(StorageStatsManager::class.java)
            .getFreeBytes(StorageManager.UUID_DEFAULT)
    }.getOrElse {
        StatFs(Environment.getDataDirectory().path).availableBytes
    }

    private fun readNetwork(context: Context): NetworkKind = runCatching {
        val manager = context.getSystemService(ConnectivityManager::class.java)
            ?: return@runCatching NetworkKind.None
        val caps = manager.getNetworkCapabilities(manager.activeNetwork)
            ?: return@runCatching NetworkKind.None
        when {
            caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> NetworkKind.WiFi
            caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> NetworkKind.Mobile
            else -> NetworkKind.Other
        }
    }.getOrDefault(NetworkKind.None)
}
