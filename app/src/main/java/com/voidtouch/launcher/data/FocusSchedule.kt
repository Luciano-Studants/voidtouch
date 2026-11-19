package com.voidtouch.launcher.data

/** O que fazer com o modo foco quando o horário programado muda. */
enum class FocusAction { None, TurnOn, TurnOff }

object FocusSchedule {
    const val DEFAULT_START = 18
    const val DEFAULT_END = 0

    /**
     * Decide só nas viradas da janela: entrou nela liga, saiu desliga. Sem mudança não faz nada,
     * então ligar ou desligar o foco à mão dentro da janela não é desfeito. [lastApplied] nulo
     * significa que a programação acabou de ser ligada: dentro da janela liga, fora não faz nada.
     */
    fun decide(inWindow: Boolean, lastApplied: Boolean?): FocusAction = when {
        lastApplied == inWindow -> FocusAction.None
        inWindow -> FocusAction.TurnOn
        lastApplied == null -> FocusAction.None
        else -> FocusAction.TurnOff
    }
}
