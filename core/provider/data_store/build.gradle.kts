plugins {
    alias(libs.plugins.bibleplanner.kotlinMultiplatform)
    alias(libs.plugins.androidCommonConfig)
}

kotlin {
    androidLibrary {
        namespace = "com.quare.bibleplanner.core.provider.datastore"
    }

    jvm()
    sourceSets {
        commonMain.dependencies {
            // DataStore (multiplatform)
            implementation(libs.dataStore)
            implementation(libs.dataStore.preferences)

            // Koin
            implementation(project.dependencies.platform(libs.koinBom))
            implementation(libs.koinCore)
        }
    }
}
