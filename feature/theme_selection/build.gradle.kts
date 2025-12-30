plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.androidCommonConfig)
}



kotlin {
    androidTarget()



    listOf(
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "FeatureThemeSelection"
            isStatic = true
        }
    }
    jvm()

    sourceSets {
        commonMain.dependencies {
            // Feature
            implementation(projects.feature.materialYou)

            // Core
            implementation(projects.core.provider.platform)
            implementation(projects.core.model)

            // UI
            implementation(projects.ui.component)
            implementation(projects.ui.utils)
            implementation(projects.ui.theme)

            // Compose
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.materialIconsExtended)
            implementation(compose.components.resources)

            // Navigation
            implementation(libs.compose.navigation)

            // Koin
            implementation(project.dependencies.platform(libs.koinBom))
            implementation(libs.koinCore)
            implementation(libs.koinCompose)
            implementation(libs.koinComposeViewModel)

            // Data Store
            implementation(libs.dataStore)
            implementation(libs.dataStore.preferences)
        }
    }
}

android {
    namespace = "com.quare.bibleplanner.feature.themeselection"
    compileSdk = project.property("compileSdkVersion").toString().toInt()
    
    defaultConfig {
        minSdk = project.property("minSdkVersion").toString().toInt()
    }
}
