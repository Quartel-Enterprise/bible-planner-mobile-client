plugins {
    alias(libs.plugins.bibleplanner.kotlinMultiplatform)
}

kotlin {
    android {
        namespace = "com.quare.bibleplanner.core.sync"
    }

    jvm()

    sourceSets {
        commonMain.dependencies {
            // Core
            implementation(projects.core.user)
            implementation(projects.core.date)
            implementation(projects.core.utils)
            implementation(projects.core.provider.analytics)
            implementation(projects.core.provider.connectivity)
            implementation(projects.core.provider.supabase)

            // Coroutines
            implementation(libs.kotlinx.coroutines.core)

            // Supabase (realtime status for pull-on-connect)
            implementation(project.dependencies.platform(libs.supabase.bom))
            implementation(libs.supabase.realtime)

            // Koin
            implementation(project.dependencies.platform(libs.koinBom))
            implementation(libs.koinCore)

            // Logging
            implementation(libs.kermit)
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}
