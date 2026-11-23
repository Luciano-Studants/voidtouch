package com.voidtouch.launcher.data

/**
 * Decide, pelas leituras do sensor de luz, quando ligar e desligar a alta visibilidade.
 * Histerese (liga acima de [ON_LUX], desliga abaixo de [OFF_LUX]) e [NEEDED] leituras seguidas,
 * para uma sombra passando não ficar piscando o visual.
 */
object LightAuto {
    const val ON_LUX = 12000f
    const val OFF_LUX = 5000f
    const val NEEDED = 3

    data class State(val on: Boolean = false, val streak: Int = 0)

    /** Próximo estado depois de uma leitura. */
    fun next(state: State, lux: Float): State {
        val keep = if (state.on) lux > OFF_LUX else lux < ON_LUX
        // Leitura que mantém o estado zera a contagem; as que pedem a troca vão somando.
        if (keep) return State(state.on, 0)
        val streak = state.streak + 1
        return if (streak >= NEEDED) State(!state.on, 0) else State(state.on, streak)
    }
}
