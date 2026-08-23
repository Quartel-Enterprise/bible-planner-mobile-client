plugins {
    alias(libs.plugins.bibleplanner.kotlin.multiplatform)
    alias(libs.plugins.bibleplanner.kotlin.composeMultiplatform)
}

kotlin {
    android {
        namespace = "com.quare.bibleplanner.feature.read"
    }

    jvm()

    sourceSets {
        commonMain.dependencies {
            // Core
            implementation(projects.core.books)
            implementation(projects.core.model)
            implementation(projects.core.plan)
            implementation(projects.core.provider.analytics)
            implementation(projects.core.provider.platform)
            implementation(projects.core.provider.room)
            implementation(projects.core.loginNudge)
            implementation(projects.core.provider.dataStore)
            implementation(projects.core.verseAnnotations)
            implementation(projects.core.utils)
            implementation(projects.feature.dayStudy)

            // UI
            implementation(projects.ui.component)
            implementation(projects.ui.theme)
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

            implementation(libs.kotlinx.coroutines.core)

            // DataStore
            implementation(libs.datastore.preferences)
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.kotlinx.datetime)
        }
    }
}
