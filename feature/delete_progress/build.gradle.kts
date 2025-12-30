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
            baseName = "FeatureDeleteProgress"
            isStatic = true
        }
    }
    jvm()

    sourceSets {
        commonMain.dependencies {
            // Core
            implementation(projects.core.books)
            implementation(projects.core.model)
            implementation(projects.core.plan)

            // UI
            implementation(projects.ui.utils)

            // Compose
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)

            // Navigation
            implementation(libs.compose.navigation)

            // Koin
            implementation(project.dependencies.platform(libs.koinBom))
            implementation(libs.koinCore)
            implementation(libs.koinCompose)
            implementation(libs.koinComposeViewModel)
        }
    }
}

android {
    namespace = "com.quare.bibleplanner.feature.deleteprogress"
    compileSdk = project.property("compileSdkVersion").toString().toInt()
    
    defaultConfig {
        minSdk = project.property("minSdkVersion").toString().toInt()
    }
}
