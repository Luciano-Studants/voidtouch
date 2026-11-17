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
| Home | Resumo de RAM e bateria, barra de busca, sem rolagem |
| Gaveta | Lista A–Z; abre deslizando para cima |
| Pastas | Sim, só na gaveta (criadas segurando o dedo) |
| Ocultar apps | Opcional |

## Funcionalidades

**v1**
- Saúde do aparelho: RAM, armazenamento, bateria, apps mais pesados (por tamanho, exige permissão opcional de acesso ao uso) e atalho para a tela de limpar cache.
- Modo foco: esconde os apps escolhidos e mostra uma tela calma, só dentro do launcher.

**v2**
- Widgets (clima, calendário).
- Notificações e badges.

**Fora de escopo**
- Encerrar outros apps ou liberar RAM (o Android não permite).
- Bloquear apps abertos por outros meios (exige permissões pesadas).
- Alterar a interface do sistema (barra de notificações, configurações, bloqueio).

## Regras de desempenho

- APK abaixo de 5 MB; só os pesos de fonte usados.
- Sem imagens grandes, sem blur, sem sombras, sem animações pesadas.
- Nenhuma tarefa ou serviço em segundo plano.
- Listas com carregamento sob demanda.
- Sem bibliotecas desnecessárias.

## Ordem de desenvolvimento

1. Repositório, projeto base e compilação automática do APK. **(feito)**
2. Esqueleto do launcher no A05, como padrão. **(feito; falta testar no aparelho)**
3. Temas e fontes. **(feito na v0.2)**
4. Home: relógio, favoritos, RAM e bateria **(v0.3)** e busca **(v0.4)**.
5. Ícones.
6. Gaveta A–Z, gesto e pastas. **(gesto de deslizar para cima e busca na v0.4; faltam pastas)**
7. Saúde do aparelho.
8. Modo foco.
9. Ajustes e persistência.
10. Otimização e APK de release.

A v1 termina quando a etapa 9 funcionar no A05. Ideias novas entram na lista da v2 e não interrompem a ordem acima.
