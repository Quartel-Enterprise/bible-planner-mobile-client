import com.bibleplanner.buildlogic.getAndroidSdkVersions
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlinMultiplatformLibrary)
    alias(libs.plugins.bibleplanner.kotlin.composeMultiplatform)
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
            implementation(libs.compose.ui.toolingPreview)
            implementation(libs.androidx.activity.compose)

            // Koin
            implementation(libs.koin.android)

            // WorkManager
            implementation(libs.androidx.work.runtime.ktx)

            // Room: framework SQLite driver, so the Android app does not ship the
            // bundled libsqliteJni.so native library (missing-ABI-split crashes).
            implementation(libs.androidx.sqlite.framework)
        }

        iosMain.dependencies {
            implementation(libs.androidx.sqlite.bundled)
        }

        val jvmMain by getting {
            dependencies {
                implementation(libs.androidx.sqlite.bundled)
            }
        }
        commonMain.dependencies {
            // Feature
            api(projects.feature.materialYou)
            api(projects.feature.inAppUpdate)
            api(projects.feature.notificationPermission)
            api(projects.feature.preferences.themeSelection)
            api(projects.feature.readingPlan)
            api(projects.feature.day)
            api(projects.feature.deleteProgress)
            api(projects.feature.deleteAccount)
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
            api(projects.core.provider.platform)
            api(projects.core.provider.room)
            api(projects.core.provider.supabase)
            api(projects.core.provider.billing)
            api(projects.core.provider.analytics)
            api(projects.core.provider.crashlytics)
            api(projects.core.utils)
            implementation(projects.core.devices)

            // UI
            api(projects.ui.theme)
            api(projects.ui.utils)

            // Compose
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.ui.toolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)

            // Koin
            implementation(project.dependencies.platform(libs.koin.bom))
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)

            // Room
            implementation(libs.androidx.room.runtime)

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
