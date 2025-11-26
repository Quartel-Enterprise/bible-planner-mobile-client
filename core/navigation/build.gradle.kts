plugins {
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeMultiplatformConvention)
}

android {
    namespace = "com.quare.bibleplanner.core.navigation"
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            // Feature
            implementation(projects.feature.readingPlan)

            // Core
            implementation(projects.core.model)
            implementation(projects.core.provider.koin)

            // Navigation
            implementation(libs.compose.navigation)
        }
    }
}
