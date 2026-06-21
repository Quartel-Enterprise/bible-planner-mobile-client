import com.bibleplanner.buildlogic.getAndroidSdkVersions
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidKotlinMultiplatformLibrary)
    alias(libs.plugins.bibleplanner.composeMultiplatform)
}

kotlin {
    val androidSdkVersions = getAndroidSdkVersions()
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
            baseName = "Shared"
            isStatic = true
            export(projects.core.remoteConfig)
            export(projects.core.provider.analytics)
            export(projects.core.provider.crashlytics)
        }
    }

    jvm()

    sourceSets {
        androidMain.dependencies {
            implementation(projects.core.notification)
            implementation(libs.ui.tooling.preview)
            implementation(libs.androidx.activity.compose)

            // Koin
            implementation(libs.koinAndroid)

            // Facebook SDK
            implementation(libs.facebook.sdk.android)

            // WorkManager
            implementation(libs.androidx.work.runtime.ktx)
        }
        commonMain.dependencies {
            // Feature
            api(projects.feature.materialYou)
            api(projects.feature.notificationPermission)
            api(projects.feature.preferences.themeSelection)
            api(projects.feature.readingPlan)
            api(projects.feature.day)
            api(projects.feature.deleteProgress)
            api(projects.feature.login)
            api(projects.feature.logout)
            api(projects.feature.preferences.editPlanStartDate)
            api(projects.feature.preferences.bibleVersion)
            api(projects.feature.preferences.appLanguage)

            // Core
            api(projects.core.books)
            api(projects.core.model)
            api(projects.core.navigation)
            api(projects.core.plan)
            api(projects.core.sync)
            api(projects.core.remoteConfig)
            api(projects.core.provider.koin)
            api(projects.core.provider.language)
            api(projects.core.provider.room)
            api(projects.core.provider.supabase)
            api(projects.core.provider.billing)
            api(projects.core.provider.analytics)
            api(projects.core.provider.crashlytics)
            api(projects.core.utils)

            // UI
            api(projects.ui.theme)
            api(projects.ui.utils)

            // Compose
            implementation(libs.runtime)
            implementation(libs.foundation)
            implementation(libs.material3)
            implementation(libs.ui)
            implementation(libs.components.resources)
            implementation(libs.ui.tooling.preview)
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

            // Date
            implementation(libs.kotlinx.datetime)
        }
    }
}

// Exposes the generated resources accessor so platform application modules
// (e.g. desktopApp) can reference shared strings and drawables.
compose.resources {
    publicResClass = true
}

// Ensure resources from library modules are included
tasks.named<KotlinJvmCompile>("compileKotlinJvm") {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_21)
    }
}
