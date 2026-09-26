plugins {
    alias(libs.plugins.bibleplanner.kotlin.multiplatform)
}

kotlin {
    android {
        namespace = "com.quare.bibleplanner.core.preferences.studysuggestion"
        withHostTest {}
    }

    jvm()

    sourceSets {
        commonMain.dependencies {
            // Core
            implementation(projects.core.date)
            implementation(projects.core.provider.dataStore)
            implementation(projects.core.provider.room)

            // Coroutines
            implementation(libs.kotlinx.coroutines.core)

            // Koin
            implementation(project.dependencies.platform(libs.koin.bom))
            implementation(libs.koin.core)

            // Data Store
            implementation(libs.datastore)
            implementation(libs.datastore.preferences)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}
