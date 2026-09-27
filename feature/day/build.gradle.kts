import com.bibleplanner.buildlogic.configureAppleStoreScreenshots

plugins {
    alias(libs.plugins.bibleplanner.kotlin.multiplatform)
    alias(libs.plugins.bibleplanner.kotlin.composeMultiplatform)
}

kotlin {
    android {
        namespace = "com.quare.bibleplanner.feature.day"
        withHostTest {
            isIncludeAndroidResources = true
            isReturnDefaultValues = true
        }
    }

    jvm()

    sourceSets {
        commonMain.dependencies {
            // Core
            implementation(projects.core.books)
            implementation(projects.core.plan)
            implementation(projects.core.model)
            implementation(projects.core.dayStudy)
            implementation(projects.core.utils)
            implementation(projects.core.date)
            implementation(projects.core.provider.analytics)
            implementation(projects.core.provider.platform)
            implementation(projects.core.provider.room)
            implementation(projects.core.provider.billing)
            implementation(projects.core.loginNudge)

            // UI
            implementation(projects.ui.component)
            implementation(projects.ui.utils)

            // Compose
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.materialIconsExtended)
            implementation(libs.compose.components.resources)

            // Navigation 3
            implementation(libs.navigation3.ui)

            // DateTime
            implementation(libs.kotlinx.datetime)

            // Koin
            implementation(project.dependencies.platform(libs.koin.bom))
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)

            // Kermit
            implementation(libs.kermit)

            // Calf
            implementation(libs.calf.ui)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.kotlinx.coroutines.test)
            implementation(projects.core.remoteConfig)
            // The store screenshots draw the study card the day screen gets from the root
            implementation(projects.feature.dayStudy)
            implementation(projects.ui.theme)

            // Shared fakes
            implementation(projects.core.books.testing)
            implementation(projects.core.dayStudy.testing)
            implementation(projects.core.plan.testing)
        }

        jvmTest.dependencies {
            implementation(libs.androidx.room.runtime)
            implementation(libs.androidx.sqlite.bundled)
        }

        getByName("androidHostTest").dependencies {
            implementation(libs.storeScreenshots.library)
            implementation(libs.androidx.compose.ui.testManifest)
        }
    }
}

tasks.withType<Test>().configureEach {
    // Declared as an output so deleting the collected screenshots makes this task out of date;
    // each module owns its own directory so the four generators never overlap.
    val screenshotsDir = rootProject.layout.buildDirectory.dir("outputs/store-screenshots/day")
    systemProperty("storeScreenshots.outputRoot", screenshotsDir.get().asFile.absolutePath)
    systemProperty("roborazzi.test.record", "true")
    outputs.dir(screenshotsDir)
}

// An iOS test binary carries the Compose resources of the module's production dependencies only,
// so the study card, which :feature:day_study reaches the tests through commonTest, would render
// without its strings on the simulator. Its resources join the test binary's by hand.
tasks.named<Copy>("copyTestComposeResourcesForIosSimulatorArm64") {
    from(project(":feature:day_study").tasks.named("iosSimulatorArm64AggregateResources")) {
        include("composeResources/bibleplanner.feature.day_study.generated.resources/**")
    }
}

configureAppleStoreScreenshots(screenshotsName = "day")
