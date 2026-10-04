plugins {
    alias(libs.plugins.bibleplanner.kotlin.multiplatform)
    alias(libs.plugins.bibleplanner.kotlin.composeMultiplatform)
}

// The base of the screenshot tests: renders a screen under Robolectric in each theme, language and
// font scale, compares it with the image committed next to the test and runs the accessibility
// checks over it. Test-only, like :ui:testing, and added to a module's androidHostTest by
// configureScreenshotTests in build-logic. See docs/testing/screenshot-tests.md.
kotlin {
    android {
        namespace = "com.quare.bibleplanner.ui.screenshots.testing"
    }

    sourceSets {
        androidMain.dependencies {
            implementation(projects.ui.theme)
            implementation(projects.core.model)

            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)

            api(libs.androidx.compose.ui.testJunit4Android)
            api(libs.robolectric)
            api(libs.roborazzi)
            api(libs.roborazzi.compose)
            api(libs.roborazzi.accessibility.check)
        }
    }
}
