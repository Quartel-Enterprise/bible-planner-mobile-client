plugins {
    alias(libs.plugins.bibleplanner.kotlin.multiplatform)
    alias(libs.plugins.bibleplanner.kotlin.composeMultiplatform)
}

// Shared by the Compose UI tests of every module, as a test-only dependency: it sets a screen's
// content with the composition locals each platform's test host leaves out, and its Android manifest
// gives every device test APK the AdMob App ID that only the app's manifest has otherwise (see
// docs/testing/compose-ui-tests.md). It also carries the App Store screenshots' two halves: the iOS
// simulator capture and the image the Robolectric generators frame (see
// docs/store-listing-screenshots.md). ViewModel tests use its ViewModelTestStore, which clears the ViewModels
// and waits for their work before the main dispatcher is reset (see docs/testing/fakes-and-coroutines.md).
kotlin {
    android {
        namespace = "com.quare.bibleplanner.ui.testing"
    }

    jvm()

    sourceSets {
        commonMain.dependencies {
            api(libs.compose.ui.test)
            api(libs.kotlinx.coroutines.test)
            api(libs.androidx.lifecycle.viewmodel)
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.ui)
        }
    }
}
