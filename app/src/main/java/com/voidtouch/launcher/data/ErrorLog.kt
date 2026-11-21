package com.voidtouch.launcher.data

import kotlin.coroutines.cancellation.CancellationException

/**
 * Registro dos erros que o app tratou sem travar (só na memória, até 20, os mais novos primeiro).
 * Aparece em Saúde do aparelho, para você ver o que deu errado sem o app ter fechado.
 */
object ErrorLog {
    const val MAX = 20
    private val lines = ArrayDeque<String>()

    fun format(tag: String, error: Throwable?): String {
        val kind = error?.javaClass?.simpleName?.takeIf { it.isNotBlank() } ?: "erro"
        val msg = error?.message?.takeIf { it.isNotBlank() }?.take(80)
        return if (msg == null) "$tag: $kind" else "$tag: $kind ($msg)"
    }

    @Synchronized
    fun note(tag: String, error: Throwable?) {
        lines.addFirst(format(tag, error))
        while (lines.size > MAX) lines.removeLast()
    }

    @Synchronized
    fun recent(): List<String> = lines.toList()

    @Synchronized
    fun clear() = lines.clear()
}

/**
 * Executa [block] e, se algo der errado (inclusive falta de memória), registra e devolve [fallback]
 * em vez de derrubar o app. Cancelamento de coroutine continua passando.
 */
inline fun <T> guarded(tag: String, fallback: T, block: () -> T): T =
    try {
        block()
    } catch (c: CancellationException) {
        throw c
    } catch (t: Throwable) {
        ErrorLog.note(tag, t)
        fallback
    }
