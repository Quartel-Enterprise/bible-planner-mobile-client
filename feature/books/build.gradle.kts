plugins {
    alias(libs.plugins.bibleplanner.kotlin.multiplatform)
    alias(libs.plugins.bibleplanner.kotlin.composeMultiplatform)
}

kotlin {
    android {
        namespace = "com.quare.bibleplanner.feature.books"
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
            implementation(projects.core.model)
            implementation(projects.core.provider.analytics)
            implementation(projects.core.remoteConfig)
            implementation(projects.core.loginNudge)

            // UI
            implementation(projects.ui.component)
            implementation(projects.ui.utils)

            // Navigation 3
            implementation(libs.navigation3.ui)
            implementation(libs.androidx.lifecycle.viewmodelCompose)

            implementation(project.dependencies.platform(libs.koin.bom))
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)

            // Compose
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.materialIconsExtended)
            implementation(libs.compose.components.resources)
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
    val screenshotsDir = rootProject.layout.buildDirectory.dir("outputs/store-screenshots/books")
    systemProperty("storeScreenshots.outputRoot", screenshotsDir.get().asFile.absolutePath)
    systemProperty("roborazzi.test.record", "true")
    outputs.dir(screenshotsDir)
}
