plugins {
    alias(libs.plugins.bibleplanner.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.bibleplanner.kotlin.composeMultiplatform)
}

kotlin {
    android {
        namespace = "com.quare.bibleplanner.core.books"
    }

    jvm()

    sourceSets {
        commonMain.dependencies {
            // Core
            implementation(projects.core.model)
            implementation(projects.core.provider.dataStore)
            implementation(projects.core.provider.analytics)
            implementation(projects.core.provider.room)
            implementation(projects.core.provider.language)
            implementation(projects.core.utils)
            implementation(projects.core.remoteConfig)
            implementation(projects.core.provider.supabase)
            implementation(projects.core.provider.connectivity)
            implementation(projects.core.sync)
            implementation(projects.core.user)
            implementation(projects.core.date)

            // Room
            implementation(libs.androidx.room.runtime)

            // Supabase
            implementation(project.dependencies.platform(libs.supabase.bom))
            implementation(libs.supabase.storage)
            implementation(libs.supabase.auth)
            implementation(libs.supabase.postgrest)
            implementation(libs.supabase.realtime)

            // Compose
            implementation(libs.compose.runtime)
            implementation(libs.compose.components.resources)

            // Coroutines
            implementation(libs.kotlinx.coroutines.core)

            // DataStore
            implementation(libs.datastore.preferences)

            // Serialization
            implementation(libs.kotlinx.serialization.json)

            // Koin
            implementation(project.dependencies.platform(libs.koin.bom))
            implementation(libs.koin.core)

            // Logging
            implementation(libs.kermit)
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}
