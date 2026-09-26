plugins {
    alias(libs.plugins.bibleplanner.kotlin.multiplatform)
}

// Test-only: the in-memory DataStore every module's tests share. Depend on it from a test source set.
kotlin {
    android {
        namespace = "com.quare.bibleplanner.core.provider.datastore.testing"
    }

    jvm()

    sourceSets {
        commonMain.dependencies {
            api(projects.core.provider.dataStore)
            implementation(libs.kotlinx.coroutines.core)
        }
    }
}
