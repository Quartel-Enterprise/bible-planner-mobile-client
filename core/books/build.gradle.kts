plugins {
    alias(libs.plugins.bibleplanner.kotlinMultiplatform)
    alias(libs.plugins.serialization)
    alias(libs.plugins.bibleplanner.composeMultiplatform)
}

kotlin {
    androidLibrary {
        namespace = "com.quare.bibleplanner.core.books"
    }

    jvm()

    sourceSets {
        commonMain.dependencies {
            // Core
            implementation(projects.core.model)
            implementation(projects.core.provider.room)
            implementation(projects.core.utils)
            implementation(projects.core.remoteConfig)
            implementation(projects.core.provider.supabase)

            // Room
            implementation(libs.androidx.room.runtime)

            // Supabase
            implementation(project.dependencies.platform(libs.supabase.bom))
            implementation(libs.supabase.storage)

            // Compose
            implementation(libs.runtime)
            implementation(libs.components.resources)

            // Coroutines
            implementation(libs.kotlinx.coroutines.core)

            // DataStore
            implementation(libs.dataStore.preferences)

            // Serialization
            implementation(libs.kotlin.serialization.json)

            // Koin
            implementation(project.dependencies.platform(libs.koinBom))
            implementation(libs.koinCore)
        }
    }
}
