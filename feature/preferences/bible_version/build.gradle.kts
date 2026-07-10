plugins {
    alias(libs.plugins.bibleplanner.kotlinMultiplatform)
    alias(libs.plugins.bibleplanner.composeMultiplatform)
    alias(libs.plugins.serialization)
}

kotlin {
    android {
        namespace = "com.quare.bibleplanner.feature.bibleversion"
    }

    jvm()

    sourceSets {
        commonMain.dependencies {
            // Core
            implementation(projects.core.provider.language)
            implementation(projects.core.provider.platform)
            implementation(projects.core.provider.room)
            implementation(projects.core.provider.supabase)
            implementation(projects.core.provider.analytics)
            implementation(projects.core.model)
            implementation(projects.core.books)
            implementation(projects.core.network)
            implementation(projects.core.utils)

            // UI
            implementation(projects.ui.component)
            implementation(projects.ui.utils)
            implementation(projects.ui.theme)
            implementation(libs.material.icons.extended)

            // Compose
            implementation(libs.runtime)
            implementation(libs.foundation)
            implementation(libs.material3)
            implementation(libs.ui)
            implementation(libs.components.resources)

            // Navigation 3
            implementation(libs.compose.navigation3.ui)

            // Koin
            implementation(project.dependencies.platform(libs.koinBom))
            implementation(libs.koinCore)
            implementation(libs.koinCompose)
            implementation(libs.koinComposeViewModel)

            // Coroutines
            implementation(libs.kotlinx.coroutines.core)

            // Kermit
            implementation(libs.kermit)

            // Supabase
            implementation(project.dependencies.platform(libs.supabase.bom))
            implementation(libs.supabase.storage)

            // Data Store
            implementation(libs.dataStore)
            implementation(libs.dataStore.preferences)
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}
