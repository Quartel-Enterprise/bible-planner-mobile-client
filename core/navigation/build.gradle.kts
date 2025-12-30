plugins {
    alias(libs.plugins.bibleplanner.kotlinMultiplatform)
    alias(libs.plugins.bibleplanner.composeMultiplatform)
    alias(libs.plugins.serialization)
    alias(libs.plugins.androidCommonConfig)
}

kotlin {
    androidLibrary {
        namespace = "com.quare.bibleplanner.core.navigation"
    }

    jvm()

    sourceSets {
        commonMain.dependencies {
            // Feature
            implementation(projects.feature.readingPlan)
            implementation(projects.feature.themeSelection)
            implementation(projects.feature.materialYou)
            implementation(projects.feature.day)
            implementation(projects.feature.deleteProgress)
            implementation(projects.feature.editPlanStartDate)
            implementation(projects.feature.onboardingStartDate)

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
