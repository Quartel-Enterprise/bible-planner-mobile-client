plugins {
    alias(libs.plugins.bibleplanner.kotlin.multiplatform)
    alias(libs.plugins.bibleplanner.kotlin.composeMultiplatform)
}

kotlin {
    android {
        namespace = "com.quare.bibleplanner.feature.readingplan"
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
            implementation(projects.core.date)
            implementation(projects.core.plan)
            implementation(projects.core.model)
            implementation(projects.core.provider.analytics)
            implementation(projects.core.provider.dataStore)
            implementation(projects.core.utils)
            implementation(projects.core.loginNudge)
            implementation(projects.core.review)

            // Features

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

            // Koin
            implementation(project.dependencies.platform(libs.koin.bom))
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)

            // Data Store
            implementation(libs.datastore)
            implementation(libs.datastore.preferences)

            // Dates
            implementation(libs.kotlinx.datetime)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
        getByName("androidHostTest").dependencies {
            implementation(projects.ui.theme)
            implementation(libs.storeScreenshots.library)
            implementation(libs.androidx.compose.ui.testManifest)
        }
    }
}

tasks.withType<Test>().configureEach {
    // Declared as an output so deleting the collected screenshots makes this task out of date;
    // each module owns its own directory so the four generators never overlap.
    val screenshotsDir = rootProject.layout.buildDirectory.dir("outputs/store-screenshots/reading_plan")
    systemProperty("storeScreenshots.outputRoot", screenshotsDir.get().asFile.absolutePath)
    systemProperty("roborazzi.test.record", "true")
    outputs.dir(screenshotsDir)
}
