plugins {
    alias(libs.plugins.bibleplanner.kotlinMultiplatform)
    alias(libs.plugins.bibleplanner.composeMultiplatform)
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

            // UI
            implementation(projects.ui.utils)

            implementation(libs.runtime)
            implementation(libs.foundation)
            implementation(libs.material3)
            implementation(libs.ui)
            implementation(libs.material.icons.extended)
            implementation(libs.components.resources)

            // Shimmer
            implementation(libs.compose.shimmer)

            // Coil (image loading)
            implementation(libs.coil.compose)
            implementation(libs.coil.network)
        }
    }
}

compose.resources {
    publicResClass = true
}
