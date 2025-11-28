import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.androidCommonConfig)
    alias(libs.plugins.composeHotReload)
}

android {
    namespace = "com.quare.bibleplanner"
    compileSdk = project.property("compileSdkVersion").toString().toInt()

    defaultConfig {
        applicationId = "com.quare.bibleplanner"
        minSdk = project.property("minSdkVersion").toString().toInt()
        targetSdk = project.property("targetSdkVersion").toString().toInt()
        versionCode = project.property("versionCode").toString().toInt()
        versionName = project.property("versionName").toString()
        ndk {
            debugSymbolLevel = "FULL"
        }
        vectorDrawables {
            useSupportLibrary = true
        }

        buildTypes {
            release {
                isMinifyEnabled = true
                proguardFiles(
                    getDefaultProguardFile("proguard-android-optimize.txt"),
                    "proguard-rules.pro",
                )
                signingConfig = signingConfigs.getByName("debug")
            }
        }
        buildFeatures {
            buildConfig = true
        }
        packaging {
            resources {
                excludes += "/META-INF/{AL2.0,LGPL2.1}"
            }
        }
        compileOptions {
            sourceCompatibility = JavaVersion.VERSION_21
            targetCompatibility = JavaVersion.VERSION_21
        }
    }
}

// Add project-specific dependencies
kotlin {
    sourceSets {
        androidTarget {
            compilerOptions {
                jvmTarget.set(JvmTarget.JVM_21)
            }
        }

        listOf(
            iosArm64(),
            iosSimulatorArm64(),
        ).forEach { iosTarget ->
            iosTarget.binaries.framework {
                baseName = "ComposeApp"
                isStatic = true
            }
        }

        jvm()

        androidMain.dependencies {
            implementation(compose.preview)
            implementation(libs.androidx.activity.compose)

            // Koin
            implementation(libs.koinAndroid)
        }
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)

            // Core
            implementation(projects.core.books)
            implementation(projects.core.navigation)
            implementation(projects.core.provider.koin)
            implementation(projects.core.provider.room)

            // UI
            implementation(projects.ui.theme)

            // Koin
            implementation(project.dependencies.platform(libs.koinBom))
            implementation(libs.koinCore)

            // Room
            implementation(libs.androidx.room.runtime)
            implementation(libs.androidx.sqlite.bundled)
        }
        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutines.swing)
        }
    }
}

// Override compose.desktop configuration
compose.desktop {
    application {
        mainClass = "com.quare.bibleplanner.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "com.quare.bibleplanner"
            packageVersion = "1.0.0"
        }
    }
}

// Ensure resources from library modules are included
tasks.named<KotlinJvmCompile>("compileKotlinJvm") {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_21)
    }
}
