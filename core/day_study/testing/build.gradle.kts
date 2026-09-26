plugins {
    alias(libs.plugins.bibleplanner.kotlin.multiplatform)
}

// Test-only: in-memory fakes of the day study domain that every module's tests share. Depend on it
// from a test source set.
kotlin {
    android {
        namespace = "com.quare.bibleplanner.core.daystudy.testing"
    }

    jvm()

    sourceSets {
        commonMain.dependencies {
            // Core
            api(projects.core.dayStudy)
            implementation(projects.core.model)

            // Coroutines
            implementation(libs.kotlinx.coroutines.core)
        }
    }
}
