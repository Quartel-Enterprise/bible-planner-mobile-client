plugins {
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeMultiplatformConvention)
}

android {
    namespace = "quare.software.bibleplanner.core.navigation"
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            // Add navigation library dependency here when needed
            // e.g., Voyager, Decompose, or Compose Navigation
        }
    }
}

