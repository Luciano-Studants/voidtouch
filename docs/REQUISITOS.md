# VoidTouch – Requisitos e plano

## Visão

Launcher Android minimalista, rápido e muito polido. O app precisa ser o mais leve do aparelho, porque um launcher fica sempre na memória.

- Primeiro alvo: Samsung Galaxy A05 (SM-A055M), 2 GB de RAM (pode chegar a 4 GB).
- Uso pessoal, em português (PT-BR). Sem prazo definido.
- No futuro: outros aparelhos Android. Nada específico da Samsung.
- Prioridades: velocidade de uso, praticidade, precisão e personalização. O fluxo não pode ser difícil.

## Decisões

| Tema | Decisão |
|---|---|
| Formato | Launcher (tela inicial) |
| Tema | Só escuro. Void (preto puro) e Grafite (cinza) |
| Fundo | Três modos: Void (preto), Grafite (cinza) e Wallpaper (papel de parede do sistema com camada escura de 55%). Aplicado como fundo da janela, sem imagem dentro do app |
| Cor de destaque | Nenhuma |
| Fonte da lista | Monoespaçada (JetBrains Mono Light) |
| Relógio | 24 h, fonte quadrada fina (Oxanium ExtraLight) |
| Ícones | Vetor próprio; letra inicial em círculo fino como reserva |
| Favoritos na home | 10 |
| Topo da home | Hora em 24 h, mês em letra manuscrita (Allura), dia da semana e dia do mês por extenso, bateria e RAM (referência visual: print enviado pelo usuário) |
| Home | Carrossel de 4 páginas, notificações, status, favoritos, busca e gestos configuráveis, sem rolagem |
| Gaveta | Grade de 3 colunas com ícones e régua por letra; abre deslizando para cima |
| Pastas | Sim; criadas pelo menu do app e podem ser fixadas na home (limite de 10 favoritos para apps e pastas juntos) |
| Ocultar apps | Opcional |

## Funcionalidades entregues

| Área | O que faz | Versão |
|---|---|---|
| Home | Carrossel (hora, dia, música, agenda), notificações, status, favoritos | 0.8 a 0.18 |
| Gestos | Esquerda, direita, baixo e toque duplo com apps ou ações rápidas | 0.8, 0.14 |
| Lista de apps | Grade de 3 colunas, ícones, régua, busca, menu ao segurar, atalhos dos apps | 0.10, 0.13, 0.16 |
| Pastas | Criar, renomear, excluir, fixar na home | 0.13, 0.14.2 |
| Modo foco | Esconde apps e notificações marcados; automático por horário | 0.7, 0.19 |
| Modo noite | Home mais escura e calma em um horário | 0.17 |
| Memórias do dia | Foto da galeria ao lado da hora, ocultável | 0.12, 0.14.1 |
| Agenda | Próximos 7 dias do calendário no carrossel | 0.18 |
| Saúde | RAM, armazenamento, bateria, apps pesados, desempenho sob demanda | 0.6, 0.24 |
| Otimizar | Cache por app, limpeza guiada, apps parados, atalho Samsung | 0.10, 0.18.1 |
| Permissões | Estado, motivo e atalho de cada permissão | 0.23 |
| Cenas | Home por momento: horário, foco, home calma e favoritos próprios | 0.25 |
| Backup | Exportar e restaurar com validação completa | 0.15, 0.20 |
| Atualização | Dentro do app, com conferência de tamanho, pacote, versão e assinatura | 0.8.1, 0.21 |
| Dicas e novidades | Tela de dicas, novidades após cada atualização, histórico em Ajustes | 0.9, 0.15 |
| Testes | Busca, versões, pastas, novidades, gestos, noite, agenda, foco, backup, atualizador, regras da home, permissões, desempenho | 0.13 a 0.24 |

**Roadmap de novas funcionalidades (em ordem de produção)**

| # | Função | Resumo | Status |
|---|---|---|---|
| 1 | Cenas | A home muda por horário ou toque: favoritos próprios, foco e home calma | v0.25 |
| 2 | Busca universal | Apps, contatos, ações, configurações, atalhos, contas e conversões | a fazer |
| 3 | Ilha dinâmica interna | Pílula no topo com atividades ao vivo (música, cronômetro, download, entrega) | a fazer |
| 4 | Responder pela home | Responder notificações de mensagem sem abrir o app; agrupar por conversa; ler depois | a fazer |
| 5 | Linha "agora" preditiva | Três apps prováveis para a hora, pelo histórico local de aberturas | a fazer |
| 6 | Captura rápida | Gesto abre campo de nota; vira "a tarefa do foco" | a fazer |
| 7 | Sessão de foco | Foco com cronômetro (25, 50 min ou livre) e resumo ao final | a fazer |
| 8 | Relatório de uso | Tempo de tela por app, no dia e na semana | a fazer |
| 9 | Modo carregando | Relógio grande de cabeceira enquanto carrega com a home aberta | a fazer |
| 10 | Ações no favorito | Deslizar em um favorito abre uma segunda ação | a fazer |
| 11 | Animação de abertura | Abrir o app a partir do ícone, com movimento de mola e vibração refinada | a fazer |
| 12 | Paralaxe do papel de parede | Giroscópio, só com a home visível | a fazer |
| 13 | Reordenar favoritos | Arrastar para mudar a ordem | a fazer |

Descartado pelo usuário: gestos por desenho. Evitar: widgets completos do Android, blur e sombras pesadas.

**Ideias menores**
- Widget de clima, opcional e desligado por padrão.
- Histórico local de erros e configurações pesquisáveis.
- Foco com mais de uma programação e dias da semana.
- Divisão completa da MainActivity por tela.

**Fora de escopo**
- Encerrar outros apps ou liberar RAM; limpar o cache de outros apps (o Android não permite).
- Bloquear apps abertos por outros meios (exige permissões pesadas).
- Alterar a interface do sistema (barra de notificações, configurações, bloqueio).

## Regras de desempenho

- APK abaixo de 5 MB; só os pesos de fonte usados.
- Sem imagens grandes, sem blur, sem sombras, sem animações pesadas.
- Nenhuma tarefa ou serviço em segundo plano; relógio, status e player só atualizam com a tela visível.
- A lista de apps só é relida quando um app é instalado ou removido.
- Listas com carregamento sob demanda.
- Sem bibliotecas desnecessárias.

## Ordem de desenvolvimento

1. Repositório, projeto base e APK automático. **(feito)**
2. Esqueleto do launcher, temas, fontes, home, busca, ícones e saúde. **(v0.1 a v0.6)**
3. Modo foco, ajustes, carrossel, notificações e player. **(v0.7 a v0.9)**
4. Atualização interna com chave fixa e tela de novidades. **(v0.8.1, v0.9)**
5. Grade, transições, Otimizar, memórias do dia, pastas e testes. **(v0.10 a v0.13)**
6. Gestos extras, favoritos com pastas, dicas, backup, atalhos, modo noite, agenda e limpeza guiada. **(v0.14 a v0.18.1)**
7. Foco automático. **(v0.19)**
8. Robustez, a partir da auditoria: backup seguro, atualizador seguro, regras da home com testes, painel de permissões e métricas de desempenho. **(v0.20 a v0.24)**
9. Compatibilidade com outras marcas de Android, testes em outros aparelhos e otimização final. **(v1.0)**

A v1.0 termina quando a etapa 9 funcionar. Ideias novas entram na lista de ideias abertas e não interrompem a ordem acima.
