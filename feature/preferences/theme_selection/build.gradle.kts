plugins {
    alias(libs.plugins.bibleplanner.kotlinMultiplatform)
    alias(libs.plugins.bibleplanner.composeMultiplatform)
}

kotlin {
    android {
        namespace = "com.quare.bibleplanner.feature.themeselection"
    }

    jvm()

    sourceSets {
        commonMain.dependencies {
            // Feature
            implementation(projects.feature.materialYou)

            // Core
            implementation(projects.core.provider.analytics)
            implementation(projects.core.provider.platform)
            implementation(projects.core.provider.room)
            implementation(projects.core.date)
            implementation(projects.core.model)
            implementation(projects.core.user)

            // UI
            implementation(projects.ui.component)
            implementation(projects.ui.utils)
            implementation(projects.ui.theme)

            // Compose
            implementation(libs.runtime)
            implementation(libs.foundation)
            implementation(libs.material3)
            implementation(libs.ui)
            implementation(libs.material.icons.extended)
            implementation(libs.components.resources)

            // Navigation 3
            implementation(libs.compose.navigation3.ui)

            // Koin
            implementation(project.dependencies.platform(libs.koinBom))
            implementation(libs.koinCore)
            implementation(libs.koinCompose)
            implementation(libs.koinComposeViewModel)

            // Data Store
            implementation(libs.dataStore)
            implementation(libs.dataStore.preferences)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}
