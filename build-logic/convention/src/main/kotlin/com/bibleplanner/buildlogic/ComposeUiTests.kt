package com.bibleplanner.buildlogic

import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryExtension
import com.android.build.api.variant.KotlinMultiplatformAndroidComponentsExtension
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.withType
import org.jetbrains.compose.ComposePlugin
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

// Compose UI tests are written once, in commonTest, with runComposeUiTest. They run on the jvm
// target and the iOS simulator in every Compose module, and on an Android device in the modules
// that have a src/androidDeviceTest directory. scripts/ui_test_shard.sh finds those modules by
// the same directory, so the build and the CI workflow never disagree on which ones they are.
private const val UI_TEST_CLASS_PATTERN = "*UiTest"
private const val UI_TESTING_MODULE = ":ui:testing"
private const val DEVICE_TEST_SOURCE_SET = "androidDeviceTest"
private const val INSTRUMENTATION_RUNNER = "androidx.test.runner.AndroidJUnitRunner"
private const val DEVICE_TEST_TASK_SUFFIX = "AndroidDeviceTest"
private const val DEVICE_TEST_MIN_SDK = 30
private const val UI_TESTS_PROPERTY = "uiTests"
private const val UI_TESTS_EXCLUDED = "exclude"
private const val UI_TESTS_ONLY = "only"

private val Project.hasDeviceTests: Boolean
    get() = file("src/$DEVICE_TEST_SOURCE_SET").isDirectory

private val Project.uiTestsSelection: String?
    get() = providers.gradleProperty(UI_TESTS_PROPERTY).orNull

private val Project.isBuildingDeviceTests: Boolean
    get() = gradle.startParameter.taskNames.any { taskName -> taskName.endsWith(DEVICE_TEST_TASK_SUFFIX) }

fun Project.configureComposeUiTests() {
    if (path == UI_TESTING_MODULE) return
    extensions.configure<KotlinMultiplatformExtension> {
        sourceSets.named("commonTest") {
            dependencies {
                // The ui-test API, and setUiTestContent, which fills in what each platform's test
                // host leaves out.
                implementation(project(UI_TESTING_MODULE))
            }
        }
        // Skiko's native library for the machine running the tests: the jvm target renders
        // through it, and no Compose artifact pulls in a platform-specific one on its own.
        sourceSets.matching { sourceSet -> sourceSet.name == "jvmTest" }.configureEach {
            dependencies {
                implementation(ComposePlugin.DesktopDependencies.currentOs)
            }
        }
        if (hasDeviceTests) {
            (this as ExtensionAware).extensions.configure<KotlinMultiplatformAndroidLibraryExtension>(
                "androidLibrary",
            ) {
                // The "test" tree makes androidDeviceTest depend on commonTest, as jvmTest and
                // iosTest already do, so the same tests run on the device.
                withDeviceTestBuilder { sourceSetTreeName = "test" }.configure {
                    instrumentationRunner = INSTRUMENTATION_RUNNER
                }
            }
            // Test names are backticked sentences, and D8 accepts a space in a method name only
            // from the DEX format of API 30 on. AGP dexes the test APK at the library's minSdk and
            // has no setting of its own for it, so a build that asks for device tests raises the
            // minSdk of the modules that have them. Only that build: the app, which still supports
            // API 26, never builds against the raised value. It happens in finalizeDsl, after the
            // module's own build script, so a module that sets its minSdk there (as :shared does)
            // doesn't put it back.
            if (isBuildingDeviceTests) {
                project.extensions.getByType<KotlinMultiplatformAndroidComponentsExtension>().finalizeDsl { android ->
                    android.minSdk = DEVICE_TEST_MIN_SDK
                }
            }
            sourceSets.matching { sourceSet -> sourceSet.name == DEVICE_TEST_SOURCE_SET }.configureEach {
                dependencies {
                    implementation(libs.findLibrary("androidx-test-runner").get())
                    // Compose's ui-test pulls an Espresso that calls InputManager.getInstance, which
                    // Android 16 removed; 3.7 no longer does.
                    implementation(libs.findLibrary("androidx-test-espresso-core").get())
                    implementation(libs.findLibrary("androidx-compose-ui-testJunit4Android").get())
                    implementation(libs.findLibrary("androidx-compose-ui-testManifest").get())
                }
            }
        }
    }

    // The Android host has no Instrumentation to launch the test activity with, so the UI tests
    // that commonTest hands it can't run there. They run on a device instead. A module whose tests
    // are all UI tests, like :shared with its end-to-end flows, is left with none, which is fine.
    tasks.withType<Test>().matching { task -> task.name == "testAndroidHostTest" }.configureEach {
        filter.excludeTestsMatching(UI_TEST_CLASS_PATTERN)
        filter.isFailOnNoMatchingTests = false
    }

    // -PuiTests=exclude leaves jvmTest with the unit tests alone, for CI's unit-tests job and the
    // coverage rules. -PuiTests=only keeps only the UI tests, for the desktop job of ui-tests.
    // Without it jvmTest runs both, so a local run and the IDE still see every test. A property
    // and not --tests, which applies only to the task right before it on the command line.
    val selection = uiTestsSelection
    tasks.withType<Test>().matching { task -> task.name == "jvmTest" }.configureEach {
        when (selection) {
            UI_TESTS_EXCLUDED -> {
                filter.excludeTestsMatching(UI_TEST_CLASS_PATTERN)
                filter.isFailOnNoMatchingTests = false
            }

            UI_TESTS_ONLY -> {
                filter.includeTestsMatching(UI_TEST_CLASS_PATTERN)
                filter.isFailOnNoMatchingTests = false
            }

            null -> Unit

            else -> error("-P$UI_TESTS_PROPERTY must be $UI_TESTS_EXCLUDED or $UI_TESTS_ONLY, not $selection")
        }
    }
}
