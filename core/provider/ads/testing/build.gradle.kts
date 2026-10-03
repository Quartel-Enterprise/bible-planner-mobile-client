plugins {
    alias(libs.plugins.bibleplanner.kotlin.multiplatform)
}

// Test-only: in-memory fakes of the ads services that every module's tests share. Depend on it from
// a test source set. The ad SDKs never run in unit tests.
kotlin {
    android {
        namespace = "com.quare.bibleplanner.core.provider.ads.testing"
    }

    jvm()

    sourceSets {
        commonMain.dependencies {
            // Core
            api(projects.core.provider.ads)

            // Coroutines
            implementation(libs.kotlinx.coroutines.core)
        }
    }
}
