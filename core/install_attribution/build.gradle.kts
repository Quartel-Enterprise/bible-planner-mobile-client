plugins {
    alias(libs.plugins.bibleplanner.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    android {
        namespace = "com.quare.bibleplanner.core.installattribution"
        withHostTest {}
    }
    jvm()

    sourceSets {
        commonMain.dependencies {
            // Core
            implementation(projects.core.provider.dataStore)
            implementation(projects.core.provider.supabase)
            implementation(projects.core.utils)

            // Supabase
            implementation(project.dependencies.platform(libs.supabase.bom))
            implementation(libs.supabase.functions)

            // Ktor
            implementation(libs.ktor.client.core)

            // Serialization
            implementation(libs.kotlinx.serialization.json)

            // Coroutines
            implementation(libs.kotlinx.coroutines.core)

            // Logging
            implementation(libs.kermit)

            // DataStore
            implementation(libs.datastore)
            implementation(libs.datastore.preferences)

            // Koin
            implementation(project.dependencies.platform(libs.koin.bom))
            implementation(libs.koin.core)
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.ktor.client.mock)

            // Shared fakes
            implementation(projects.core.provider.dataStore.testing)
        }

        androidMain.dependencies {
            implementation(libs.koin.android)
            implementation(libs.install.referrer)
            implementation(libs.play.services.ads.identifier)
        }
    }
}
