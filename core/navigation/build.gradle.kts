plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.serialization)
    alias(libs.plugins.androidCommonConfig)
}

android {
    namespace = "com.quare.bibleplanner.core.navigation"
}

kotlin {
    androidTarget()

    listOf(
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "CoreNavigation"
            isStatic = true
        }
    }

    jvm()

    sourceSets {
        commonMain.dependencies {
            // Feature
            implementation(projects.feature.readingPlan)
            implementation(projects.feature.themeSelection)
            implementation(projects.feature.day)

            // Core
            implementation(projects.core.model)
            implementation(projects.core.provider.koin)
            implementation(projects.core.provider.room)

            // Compose
            implementation(compose.runtime)

            // Navigation
            implementation(libs.compose.navigation)
        }
    }
}
