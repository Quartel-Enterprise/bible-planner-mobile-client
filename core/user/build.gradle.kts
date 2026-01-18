plugins {
    alias(libs.plugins.bibleplanner.kotlinMultiplatform)
    alias(libs.plugins.serialization)
}

kotlin {
    androidLibrary {
        namespace = "com.quare.bibleplanner.core.user"
    }
    jvm()
    sourceSets {
        commonMain.dependencies {
            // Core
            implementation(projects.core.model)

            // Json
            implementation(libs.kotlin.serialization.json)

            // Supabase
            implementation(project.dependencies.platform(libs.supabase.bom))
            implementation(libs.supabase.auth)
            implementation(libs.supabase.compose.auth)
            implementation(libs.supabase.compose.auth.ui)

            // Koin
            implementation(project.dependencies.platform(libs.koinBom))
            implementation(libs.koinCore)
        }
    }
}
