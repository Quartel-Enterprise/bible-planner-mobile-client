package com.bibleplanner.buildlogic

import com.android.build.api.variant.KotlinMultiplatformAndroidComponentsExtension
import org.gradle.api.Project
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

// Screenshot tests render a module's screens under Robolectric, in its androidHostTest, and compare
// them with the PNGs committed in src/androidHostTest/screenshots. They share the host test task
// with the store screenshot generators, which also capture through Roborazzi and leave
// roborazzi.test.record on for every test in the module, so the mode is a property of their own
// that ScreenshotTest hands Roborazzi explicitly.
//
// -PscreenshotTests=verify compares, -PscreenshotTests=record rewrites the references, and either
// one runs the *ScreenshotTest classes alone. Without it they run with the module's other host
// tests and only render: a screen that stops composing or fails an accessibility check still
// fails, but nothing is compared, since the references are rendered on Linux and a Mac draws the
// same screen a few pixels apart. A build that renders the store or README screenshots leaves them
// out: those runs only want the generators. See docs/testing/screenshot-tests.md.
private const val SCREENSHOT_TEST_CLASS_PATTERN = "*ScreenshotTest"
private const val SCREENSHOT_TESTING_MODULE = ":ui:screenshots:testing"
private const val HOST_TEST_TASK = "testAndroidHostTest"
private const val HOST_TEST_SOURCE_SET = "androidHostTest"
private const val REFERENCES_DIRECTORY = "src/androidHostTest/screenshots"
private const val LIFECYCLE_TASK = "screenshotTests"
private const val MODE_GRADLE_PROPERTY = "screenshotTests"
private const val MODE_SYSTEM_PROPERTY = "screenshotTests.mode"
private const val VERIFY_MODE = "verify"
private const val RECORD_MODE = "record"
private const val TEST_HEAP = "4g"
private const val SCREENSHOT_GENERATION_TASK_SUFFIX = "Screenshots"

// Decided from the tasks the build was asked for, like the App Store generators in
// AppleStoreScreenshots.kt: collectPlayStoreScreenshots, stageAppStoreScreenshots,
// updateReadmeScreenshots and the rest all end the same way.
private val Project.isGeneratingScreenshots: Boolean
    get() = gradle.startParameter.taskNames.any { requested ->
        requested.substringAfterLast(':').endsWith(SCREENSHOT_GENERATION_TASK_SUFFIX)
    }

fun Project.configureScreenshotTests() {
    extensions.configure<KotlinMultiplatformExtension> {
        sourceSets.named(HOST_TEST_SOURCE_SET) {
            dependencies {
                implementation(project(SCREENSHOT_TESTING_MODULE))
                implementation(libs.findLibrary("androidx-compose-ui-testManifest").get())
            }
        }
    }

    // Without the module's Android resources on the Robolectric classpath its strings render empty,
    // and a screen without its text would be recorded as the reference. The module's own
    // withHostTest sets it, since AGP takes only one, so this checks it did.
    val modulePath = path
    extensions.getByType<KotlinMultiplatformAndroidComponentsExtension>().onVariants { variant ->
        variant.hostTests.values.forEach { hostTest ->
            check(hostTest.androidResourcesIncluded) {
                "$modulePath has screenshot tests, so its withHostTest {} must set isIncludeAndroidResources = true"
            }
        }
    }

    val mode = providers.gradleProperty(MODE_GRADLE_PROPERTY).orNull
    val generatesScreenshots = isGeneratingScreenshots
    val references = layout.projectDirectory.dir(REFERENCES_DIRECTORY)
    tasks.withType<Test>().matching { task -> task.name == HOST_TEST_TASK }.configureEach {
        maxHeapSize = TEST_HEAP
        when (mode) {
            VERIFY_MODE -> {
                filter.includeTestsMatching(SCREENSHOT_TEST_CLASS_PATTERN)
                systemProperty(MODE_SYSTEM_PROPERTY, VERIFY_MODE)
                inputs.files(references).withPathSensitivity(PathSensitivity.RELATIVE)
            }

            RECORD_MODE -> {
                filter.includeTestsMatching(SCREENSHOT_TEST_CLASS_PATTERN)
                systemProperty(MODE_SYSTEM_PROPERTY, RECORD_MODE)
                // Writes into the source tree, which Gradle cannot restore from a cache entry.
                outputs.upToDateWhen { false }
                outputs.cacheIf { false }
                // Wiped first so a renamed or removed state leaves no orphan reference behind.
                doFirst { references.asFile.deleteRecursively() }
            }

            null -> if (generatesScreenshots) {
                filter.excludeTestsMatching(SCREENSHOT_TEST_CLASS_PATTERN)
            }

            else -> error("-P$MODE_GRADLE_PROPERTY must be $VERIFY_MODE or $RECORD_MODE, not $mode")
        }
    }

    tasks.register(LIFECYCLE_TASK) {
        group = "verification"
        description = "Runs the screenshot tests; pass -P$MODE_GRADLE_PROPERTY=$VERIFY_MODE or $RECORD_MODE."
        dependsOn(HOST_TEST_TASK)
    }
}
