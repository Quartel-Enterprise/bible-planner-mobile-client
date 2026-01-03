import com.bibleplanner.buildlogic.getAndroidSdkVersions
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidKotlinMultiplatformLibrary)
    alias(libs.plugins.bibleplanner.composeMultiplatform)
    alias(libs.plugins.composeHotReload)
}

kotlin {
    val androidSdkVersions = getAndroidSdkVersions()
    androidLibrary {
        compileSdk = androidSdkVersions.compileSdk
        minSdk = androidSdkVersions.minSdk
        namespace = "com.quare.bibleplanner.shared"
    }

    android {
        compileSdk = androidSdkVersions.compileSdk
        minSdk = androidSdkVersions.minSdk
        namespace = "com.quare.bibleplanner.shared"
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

    sourceSets {
        androidMain.dependencies {
            implementation(compose.preview)
            implementation(libs.androidx.activity.compose)

            // Koin
            implementation(libs.koinAndroid)

            // Facebook SDK
            implementation(libs.facebook.sdk.android)
        }
        commonMain.dependencies {
            // Feature
            api(projects.feature.materialYou)
            api(projects.feature.themeSelection)
            api(projects.feature.readingPlan)
            api(projects.feature.day)
            api(projects.feature.deleteProgress)
            api(projects.feature.editPlanStartDate)
            api(projects.feature.onboardingStartDate)

            // Core
            api(projects.core.books)
            api(projects.core.model)
            api(projects.core.navigation)
            api(projects.core.provider.koin)
            api(projects.core.provider.room)
            api(projects.core.provider.billing)

            // UI
            api(projects.ui.theme)
            api(projects.ui.utils)

            // Compose
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)

            // Navigation
            implementation(libs.compose.navigation)

            // Koin
            implementation(project.dependencies.platform(libs.koinBom))
            implementation(libs.koinCore)
            implementation(libs.koinCompose)
            implementation(libs.koinComposeViewModel)

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

compose.desktop {
    application {
        mainClass = "com.quare.bibleplanner.MainKt"

        jvmArgs += listOf("-Xdock:icon=${project.file("../icons/bible_planner_logo.icns").absolutePath}")

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "com.quare.bibleplanner"
            packageVersion = "1.3.0"

            macOS {
                iconFile.set(project.file("../icons/bible_planner_logo.icns"))
            }

            windows {
                iconFile.set(project.file("../icons/bible_planner_logo.ico"))
            }

            linux {
                iconFile.set(project.file("../icons/bible_planner_logo.png"))
            }
        }
    }
}

// Ensure resources from library modules are included
tasks.named<KotlinJvmCompile>("compileKotlinJvm") {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_21)
    }
}
