plugins {
    alias(libs.plugins.android.test)
    alias(libs.plugins.androidx.baselineprofile)
}

// Generates :androidApp's Baseline Profile and measures the app's critical journeys. Nothing here
// runs on CI: both need a physical device nothing else is using. See docs/performance.md.
android {
    namespace = "com.quare.bibleplanner.baselineprofile"
    compileSdk = libs.versions.android.compileSdk
        .get()
        .toInt()

    defaultConfig {
        // Macrobenchmark needs API 28 to measure frames and to compile the app ahead of time
        minSdk = 28
        targetSdk = libs.versions.android.targetSdk
            .get()
            .toInt()
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    targetProjectPath = ":androidApp"
}

// A debug build of :androidApp is debuggable, which Macrobenchmark refuses to measure. Without this,
// a local `connectedDebugAndroidTest` would pick this module up and fail on it.
androidComponents {
    beforeVariants { variant ->
        variant.enable = variant.buildType != "debug"
    }
}

baselineProfile {
    // Runs against the device ANDROID_SERIAL names instead of a Gradle managed device
    useConnectedDevices = true
}

dependencies {
    implementation(libs.androidx.benchmark.macroJunit4)
    implementation(libs.androidx.test.ext.junit)
    implementation(libs.androidx.test.uiautomator)
}
