import com.codingfeline.buildkonfig.compiler.FieldSpec

plugins {
    alias(libs.plugins.bibleplanner.kotlinMultiplatform)
    alias(libs.plugins.bibleplanner.composeMultiplatform)
    alias(libs.plugins.buildkonfig)
}

kotlin {
    androidLibrary {
        namespace = "com.quare.bibleplanner.feature.more"
    }

    jvm()

    sourceSets {
        commonMain.dependencies {
            // Core
            implementation(projects.core.model)
            implementation(projects.core.utils)
            implementation(projects.core.provider.billing)
            implementation(projects.core.books)
            implementation(projects.core.remoteConfig)
            implementation(projects.core.plan)
            implementation(libs.kotlinx.datetime)

            // Features
            implementation(projects.feature.themeSelection)
            implementation(projects.feature.materialYou)
            implementation(projects.feature.subscriptionDetails)

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
        }
    }
}

buildkonfig {
    packageName = "com.quare.bibleplanner.feature.more.generated"
    objectName = "MoreBuildKonfig"
    exposeObjectWithName = "MoreBuildKonfig"

    defaultConfigs {
        buildConfigField(FieldSpec.Type.STRING, "APP_VERSION", project.property("versionName").toString())
    }
}
