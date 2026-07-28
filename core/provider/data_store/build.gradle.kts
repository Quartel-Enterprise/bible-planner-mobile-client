plugins {
    alias(libs.plugins.bibleplanner.kotlin.multiplatform)
}

kotlin {
    android {
        namespace = "com.quare.bibleplanner.core.provider.datastore"
    }

    jvm()
    sourceSets {
        commonMain.dependencies {
            // DataStore (multiplatform)
            api(libs.datastore)
            api(libs.datastore.preferences)

            // Coroutines
            implementation(libs.kotlinx.coroutines.core)

            // Koin
            implementation(project.dependencies.platform(libs.koin.bom))
            implementation(libs.koin.core)
        }
    }
}
