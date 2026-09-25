plugins {
    alias(libs.plugins.bibleplanner.kotlin.multiplatform)
    alias(libs.plugins.bibleplanner.kotlin.composeMultiplatform)
}

kotlin {
    android {
        namespace = "com.quare.bibleplanner.ui.icons"
    }

    jvm()

    sourceSets {
        commonMain.dependencies {
            // Compose
            implementation(libs.compose.runtime)
            implementation(libs.compose.ui)
            implementation(libs.compose.material3)
            implementation(libs.compose.materialIconsExtended)

            // Calf (SF Symbol names)
            implementation(libs.calf.ui)
        }
    }
}
