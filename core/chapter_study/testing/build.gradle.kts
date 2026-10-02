plugins {
    alias(libs.plugins.bibleplanner.kotlin.multiplatform)
}

// Test-only: in-memory fakes of the chapter study domain that every module's tests share. Depend on
// it from a test source set.
kotlin {
    android {
        namespace = "com.quare.bibleplanner.core.chapterstudy.testing"
    }

    jvm()

    sourceSets {
        commonMain.dependencies {
            // Core
            api(projects.core.chapterStudy)
            implementation(projects.core.model)

            // Coroutines
            implementation(libs.kotlinx.coroutines.core)
        }
    }
}
