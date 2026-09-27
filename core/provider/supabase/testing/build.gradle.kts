plugins {
    alias(libs.plugins.bibleplanner.kotlin.multiplatform)
}

// Test-only: the supabase fakes every module's tests share. Depend on it from a test source set.
kotlin {
    android {
        namespace = "com.quare.bibleplanner.core.provider.supabase.testing"
    }

    jvm()

    sourceSets {
        commonMain.dependencies {
            // Supabase
            api(project.dependencies.platform(libs.supabase.bom))
            api(libs.supabase.realtime)

            // Serialization
            implementation(libs.kotlinx.serialization.json)

            // Coroutines
            implementation(libs.kotlinx.coroutines.core)
        }
    }
}
