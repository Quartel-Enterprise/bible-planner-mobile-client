plugins {
    alias(libs.plugins.bibleplanner.kotlin.multiplatform)
}

kotlin {
    android {
        namespace = "com.quare.bibleplanner.core.loginnudge"
    }
    jvm()

    sourceSets {
        commonMain.dependencies {
            // Core
            implementation(projects.core.model)
            implementation(projects.core.provider.dataStore)
            implementation(projects.core.user)
            implementation(projects.core.date)
            implementation(projects.core.provider.analytics)
            implementation(projects.core.provider.connectivity)

            // Coroutines
            implementation(libs.kotlinx.coroutines.core)

            // DataStore
            implementation(libs.datastore)
            implementation(libs.datastore.preferences)

            // Koin
            implementation(project.dependencies.platform(libs.koin.bom))
            implementation(libs.koin.core)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}
