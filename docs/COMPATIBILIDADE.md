# Compatibilidade do VoidTouch

Apuração feita em 21/11/2026. O app foi testado de verdade só no Samsung Galaxy A05 (One UI 7). O resto abaixo vem da leitura da documentação do Android e do código.

## Versões do Android

O app tem minSdk 26 (Android 8.0) e targetSdk 35. Participação de cada versão em 1/10/2026 ([composables.com](https://composables.com/android-distribution-chart.md)):

| Android | API | Uso | Situação no VoidTouch |
| --- | --- | --- | --- |
| 8.0 / 8.1 | 26 / 27 | 0,5% / 1,5% | Suportado. Conferência de assinatura pelo campo antigo; cores do papel de parede só da API 27 em diante |
| 9 | 28 | 3,1% | Suportado |
| 10 | 29 | 5,7% | Suportado |
| 11 | 30 | 10,5% | Suportado. Visibilidade de pacotes resolvida com `<queries>` |
| 12 / 12L | 31 / 32 | 9,6% | Suportado |
| 13 | 33 | 11,8% | Suportado. Imagem do papel de parede costuma ser bloqueada: paralaxe pede imagem própria |
| 14 | 34 | 15,3% | Suportado. Fotos: acesso parcial (`READ_MEDIA_VISUAL_USER_SELECTED`) |
| 15 | 35 | 14,7% | Alvo do app. Tela de borda a borda tratada com `safeDrawingPadding` |
| 16 | 36 | 25,4% | Suportado (versão do Motorola Edge 70 Fusion). Ainda sem teste real |
| 17 | 37 | 0,9% | Sem teste. Ver pontos abaixo |

Cobertura: do 8.0 ao 16, cerca de 99% dos aparelhos.

## Motorola Edge 70 Fusion e parecidos

Dados da análise da [FoneArena](https://www.fonearena.com/blog/477577/motorola-edge-70-fusion-review.html): Snapdragon 7s Gen 4, 8 ou 12 GB de RAM, tela AMOLED curva de 6,78" (2772 x 1272, até 144 Hz), Android 16 com Hello UI, quase puro.

| Ponto | Situação |
| --- | --- |
| Tela curva e câmera na borda | O app usa `safeDrawingPadding`, que respeita a câmera e as barras; ver na prática |
| 144 Hz | O Compose acompanha a taxa do sistema; nada fixo no app |
| Tela mais alta e estreita (20:9) | A home usa peso e altura mínima; os favoritos se ajustam. Falta teste visual |
| Navegação por gestos ou 3 botões | Ambas dentro do `safeDrawingPadding` |
| Hello UI | Sem recursos próprios usados. A limpeza "Samsung" só aparece em aparelho Samsung (0.46.4) |
| Painel de notificações | Usa um método interno do Android; se o fabricante bloquear, o app avisa na tela |
| Memória (muita RAM) | Sem efeito: o app já usa pouca |

## Android 17 (API 37): o que pode afetar, caso o alvo suba

Fonte: documentação do Android ([todos os apps](https://developer.android.com/about/versions/17/behavior-changes-all), [alvo 37](https://developer.android.com/about/versions/17/behavior-changes-17)).

- **Limite de memória por app (todos):** processo acima do limite é encerrado. O VoidTouch é leve; vale conferir na saúde do aparelho.
- **Teclado após girar a tela (todos):** a busca pode perder o teclado se a tela girar. O app é só em pé.
- **Atividades em segundo plano via `IntentSender` (alvo 37):** o app abre atalhos e respostas com a home visível; a mudança não deve afetar.
- **Orientação e redimensionamento ignorados em telas grandes (alvo 37):** só tablets e dobráveis.
- **Contatos (alvo 37):** o app lê só nome e número; nada de colunas removidas.
- **Reflexão em campos finais (alvo 37):** o app não usa.

Decisão: o alvo fica em 35 por enquanto. Subir para 36 ou 37 exige uma versão própria, só com isso.

## Itens fora do que consegui apurar

- Comportamento em dobráveis (Razr) e tablets.
- Telas de configuração de outras marcas que o app abre (armazenamento, bateria, uso): o app tenta e avisa se não existir.
- Nenhum teste real em Android 8 a 12 e 16 ou mais novo.
