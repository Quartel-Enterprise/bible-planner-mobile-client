plugins {
    alias(libs.plugins.bibleplanner.kotlinMultiplatform)
}

kotlin {
    android {
        namespace = "com.quare.bibleplanner.core.provider.datastore"
    }

    jvm()
    sourceSets {
        commonMain.dependencies {
            // DataStore (multiplatform)
            api(libs.dataStore)
            api(libs.dataStore.preferences)

            // Coroutines
            implementation(libs.kotlinx.coroutines.core)

            // Koin
            implementation(project.dependencies.platform(libs.koinBom))
            implementation(libs.koinCore)
        }
    }
}
