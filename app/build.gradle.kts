plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.voidtouch.launcher"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.voidtouch.launcher"
        minSdk = 26
        targetSdk = 35
        versionCode = 62
        versionName = "0.48.0"
    }

    // Chave fixa (vinda dos secrets do GitHub) para uma versão nova instalar por cima da anterior.
    // Sem a chave (build local), cai na chave de debug.
    val keystorePath = System.getenv("KEYSTORE_FILE")
    signingConfigs {
        if (keystorePath != null) {
            create("fixed") {
                storeFile = file(keystorePath)
                storePassword = System.getenv("KEYSTORE_PASSWORD")
                keyAlias = "voidtouch"
                keyPassword = System.getenv("KEYSTORE_PASSWORD")
            }
        }
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.getByName(if (keystorePath != null) "fixed" else "debug")
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }

    lint {
        // Informa sem bloquear o build; o relatório sai como artefato da CI.
        // O verificador de produceState reclama de usos corretos (atribuição dentro de withContext); fica como aviso.
        warning += "ProduceStateDoesNotAssignValue"
        abortOnError = false
        checkReleaseBuilds = false
        htmlReport = true
        xmlReport = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.material3)

    testImplementation("junit:junit:4.13.2")
    testImplementation("org.json:json:20231013")
}
