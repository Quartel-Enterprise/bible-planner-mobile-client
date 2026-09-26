plugins {
    alias(libs.plugins.bibleplanner.kotlin.multiplatform)
    alias(libs.plugins.bibleplanner.kotlin.composeMultiplatform)
}

// Shared by the Compose UI tests of every module, as a test-only dependency: it sets a screen's
// content with the composition locals each platform's test host leaves out.
kotlin {
    android {
        namespace = "com.quare.bibleplanner.ui.testing"
    }

    jvm()

    sourceSets {
        commonMain.dependencies {
            api(libs.compose.ui.test)
            implementation(libs.compose.runtime)
            implementation(libs.compose.ui)
        }
    }
}
