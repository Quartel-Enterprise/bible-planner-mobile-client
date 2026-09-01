import com.codingfeline.buildkonfig.compiler.FieldSpec

plugins {
    alias(libs.plugins.bibleplanner.kotlin.multiplatform)
    alias(libs.plugins.bibleplanner.kotlin.composeMultiplatform)
    alias(libs.plugins.buildkonfig)
}

kotlin {
    android {
        namespace = "com.quare.bibleplanner.feature.profile"
    }

    jvm()

    sourceSets {
        commonMain.dependencies {
            // Core
            implementation(projects.core.model)
            implementation(projects.core.provider.analytics)
            implementation(projects.core.provider.language)
            implementation(projects.core.provider.platform)
            implementation(projects.core.provider.connectivity)
            implementation(projects.core.utils)
            implementation(projects.core.provider.billing)
            implementation(projects.core.books)
            implementation(projects.core.remoteConfig)
            implementation(projects.core.plan)
            implementation(projects.core.user)
            implementation(projects.core.profile)
            implementation(projects.core.provider.supabase)
            implementation(projects.core.provider.room)
            implementation(libs.androidx.room.runtime)

            // Features
            implementation(projects.feature.preferences.appLanguage)
            implementation(projects.feature.preferences.themeSelection)
            implementation(projects.feature.preferences.studySuggestion)
            implementation(projects.feature.materialYou)
            implementation(projects.feature.inAppUpdate)

            // UI
            implementation(projects.ui.component)
            implementation(projects.ui.utils)
            implementation(projects.ui.theme)

            // Compose
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.materialIconsExtended)
            implementation(libs.compose.components.resources)

            // Navigation 3
            implementation(libs.navigation3.ui)

            // Koin
            implementation(project.dependencies.platform(libs.koin.bom))
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)

            // Date
            implementation(libs.kotlinx.datetime)

            // Supabase
            implementation(project.dependencies.platform(libs.supabase.bom))
            implementation(libs.supabase.auth)
            implementation(libs.supabase.compose.auth)
            implementation(libs.supabase.compose.authUi)

            // Coil (image loading)
            implementation(libs.coil.compose)
            implementation(libs.coil.network.ktor)

            // Logging
            implementation(libs.kermit)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}

buildkonfig {
    packageName = "com.quare.bibleplanner.feature.profile.generated"
    objectName = "ProfileBuildKonfig"
    exposeObjectWithName = "ProfileBuildKonfig"

    defaultConfigs {
        buildConfigField(FieldSpec.Type.STRING, "APP_VERSION", project.property("versionName").toString())
    }
}
