plugins {
    alias(libs.plugins.bibleplanner.kotlin.multiplatform)
    alias(libs.plugins.bibleplanner.kotlin.composeMultiplatform)
}

kotlin {
    android {
        namespace = "com.quare.bibleplanner.core.provider.platform"
    }

    jvm()

    sourceSets {
        commonMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(projects.core.utils)
            implementation(projects.core.provider.language)

            // Koin
            implementation(project.dependencies.platform(libs.koin.bom))
            implementation(libs.koin.core)

            // Logging
            implementation(libs.kermit)
        }
        androidMain.dependencies {
            implementation(libs.koin.android)
            implementation(libs.play.review)
            implementation(libs.play.review.ktx)
        }
    }
}
