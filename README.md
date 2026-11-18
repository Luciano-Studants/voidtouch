# VoidTouch

Launcher Android minimalista, escuro e leve, feito sob medida para o Samsung Galaxy A05 (2 GB de RAM).

Lista de apps em texto, ícones vetoriais pequenos, preto/cinza/branco, sem cor de destaque e sem tarefas em segundo plano.

> Status: **v0.8.1 – atualização dentro do app**. Em Ajustes > sobre, "verificar atualização" baixa a versão nova publicada no GitHub. (v0.8: nova home e ajustes.) Carrossel (hora, dia, música), bloco de notificações/status, deslizes configuráveis e tela de ajustes. Tudo o mais está no [plano](docs/REQUISITOS.md).

**Abrir a lista completa:** deslize para cima na home ou toque no círculo (canto inferior direito). Para voltar, deslize para baixo com a lista no topo ou use o botão voltar.

**Buscar:** toque em "buscar app" (canto inferior esquerdo) e digite; acentos e maiúsculas não importam ("camera" acha "Câmera"). A tecla de confirmar do teclado abre o primeiro resultado.

**Saúde do aparelho:** toque na linha "bateria · ram" da home. Mostra RAM, armazenamento e bateria em barras finas e a lista dos 10 apps que mais ocupam espaço (app + dados), com o cache de cada um. Tocar num app abre a tela dele no Android, onde fica Armazenamento > Limpar cache. Para medir o tamanho dos apps o Android exige a permissão especial **acesso ao uso**: o botão "permitir acesso" leva direto até ela. O VoidTouch só lê os tamanhos, não tem permissão de internet e não envia nada. Sem a permissão, o resto da tela funciona normalmente.

**Home (v0.8):** carrossel no topo (passe para os lados: hora, dia da semana, música), depois notificações (chegam na v0.9) e status do celular (toque para abrir a saúde do aparelho), a linha do foco e os favoritos. Deslize para cima abre a lista. **Deslizar para a esquerda ou direita abaixo do carrossel abre os apps escolhidos em Ajustes.** Segure o carrossel, ou toque em "ajustes" na lista, para abrir os Ajustes (fundo, formato da hora 20H00/20:00, apps dos deslizes). Na lista, arraste a linha tracejada à direita para pular por letra.

**Modo foco:** na lista completa, toque em **apps do foco** (canto direito, abaixo da busca) e toque nos apps que distraem; eles ganham um círculo vazio à direita. Toque em **concluir**. Depois, na home, toque na linha **foco desligado** (abaixo do relógio) para ligar: os apps marcados somem dos favoritos, da lista e da busca, a linha de bateria e RAM some e a linha passa a mostrar há quanto tempo o foco está ligado. Toque de novo para desligar. Sem nenhum app marcado o foco não liga e o app avisa.

Limite do Android: o foco esconde os apps **só dentro do VoidTouch**. Um app marcado ainda pode ser aberto por uma notificação ou pela tela de apps recentes, porque um launcher não consegue bloquear isso.

**Fixar favoritos:** na lista completa, **segure** um app para fixar na home (o ponto à direita marca os fixados). Na home, segurar um favorito desafixa. Máximo de 10.

**Trocar o fundo (provisório):** segure o dedo no relógio. Alterna entre preto, grafite e papel de parede. Vira uma opção na tela de Ajustes mais adiante.

## Instalar o APK (sem computador)

1. No GitHub, abra a aba **Actions** e entre na execução mais recente de **Build APK**.
2. Em **Artifacts**, baixe `voidtouch-release-apk` (vem em .zip) e extraia o `.apk`. É a versão otimizada; use sempre essa.
   Cada compilação usa uma chave nova, então para atualizar é preciso **desinstalar a versão anterior** antes de instalar a nova.
3. Abra o `.apk` no celular. Se pedir, permita "instalar apps desconhecidos" para o navegador ou gerenciador de arquivos.
4. Aperte o botão Home. O Android pergunta qual tela inicial usar: escolha **VoidTouch** e **Sempre**.

Para voltar à tela inicial da Samsung: Ajustes > Apps > Escolher apps padrão > App de tela inicial.

## Compilar

O APK é compilado automaticamente pelo GitHub Actions a cada push na `main`. Para compilar localmente é preciso JDK 17 e o Android SDK (Android Studio):

```
./gradlew assembleRelease
```

O APK sai em `app/build/outputs/apk/release/`.

## Tecnologia

Kotlin + Jetpack Compose, `minSdk 26`, `targetSdk 35`, uma única Activity, sem bibliotecas além do necessário.

Logotipos: [Simple Icons](https://simpleicons.org) (CC0); as marcas pertencem aos seus donos.

Fontes: JetBrains Mono Light, Oxanium ExtraLight e Allura (licença OFL, ver [licenses](licenses/README.md)).

## Atualização dentro do app (v0.8.1)

O app consulta as Releases do GitHub só quando você toca em **Ajustes > verificar atualização**. Para funcionar sem desinstalar, o build precisa da chave fixa: cadastre em *Settings > Secrets and variables > Actions* os secrets `KEYSTORE_BASE64` e `KEYSTORE_PASSWORD`. Com eles, cada push na `main` assina o APK com a mesma chave e publica a Release `v<versão>`. Sem eles, o build continua funcionando com a chave de debug, mas sem Release.
