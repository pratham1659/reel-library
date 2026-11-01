import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.reelrecall.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.reelrecall.app"
        minSdk = 26
        targetSdk = 35
        versionCode = 6
        versionName = "1.2.4"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        // Room SQLite schema export directory
        ksp {
            arg("room.schemaLocation", "$projectDir/schemas")
        }
    }

    val keystorePropertiesFile = rootProject.file("keystore.properties")
    val keystoreProperties = Properties()
    if (keystorePropertiesFile.exists()) {
        keystoreProperties.load(FileInputStream(keystorePropertiesFile))
    }

    val isReleaseTaskRequested = gradle.startParameter.taskNames.any { it.contains("Release", ignoreCase = true) }

    signingConfigs {
        create("release") {
            val resolvedStoreFile: File? = when {
                keystorePropertiesFile.exists() && !keystoreProperties.getProperty("storeFile").isNullOrBlank() -> {
                    val rawPath = keystoreProperties.getProperty("storeFile").removePrefix("../")
                    rootProject.file(rawPath)
                }
                !System.getenv("RELEASE_STORE_FILE").isNullOrBlank() -> {
                    rootProject.file(System.getenv("RELEASE_STORE_FILE")!!)
                }
                rootProject.file("release.keystore").exists() -> {
                    rootProject.file("release.keystore")
                }
                else -> null
            }

            val resolvedStorePassword = keystoreProperties.getProperty("storePassword")
                ?: System.getenv("RELEASE_STORE_PASSWORD")
                ?: System.getenv("KEYSTORE_PASSWORD")

            val resolvedKeyAlias = keystoreProperties.getProperty("keyAlias")
                ?: System.getenv("RELEASE_KEY_ALIAS")
                ?: System.getenv("KEY_ALIAS")

            val resolvedKeyPassword = keystoreProperties.getProperty("keyPassword")
                ?: System.getenv("RELEASE_KEY_PASSWORD")
                ?: System.getenv("KEY_PASSWORD")

            if (resolvedStoreFile != null && resolvedStoreFile.exists() &&
                !resolvedStorePassword.isNullOrBlank() &&
                !resolvedKeyAlias.isNullOrBlank() &&
                !resolvedKeyPassword.isNullOrBlank()
            ) {
                storeFile = resolvedStoreFile
                storePassword = resolvedStorePassword
                keyAlias = resolvedKeyAlias
                keyPassword = resolvedKeyPassword
            } else if (isReleaseTaskRequested) {
                throw GradleException(
                    "Production release signing failure: Production keystore and credentials are required for Release builds. " +
                    "Configure keystore.properties or environment variables (RELEASE_STORE_FILE, RELEASE_STORE_PASSWORD, RELEASE_KEY_ALIAS, RELEASE_KEY_PASSWORD). " +
                    "Silent fallback to debug signing is strictly disallowed."
                )
            } else {
                // Point to non-existent file for non-release tasks so release cannot accidentally build with debug keys
                storeFile = file("missing-release.keystore")
            }
        }
    }

    buildTypes {
        debug {
            isDebuggable = false
        }
        release {
            isDebuggable = true
            isMinifyEnabled = true
            isShrinkResources = true
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    testOptions {
        unitTests.isReturnDefaultValues = true
    }


    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
        jniLibs {
            useLegacyPackaging = true
        }
    }

    applicationVariants.all {
        val variant = this
        if (variant.buildType.name == "release") {
            variant.outputs.all {
                val output = this as? com.android.build.gradle.internal.api.BaseVariantOutputImpl
                output?.outputFileName = "ReelLibrary-release-v${variant.versionName}.apk"
            }
        }
    }
}

val copyReleaseApk by tasks.registering(Copy::class) {
    from(layout.buildDirectory.dir("outputs/apk/release"))
    include("*.apk")
    into(rootProject.layout.projectDirectory.dir("release"))
    rename { "ReelLibrary-release-v1.2.4.apk" }
}

tasks.matching { it.name == "assembleRelease" }.configureEach {
    finalizedBy(copyReleaseApk)
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.coil.compose)

    // Room Database + FTS5
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // WorkManager for background ingestion
    implementation(libs.androidx.work.runtime.ktx)

    // DataStore for Settings & Engine Preferences
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.security.crypto)

    // Networking & Serialization
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.coroutines.android)

    // MediaPipe GenAI for On-Device LLM Inference
    implementation("com.google.mediapipe:tasks-genai:0.10.14")

    // Google Sign-In & Google Play Services Auth for Google Drive Personal Cloud
    implementation("com.google.android.gms:play-services-auth:21.3.0")

    // Unit Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
