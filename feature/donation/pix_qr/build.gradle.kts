plugins {
    alias(libs.plugins.bibleplanner.kotlinMultiplatform)
    alias(libs.plugins.bibleplanner.composeMultiplatform)
}

kotlin {
    androidLibrary {
        namespace = "com.quare.bibleplanner.feature.donation.pixqr"
    }

    jvm()

    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.model)
            implementation(projects.core.utils)
            implementation(projects.core.provider.platform)
            implementation(projects.ui.utils)
            implementation(projects.ui.component)

            implementation(libs.compose.navigation)
            implementation(libs.androidx.lifecycle.viewmodelCompose)

            implementation(project.dependencies.platform(libs.koinBom))
            implementation(libs.koinCore)
            implementation(libs.koinCompose)
            implementation(libs.koinComposeViewModel)

            // Compose
            implementation(libs.runtime)
            implementation(libs.foundation)
            implementation(libs.material3)
            implementation(libs.ui)
            implementation(libs.material.icons.extended)
            implementation(libs.components.resources)
        }
    }
}

compose.resources {
    publicResClass = true
}
