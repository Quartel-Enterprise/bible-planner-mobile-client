plugins {
    alias(libs.plugins.bibleplanner.kotlin.multiplatform)
}

// Test-only: in-memory fakes of the plan domain that every module's tests share. Depend on it from
// a test source set.
kotlin {
    android {
        namespace = "com.quare.bibleplanner.core.plan.testing"
    }

    jvm()

    sourceSets {
        commonMain.dependencies {
            // Core
            api(projects.core.plan)
            implementation(projects.core.model)

            // Coroutines
            implementation(libs.kotlinx.coroutines.core)

            // Date
            implementation(libs.kotlinx.datetime)
        }
    }
}
