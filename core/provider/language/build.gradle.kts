plugins {
    alias(libs.plugins.bibleplanner.kotlin.multiplatform)
}

kotlin {
    android {
        namespace = "com.quare.bibleplanner.core.provider.language"
    }

    jvm()

    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.utils)
            implementation(projects.core.provider.dataStore)
            implementation(projects.core.provider.room)
            implementation(projects.core.date)

            // Coroutines
            implementation(libs.kotlinx.coroutines.core)

            // DataStore
            implementation(libs.datastore)
            implementation(libs.datastore.preferences)

            // Koin
            implementation(project.dependencies.platform(libs.koin.bom))
            implementation(libs.koin.core)
        }
        androidMain.dependencies {
            implementation(libs.koin.android)
            implementation(libs.androidx.core.ktx)
        }
    }
}
