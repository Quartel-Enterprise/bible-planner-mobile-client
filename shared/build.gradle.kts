import com.bibleplanner.buildlogic.getAndroidSdkVersions
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.targets.native.tasks.KotlinNativeSimulatorTest
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
        experimentalProperties["android.experimental.kmp.enableAndroidResources"] = true
        withHostTest {}
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

            implementation(libs.kermit)

            // Room: framework SQLite driver, so the Android app does not ship the
            // bundled libsqliteJni.so native library (missing-ABI-split crashes).
            implementation(libs.androidx.sqlite.framework)
        }

        iosMain.dependencies {
            implementation(libs.androidx.sqlite.bundled)
        }

        jvmMain.dependencies {
            implementation(libs.androidx.sqlite.bundled)
        }
        commonMain.dependencies {
            // Feature
            api(projects.feature.materialYou)
            api(projects.feature.inAppUpdate)
            api(projects.feature.notificationPermission)
            api(projects.feature.preferences.themeSelection)
            api(projects.feature.preferences.studySuggestion)
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

        // The end-to-end flows: the whole app, with only the outside world faked through Koin.
        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.kotlinx.coroutines.test)
            implementation(projects.core.date)
            implementation(projects.core.provider.connectivity)
            implementation(projects.core.provider.dataStore)
            implementation(libs.ktor.client.mock)
            implementation(project.dependencies.platform(libs.supabase.bom))
            implementation(libs.supabase.auth)
            implementation(libs.supabase.functions)
            implementation(libs.supabase.postgrest)
            implementation(libs.supabase.realtime)
            implementation(libs.supabase.storage)
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

// The end-to-end flows switch tabs, and on iOS the main tabs are Calf's native UITabBar, outside the
// Compose semantics tree, so they run on the JVM and on Android only.
tasks.withType<KotlinNativeSimulatorTest>().configureEach {
    filter.excludeTestsMatching("com.quare.bibleplanner.e2e.*")
}
