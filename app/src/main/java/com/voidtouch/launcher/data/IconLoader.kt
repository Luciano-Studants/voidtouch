package com.voidtouch.launcher.data

import android.content.ComponentName
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.AdaptiveIconDrawable
import android.os.Build
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import java.util.concurrent.ConcurrentHashMap

/** Estado do ícone de um app. [Ready] com bitmap nulo significa "usar a letra em círculo". */
sealed interface IconResult {
    data object Loading : IconResult
    data class Ready(val bitmap: ImageBitmap?) : IconResult
}

/**
 * Gera ícones brancos e finos a partir da camada monocromática que o Android 13+ oferece
 * (a mesma dos ícones temáticos). Apps sem essa camada ficam com a letra em círculo.
 * Só os favoritos pedem ícone, então o cache é pequeno.
 */
object IconLoader {

    private const val SIZE_DP = 72

    private val cache = ConcurrentHashMap<String, IconResult.Ready>()

    fun peek(key: String): IconResult = cache[key] ?: IconResult.Loading

    fun load(context: Context, app: AppEntry): IconResult.Ready =
        cache.getOrPut(app.key) { IconResult.Ready(render(context, app)) }

    private fun render(context: Context, app: AppEntry): ImageBitmap? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return null
        return runCatching {
            val icon = context.packageManager
                .getActivityIcon(ComponentName(app.packageName, app.activityName))
            val mono = (icon as? AdaptiveIconDrawable)?.monochrome ?: return null

            val px = (SIZE_DP * context.resources.displayMetrics.density).toInt().coerceAtLeast(1)
            // A camada tem 108 dp e só os 72 dp centrais aparecem; desenhamos recortando o centro.
            val full = px * 108f / 72f
            val offset = ((full - px) / 2f).toInt()

            val bitmap = Bitmap.createBitmap(px, px, Bitmap.Config.ARGB_8888)
            mono.mutate().apply {
                setBounds(-offset, -offset, px + offset, px + offset)
                colorFilter = PorterDuffColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN)
                draw(Canvas(bitmap))
            }
            bitmap.asImageBitmap()
        }.getOrNull()
    }
}
