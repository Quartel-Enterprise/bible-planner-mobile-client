import com.codingfeline.buildkonfig.compiler.FieldSpec

plugins {
    alias(libs.plugins.bibleplanner.kotlinMultiplatform)
    alias(libs.plugins.bibleplanner.composeMultiplatform)
    alias(libs.plugins.buildkonfig)
}

kotlin {
    android {
        namespace = "com.quare.bibleplanner.feature.contactsupport"
    }

    jvm()

    sourceSets {
        commonMain.dependencies {
            // Core
            implementation(projects.core.model)
            implementation(projects.core.utils)
            implementation(projects.core.provider.platform)
            implementation(projects.core.provider.billing)
            implementation(projects.core.provider.language)
            implementation(projects.core.user)

            // Features
            implementation(projects.feature.preferences.appLanguage)

            // UI
            implementation(projects.ui.component)
            implementation(projects.ui.utils)
            implementation(projects.ui.theme)

            // Compose
            implementation(libs.runtime)
            implementation(libs.foundation)
            implementation(libs.material3)
            implementation(libs.ui)
            implementation(libs.material.icons.extended)
            implementation(libs.components.resources)

            // Navigation
            implementation(libs.compose.navigation)

            // Koin
            implementation(project.dependencies.platform(libs.koinBom))
            implementation(libs.koinCore)
            implementation(libs.koinCompose)
            implementation(libs.koinComposeViewModel)

            // Supabase (SessionStatus type)
            implementation(project.dependencies.platform(libs.supabase.bom))
            implementation(libs.supabase.auth)
        }

        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(libs.kotlinx.coroutines.test)
            }
        }

        val jvmTest by getting {
            dependencies {
                // Skiko native library, required by compose-resources' getString() on the JVM target
                implementation(compose.desktop.currentOs)
            }
        }
    }
}

buildkonfig {
    packageName = "com.quare.bibleplanner.feature.contactsupport.generated"
    objectName = "ContactSupportBuildKonfig"
    exposeObjectWithName = "ContactSupportBuildKonfig"

    defaultConfigs {
        buildConfigField(FieldSpec.Type.STRING, "APP_VERSION", project.property("versionName").toString())
    }
}
