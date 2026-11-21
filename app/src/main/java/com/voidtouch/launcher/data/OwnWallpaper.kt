package com.voidtouch.launcher.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.File

/**
 * Imagem escolhida pelo usuário para o fundo do VoidTouch. Funciona em qualquer Android, porque não
 * depende de ler o papel de parede do sistema (que muitos aparelhos recusam a apps comuns).
 * A imagem é reduzida ao tamanho da tela e guardada só no armazenamento privado do app.
 */
object OwnWallpaper {
    private fun file(context: Context) = File(context.filesDir, "own_wallpaper.jpg")

    fun exists(context: Context): Boolean = file(context).isFile

    /** Muda quando a imagem muda (serve de chave para o cache do tom). */
    fun stamp(context: Context): Int = file(context).lastModified().hashCode()

    fun remove(context: Context) {
        file(context).delete()
    }

    /** Lê a imagem escolhida já reduzida e grava a cópia. Chamar fora da thread principal. */
    fun import(context: Context, uri: Uri, width: Int, height: Int): Boolean = runCatching {
        val resolver = context.contentResolver
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return false
        val opts = BitmapFactory.Options().apply {
            inSampleSize = WallpaperLoader.sampleSize(bounds.outWidth, bounds.outHeight, width, height)
            inPreferredConfig = Bitmap.Config.RGB_565
        }
        val source = resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, opts) } ?: return false
        val fitted = WallpaperLoader.fromBitmap(source, width, height).bitmap
        val tmp = File(context.filesDir, "own_wallpaper.tmp")
        tmp.outputStream().use { fitted.compress(Bitmap.CompressFormat.JPEG, 90, it) }
        fitted.recycle()
        file(context).delete()
        tmp.renameTo(file(context))
    }.getOrDefault(false)

    fun load(context: Context, width: Int, height: Int): LoadedWallpaper? = runCatching {
        val f = file(context)
        if (!f.isFile) return null
        val opts = BitmapFactory.Options().apply { inPreferredConfig = Bitmap.Config.RGB_565 }
        val source = BitmapFactory.decodeFile(f.absolutePath, opts) ?: return null
        WallpaperLoader.fromBitmap(source, width, height)
    }.getOrNull()
}
