package com.voidtouch.launcher.data

import android.Manifest
import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Size
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.content.ContextCompat
import java.time.Instant
import java.time.ZoneId
import kotlin.random.Random

/** Com que frequência uma foto aparece na home. */
enum class MemoryMode(val key: String, val label: String) {
    Off("off", "desligado"),
    Sometimes("sometimes", "de vez em quando"),
    Always("always", "sempre que houver");

    fun next(): MemoryMode = entries[(ordinal + 1) % entries.size]

    companion object {
        fun fromKey(key: String?): MemoryMode = entries.firstOrNull { it.key == key } ?: Off
    }
}

/** Uma foto de um dia parecido com hoje, em anos ou meses anteriores. */
data class Memory(val id: Long, val uri: Uri, val label: String)

/** A foto escolhida já com a miniatura pronta para desenhar. */
data class MemoryShown(val memory: Memory, val bitmap: ImageBitmap)

/**
 * "Neste dia": procura na galeria fotos (de qualquer pasta, menos capturas de tela) tiradas por volta da data de hoje em anos
 * anteriores (e há 1, 3 e 6 meses). Lê só o que é preciso, uma vez por bloco de 3 horas e com a
 * tela aberta; as fotos nunca saem do aparelho.
 */
object MemoryReader {

    fun hasAccess(context: Context): Boolean {
        fun granted(p: String) = ContextCompat.checkSelfPermission(context, p) == PackageManager.PERMISSION_GRANTED
        return when {
            Build.VERSION.SDK_INT >= 33 ->
                granted(Manifest.permission.READ_MEDIA_IMAGES) ||
                    (Build.VERSION.SDK_INT >= 34 && granted("android.permission.READ_MEDIA_VISUAL_USER_SELECTED"))
            else -> granted(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }

    /** Permissões a pedir, conforme a versão do Android. */
    fun permissionsToRequest(): Array<String> = when {
        Build.VERSION.SDK_INT >= 34 -> arrayOf(
            Manifest.permission.READ_MEDIA_IMAGES,
            "android.permission.READ_MEDIA_VISUAL_USER_SELECTED",
        )
        Build.VERSION.SDK_INT >= 33 -> arrayOf(Manifest.permission.READ_MEDIA_IMAGES)
        else -> arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
    }

    /** Lado mínimo, em pixels, para a imagem contar como foto. */
    private const val MIN_SIDE = 600

    /** Trechos de pasta ou nome que indicam captura de tela (LIKE ignora maiúsculas). */
    private val EXCLUDED_WORDS = listOf("screenshot", "captura", "screen_rec", "screenrec")

    private const val DAY = 24L * 60L * 60L * 1000L

    /**
     * Escolhe a foto do momento. A escolha é fixa dentro de cada bloco de 3 horas (para a foto não
     * trocar a cada vez que você volta à home) e, no modo "de vez em quando", só 1 bloco em 3 mostra foto.
     */
    fun pick(context: Context, nowMillis: Long, mode: MemoryMode, hidden: Set<Long>): Memory? {
        if (mode == MemoryMode.Off || !hasAccess(context)) return null
        val zone = ZoneId.systemDefault()
        val now = Instant.ofEpochMilli(nowMillis).atZone(zone)
        val seed = now.toLocalDate().toEpochDay() * 8 + now.hour / 3
        val random = Random(seed)
        if (mode == MemoryMode.Sometimes && random.nextInt(3) != 0) return null

        val windows = buildList {
            for (years in 1..15) add(Triple(now.minusYears(years.toLong()), 3, if (years == 1) "há 1 ano" else "há $years anos"))
            add(Triple(now.minusMonths(1), 2, "há 1 mês"))
            add(Triple(now.minusMonths(3), 2, "há 3 meses"))
            add(Triple(now.minusMonths(6), 2, "há 6 meses"))
        }

        val found = ArrayList<Memory>()
        for ((center, spread, label) in windows) {
            val ms = center.toInstant().toEpochMilli()
            query(context, ms - spread * DAY, ms + spread * DAY).forEach { id ->
                if (id !in hidden) {
                    val uri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
                    found += Memory(id, uri, label)
                }
            }
        }
        return if (found.isEmpty()) null else found[random.nextInt(found.size)]
    }

    private fun query(context: Context, from: Long, to: Long): List<Long> = runCatching {
        // Qualquer foto serve, menos capturas de tela: ficam de fora as pastas e nomes de prints
        // (Screenshots, Captura de tela) e imagens muito pequenas (figurinhas, ícones, miniaturas).
        val pathColumn = if (Build.VERSION.SDK_INT >= 29) MediaStore.MediaColumns.RELATIVE_PATH else MediaStore.MediaColumns.DATA
        val name = MediaStore.Images.Media.DISPLAY_NAME
        val selection = buildString {
            append("${MediaStore.Images.Media.DATE_TAKEN} BETWEEN ? AND ?")
            append(" AND ${MediaStore.Images.Media.WIDTH} >= ${MIN_SIDE} AND ${MediaStore.Images.Media.HEIGHT} >= ${MIN_SIDE}")
            EXCLUDED_WORDS.forEach { append(" AND $pathColumn NOT LIKE ? AND $name NOT LIKE ?") }
        }
        val selectionArgs = buildList {
            add(from.toString())
            add(to.toString())
            EXCLUDED_WORDS.forEach { word ->
                add("%$word%")
                add("%$word%")
            }
        }.toTypedArray()
        val args = Bundle().apply {
            putString(ContentResolver.QUERY_ARG_SQL_SELECTION, selection)
            putStringArray(ContentResolver.QUERY_ARG_SQL_SELECTION_ARGS, selectionArgs)
            putStringArray(ContentResolver.QUERY_ARG_SORT_COLUMNS, arrayOf(MediaStore.Images.Media.DATE_TAKEN))
            putInt(ContentResolver.QUERY_ARG_SORT_DIRECTION, ContentResolver.QUERY_SORT_DIRECTION_DESCENDING)
            putInt(ContentResolver.QUERY_ARG_LIMIT, 12)
        }
        val ids = ArrayList<Long>()
        context.contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            arrayOf(MediaStore.Images.Media._ID),
            args,
            null,
        )?.use { c -> while (c.moveToNext()) ids += c.getLong(0) }
        ids
    }.getOrDefault(emptyList())

    /** Miniatura pequena (cerca de 300 px): a foto inteira nunca é carregada na memória. */
    fun thumbnail(context: Context, memory: Memory): ImageBitmap? = runCatching {
        val bitmap: Bitmap? = if (Build.VERSION.SDK_INT >= 29) {
            context.contentResolver.loadThumbnail(memory.uri, Size(320, 320), null)
        } else {
            val opts = BitmapFactory.Options().apply { inSampleSize = 8 }
            context.contentResolver.openInputStream(memory.uri)?.use { BitmapFactory.decodeStream(it, null, opts) }
        }
        bitmap?.asImageBitmap()
    }.getOrNull()
}
