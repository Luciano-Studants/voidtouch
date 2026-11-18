package com.voidtouch.launcher.data

/** Uma versão e o que ela trouxe. */
data class ReleaseNote(val version: String, val items: List<String>)

/**
 * Histórico de novidades, da mais nova para a mais antiga. Mostrado uma vez depois de cada
 * atualização e guardado em Ajustes > sobre > novidades. Toda versão nova ganha uma entrada aqui.
 */
object Changelog {
    val all: List<ReleaseNote> = listOf(
        ReleaseNote(
            "0.9.0",
            listOf(
                "Notificações na home: as mais recentes aparecem na coluna da esquerda. Toque para abrir, segure para dispensar.",
                "Música no carrossel: título, artista e controles de anterior, tocar/pausar e próxima.",
                "Acesso às notificações em Ajustes, com atalho para liberar as configurações restritas.",
                "Com o modo foco ligado, notificações dos apps marcados ficam escondidas.",
                "Tela de novidades depois de cada atualização, guardada em Ajustes > sobre.",
            ),
        ),
        ReleaseNote(
            "0.8.1",
            listOf(
                "Atualização dentro do app: Ajustes > verificar atualização baixa a versão nova do GitHub.",
                "Assinatura fixa: as próximas versões instalam por cima, sem desinstalar.",
            ),
        ),
        ReleaseNote(
            "0.8.0",
            listOf(
                "Nova home: carrossel (hora, dia, música), notificações, status do celular e favoritos.",
                "Deslizar para esquerda ou direita abre apps que você escolhe em Ajustes.",
                "Tela de Ajustes: fundo, formato da hora e apps dos deslizes.",
                "Régua tracejada na lista de apps para pular por letra.",
            ),
        ),
        ReleaseNote("0.7.0", listOf("Modo foco: esconde os apps que distraem da home, da lista e da busca.")),
        ReleaseNote("0.6.0", listOf("Saúde do aparelho: memória, armazenamento, bateria e apps pesados.")),
        ReleaseNote("0.5.0", listOf("Ícones próprios em vetor para 43 apps, com letra como alternativa.")),
        ReleaseNote("0.4.0", listOf("Busca de apps e deslizar para cima para abrir a lista A–Z.")),
        ReleaseNote("0.3.0", listOf("Home com relógio, favoritos, RAM e bateria.")),
        ReleaseNote("0.2.0", listOf("Fontes (JetBrains Mono, Oxanium, Allura) e fundos preto, grafite e papel de parede.")),
    )

    fun forVersion(version: String): ReleaseNote? = all.firstOrNull { it.version == version }
}
