plugins {
    alias(libs.plugins.bibleplanner.kotlinMultiplatform)
    alias(libs.plugins.bibleplanner.composeMultiplatform)
}

kotlin {
    androidLibrary {
        namespace = "com.quare.bibleplanner.ui.utils"
    }

    jvm()

    sourceSets {
        commonMain.dependencies {
            // Core
            implementation(projects.core.date)

            // View Model
            implementation(libs.androidx.lifecycle.viewmodelCompose)

            // DateTime
            implementation(libs.kotlinx.datetime)

            // Compose
            implementation(libs.runtime)
            implementation(libs.foundation)
            implementation(libs.material3)
            implementation(libs.ui)
            implementation(libs.components.resources)
        }
    }
}
