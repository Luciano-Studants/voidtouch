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
            "0.46.4",
            listOf(
                "Compatibilidade com outras marcas: em aparelhos que não são Samsung (como Motorola), a tela otimizar não fala mais em \"Samsung\" e leva ao armazenamento e à bateria do próprio sistema.",
                "Nova documentação docs/COMPATIBILIDADE.md com a apuração das versões do Android 8 ao 17 e do Motorola Edge 70 Fusion.",
            ),
        ),
        ReleaseNote(
            "0.46.3",
            listOf(
                "Correção: com a fonte do Android muito grande, os textos da home se sobrepunham e quebravam no meio das palavras. O aumento da fonte agora tem um limite (115%), então a home continua legível.",
                "A pílula \"buscar app\" não quebra mais em duas linhas.",
            ),
        ),
        ReleaseNote(
            "0.46.2",
            listOf(
                "Revisão dos textos: mensagens de erro mais claras e no mesmo tom em todo o app (por exemplo, \"não foi possível verificar\" no lugar de \"não deu\").",
                "Nada muda no uso.",
            ),
        ),
        ReleaseNote(
            "0.46.1",
            listOf(
                "Desempenho: ao instalar ou atualizar vários apps de uma vez (como numa atualização em massa da Play Store), a lista é relida uma vez só, em vez de uma vez por app.",
                "O identificador de cada app passou a ser calculado uma vez, o que alivia a rolagem e a busca em celulares com muitos apps.",
                "Ícones de apps atualizados ou removidos são descartados da memória, e o preparo dos ícones é interrompido se a lista mudar no meio.",
            ),
        ),
        ReleaseNote(
            "0.46.0",
            listOf(
                "Acessibilidade: botões e linhas tocáveis agora são anunciados como botão pelo leitor de tela (TalkBack).",
                "Alvos de toque pequenos ganharam altura mínima de 44 dp, sem mudar o visual das listas.",
                "Com fonte grande no Android, a pílula de busca, as linhas dos favoritos e os botões crescem em vez de cortar o texto.",
                "O contraste do texto passou a ser verificado por teste em todos os conjuntos de cores (mínimo AA, 4,5 para 1).",
                "Com \"remover animações\" ligado no Android, a animação de abertura de apps é desligada.",
            ),
        ),
        ReleaseNote(
            "0.45.2",
            listOf(
                "Organização interna: a navegação entre telas (lista, Ajustes, Saúde, Otimizar e as outras) agora tem regras próprias e testadas, no lugar de dez marcadores soltos na tela principal. Nada muda no uso.",
            ),
        ),
        ReleaseNote(
            "0.45.1",
            listOf(
                "Organização interna: tudo do fundo (imagem, paralaxe e cores) saiu da tela principal para uma peça própria, mais fácil de manter e de testar. Nada muda no uso.",
            ),
        ),
        ReleaseNote(
            "0.45.0",
            listOf(
                "Bibliotecas atualizadas (Compose e Activity) para a versão estável de janeiro de 2025, com correções de desempenho e de compatibilidade.",
            ),
        ),
        ReleaseNote(
            "0.44.0",
            listOf(
                "Teste do modo seguro: em Ajustes > sobre, segure o texto da versão para mostrar \"simular travamento\". Cada toque fecha o app de verdade, para você ver a proteção agir antes de depender dela.",
                "Ajustes mostra a primeira linha do último travamento registrado, quando houver.",
            ),
        ),
        ReleaseNote(
            "0.43.0",
            listOf(
                "Imagem de fundo própria: em Ajustes > papel de parede, escolha uma foto para ser o fundo do VoidTouch. Funciona em qualquer Android, sem depender do papel de parede do sistema.",
                "A imagem é reduzida ao tamanho da tela e fica só neste aparelho. A paralaxe e as cores automáticas passam a usar essa imagem.",
                "O fundo agora cobre a tela inteira, inclusive atrás da barra de status e da barra de navegação.",
            ),
        ),
        ReleaseNote(
            "0.42.0",
            listOf(
                "Compatibilidade com Android 8.0 e 8.1: a conferência da atualização e a contagem de travamentos usavam recursos do Android 9 e podiam falhar nesses aparelhos.",
                "As cores do papel de parede agora vêm das cores que o próprio Android calcula, sem precisar ler a imagem nem de permissão, então funcionam em qualquer aparelho.",
                "A paralaxe avisa \"indisponível neste aparelho\" quando o Android não deixa apps lerem a imagem do papel de parede (comum no Android 13 ou mais novo), em vez de falhar em silêncio.",
                "Nada do app vai para o backup na nuvem do Google: o backup é só o arquivo que você salva em Ajustes.",
            ),
        ),
        ReleaseNote(
            "0.41.0",
            listOf(
                "Atualizador mais seguro: só aceita download por https, de servidores do GitHub e do repositório do VoidTouch, mesmo depois de redirecionamentos.",
                "A resposta da consulta e o APK têm tamanho máximo, então um arquivo gigante não enche o armazenamento nem a memória.",
                "A verificação automática do código (lint) passa a rodar a cada versão e guarda um relatório para eu corrigir o que aparecer.",
            ),
        ),
        ReleaseNote(
            "0.40.0",
            listOf(
                "Defesa contra erros: a lista de apps, os ícones, a agenda e as memórias agora se recuperam sozinhos se o sistema falhar na leitura, em vez de fechar o app.",
                "O serviço de notificações ficou blindado: uma notificação estranha de outro app não derruba mais a home.",
                "Aparelhos sem seletor de arquivos, instalador ou tela de permissão não travam mais: o app avisa na tela.",
                "Em Saúde do aparelho há uma seção \"erros tratados\" com o que o app resolveu sozinho nesta sessão.",
            ),
        ),
        ReleaseNote(
            "0.39.0",
            listOf(
                "Proteção contra travamentos: se o VoidTouch fechar sozinho 3 vezes seguidas (2 logo depois de uma atualização), ele abre um modo seguro em vez da home normal.",
                "O modo seguro tem a lista de todos os apps, atalho para trocar a tela inicial padrão do Android, cópia do erro e um botão para tentar a home de novo com os recursos novos desligados.",
                "Em Ajustes > celular há agora \"trocar tela inicial padrão\", para voltar a outra tela inicial a qualquer momento.",
            ),
        ),
        ReleaseNote(
            "0.38.1",
            listOf(
                "Polimento: o papel de parede agora é lido em segundo plano e já reduzido ao tamanho da tela, sem travar a home nem pesar na memória.",
                "O tom das cores fica guardado até o papel de parede mudar, então não é recalculado a cada volta para a home.",
                "Restaurar backup passa a recusar arquivos enormes sem encher a memória.",
            ),
        ),
        ReleaseNote(
            "0.38.0",
            listOf(
                "Conjuntos de cores: em Ajustes > papel de parede você troca o tom da interface entre automático (segue o papel de parede), neutro, âmbar, verde-musgo, azul-gelo, violeta e rosa.",
                "Cada nome aparece já na própria cor, como pré-visualização, e a troca vale na hora.",
                "O tema continua escuro e a leitura é a mesma em todos. Substitui o liga/desliga da v0.37.",
            ),
        ),
        ReleaseNote(
            "0.37.0",
            listOf(
                "Cores do papel de parede: com o fundo em papel de parede, o texto, as linhas e a camada escura ganham um leve tom da cor dominante da imagem.",
                "A luminosidade é fixa, então o tema continua escuro e a leitura não piora. Imagens cinzas ficam neutras.",
                "Em Ajustes > papel de parede dá para desligar.",
            ),
        ),
        ReleaseNote(
            "0.36.0",
            listOf(
                "Reordenar favoritos: em Ajustes > favoritos > ordem dos favoritos, setas ↑ ↓ mudam a posição de cada app ou pasta da home.",
                "A mudança vale na hora e entra no backup.",
            ),
        ),
        ReleaseNote(
            "0.35.0",
            listOf(
                "Paralaxe: com o fundo em papel de parede, a imagem se move de leve conforme você inclina o celular.",
                "O sensor só fica ligado com a tela visível e a imagem é reduzida ao tamanho da tela, sem pesar na memória.",
                "Em Ajustes > papel de parede dá para ligar e desligar.",
            ),
        ),
        ReleaseNote(
            "0.34.0",
            listOf(
                "Animação de abertura: o app se abre crescendo do ponto onde você tocou, como nos launchers de ponta.",
                "Os itens da home e da lista afundam ao toque e voltam com um pequeno quique (movimento de mola), e uma vibração leve confirma a abertura.",
                "Em Ajustes > abrir apps dá para desligar a animação e a vibração separadamente.",
            ),
        ),
        ReleaseNote(
            "0.33.0",
            listOf(
                "Modo carregando: com o celular carregando e a home parada por 15 segundos, aparece um relógio grande de cabeceira, com data, bateria e a música que está tocando.",
                "Um toque sai; o relógio só volta depois que você desconectar e conectar o carregador de novo. O conteúdo muda de lugar um pouco a cada minuto para não marcar a tela.",
                "Em Ajustes > modo carregando dá para desligar e escolher se a tela fica acesa enquanto ele aparece.",
            ),
        ),
        ReleaseNote(
            "0.32.1",
            listOf(
                "Correção: em Ajustes, deslizar para baixo não fecha mais a tela e a lista rola normalmente. Para sair, use \"fechar\" ou o voltar.",
            ),
        ),
        ReleaseNote(
            "0.32.0",
            listOf(
                "Relatório de uso: veja o tempo de tela de hoje e dos últimos 7 dias, com os apps mais usados em barras e o total de cada dia da semana.",
                "Abra em Ajustes > relatório de uso, por um gesto (ação \"relatório de uso\") ou buscando \"uso\".",
                "Usa o acesso ao uso do Android, lê só com a tela aberta e não conta o tempo na tela inicial. Nada sai do aparelho.",
            ),
        ),
        ReleaseNote(
            "0.31.0",
            listOf(
                "Sessão de foco: segure a linha \"foco\" na home e escolha 25, 50 ou 90 minutos (ou sem fim). A linha mostra quanto falta e o foco desliga sozinho no fim.",
                "Ao terminar qualquer foco, aparece um resumo: quanto tempo você focou e quantas notificações dos apps escondidos estão esperando. Toque no resumo para dispensar.",
                "Tocar na linha continua ligando e desligando o foco como antes.",
            ),
        ),
        ReleaseNote(
            "0.30.0",
            listOf(
                "Captura rápida: nova ação \"anotação rápida\" para qualquer gesto (ou busque \"anotar\"). A tela abre já com o teclado: escreva, confirme e a ideia fica guardada.",
                "A primeira anotação pendente vira a tarefa do foco: aparece na home com ●. Toque nela para marcar como feita; segure para abrir as anotações. A próxima pendente assume sozinha.",
                "Na tela de anotações: toque no texto marca ou desmarca, \"foco\" escolhe outra tarefa, × apaga e \"limpar\" remove as feitas. Até 50 anotações, guardadas no aparelho e incluídas no backup.",
            ),
        ),
        ReleaseNote(
            "0.29.0",
            listOf(
                "Linha \"agora\": abaixo do foco, a home mostra até 3 apps que você costuma abrir nesse horário. Um toque abre.",
                "Aprende só com o seu histórico no aparelho, ao abrir apps: nada é enviado e nada roda em segundo plano. Precisa de uns dias de uso para começar a sugerir.",
                "Não repete os favoritos nem os apps escondidos pelo foco, e some na home calma. Em Ajustes > linha agora dá para desligar ou apagar o histórico.",
            ),
        ),
        ReleaseNote(
            "0.28.0",
            listOf(
                "Notificações mais úteis na home: segure uma notificação para responder sem abrir o app (quando o app permite), guardar em \"ler depois\" ou dispensar.",
                "Cada notificação mostra também o texto da mensagem, e as da mesma conversa aparecem juntas, com a contagem.",
                "Ler depois: o contador aparece abaixo das notificações e abre uma lista de até 30 itens, guardados só no aparelho e fora do backup.",
            ),
        ),
        ReleaseNote(
            "0.27.0",
            listOf(
                "Ilha dinâmica: uma pílula no topo da home mostra o que está acontecendo agora, como chamada, navegação, cronômetro ou timer e downloads com progresso.",
                "Toque na pílula para abrir a lista e toque numa linha para abrir o app. Com várias atividades, mostra a mais importante e quantas mais existem.",
                "Usa o acesso às notificações; o cronômetro só anda com a home visível, e a pílula some na home calma (noite).",
            ),
        ),
        ReleaseNote(
            "0.26.0",
            listOf(
                "Busca universal: a mesma barra agora acha ações (lanterna, foco, otimizar...), telas de configuração do Android (Wi-Fi, Bluetooth, bateria...) e contatos.",
                "Faz contas e conversões: \"15% de 230\", \"(2+3)*4\", \"10 km em mi\", \"100 f para c\". Tocar na resposta copia.",
                "A busca aprende: os apps que você mais abre aparecem primeiro entre os resultados parecidos.",
                "Contatos só entram se você ligar em Ajustes > permissões; o app não copia nem envia nada.",
            ),
        ),
        ReleaseNote(
            "0.25.0",
            listOf(
                "Cenas: a home muda de jeito conforme o momento. Em Ajustes > cenas, crie manhã, trabalho, noite... cada uma com horário, foco ligado ou não e home calma.",
                "Cada cena lembra os próprios favoritos: ao sair dela, os favoritos de agora ficam guardados e voltam na próxima vez.",
                "Pode trocar sozinho pelo horário ou à mão, escolhendo \"usar agora\"; a troca à mão vale até o próximo horário de virada.",
            ),
        ),
        ReleaseNote(
            "0.24.0",
            listOf(
                "Desempenho na tela Saúde: mostra quanto levaram a abertura da home, até os apps ficarem prontos, abrir a lista de apps e cada busca, com média e pior tempo.",
                "As medidas são feitas só quando você usa o app e ficam na memória; nada roda em segundo plano.",
            ),
        ),
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
