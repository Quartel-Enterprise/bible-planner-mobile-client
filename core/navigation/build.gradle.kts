plugins {
    alias(libs.plugins.bibleplanner.kotlinMultiplatform)
    alias(libs.plugins.bibleplanner.composeMultiplatform)
    alias(libs.plugins.serialization)
}

kotlin {
    androidLibrary {
        namespace = "com.quare.bibleplanner.core.navigation"
    }

    jvm()

    sourceSets {
        commonMain.dependencies {
            // Feature
            implementation(projects.feature.bookDetails)
            implementation(projects.feature.main)
            implementation(projects.feature.themeSelection)
            implementation(projects.feature.materialYou)
            implementation(projects.feature.day)
            implementation(projects.feature.deleteProgress)
            implementation(projects.feature.deleteNotes)
            implementation(projects.feature.addNotesFreeWarning)
            implementation(projects.feature.editPlanStartDate)

            implementation(projects.feature.paywall)
            implementation(projects.feature.congrats)
            implementation(projects.feature.releaseNotes)
            implementation(projects.feature.donation)
            implementation(projects.feature.donation.pixQr)

            // Core
            implementation(projects.core.model)
            implementation(projects.core.provider.koin)
            implementation(projects.core.provider.room)

            // Compose
            implementation(libs.runtime)
            implementation(libs.material3)
            implementation(libs.ui)

            // UI
            implementation(projects.ui.utils)

            // Navigation
            implementation(libs.compose.navigation)
        }
    }
}
