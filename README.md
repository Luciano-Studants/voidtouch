# VoidTouch

Launcher Android minimalista, escuro e leve, feito sob medida para o Samsung Galaxy A05 (2 GB de RAM).

Lista de apps em texto, ícones vetoriais pequenos, preto/cinza/branco, sem cor de destaque e sem tarefas em segundo plano.

> Status: **v0.3 – home da referência**. Hora, mês em letra manuscrita, dia por extenso, bateria e RAM, até 10 favoritos com ícone e um botão redondo que abre a lista de todos os apps. Tudo o mais está no [plano](docs/REQUISITOS.md).

**Fixar favoritos:** toque no círculo (canto inferior direito) para ver todos os apps e **segure** um app para fixar na home (o ponto à direita marca os fixados). Na home, segurar um favorito desafixa. Máximo de 10.

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

Fontes: JetBrains Mono Light, Oxanium ExtraLight e Allura (licença OFL, ver [licenses](licenses/README.md)).
