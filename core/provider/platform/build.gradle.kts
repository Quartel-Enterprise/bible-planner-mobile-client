plugins {
    alias(libs.plugins.bibleplanner.kotlinMultiplatform)
    alias(libs.plugins.bibleplanner.composeMultiplatform)
}

kotlin {
    android {
        namespace = "com.quare.bibleplanner.core.provider.platform"
    }

    jvm()

    sourceSets {
        commonMain.dependencies {
            implementation(libs.runtime)
            implementation(projects.core.utils)
            implementation(projects.core.provider.language)

            // Koin
            implementation(project.dependencies.platform(libs.koinBom))
            implementation(libs.koinCore)
        }
    }
}
