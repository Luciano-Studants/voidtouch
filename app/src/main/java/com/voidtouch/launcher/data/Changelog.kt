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
            "0.23.0",
            listOf(
                "Painel de permissões em Ajustes > permissões: mostra se cada uma está ligada, para que serve e abre a configuração certa com um toque.",
                "Cobre notificações, fotos, calendário, acesso ao uso e instalar apps desconhecidos.",
            ),
        ),
        ReleaseNote(
            "0.22.0",
            listOf(
                "Organização interna: as regras de favoritos, foco e notificações saíram da tela principal e agora têm testes automáticos.",
                "Nada muda no uso; serve para cada versão nova não quebrar o que já funciona.",
            ),
        ),
        ReleaseNote(
            "0.21.0",
            listOf(
                "Atualização mais segura: o arquivo baixado é conferido antes de abrir o instalador (tamanho, pacote, versão e assinatura).",
                "Download interrompido ou arquivo errado não chega ao instalador; o app mostra o motivo e continua funcionando na versão atual.",
            ),
        ),
        ReleaseNote(
            "0.20.0",
            listOf(
                "Backup mais seguro: o arquivo é conferido por inteiro antes de restaurar (formato, versão, tamanhos e tipos).",
                "Se algo estiver errado, nada é alterado e o aviso diz o motivo; antes, um arquivo ruim podia deixar os ajustes pela metade.",
                "O backup agora guarda a data da exportação e recusa arquivos de versões mais novas do app.",
            ),
        ),
        ReleaseNote(
            "0.19.0",
            listOf(
                "Foco automático: em Ajustes, escolha o horário (padrão das 18h às 0h) e o modo foco liga e desliga sozinho.",
                "A troca acontece nas viradas do horário, então você pode ligar ou desligar à mão dentro da janela sem ser desfeito.",
                "Precisa de apps marcados em \"apps do foco\"; sem eles, o aviso lembra de marcar.",
            ),
        ),
        ReleaseNote(
            "0.18.1",
            listOf(
                "Limpeza de cache mais honesta: o Android não deixa um app comum apagar o cache dos outros sozinho, então o botão antigo foi trocado por dois caminhos que funcionam.",
                "Limpeza guiada: mostra o app com mais cache e abre a tela dele no sistema; depois de limpar e voltar, o próximo aparece, com opção de pular.",
                "Limpeza de todos de uma vez: abre Cuidados com o dispositivo da Samsung, e o VoidTouch confere quanto foi liberado ao voltar.",
            ),
        ),
        ReleaseNote(
            "0.18.0",
            listOf(
                "Nova página no carrossel: agenda. Mostra os próximos compromissos do calendário do celular (hoje, amanhã e os dias seguintes). Toque para abrir o calendário.",
                "A agenda pede permissão de calendário só quando você tocar em \"ativar agenda\"; os dados não saem do aparelho.",
            ),
        ),
        ReleaseNote(
            "0.17.0",
            listOf(
                "Modo noite: em Ajustes, escolha o horário (padrão das 22h às 6h). Nesse período a home fica mais escura e calma: some o bloco de status e notificações e a foto do dia.",
            ),
        ),
        ReleaseNote(
            "0.16.0",
            listOf(
                "Atalhos dos apps: ao segurar um app na lista, o menu mostra os atalhos do próprio app (como \"nova mensagem\" ou \"selfie\") no topo, e um toque abre direto.",
            ),
        ),
        ReleaseNote(
            "0.15.0",
            listOf(
                "Dicas de uso: uma tela rápida com todos os gestos aparece no primeiro uso e fica em Ajustes > dicas de uso.",
                "Backup dos ajustes: salve favoritos, pastas, apps do foco, gestos e ajustes em um arquivo e restaure quando quiser (útil ao trocar de celular).",
                "Ajustes > sobre mostra quanto tempo a última abertura levou, em milissegundos.",
            ),
        ),
        ReleaseNote(
            "0.14.3",
            listOf(
                "Correção: tocar no espaço vazio ao lado de um favorito não abre mais o app. Só o ícone e o nome respondem ao toque, e o resto da linha fica livre para o toque duplo e os deslizes.",
                "O mesmo vale para o bloco de status e para as notificações: só o texto responde.",
            ),
        ),
        ReleaseNote(
            "0.14.2",
            listOf(
                "Pastas podem ser fixadas nos favoritos da home: segure a pasta na lista > fixar na home.",
                "Na home, a pasta aparece com ícone de pasta e a quantidade de apps; tocar abre a lista já dentro dela.",
                "Segurar a pasta na home a tira dos favoritos. O limite de 10 favoritos vale para apps e pastas juntos.",
            ),
        ),
        ReleaseNote(
            "0.14.1",
            listOf(
                "Memórias do dia: agora vale qualquer foto da galeria (câmera, WhatsApp, downloads...), só ficam de fora capturas de tela e imagens muito pequenas.",
            ),
        ),
        ReleaseNote(
            "0.14.0",
            listOf(
                "Novos gestos na home: deslizar para baixo e toque duplo, além dos deslizes para os lados.",
                "Cada gesto abre um app ou faz uma ação rápida: lanterna, painel de notificações, buscar app, lista de apps, foco, otimizar, saúde ou ajustes.",
                "Deslizar para baixo já vem abrindo o painel de notificações.",
                "Ícones preparados em segundo plano: a grade de apps rola mais lisa.",
                "A lista de apps só é relida quando algo é instalado ou removido, e não a cada volta à home: a home abre mais rápido.",
                "Toque leve ao arrastar a régua por letras.",
            ),
        ),
        ReleaseNote(
            "0.13.0",
            listOf(
                "Pastas na lista de apps: segure um app > adicionar à pasta. As pastas aparecem primeiro na grade.",
                "Segurar um app abre um menu: fixar na home, pasta, informações do app e desinstalar.",
                "Segurar uma pasta permite renomear ou excluir; voltar ou deslizar para baixo sai da pasta.",
                "A busca procura em todos os apps, inclusive os que estão em pastas.",
                "Testes automáticos (busca, pastas, versões e novidades) rodando a cada build.",
            ),
        ),
        ReleaseNote(
            "0.12.0",
            listOf(
                "Memórias do dia: ao lado da hora aparece, de vez em quando, uma foto da câmera tirada por volta de hoje em anos ou meses atrás.",
                "Toque na foto para abri-la na galeria; o X esconde essa foto para sempre.",
                "Ajustes > memórias do dia: desligado, de vez em quando ou sempre; e restaurar as fotos ocultas.",
                "As fotos nunca saem do aparelho e só uma miniatura é carregada.",
            ),
        ),
        ReleaseNote(
            "0.11.0",
            listOf(
                "Ajustes virou uma engrenagem no canto superior direito da home (e da lista de apps).",
                "Transições entre a home, a grade de apps e as demais telas: entram com fade e subida suave.",
                "A grade anima quando a busca filtra ou reordena os apps.",
                "Os itens encolhem um pouco ao toque, para o toque ser sentido na hora.",
                "Avisos na própria tela no lugar do Toast: fixar app, foco, deslizes e limpeza de cache.",
                "Otimizar: barra de carregamento ao medir, estado de \"aguardando o sistema\" e resultado de quanto cache foi liberado.",
                "Atualização: barra de progresso ao verificar e ao baixar.",
            ),
        ),
        ReleaseNote(
            "0.10.0",
            listOf(
                "Lista de apps em grade de 3 colunas, com os ícones.",
                "Textos do topo maiores: notificações em duas linhas (app e título), status e carrossel mais legíveis.",
                "Player de música centralizado, com capa ao fundo, barra de progresso (toque para pular) e controles desenhados.",
                "Nova tela Otimizar (Ajustes > otimizar): cache total e por app, apps parados há 30 dias ou mais e atalhos para bateria, animações e Cuidados com o dispositivo.",
            ),
        ),
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
