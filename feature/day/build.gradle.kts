plugins {
    alias(libs.plugins.bibleplanner.kotlinMultiplatform)
    alias(libs.plugins.bibleplanner.composeMultiplatform)
}

kotlin {
    android {
        namespace = "com.quare.bibleplanner.feature.day"
    }

    jvm()

    sourceSets {
        commonMain.dependencies {
            // Core
            implementation(projects.core.books)
            implementation(projects.core.plan)
            implementation(projects.core.model)
            implementation(projects.feature.dayStudy)
            implementation(projects.core.utils)
            implementation(projects.core.date)
            implementation(projects.core.provider.analytics)
            implementation(projects.core.provider.platform)
            implementation(projects.core.provider.room)
            implementation(projects.core.provider.billing)
            implementation(projects.core.loginNudge)

            // UI
            implementation(projects.ui.component)
            implementation(projects.ui.utils)

            // Compose
            implementation(libs.runtime)
            implementation(libs.foundation)
            implementation(libs.material3)
            implementation(libs.ui)
            implementation(libs.material.icons.extended)
            implementation(libs.components.resources)

            // Navigation 3
            implementation(libs.compose.navigation3.ui)

            // DateTime
            implementation(libs.kotlinx.datetime)

            // Koin
            implementation(project.dependencies.platform(libs.koinBom))
            implementation(libs.koinCore)
            implementation(libs.koinCompose)
            implementation(libs.koinComposeViewModel)

            // Kermit
            implementation(libs.kermit)
        }
    }
}
