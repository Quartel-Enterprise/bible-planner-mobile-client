plugins {
    alias(libs.plugins.bibleplanner.kotlin.multiplatform)
    alias(libs.plugins.bibleplanner.kotlin.composeMultiplatform)
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    android {
        namespace = "com.quare.bibleplanner.feature.chat"
        withHostTest {
            isIncludeAndroidResources = true
            isReturnDefaultValues = true
        }
    }

    jvm()

    sourceSets {
        commonMain.dependencies {
            // Core
            implementation(projects.core.clear)
            implementation(projects.core.model)
            implementation(projects.core.utils)
            implementation(projects.core.books)
            implementation(projects.core.date)
            implementation(projects.core.sync)
            implementation(projects.core.provider.connectivity)
            implementation(projects.core.user)
            implementation(projects.core.provider.language)
            implementation(projects.core.provider.dataStore)
            implementation(projects.core.provider.room)
            implementation(projects.core.provider.supabase)
            implementation(projects.core.provider.billing)
            implementation(projects.core.provider.analytics)
            implementation(projects.core.provider.platform)

            // Feature
            implementation(projects.feature.dayStudy)

            // UI
            implementation(projects.ui.component)
            implementation(projects.ui.utils)
            implementation(projects.ui.theme)

            // Compose
            implementation(libs.datastore)
            implementation(libs.datastore.preferences)

            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.materialIconsExtended)
            implementation(libs.compose.components.resources)

            // Supabase
            implementation(project.dependencies.platform(libs.supabase.bom))
            implementation(libs.supabase.functions)
            implementation(libs.supabase.postgrest)
            implementation(libs.supabase.realtime)

            // Ktor
            implementation(libs.ktor.client.core)

            // Serialization
            implementation(libs.kotlinx.serialization.json)

            // Koin
            implementation(project.dependencies.platform(libs.koin.bom))
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)

            // Logging
            implementation(libs.kermit)
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.kotlinx.coroutines.test)
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
    // each module owns its own directory so the generators never overlap.
    val screenshotsDir = rootProject.layout.buildDirectory.dir("outputs/store-screenshots/chat")
    systemProperty("storeScreenshots.outputRoot", screenshotsDir.get().asFile.absolutePath)
    systemProperty("roborazzi.test.record", "true")
    outputs.dir(screenshotsDir)
}
