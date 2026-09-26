plugins {
    alias(libs.plugins.bibleplanner.kotlin.multiplatform)
}

// Test-only: the in-memory repository every module's tests share. Depend on it from a test source
// set.
kotlin {
    android {
        namespace = "com.quare.bibleplanner.core.preferences.themeselection.testing"
    }

    jvm()

    sourceSets {
        commonMain.dependencies {
            // Core
            api(projects.core.preferences.themeSelection)
            implementation(projects.core.model)

            // Coroutines
            implementation(libs.kotlinx.coroutines.core)
        }
    }
}
