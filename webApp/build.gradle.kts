import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.bibleplanner.kotlin.composeMultiplatform)
}

kotlin {
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        outputModuleName = "webApp"
        browser {
            commonWebpackConfig {
                outputFileName = "webApp.js"
            }
        }
        binaries.executable()
    }

    sourceSets {
        wasmJsMain.dependencies {
            implementation(projects.shared)
            implementation(projects.core.books)
            implementation(projects.core.model)
            implementation(projects.core.provider.crashlytics)
            implementation(projects.core.provider.language)
            implementation(projects.core.provider.platform)
            implementation(projects.core.provider.room)
            implementation(projects.feature.login)
            implementation(projects.feature.preferences.appLanguage)
            implementation(projects.feature.preferences.bibleVersion)

            implementation(libs.compose.runtime)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)

            implementation(project.dependencies.platform(libs.koin.bom))
            implementation(libs.koin.core)

            implementation(libs.androidx.room.runtime)
        }
    }
}
