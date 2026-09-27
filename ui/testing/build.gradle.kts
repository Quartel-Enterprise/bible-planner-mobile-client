plugins {
    alias(libs.plugins.bibleplanner.kotlin.multiplatform)
    alias(libs.plugins.bibleplanner.kotlin.composeMultiplatform)
}

// Shared by the Compose UI tests of every module, as a test-only dependency: it sets a screen's
// content with the composition locals each platform's test host leaves out. It also carries the
// App Store screenshots' two halves: the iOS simulator capture and the image the Robolectric
// generators frame (see docs/store-listing-screenshots.md).
kotlin {
    android {
        namespace = "com.quare.bibleplanner.ui.testing"
    }

    jvm()

    sourceSets {
        commonMain.dependencies {
            api(libs.compose.ui.test)
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.ui)
        }
    }
}
