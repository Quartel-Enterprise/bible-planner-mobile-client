plugins {
    alias(libs.plugins.bibleplanner.kotlinMultiplatform)
    alias(libs.plugins.serialization)
}

kotlin {
    android {
        namespace = "com.quare.bibleplanner.core.devices"
    }
    jvm()

    sourceSets {
        commonMain.dependencies {
            // Core
            implementation(projects.core.user)
            implementation(projects.core.provider.dataStore)
            implementation(projects.core.utils)
            implementation(projects.core.date)
            implementation(projects.core.sync)
            implementation(projects.core.provider.supabase)
            implementation(projects.core.provider.room)
            implementation(projects.core.provider.connectivity)

            // Supabase
            implementation(project.dependencies.platform(libs.supabase.bom))
            implementation(libs.supabase.auth)
            implementation(libs.supabase.postgrest)
            implementation(libs.supabase.realtime)
            implementation(libs.supabase.functions)

            // Ktor
            implementation(libs.ktor.client.core)

            // Serialization
            implementation(libs.kotlin.serialization.json)

            // Coroutines
            implementation(libs.kotlinx.coroutines.core)

            // Logging
            implementation(libs.kermit)

            // DataStore
            implementation(libs.dataStore)
            implementation(libs.dataStore.preferences)

            // Koin
            implementation(project.dependencies.platform(libs.koinBom))
            implementation(libs.koinCore)
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}
