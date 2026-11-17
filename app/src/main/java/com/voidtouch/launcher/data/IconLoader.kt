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
import androidx.core.content.ContextCompat
import java.util.concurrent.ConcurrentHashMap

/** Estado do ícone de um app. [Ready] com bitmap nulo significa "usar a letra em círculo". */
sealed interface IconResult {
    data object Loading : IconResult
    data class Ready(val bitmap: ImageBitmap?) : IconResult
}

/**
 * Gera ícones brancos e finos. A ordem de tentativa é:
 * 1. logotipo próprio do app ([BrandIcons]);
 * 2. camada monocromática que o Android 13+ oferece (a mesma dos ícones temáticos);
 * 3. nenhum, e a interface mostra a letra inicial em um círculo.
 * Só os favoritos pedem ícone, então o cache é pequeno.
 */
object IconLoader {

    /** Tamanho do bitmap. A home mostra o ícone com 26 dp, então não precisa mais que isso. */
    private const val SIZE_DP = 36

    /** Margem dos logotipos, para ficarem do mesmo tamanho visual dos ícones do sistema. */
    private const val BRAND_INSET = 0.14f

    private val cache = ConcurrentHashMap<String, IconResult.Ready>()

    fun peek(key: String): IconResult = cache[key] ?: IconResult.Loading

    fun load(context: Context, app: AppEntry): IconResult.Ready =
        cache.getOrPut(app.key) { IconResult.Ready(render(context, app)) }

    private fun render(context: Context, app: AppEntry): ImageBitmap? {
        val px = (SIZE_DP * context.resources.displayMetrics.density).toInt().coerceAtLeast(1)
        val brand = BrandIcons.resFor(app.packageName)?.let { renderBrand(context, it, px) }
        return brand ?: renderMonochrome(context, app, px)
    }

    private fun renderBrand(context: Context, resId: Int, px: Int): ImageBitmap? = runCatching {
        val drawable = ContextCompat.getDrawable(context, resId)?.mutate()
            ?: return@runCatching null
        val inset = (px * BRAND_INSET).toInt()
        val bitmap = Bitmap.createBitmap(px, px, Bitmap.Config.ARGB_8888)
        drawable.setBounds(inset, inset, px - inset, px - inset)
        drawable.draw(Canvas(bitmap))
        bitmap.asImageBitmap()
    }.getOrNull()

    private fun renderMonochrome(context: Context, app: AppEntry, px: Int): ImageBitmap? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return null
        return runCatching {
            val icon = context.packageManager
                .getActivityIcon(ComponentName(app.packageName, app.activityName))
            val mono = (icon as? AdaptiveIconDrawable)?.monochrome ?: return@runCatching null

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
