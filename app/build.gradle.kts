plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.whitenoise.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.whitenoise.app"
        minSdk = 26
        targetSdk = 35
        versionCode = 24
        versionName = "2.1.1"
    }

    signingConfigs {
        create("release") {
            val envKeystore = System.getenv("KEYSTORE_PATH")
            val envStorePass = System.getenv("KEYSTORE_PASSWORD") ?: System.getenv("STORE_PASSWORD")
            val envKeyAlias = System.getenv("KEY_ALIAS")
            val envKeyPass = System.getenv("KEY_PASSWORD")

            val localKeystore = file("saltambience.keystore")

            if (!envKeystore.isNullOrEmpty() && file(envKeystore).exists()) {
                storeFile = file(envKeystore)
                storePassword = envStorePass
                keyAlias = envKeyAlias
                keyPassword = envKeyPass
            } else if (localKeystore.exists()) {
                storeFile = localKeystore
                storePassword = envStorePass ?: "saltambience"
                keyAlias = envKeyAlias ?: "saltambience"
                keyPassword = envKeyPass ?: "saltambience"
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            val releaseConfig = signingConfigs.getByName("release")
            if (releaseConfig.storeFile != null && releaseConfig.storeFile?.exists() == true) {
                signingConfig = releaseConfig
            } else {
                signingConfig = signingConfigs.getByName("debug")
            }
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    kotlinOptions {
        jvmTarget = "21"
    }

    buildFeatures {
        compose = true
    }

    dependenciesInfo {
        includeInApk = false
        includeInBundle = false
    }

    lint {
        abortOnError = false
        checkReleaseBuilds = false
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

tasks.matching { it.name.contains("AarMetadata") || it.name.startsWith("lintVital") }.configureEach {
    enabled = false
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    compilerOptions {
        freeCompilerArgs.add("-Xskip-metadata-version-check")
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.activity.compose)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.foundation)

    implementation(libs.salt.ui)

    implementation(libs.media3.exoplayer)
    implementation(libs.media3.session)

    implementation(libs.datastore.preferences)
    implementation(libs.kotlinx.serialization.json)

    testImplementation(libs.junit)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
