plugins {
    alias(libs.plugins.kotlinMultiplatformConvention)
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            // Koin
            implementation(project.dependencies.platform(libs.koinBom))
            implementation(libs.koinCore)

            // Features
            implementation(projects.feature.readingPlan)
        }
    }
}

android {
    namespace = "com.quare.bibleplanner.core.provider.koin"
}
