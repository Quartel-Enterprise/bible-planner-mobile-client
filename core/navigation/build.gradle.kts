plugins {
    alias(libs.plugins.bibleplanner.kotlinMultiplatform)
    alias(libs.plugins.bibleplanner.composeMultiplatform)
    alias(libs.plugins.serialization)
}

kotlin {
    android {
        namespace = "com.quare.bibleplanner.core.navigation"
    }

    jvm()

    sourceSets {
        commonMain.dependencies {
            // Feature
            implementation(projects.feature.bookDetails)
            implementation(projects.feature.main)
            implementation(projects.feature.preferences.themeSelection)
            implementation(projects.feature.materialYou)
            implementation(projects.feature.day)
            implementation(projects.feature.deleteProgress)
            implementation(projects.feature.deleteVersion)
            implementation(projects.feature.deleteNotes)
            implementation(projects.feature.addNotesFreeWarning)
            implementation(projects.feature.preferences.editPlanStartDate)
            implementation(projects.feature.paywall)
            implementation(projects.feature.congrats)
            implementation(projects.feature.releaseNotes)
            implementation(projects.feature.donation)
            implementation(projects.feature.donation.pixQr)
            implementation(projects.feature.login)
            implementation(projects.feature.loginWarning)
            implementation(projects.feature.loginSyncNudge)
            implementation(projects.feature.logout)
            implementation(projects.feature.preferences.bibleVersion)
            implementation(projects.feature.preferences.appLanguage)
            implementation(projects.feature.read)
            implementation(projects.feature.notificationPermission)

            // Core
            implementation(projects.core.model)
            implementation(projects.core.provider.koin)
            implementation(projects.core.provider.room)

            // Koin
            implementation(project.dependencies.platform(libs.koinBom))
            implementation(libs.koinCompose)

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
