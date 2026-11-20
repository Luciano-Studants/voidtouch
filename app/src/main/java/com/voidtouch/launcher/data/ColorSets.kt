package com.voidtouch.launcher.data

/** Um conjunto de cores: [tone] null = neutro; [auto] = tom tirado do papel de parede. */
data class ColorSet(val key: String, val name: String, val tone: Tone?, val auto: Boolean = false)

/**
 * Conjuntos de cores do VoidTouch. Todos mantêm o tema escuro e a luminosidade fixa (veja WallTone);
 * só o matiz muda, então a leitura é a mesma em qualquer um.
 */
object ColorSets {
    const val DEFAULT = "auto"

    val all: List<ColorSet> = listOf(
        ColorSet("auto", "automático (papel de parede)", null, auto = true),
        ColorSet("neutral", "neutro", null),
        ColorSet("amber", "âmbar", Tone(38f, 0.8f)),
        ColorSet("moss", "verde-musgo", Tone(120f, 0.6f)),
        ColorSet("ice", "azul-gelo", Tone(205f, 0.8f)),
        ColorSet("violet", "violeta", Tone(270f, 0.7f)),
        ColorSet("rose", "rosa", Tone(335f, 0.7f)),
    )

    fun fromKey(key: String?): ColorSet = all.firstOrNull { it.key == key } ?: all.first()

    /** Paleta do conjunto. [wallTone] é o tom do papel de parede (ou null); o automático só vale com o fundo em papel de parede. */
    fun palette(set: ColorSet, wallTone: Tone?, wallpaperBackdrop: Boolean): TonePalette = when {
        set.auto -> if (wallpaperBackdrop) WallTone.palette(wallTone) else WallTone.NEUTRAL
        else -> WallTone.palette(set.tone)
    }
}
