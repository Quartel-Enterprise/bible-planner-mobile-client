plugins {
    alias(libs.plugins.bibleplanner.kotlin.multiplatform)
}

// Test-only: in-memory fakes of the books domain that every module's tests share. Depend on it from
// a test source set.
kotlin {
    android {
        namespace = "com.quare.bibleplanner.core.books.testing"
    }

    jvm()

    sourceSets {
        commonMain.dependencies {
            // Core
            api(projects.core.books)
            implementation(projects.core.model)

            // Coroutines
            implementation(libs.kotlinx.coroutines.core)
        }
    }
}
