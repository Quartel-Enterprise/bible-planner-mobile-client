plugins {
    alias(libs.plugins.bibleplanner.kotlin.multiplatform)
    alias(libs.plugins.bibleplanner.kotlin.composeMultiplatform)
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    android {
        namespace = "com.quare.bibleplanner.core.plan"
    }

    jvm()

    sourceSets {
        commonMain.dependencies {
            // Core
            implementation(projects.core.books)
            implementation(projects.core.provider.dataStore)
            implementation(projects.core.model)
            implementation(projects.core.provider.analytics)
            implementation(projects.core.utils)
            implementation(projects.core.date)
            implementation(projects.core.remoteConfig)
            implementation(projects.core.sync)
            implementation(projects.core.user)
            implementation(projects.core.provider.room)
            implementation(projects.core.provider.supabase)
            implementation(projects.core.provider.connectivity)

            // Room
            implementation(libs.androidx.room.runtime)

            // Supabase
            implementation(project.dependencies.platform(libs.supabase.bom))
            implementation(libs.supabase.postgrest)
            implementation(libs.supabase.realtime)

            // Compose
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)

            // Serialization
            implementation(libs.kotlinx.serialization.json)

            // Coroutines
            implementation(libs.kotlinx.coroutines.core)

            // Koin
            implementation(project.dependencies.platform(libs.koin.bom))
            implementation(libs.koin.core)

            // Dates
            implementation(libs.kotlinx.datetime)

            // DataStore (multiplatform)
            implementation(libs.datastore)
            implementation(libs.datastore.preferences)
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}
