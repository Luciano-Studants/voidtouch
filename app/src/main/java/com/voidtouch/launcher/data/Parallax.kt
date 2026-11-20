package com.voidtouch.launcher.data

/** Matemática da paralaxe: inclinação do celular (acelerômetro) vira deslocamento suave do papel de parede. */
object Parallax {
    /** Deslocamento máximo, em dp, para cada lado. O papel de parede é ampliado o bastante para cobrir. */
    const val MAX_DP = 16f

    /** Ampliação do papel de parede para as bordas nunca aparecerem. */
    const val OVERSCAN = 1.12f

    private const val GRAVITY = 9.81f
    private const val FULL_TILT = 0.5f // inclinação (fração da gravidade) que leva ao deslocamento máximo

    /** Converte a aceleração de um eixo em valor entre -1 e 1. */
    fun target(axis: Float): Float = (axis / GRAVITY / FULL_TILT).coerceIn(-1f, 1f)

    /** Filtro passa-baixa: [alpha] pequeno = movimento mais suave. */
    fun smooth(previous: Float, target: Float, alpha: Float = 0.12f): Float =
        previous + (target - previous) * alpha.coerceIn(0f, 1f)
}
