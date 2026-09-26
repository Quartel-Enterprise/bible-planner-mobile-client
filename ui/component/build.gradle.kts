plugins {
    alias(libs.plugins.bibleplanner.kotlin.multiplatform)
    alias(libs.plugins.bibleplanner.kotlin.composeMultiplatform)
}

kotlin {
    android {
        namespace = "com.quare.bibleplanner.ui.component"
    }

    jvm()

    sourceSets {
        commonMain.dependencies {
            // Core
            implementation(projects.core.provider.platform)
            implementation(projects.core.utils)
            implementation(projects.core.verseAnnotations)

            // UI
            implementation(projects.ui.theme)
            implementation(projects.ui.utils)
            implementation(projects.ui.icons)

            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.materialIconsExtended)
            implementation(libs.compose.components.resources)

            // Calf (adaptive iOS components)
            implementation(libs.calf.ui)

            // Shimmer
            implementation(libs.compose.shimmer)

            // Coil (image loading)
            implementation(libs.coil.compose)
            implementation(libs.coil.network.ktor)
        }
    }
}

compose.resources {
    publicResClass = true
}
