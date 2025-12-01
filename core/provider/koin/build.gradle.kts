plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.androidCommonConfig)
}

kotlin {
    androidTarget()

    listOf(
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "KoinInitializer"
            isStatic = true
        }
    }

    jvm()

    sourceSets {
        commonMain.dependencies {
            // Core
            implementation(projects.core.provider.dataStore)
            implementation(projects.core.provider.room)
            implementation(projects.core.books)
            implementation(projects.core.plan)

            // Features
            implementation(projects.feature.readingPlan)
            implementation(projects.feature.themeSelection)
            implementation(projects.feature.day)

            // Koin
            implementation(project.dependencies.platform(libs.koinBom))
            implementation(libs.koinCore)
        }
    }
}

android {
    namespace = "com.quare.bibleplanner.core.provider.koin"
}
