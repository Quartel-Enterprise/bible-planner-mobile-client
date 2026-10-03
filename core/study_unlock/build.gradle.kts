plugins {
    alias(libs.plugins.bibleplanner.kotlin.multiplatform)
}

kotlin {
    android {
        namespace = "com.quare.bibleplanner.core.studyunlock"
        withHostTest {}
    }

    jvm()

    sourceSets {
        commonMain.dependencies {
            // Core
            implementation(projects.core.remoteConfig)
            api(projects.core.provider.ads)

            // Coroutines
            implementation(libs.kotlinx.coroutines.core)

            // Koin
            implementation(project.dependencies.platform(libs.koin.bom))
            implementation(libs.koin.core)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.kotlinx.coroutines.test)

            // Shared fakes
            implementation(projects.core.provider.ads.testing)
        }
    }
}
