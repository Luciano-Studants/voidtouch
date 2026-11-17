package com.voidtouch.launcher.data

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager

/** Resumo do aparelho mostrado na home. */
data class DeviceStats(
    val batteryPercent: Int,
    val charging: Boolean,
    val ramFreeMb: Long,
    val ramTotalMb: Long,
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
        val percent = if (level >= 0 && scale > 0) level * 100 / scale else -1
        val charging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
            status == BatteryManager.BATTERY_STATUS_FULL

        val memory = ActivityManager.MemoryInfo()
        context.getSystemService(ActivityManager::class.java)?.getMemoryInfo(memory)

        return DeviceStats(
            batteryPercent = percent,
            charging = charging,
            ramFreeMb = memory.availMem / MB,
            ramTotalMb = memory.totalMem / MB,
        )
    }
}
