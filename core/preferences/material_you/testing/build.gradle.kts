plugins {
    alias(libs.plugins.bibleplanner.kotlin.multiplatform)
}

// Test-only: the in-memory repository every module's tests share. Depend on it from a test source
// set.
kotlin {
    android {
        namespace = "com.quare.bibleplanner.core.preferences.materialyou.testing"
    }

    jvm()

    sourceSets {
        commonMain.dependencies {
            // Core
            api(projects.core.preferences.materialYou)
            implementation(projects.core.model)

            // Coroutines
            implementation(libs.kotlinx.coroutines.core)
        }
    }
}
