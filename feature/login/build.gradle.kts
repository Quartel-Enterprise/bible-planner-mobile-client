plugins {
    alias(libs.plugins.bibleplanner.kotlin.multiplatform)
    alias(libs.plugins.bibleplanner.kotlin.composeMultiplatform)
}

kotlin {
    android {
        namespace = "com.quare.bibleplanner.feature.login"
    }

    jvm()

    sourceSets {
        androidMain.dependencies {
            // NoCredentialException, which AndroidIsGoogleCredentialUnavailable reads to tell a Google
            // credential the platform could not provide from any other sign-in failure.
            implementation(libs.androidx.credentials)
        }
        jvmMain.dependencies {
            // Desktop-only: needed by JvmGoogleSignInStarter to render the OAuth success
            // page using the user's selected theme and language instead of the browser's.
            implementation(projects.core.provider.language)
            implementation(projects.feature.preferences.themeSelection)
            implementation(projects.ui.theme)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.kotlinx.coroutines.test)
        }
        commonMain.dependencies {
            // Core
            implementation(projects.core.model)
            implementation(projects.core.user)
            implementation(projects.core.utils)
            implementation(projects.core.provider.analytics)
            implementation(projects.core.provider.platform)

            // UI
            implementation(projects.ui.component)
            implementation(projects.ui.utils)
            implementation(projects.ui.theme)

            // Compose
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.materialIconsExtended)
            implementation(compose.components.resources)

            // Navigation 3
            implementation(libs.navigation3.ui)

            // Ktor (HttpRequestTimeoutException, used to map login failures to a friendly message)
            implementation(libs.ktor.client.core)

            // Supabase
            implementation(project.dependencies.platform(libs.supabase.bom))
            implementation(libs.supabase.auth)
            implementation(libs.supabase.compose.auth)

            // Koin
            implementation(project.dependencies.platform(libs.koin.bom))
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)
        }
    }
}
