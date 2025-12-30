plugins {
    alias(libs.plugins.bibleplanner.kotlinMultiplatform)
    alias(libs.plugins.androidCommonConfig)
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
            implementation(projects.core.books)
            implementation(projects.core.plan)
            implementation(projects.core.utils)
            implementation(projects.core.date)

            // Features
            implementation(projects.feature.readingPlan)
            implementation(projects.feature.deleteProgress)
            implementation(projects.feature.themeSelection)
            implementation(projects.feature.materialYou)
            implementation(projects.feature.day)
            implementation(projects.feature.editPlanStartDate)
            implementation(projects.feature.onboardingStartDate)

            // Koin
            implementation(project.dependencies.platform(libs.koinBom))
            implementation(libs.koinCore)
        }
    }
}
