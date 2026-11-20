# VoidTouch

Launcher Android minimalista, escuro e leve, feito sob medida para o Samsung Galaxy A05 (2 GB de RAM, One UI 7). Uso pessoal, em português do Brasil.

Preto/cinza/branco, sem cor de destaque, ícones vetoriais próprios e **nenhuma tarefa em segundo plano**: relógio, status e player só atualizam com a tela visível.

> Versão atual: **0.38.0**. A documentação completa de tudo o que o app faz está em [docs/REQUISITOS.md](docs/REQUISITOS.md); o histórico por versão fica no app, em Ajustes > sobre > novidades.

## O que ele faz

**Home**
- Carrossel de 4 páginas (deslize para os lados): hora (com a foto das memórias do dia), dia, música (player com capa, progresso e controles) e agenda (próximos 7 dias do calendário).
- **Ilha dinâmica**: pílula no topo com o que está acontecendo agora (chamada, navegação, cronômetro ou timer, downloads com progresso). Toque expande; tocar numa linha abre o app. Precisa do acesso às notificações.
- Notificações recentes, com o texto e agrupadas por conversa: toque abre; segurar mostra **responder sem abrir o app** (quando o app permite), **ler depois** e dispensar. O "ler depois" guarda até 30 itens só no aparelho, fora do backup e bloco de status do aparelho.
- **Linha "agora"**: até 3 apps que você costuma abrir neste horário, só pelo histórico no aparelho (fora do backup; dá para desligar e apagar em Ajustes).
- **Captura rápida**: ação "anotação rápida" (para qualquer gesto, ou busque "anotar"). A primeira anotação vira a **tarefa do foco**, mostrada na home; toque marca como feita.
- Até 10 favoritos, entre apps e pastas. Segure um favorito para soltá-lo.
- Engrenagem no canto superior direito abre os Ajustes.

**Gestos da home** (Ajustes > gestos da home): deslizar para esquerda, direita e baixo, e toque duplo. Cada um abre um app ou uma ação: lanterna, painel de notificações, buscar app, lista de apps, foco, otimizar, saúde ou ajustes. Por padrão, deslizar para baixo abre o painel de notificações.

**Lista de apps**: grade de 3 colunas com ícones, régua tracejada para pular por letra, busca sem acento e por início de palavra. A busca também faz contas e conversões ("15% de 230", "10 km em mi"), acha ações e telas de configuração do Android e, se você liberar a permissão, contatos; os apps que você mais abre aparecem primeiro. Segurar um app abre o menu: atalhos do próprio app, fixar na home, adicionar à pasta, informações e desinstalar. Pastas aparecem primeiro e também podem ser fixadas na home.

**Modo foco**: esconde notificações e apps marcados, só dentro do VoidTouch. Segurando a linha "foco" na home dá para iniciar uma **sessão** de 25, 50 ou 90 minutos (ou sem fim), que desliga sozinha e mostra um resumo (tempo focado e notificações esperando). O **foco automático** liga e desliga sozinho em um horário (padrão 18h às 0h), nas viradas da janela, com a home aberta.

**Cenas** (Ajustes > cenas): jeitos da home por momento (manhã, trabalho, noite...). Cada cena tem horário, liga o foco ou não, pode deixar a home calma e lembra os próprios favoritos. Troca sozinha pelo horário ou à mão.

**Abrir apps**: o app se abre crescendo do ponto onde você tocou, com os itens afundando e voltando com um quique e uma vibração leve (dá para desligar em Ajustes).

**Modo carregando**: com o celular carregando e a home parada por 15 segundos, aparece um relógio grande de cabeceira com data, bateria e música. Um toque sai.

**Modo noite**: no horário escolhido (padrão 22h às 6h) a home fica mais escura e calma, sem status, notificações e foto.

**Memórias do dia**: de vez em quando uma foto da galeria (qualquer uma, menos capturas de tela) aparece ao lado da hora. Toque abre na galeria; o X esconde para sempre.

**Celular**: tela Saúde (RAM, armazenamento, bateria, apps mais pesados e **desempenho** medido sob demanda), tela Otimizar (limpeza de cache guiada, apps parados, atalho para os Cuidados com o dispositivo da Samsung) **relatório de uso** (tempo de tela de hoje e dos últimos 7 dias, por app e por dia) e painel de **permissões** com o estado de cada uma.

**Ajustes**: fundo (preto, cinza ou papel de parede), formato da hora (20H00 ou 20:00), gestos, foco automático, modo noite, memórias, backup e sobre (novidades, dicas de uso, atualização e tempo de abertura).

**Backup**: exporta e restaura favoritos, pastas, gestos e ajustes em um arquivo JSON. O arquivo é validado por inteiro antes de restaurar; se algo estiver errado, nada é alterado.

## Limites do Android

- Um launcher não consegue limpar o cache de outros apps nem encerrá-los; por isso a limpeza é guiada.
- O foco esconde apps só dentro do VoidTouch.
- O painel de notificações abre por uma chamada oculta do sistema, que pode mudar em outras versões ou marcas.
- Atalhos dos apps só funcionam com o VoidTouch como launcher padrão.
- Fotos que existem só na nuvem do Google Fotos não são acessíveis.
- Compatibilidade com outras marcas de Android está adiada para a v1.0.

## Instalar

1. No GitHub, abra **Releases** e baixe o `.apk` da versão mais recente (ou em **Actions > Build APK > Artifacts**).
2. Abra o `.apk` no celular e, se pedir, permita "instalar apps desconhecidos".
3. Aperte Home e escolha **VoidTouch** > **Sempre**.

Para voltar à tela da Samsung: Ajustes > Apps > Escolher apps padrão > App de tela inicial.

## Atualizar dentro do app

Em **Ajustes > sobre > verificar atualização**. O app só consulta as Releases do GitHub quando você toca. O APK baixado é conferido antes de instalar: tamanho, pacote, versão e assinatura. Se algo falhar, nada é instalado.

Para as atualizações instalarem por cima, o build usa uma chave fixa: cadastre em *Settings > Secrets and variables > Actions* os secrets `KEYSTORE_BASE64` e `KEYSTORE_PASSWORD`. Com eles, cada push na `main` assina o APK e publica a Release `v<versão>`; sem eles o build usa a chave de debug e não publica Release. Os testes rodam antes, e se falharem a Release não sai.

## Compilar

O APK é compilado pelo GitHub Actions a cada push na `main`. Localmente, com JDK 17 e o Android SDK:

```
./gradlew testDebugUnitTest assembleRelease
```

O APK sai em `app/build/outputs/apk/release/`.

## Tecnologia

Kotlin 2.0 + Jetpack Compose, `minSdk 26`, `targetSdk 35`, uma única Activity, sem bibliotecas além do necessário. A lógica pura fica em `data/` e é coberta por testes unitários.

Logotipos: [Simple Icons](https://simpleicons.org) (CC0); as marcas pertencem aos seus donos.

Fontes: JetBrains Mono Light, Oxanium ExtraLight e Allura (licença OFL, ver [licenses](licenses/README.md)).
