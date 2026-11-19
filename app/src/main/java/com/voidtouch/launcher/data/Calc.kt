package com.voidtouch.launcher.data

import java.util.Locale

/**
 * Calculadora da busca: contas com + - * / ^, parênteses, % e frases como "15% de 230".
 * Só responde quando o texto é mesmo uma conta (tem número e operador); um nome de app como
 * "2048" não vira resposta.
 */
object Calc {
    private val percentOf = Regex("""^([\d.,]+)\s*%\s*(?:de|of)\s*([\d.,]+)$""")

    /** O resultado já formatado (vírgula decimal), ou nulo se o texto não for uma conta válida. */
    fun answer(text: String): String? {
        val t = text.trim().lowercase().replace('×', '*').replace('÷', '/').replace(" x ", "*")
        if (t.isEmpty() || !t.any { it.isDigit() }) return null
        percentOf.matchEntire(t)?.let { m ->
            val p = number(m.groupValues[1]) ?: return null
            val base = number(m.groupValues[2]) ?: return null
            return format(p * base / 100.0)
        }
        if (t.none { it in "+-*/^()%" } && !Regex("""\dx\d""").containsMatchIn(t)) return null
        val clean = t.replace("x", "*").replace(" ", "")
        if (clean.any { !(it.isDigit() || it in "+-*/^()%.,") }) return null
        val value = runCatching { Parser(clean).parse() }.getOrNull() ?: return null
        if (value.isNaN() || value.isInfinite()) return null
        return format(value)
    }

    /** Com vírgula, o ponto é milhar ("1.234,5"); sem vírgula, o ponto é decimal ("2.5"). */
    private fun number(s: String): Double? =
        if (',' in s) s.replace(".", "").replace(',', '.').toDoubleOrNull() else s.toDoubleOrNull()

    fun format(value: Double): String {
        if (value == Math.rint(value) && kotlin.math.abs(value) < 1e15) return String.format(Locale.US, "%.0f", value)
        val s = String.format(Locale.US, "%.6f", value).trimEnd('0').trimEnd('.')
        return s.replace('.', ',')
    }

    private class Parser(private val s: String) {
        private var i = 0

        fun parse(): Double {
            val v = expr()
            if (i != s.length) throw IllegalArgumentException()
            return v
        }

        private fun peek(): Char? = s.getOrNull(i)

        private fun expr(): Double {
            var v = term()
            while (true) {
                v = when (peek()) {
                    '+' -> { i++; v + term() }
                    '-' -> { i++; v - term() }
                    else -> return v
                }
            }
        }

        private fun term(): Double {
            var v = power()
            while (true) {
                v = when (peek()) {
                    '*' -> { i++; v * power() }
                    '/' -> { i++; val d = power(); if (d == 0.0) throw ArithmeticException(); v / d }
                    else -> return v
                }
            }
        }

        private fun power(): Double {
            val base = unary()
            if (peek() == '^') {
                i++
                return Math.pow(base, power())
            }
            return base
        }

        private fun unary(): Double {
            if (peek() == '-') { i++; return -unary() }
            if (peek() == '+') { i++; return unary() }
            return postfix()
        }

        private fun postfix(): Double {
            var v = primary()
            while (peek() == '%') { i++; v /= 100.0 }
            return v
        }

        private fun primary(): Double {
            if (peek() == '(') {
                i++
                val v = expr()
                if (peek() != ')') throw IllegalArgumentException()
                i++
                return v
            }
            val start = i
            while (peek()?.let { it.isDigit() || it == '.' || it == ',' } == true) i++
            if (start == i) throw IllegalArgumentException()
            return number(s.substring(start, i)) ?: throw IllegalArgumentException()
        }
    }
}
