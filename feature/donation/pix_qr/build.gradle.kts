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
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.materialIconsExtended)
            implementation(compose.components.resources)
        }
    }
}

compose.resources {
    publicResClass = true
}
