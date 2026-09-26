package com.bibleplanner.buildlogic

import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTargetWithSimulatorTests
import org.jetbrains.kotlin.gradle.plugin.mpp.NativeBuildType
import org.jetbrains.kotlin.gradle.targets.native.tasks.KotlinNativeSimulatorTest

// The App Store screenshots are rendered in two steps. The iOS simulator renders each screen at
// the slot's logical size, so the listing shows the SF Symbols and Calf components the app really
// draws on iOS, and writes a PNG per slot and locale. Robolectric then frames that PNG with the
// same store-screenshots generators the Play listing uses. The Play shots never need the simulator.
private const val IOS_SIMULATOR_TARGET = "iosSimulatorArm64"
private const val CAPTURE_TEST_RUN = "appleScreenshots"
private const val CAPTURE_TASK = "iosSimulatorArm64AppleScreenshotsTest"
private const val CAPTURE_CLASS_PATTERN = "*AppleScreenshotCaptures"
private const val CAPTURE_OUTPUT_VARIABLE = "SIMCTL_CHILD_APPLE_SCREENSHOTS_OUTPUT"
private const val CAPTURES_DIRECTORY_PROPERTY = "appleScreenshots.capturesDir"
private const val HOST_TEST_TASK = "testAndroidHostTest"
private const val APP_STORE_TASK_SUFFIX = "AppStoreScreenshots"
private const val ALL_STORE_SCREENSHOTS_TASK = "collectStoreScreenshots"
private val appleFrameClassPatterns = listOf("*.IPhone*", "*.IPad*")

// Like the device tests' minSdk, decided from the tasks the build was asked for: only a build that
// produces the App Store shots runs the iPhone and iPad generators, and only that build needs a
// Mac with a simulator. Everything else (the Play listing, the README, a module's host tests on
// Linux) leaves them out.
private val Project.isGeneratingAppStoreScreenshots: Boolean
    get() = gradle.startParameter.taskNames.any { requested ->
        val taskName = requested.substringAfterLast(':')
        taskName.endsWith(APP_STORE_TASK_SUFFIX) || taskName == ALL_STORE_SCREENSHOTS_TASK
    }

fun Project.configureAppleStoreScreenshots(screenshotsName: String) {
    val capturesDir = rootProject.layout.buildDirectory
        .dir("outputs/apple-screenshot-captures/$screenshotsName")
        .get()
        .asFile
    extensions.configure<KotlinMultiplatformExtension> {
        targets.withType<KotlinNativeTargetWithSimulatorTests>().named(IOS_SIMULATOR_TARGET).configure {
            testRuns.create(CAPTURE_TEST_RUN) {
                setExecutionSourceFrom(binaries.getTest(NativeBuildType.DEBUG))
            }
        }
    }

    tasks.withType<KotlinNativeSimulatorTest>().configureEach {
        if (name == CAPTURE_TASK) {
            filter.includeTestsMatching(CAPTURE_CLASS_PATTERN)
            environment(CAPTURE_OUTPUT_VARIABLE, capturesDir.absolutePath)
            outputs.dir(capturesDir)
            // Wiped first so a renamed screen cannot leave a stale capture for Robolectric to frame.
            doFirst { capturesDir.deleteRecursively() }
        } else {
            filter.excludeTestsMatching(CAPTURE_CLASS_PATTERN)
        }
    }

    val generatesAppStoreScreenshots = isGeneratingAppStoreScreenshots
    tasks.withType<Test>().matching { task -> task.name == HOST_TEST_TASK }.configureEach {
        systemProperty(CAPTURES_DIRECTORY_PROPERTY, capturesDir.absolutePath)
        if (generatesAppStoreScreenshots) {
            dependsOn(CAPTURE_TASK)
            inputs.dir(capturesDir)
        } else {
            appleFrameClassPatterns.forEach(filter::excludeTestsMatching)
        }
    }
}
