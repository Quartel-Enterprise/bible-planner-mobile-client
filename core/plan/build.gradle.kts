plugins {
    alias(libs.plugins.bibleplanner.kotlinMultiplatform)
    alias(libs.plugins.bibleplanner.composeMultiplatform)
    alias(libs.plugins.serialization)
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
            implementation(libs.runtime)
            implementation(libs.foundation)
            implementation(libs.material3)
            implementation(libs.ui)
            implementation(libs.components.resources)

            // Serialization
            implementation(libs.kotlin.serialization.json)

            // Coroutines
            implementation(libs.kotlinx.coroutines.core)

            // Koin
            implementation(project.dependencies.platform(libs.koinBom))
            implementation(libs.koinCore)

            // Dates
            implementation(libs.kotlinx.datetime)

            // DataStore (multiplatform)
            implementation(libs.dataStore)
            implementation(libs.dataStore.preferences)
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}
