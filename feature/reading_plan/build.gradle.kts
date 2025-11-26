plugins {
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeMultiplatformConvention)
}
kotlin {
    sourceSets {
        commonMain.dependencies {
            // Core
            implementation(projects.core.model)

            // Compose
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
    namespace = "com.quare.bibleplanner.feature.readingplan"
}
