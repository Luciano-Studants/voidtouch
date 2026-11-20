package com.voidtouch.launcher.data

import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

/** Matiz (0..360) e saturação (0..1) dominantes de uma imagem. */
data class Tone(val hue: Float, val sat: Float)

/** Cores da interface já calculadas, em ARGB. */
data class TonePalette(val text: Int, val dim: Int, val line: Int, val veil: Int)

/**
 * Adapta o tom da interface ao papel de parede. Só o matiz vem da imagem: a luminosidade é fixa,
 * então o texto continua claro sobre o fundo escuro e a legibilidade não depende do papel de parede.
 */
object WallTone {
    val NEUTRAL = TonePalette(0xFFF2F2F2.toInt(), 0xFF8A8A8A.toInt(), 0xFF2A2A2A.toInt(), 0x8C000000.toInt())

    /** Acha a cor dominante entre pixels ARGB; devolve null se a imagem é praticamente cinza, preta ou branca. */
    fun fromPixels(px: IntArray): Tone? {
        if (px.isEmpty()) return null
        val buckets = 12
        val weight = DoubleArray(buckets)
        val sx = DoubleArray(buckets)
        val sy = DoubleArray(buckets)
        val satSum = DoubleArray(buckets)
        var kept = 0
        for (p in px) {
            val r = (p shr 16) and 0xFF
            val g = (p shr 8) and 0xFF
            val b = p and 0xFF
            val mx = max(r, max(g, b))
            val mn = min(r, min(g, b))
            val v = mx / 255.0
            val s = if (mx == 0) 0.0 else (mx - mn).toDouble() / mx
            if (v < 0.15 || s < 0.2) continue
            kept++
            val h = hue(r, g, b, mx, mn)
            val i = ((h / 30.0).toInt()) % buckets
            val w = s * v
            weight[i] += w
            val rad = Math.toRadians(h)
            sx[i] += cos(rad) * w
            sy[i] += sin(rad) * w
            satSum[i] += s * w
        }
        if (kept < px.size * 0.05) return null
        var best = 0
        for (i in 1 until buckets) if (weight[i] > weight[best]) best = i
        if (weight[best] <= 0.0) return null
        var h = Math.toDegrees(atan2(sy[best], sx[best]))
        if (h < 0) h += 360.0
        return Tone(h.toFloat(), (satSum[best] / weight[best]).toFloat())
    }

    private fun hue(r: Int, g: Int, b: Int, mx: Int, mn: Int): Double {
        val d = (mx - mn).toDouble()
        if (d == 0.0) return 0.0
        val h = when (mx) {
            r -> ((g - b) / d) % 6.0
            g -> (b - r) / d + 2.0
            else -> (r - g) / d + 4.0
        } * 60.0
        return if (h < 0) h + 360.0 else h
    }

    /** HSL (h 0..360, s e l 0..1) para ARGB opaco. */
    fun hslToArgb(h: Float, s: Float, l: Float): Int {
        val c = (1f - abs(2f * l - 1f)) * s
        val hp = (h % 360f) / 60f
        val x = c * (1f - abs(hp % 2f - 1f))
        val (r1, g1, b1) = when {
            hp < 1f -> Triple(c, x, 0f)
            hp < 2f -> Triple(x, c, 0f)
            hp < 3f -> Triple(0f, c, x)
            hp < 4f -> Triple(0f, x, c)
            hp < 5f -> Triple(x, 0f, c)
            else -> Triple(c, 0f, x)
        }
        val m = l - c / 2f
        fun ch(v: Float) = ((v + m) * 255f + 0.5f).toInt().coerceIn(0, 255)
        return (0xFF shl 24) or (ch(r1) shl 16) or (ch(g1) shl 8) or ch(b1)
    }

    fun palette(tone: Tone?): TonePalette {
        if (tone == null) return NEUTRAL
        val s = (tone.sat * 0.35f).coerceAtMost(0.30f)
        val veilRgb = hslToArgb(tone.hue, (tone.sat * 0.6f).coerceAtMost(0.5f), 0.07f) and 0x00FFFFFF
        return TonePalette(
            text = hslToArgb(tone.hue, s * 0.8f, 0.94f),
            dim = hslToArgb(tone.hue, s, 0.60f),
            line = hslToArgb(tone.hue, s, 0.18f),
            veil = (0x8C shl 24) or veilRgb,
        )
    }
}
