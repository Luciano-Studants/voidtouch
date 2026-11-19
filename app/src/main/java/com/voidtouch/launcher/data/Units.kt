package com.voidtouch.launcher.data

/** Conversão de unidades na busca: "10 km em mi", "100 f para c", "2 l em gal". */
object Units {
    private class Unit(val dim: String, val toBase: Double, val label: String)

    // Base: metro, grama, mililitro. Temperatura é tratada à parte.
    private val table: Map<String, Unit> = buildMap {
        fun add(u: Unit, vararg names: String) = names.forEach { put(it, u) }
        add(Unit("len", 1.0, "m"), "m", "metro", "metros")
        add(Unit("len", 1000.0, "km"), "km", "quilometro", "quilometros")
        add(Unit("len", 0.01, "cm"), "cm", "centimetro", "centimetros")
        add(Unit("len", 0.001, "mm"), "mm", "milimetro", "milimetros")
        add(Unit("len", 1609.344, "mi"), "mi", "milha", "milhas", "mile", "miles")
        add(Unit("len", 0.3048, "ft"), "ft", "pe", "pes", "foot", "feet")
        add(Unit("len", 0.0254, "in"), "in", "polegada", "polegadas", "inch")
        add(Unit("len", 0.9144, "yd"), "yd", "jarda", "jardas")
        add(Unit("mass", 1.0, "g"), "g", "grama", "gramas")
        add(Unit("mass", 1000.0, "kg"), "kg", "quilo", "quilos", "quilograma")
        add(Unit("mass", 453.59237, "lb"), "lb", "lbs", "libra", "libras")
        add(Unit("mass", 28.349523, "oz"), "oz", "onca", "oncas")
        add(Unit("vol", 1.0, "ml"), "ml", "mililitro", "mililitros")
        add(Unit("vol", 1000.0, "l"), "l", "litro", "litros")
        add(Unit("vol", 3785.411784, "gal"), "gal", "galao", "galoes")
        add(Unit("temp", 0.0, "°C"), "c", "°c", "celsius")
        add(Unit("temp", 0.0, "°F"), "f", "°f", "fahrenheit")
        add(Unit("temp", 0.0, "K"), "k", "kelvin")
    }

    private val pattern = Regex("""^([-\d.,]+)\s*([a-z°]+)\s+(?:em|to|in|para)\s+([a-z°]+)$""")

    /** Ex.: "10 km = 6,21 mi", ou nulo se o texto não for uma conversão que o app conheça. */
    fun answer(text: String): String? {
        val t = Search.normalize(text.trim())
        val m = pattern.matchEntire(t) ?: return null
        val value = m.groupValues[1].replace(',', '.').toDoubleOrNull() ?: return null
        val from = table[m.groupValues[2]] ?: return null
        val to = table[m.groupValues[3]] ?: return null
        if (from.dim != to.dim) return null
        val result = if (from.dim == "temp") {
            fromCelsius(toCelsius(value, from.label), to.label)
        } else {
            value * from.toBase / to.toBase
        }
        return "${Calc.format(value)} ${from.label} = ${Calc.format(round(result))} ${to.label}"
    }

    private fun round(v: Double): Double = Math.round(v * 100.0) / 100.0

    private fun toCelsius(v: Double, unit: String): Double = when (unit) {
        "°F" -> (v - 32) * 5 / 9
        "K" -> v - 273.15
        else -> v
    }

    private fun fromCelsius(c: Double, unit: String): Double = when (unit) {
        "°F" -> c * 9 / 5 + 32
        "K" -> c + 273.15
        else -> c
    }
}
