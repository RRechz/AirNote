plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.hilt)
    id("org.jetbrains.kotlin.plugin.serialization") version "2.0.0"
}

android {
    namespace = "com.babelsoftware.airnote"
    compileSdk = 36
    flavorDimensions += "default"

    buildFeatures {
        buildConfig = true
    }

    productFlavors {
        create("default") {
            dimension = "default"
            applicationId = "com.babelsoftware.airnote"
            versionNameSuffix = "-default"
        }

        create("accrescent") {
            dimension = "default"
            applicationId = "by.babelapps.airnote"
            versionNameSuffix = "-babelapps"
        }
    }

    defaultConfig {
        applicationId = "com.babelsoftware.airnote"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "v1.6.1"
        vectorDrawables {
            useSupportLibrary = true
        }

        // https://developer.android.com/guide/topics/resources/app-languages#gradle-config
        resourceConfigurations.plus(
            listOf("en", "ar", "de", "es", "fa", "fil", "fr", "hi", "it", "ja", "ru", "sk", "tr", "da", "nl", "pl", "tr", "uk", "vi", "ota", "pt-rBR", "sr", "zh-rCN")
        )
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            isShrinkResources = false

            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("debug")
        }

        debug {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("debug")
            isDebuggable = true
            applicationIdSuffix = ".debug"
        }
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
    buildToolsVersion = "36.0.0"
}

dependencies {
    implementation(libs.androidx.biometric.ktx)
    implementation(libs.androidx.glance)
    implementation(libs.coil.compose)
    implementation(libs.hilt.navigation.compose)
    implementation(libs.androidx.glance.appwidget)
    implementation(libs.androidx.material3.window.size.class1.android)
    ksp(libs.androidx.room.compiler)
    ksp(libs.hilt.android.compiler)
    ksp(libs.hilt.compile)

    implementation("com.google.ai.client.generativeai:generativeai:0.9.0")
    implementation("androidx.security:security-crypto:1.1.0")
    implementation("com.udojava:EvalEx:2.7")
    implementation("androidx.activity:activity-compose:1.11.0")
    implementation("com.google.mlkit:translate:17.0.3")
    implementation("com.google.mlkit:language-id:17.0.6")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.10.2")
    implementation("org.jsoup:jsoup:1.21.2")
    implementation("com.google.code.gson:gson:2.13.2")
    implementation(platform("io.ktor:ktor-bom:3.0.0-beta-2"))
    implementation("io.ktor:ktor-client-core")
    implementation("io.ktor:ktor-client-android")
    implementation("io.ktor:ktor-client-content-negotiation")
    implementation("io.ktor:ktor-client-logging")
    implementation("io.ktor:ktor-serialization-kotlinx-json")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.0")

    implementation(libs.hilt.android)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.navigation.compose)
}