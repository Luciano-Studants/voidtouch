# VoidTouch

Launcher Android minimalista, escuro e leve, feito sob medida para o Samsung Galaxy A05 (2 GB de RAM).

Lista de apps em texto, ícones vetoriais pequenos, preto/cinza/branco, sem cor de destaque e sem tarefas em segundo plano.

> Status: **v0.1 – esqueleto**. Lista os apps instalados e abre cada um. Tudo o mais está no [plano](docs/REQUISITOS.md).

## Instalar o APK (sem computador)

1. No GitHub, abra a aba **Actions** e entre na execução mais recente de **Build APK**.
2. Em **Artifacts**, baixe `voidtouch-debug-apk` (vem em .zip) e extraia o `.apk`.
3. Abra o `.apk` no celular. Se pedir, permita "instalar apps desconhecidos" para o navegador ou gerenciador de arquivos.
4. Aperte o botão Home. O Android pergunta qual tela inicial usar: escolha **VoidTouch** e **Sempre**.

Para voltar à tela inicial da Samsung: Ajustes > Apps > Escolher apps padrão > App de tela inicial.

## Compilar

O APK é compilado automaticamente pelo GitHub Actions a cada push na `main`. Para compilar localmente é preciso JDK 17 e o Android SDK (Android Studio):

```
./gradlew assembleDebug
```

O APK sai em `app/build/outputs/apk/debug/`.

## Tecnologia

Kotlin + Jetpack Compose, `minSdk 26`, `targetSdk 35`, uma única Activity, sem bibliotecas além do necessário.
