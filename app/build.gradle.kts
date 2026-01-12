plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.hilt)
    id("org.jetbrains.kotlin.plugin.serialization") version "2.0.0"
}

android {
    namespace = "com.babelsoftware.loudly"
    compileSdk = 35
    flavorDimensions += "default"

    buildFeatures {
        buildConfig = true
    }

    productFlavors {
        create("default") {
            dimension = "default"
            applicationId = "com.babelsoftware.loudly"
            versionNameSuffix = "-default"
        }
    }

    defaultConfig {
        applicationId = "com.babelsoftware.loudly"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "v1.6.1"
        vectorDrawables {
            useSupportLibrary = true
        }

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
    buildToolsVersion = "35.0.0"
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.hilt.navigation.compose)

    // Room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)
    ksp(libs.hilt.compiler)

    // Coil
    implementation(libs.coil.compose)
    implementation(libs.coil.gif)

    // Media3
    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.session)
    implementation(libs.androidx.media3.ui)
    implementation(libs.androidx.media3.common)

    // Palette
    implementation(libs.androidx.palette.ktx)

    // JSON
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")

    // Coroutines
    implementation(libs.kotlinx.coroutines.android)

    // Network (Retrofit/OkHttp assumed if not using Ktor, but saw Ktor in AirNote. Loudly might differ. Sticking to generics)
    // Actually the imports showed org.json but not retrofit explicitly in the snippets.
    // I will add standard networking just in case
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    // Image Cropper (from manifest)
    implementation("com.vanniktech:android-image-cropper:4.5.0")

    // Material 3 Window Size Class
    implementation("androidx.compose.material3:material3-window-size-class:1.2.1")

    // Squiggles
    implementation("me.saket.squiggles:squiggles:0.2.0")

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}
