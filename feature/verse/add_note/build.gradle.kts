plugins {
    alias(libs.plugins.bibleplanner.kotlin.multiplatform)
    alias(libs.plugins.bibleplanner.kotlin.composeMultiplatform)
}

kotlin {
    android {
        namespace = "com.quare.bibleplanner.feature.verse.addnote"
    }

    jvm()

    sourceSets {
        commonMain.dependencies {
            // Core
            implementation(projects.core.books)
            implementation(projects.core.model)
            implementation(projects.core.provider.analytics)
            implementation(projects.core.verseAnnotations)

            // UI
            implementation(projects.ui.component)
            implementation(projects.ui.theme)
            implementation(projects.ui.utils)

            // Navigation 3
            implementation(libs.navigation3.ui)
            implementation(libs.androidx.lifecycle.viewmodelCompose)

            // Koin
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
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}
