plugins {
    alias(libs.plugins.bibleplanner.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    android {
        namespace = "com.quare.bibleplanner.core.daystudy"
        withHostTest {}
    }

    jvm()

    sourceSets {
        commonMain.dependencies {
            // Core
            implementation(projects.core.books)
            implementation(projects.core.clear)
            implementation(projects.core.model)
            implementation(projects.core.plan)
            implementation(projects.core.remoteConfig)
            implementation(projects.core.utils)
            implementation(projects.core.provider.analytics)
            implementation(projects.core.provider.billing)
            implementation(projects.core.provider.connectivity)
            implementation(projects.core.provider.dataStore)
            implementation(projects.core.provider.language)
            implementation(projects.core.provider.room)
            implementation(projects.core.provider.supabase)

            // Supabase
            implementation(project.dependencies.platform(libs.supabase.bom))
            implementation(libs.supabase.functions)
            implementation(libs.supabase.postgrest)

            // Ktor
            implementation(libs.ktor.client.core)

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
            implementation(libs.ktor.client.mock)
            implementation(projects.core.date)
        }
        jvmTest.dependencies {
            implementation(libs.androidx.room.runtime)
            implementation(libs.androidx.sqlite.bundled)
        }
    }
}
