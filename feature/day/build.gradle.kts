plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.androidCommonConfig)
}

android {
    namespace = "com.quare.bibleplanner.feature.day"
}

kotlin {

    androidTarget()

    listOf(
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "FeatureDay"
            isStatic = true
        }
    }
    jvm()

    sourceSets {
        commonMain.dependencies {
            // Core
            implementation(projects.core.books)
            implementation(projects.core.plan)
            implementation(projects.core.model)
            implementation(projects.core.utils)
            implementation(projects.core.date)
            implementation(projects.core.provider.room)

            // UI
            implementation(projects.ui.component)
            implementation(projects.ui.utils)

            // Compose
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.materialIconsExtended)
            implementation(compose.components.resources)

            // Navigation
            implementation(libs.compose.navigation)

            // DateTime
            implementation(libs.kotlinx.datetime)

            // Koin
            implementation(project.dependencies.platform(libs.koinBom))
            implementation(libs.koinCore)
            implementation(libs.koinCompose)
            implementation(libs.koinComposeViewModel)
        }
    }
}
