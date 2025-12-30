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
            baseName = "FeatureReadingPlan"
            isStatic = true
        }
    }
    jvm()

    sourceSets {
        commonMain.dependencies {
            // Core
            implementation(projects.core.books)
            implementation(projects.core.date)
            implementation(projects.core.plan)
            implementation(projects.core.model)
            implementation(projects.core.provider.dataStore)
            implementation(projects.core.utils)

            // Features
            implementation(projects.feature.onboardingStartDate)

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

            // Koin
            implementation(project.dependencies.platform(libs.koinBom))
            implementation(libs.koinCore)
            implementation(libs.koinCompose)
            implementation(libs.koinComposeViewModel)

            // Data Store
            implementation(libs.dataStore)
            implementation(libs.dataStore.preferences)

            // Dates
            implementation(libs.kotlinx.datetime)
        }
    }
}

android {
    namespace = "com.quare.bibleplanner.feature.readingplan"
    compileSdk = project.property("compileSdkVersion").toString().toInt()
    
    defaultConfig {
        minSdk = project.property("minSdkVersion").toString().toInt()
    }
}
