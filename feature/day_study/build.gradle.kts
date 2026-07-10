plugins {
    alias(libs.plugins.bibleplanner.kotlinMultiplatform)
    alias(libs.plugins.bibleplanner.composeMultiplatform)
    alias(libs.plugins.serialization)
}

kotlin {
    android {
        namespace = "com.quare.bibleplanner.feature.daystudy"
    }

    jvm()

    sourceSets {
        commonMain.dependencies {
            // Core
            implementation(projects.core.clear)
            implementation(projects.core.model)
            implementation(projects.core.utils)
            implementation(projects.core.books)
            implementation(projects.core.user)
            implementation(projects.core.provider.language)
            implementation(projects.core.provider.room)
            implementation(projects.core.provider.supabase)
            implementation(projects.core.provider.billing)
            implementation(projects.core.provider.connectivity)
            implementation(projects.core.provider.analytics)
            implementation(projects.core.remoteConfig)

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

            // Supabase
            implementation(project.dependencies.platform(libs.supabase.bom))
            implementation(libs.supabase.functions)
            implementation(libs.supabase.postgrest)

            // Ktor
            implementation(libs.ktor.client.core)

            // Serialization
            implementation(libs.kotlin.serialization.json)

            // Koin
            implementation(project.dependencies.platform(libs.koinBom))
            implementation(libs.koinCore)
            implementation(libs.koinCompose)
            implementation(libs.koinComposeViewModel)

            // Logging
            implementation(libs.kermit)
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}
