plugins {
    alias(libs.plugins.bibleplanner.kotlin.multiplatform)
}

// Test-only: in-memory DAOs every module's tests share. Depend on it from a test source set.
kotlin {
    android {
        namespace = "com.quare.bibleplanner.core.provider.room.testing"
    }

    jvm()

    sourceSets {
        commonMain.dependencies {
            // Core
            api(projects.core.provider.room)
            implementation(projects.core.model)

            // Room
            implementation(libs.androidx.room.runtime)

            // Coroutines
            implementation(libs.kotlinx.coroutines.core)
        }
    }
}
