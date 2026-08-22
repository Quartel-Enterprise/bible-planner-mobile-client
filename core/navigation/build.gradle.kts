plugins {
    alias(libs.plugins.bibleplanner.kotlin.multiplatform)
    alias(libs.plugins.bibleplanner.kotlin.composeMultiplatform)
    alias(libs.plugins.kotlin.serialization)
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
            implementation(projects.feature.inAppUpdate)
            implementation(projects.feature.main)
            implementation(projects.feature.preferences.themeSelection)
            implementation(projects.feature.materialYou)
            implementation(projects.feature.day)
            implementation(projects.feature.deleteProgress)
            implementation(projects.feature.deleteAccount)
            implementation(projects.feature.deleteVersion)
            implementation(projects.feature.deleteNotes)
            implementation(projects.feature.addNotesFreeWarning)
            implementation(projects.feature.preferences.editPlanStartDate)
            implementation(projects.feature.paywall)
            implementation(projects.feature.paywallTeaser)
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
            implementation(projects.feature.verse.share)
            implementation(projects.feature.verse.addNote)
            implementation(projects.feature.verse.selectionMenu)
            implementation(projects.feature.notificationPermission)
            implementation(projects.feature.dayStudy)
            implementation(projects.feature.chat)
            implementation(projects.feature.subscriptionDetails)
            implementation(projects.feature.accountDetails)
            implementation(projects.feature.editProfile)
            implementation(projects.feature.contactSupport)

            // Core
            implementation(projects.core.model)
            implementation(projects.core.provider.analytics)
            implementation(projects.core.provider.koin)
            implementation(projects.core.provider.room)

            // Koin
            implementation(project.dependencies.platform(libs.koin.bom))
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)

            // Compose
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)

            // UI
            implementation(projects.ui.utils)

            // Navigation 3
            implementation(libs.navigation3.ui)
            implementation(libs.androidx.lifecycle.viewmodelNavigation3)
        }
    }
}
