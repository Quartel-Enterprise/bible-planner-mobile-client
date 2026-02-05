plugins {
    alias(libs.plugins.bibleplanner.kotlinMultiplatform)
}

kotlin {
    androidLibrary {
        namespace = "com.quare.bibleplanner.core.provider.koin"
    }

    jvm()

    sourceSets {
        commonMain.dependencies {
            // Core
            implementation(projects.core.provider.dataStore)
            implementation(projects.core.provider.platform)
            implementation(projects.core.provider.room)
            implementation(projects.core.provider.billing)
            implementation(projects.core.remoteConfig)
            implementation(projects.core.books)
            implementation(projects.core.plan)
            implementation(projects.core.utils)
            implementation(projects.core.utils.jsonReader)
            implementation(projects.core.date)
            implementation(projects.core.provider.supabase)
            implementation(projects.core.network)
            implementation(projects.core.user)
            implementation(projects.core.model)

            // Features
            implementation(projects.feature.books)
            implementation(projects.feature.login)
            implementation(projects.feature.bookDetails)
            implementation(projects.feature.addNotesFreeWarning)
            implementation(projects.feature.readingPlan)
            implementation(projects.feature.deleteProgress)
            implementation(projects.feature.themeSelection)
            implementation(projects.feature.materialYou)
            implementation(projects.feature.day)
            implementation(projects.feature.editPlanStartDate)
            implementation(projects.feature.paywall)
            implementation(projects.feature.deleteNotes)
            implementation(projects.feature.main)
            implementation(projects.feature.congrats)
            implementation(projects.feature.subscriptionDetails)
            implementation(projects.feature.more)
            implementation(projects.feature.releaseNotes)
            implementation(projects.feature.donation)
            implementation(projects.feature.donation.pixQr)
            implementation(projects.feature.bibleVersion)

            // Koin
            implementation(project.dependencies.platform(libs.koinBom))
            implementation(libs.koinCore)
        }
    }
}
