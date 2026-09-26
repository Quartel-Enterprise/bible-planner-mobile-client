plugins {
    alias(libs.plugins.bibleplanner.kotlin.multiplatform)
    alias(libs.plugins.bibleplanner.kotlin.composeMultiplatform)
}

kotlin {
    android {
        namespace = "com.quare.bibleplanner.feature.main"
        withHostTest {}
    }

    jvm()

    sourceSets {
        commonMain.dependencies {
            // Core
            implementation(projects.core.model)
            implementation(projects.core.profile)
            implementation(projects.core.utils)
            implementation(projects.core.provider.analytics)
            implementation(projects.core.provider.language)

            // UI
            implementation(projects.ui.component)
            implementation(projects.ui.utils)
            implementation(projects.ui.theme)

            // Compose
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.materialIconsExtended)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.material3.adaptiveNavigationSuite)

            // Calf
            implementation(libs.calf.ui)

            // Navigation 3
            implementation(libs.navigation3.ui)

            // Koin
            implementation(project.dependencies.platform(libs.koin.bom))
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.kotlinx.coroutines.test)
        }

        iosMain.dependencies {
            // Coil
            implementation(libs.coil.compose)
        }
    }
}
