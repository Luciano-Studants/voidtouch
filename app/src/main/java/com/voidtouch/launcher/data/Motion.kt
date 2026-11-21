package com.voidtouch.launcher.data

import android.content.Context
import android.provider.Settings

/** Respeita o "remover animações" do Android (acessibilidade): com ele ligado, o app não anima. */
object Motion {
    fun enabled(context: Context): Boolean = runCatching {
        Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) > 0f
    }.getOrDefault(true)
}
