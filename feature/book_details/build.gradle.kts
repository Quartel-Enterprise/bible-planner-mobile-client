plugins {
    alias(libs.plugins.bibleplanner.kotlinMultiplatform)
    alias(libs.plugins.bibleplanner.composeMultiplatform)
    alias(libs.plugins.serialization)
}

kotlin {
    androidLibrary {
        namespace = "com.quare.bibleplanner.feature.bookdetails"
    }

    jvm()

    sourceSets {
        commonMain.dependencies {
            // Core
            implementation(projects.core.books)
            implementation(projects.core.model)

            // UI
            implementation(projects.ui.component)
            implementation(projects.ui.utils)

            implementation(libs.kotlin.serialization.json)
            implementation(libs.compose.navigation)
            implementation(libs.androidx.lifecycle.viewmodelCompose)

            implementation(project.dependencies.platform(libs.koinBom))
            implementation(libs.koinCore)
            implementation(libs.koinCompose)
            implementation(libs.koinComposeViewModel)

            // Compose
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.materialIconsExtended)
            implementation(compose.components.resources)
        }
    }
}
