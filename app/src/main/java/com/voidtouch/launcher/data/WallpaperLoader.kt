package com.voidtouch.launcher.data

import android.app.WallpaperManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint

/** Papel de parede já reduzido ao tamanho da tela e o tom dominante dele. */
class LoadedWallpaper(val bitmap: Bitmap, val tone: Tone?)

/**
 * Lê o papel de parede sem pesar: tenta decodificar o arquivo já em tamanho reduzido (nunca a imagem
 * inteira) e só cai no método do sistema se o arquivo não estiver acessível. Chamar fora da thread principal.
 */
object WallpaperLoader {
    /** Maior fator de redução (potência de 2) que ainda deixa a imagem com pelo menos o tamanho do alvo. */
    fun sampleSize(srcW: Int, srcH: Int, targetW: Int, targetH: Int): Int {
        if (srcW <= 0 || srcH <= 0 || targetW <= 0 || targetH <= 0) return 1
        var s = 1
        while (srcW / (s * 2) >= targetW && srcH / (s * 2) >= targetH) s *= 2
        return s
    }

    /** Identificador que muda quando o papel de parede muda (para guardar o tom em cache). */
    fun id(context: Context): Int = runCatching {
        WallpaperManager.getInstance(context).getWallpaperId(WallpaperManager.FLAG_SYSTEM)
    }.getOrDefault(-1)

    /**
     * Tom do papel de parede pelas cores que o próprio Android já calcula (Android 8.1 ou mais novo).
     * Não precisa de permissão nem lê a imagem, por isso funciona em todos os aparelhos.
     */
    fun colorsTone(context: Context): Tone? = runCatching {
        if (android.os.Build.VERSION.SDK_INT < 27) return null
        val c = WallpaperManager.getInstance(context).getWallpaperColors(WallpaperManager.FLAG_SYSTEM) ?: return null
        val list = listOfNotNull(c.primaryColor, c.secondaryColor, c.tertiaryColor).map { it.toArgb() }
        WallTone.fromPixels(list.toIntArray())
    }.getOrNull()

    /** Retângulo de destino [esquerda, topo, direita, base] que cobre a tela inteira, centralizado (como "centralizar e cortar"). */
    fun coverRect(srcW: Int, srcH: Int, width: Int, height: Int): FloatArray {
        if (srcW <= 0 || srcH <= 0) return floatArrayOf(0f, 0f, width.toFloat(), height.toFloat())
        val scale = maxOf(width.toFloat() / srcW, height.toFloat() / srcH)
        val dw = srcW * scale
        val dh = srcH * scale
        return floatArrayOf((width - dw) / 2f, (height - dh) / 2f, (width + dw) / 2f, (height + dh) / 2f)
    }

    /** Ajusta qualquer imagem ao tamanho da tela e tira o tom dominante dela. Libera [source]. */
    fun fromBitmap(source: Bitmap, width: Int, height: Int): LoadedWallpaper {
        val screen = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
        val r = coverRect(source.width, source.height, width, height)
        Canvas(screen).drawBitmap(source, null, android.graphics.RectF(r[0], r[1], r[2], r[3]), Paint(Paint.FILTER_BITMAP_FLAG))
        source.recycle()
        val n = 32
        val thumb = Bitmap.createScaledBitmap(screen, n, n, true)
        val px = IntArray(n * n)
        thumb.getPixels(px, 0, n, 0, 0, n, n)
        if (thumb !== screen) thumb.recycle()
        return LoadedWallpaper(screen, WallTone.fromPixels(px))
    }

    @android.annotation.SuppressLint("MissingPermission") // sem a permissão o Android recusa e devolvemos nulo
    fun load(context: Context, width: Int, height: Int): LoadedWallpaper? = runCatching {
        val source = decodeSampled(context, width, height) ?: return null
        fromBitmap(source, width, height)
    }.getOrNull()

    @android.annotation.SuppressLint("MissingPermission")
    private fun decodeSampled(context: Context, width: Int, height: Int): Bitmap? {
        val wm = WallpaperManager.getInstance(context)
        val fromFile = runCatching {
            wm.getWallpaperFile(WallpaperManager.FLAG_SYSTEM)?.use { pfd ->
                val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                BitmapFactory.decodeFileDescriptor(pfd.fileDescriptor, null, bounds)
                val opts = BitmapFactory.Options().apply {
                    inSampleSize = sampleSize(bounds.outWidth, bounds.outHeight, width, height)
                    inPreferredConfig = Bitmap.Config.RGB_565
                }
                BitmapFactory.decodeFileDescriptor(pfd.fileDescriptor, null, opts)
            }
        }.getOrNull()
        if (fromFile != null) return fromFile
        // Plano B: o método do sistema (decodifica a imagem inteira, por isso só como reserva).
        val d = runCatching { wm.drawable }.getOrNull() ?: return null
        val w = d.intrinsicWidth.coerceAtLeast(1)
        val h = d.intrinsicHeight.coerceAtLeast(1)
        val s = sampleSize(w, h, width, height)
        val bmp = Bitmap.createBitmap(w / s, h / s, Bitmap.Config.RGB_565)
        d.setBounds(0, 0, bmp.width, bmp.height)
        d.draw(Canvas(bmp))
        return bmp
    }
}
