plugins {
    alias(libs.plugins.bibleplanner.kotlinMultiplatform)
}

kotlin {
    android {
        namespace = "com.quare.bibleplanner.core.provider.language"
    }

    jvm()

    sourceSets {
        commonMain.dependencies {
            implementation(projects.core.utils)
            implementation(projects.core.provider.room)
            implementation(projects.core.date)

            // Coroutines
            implementation(libs.kotlinx.coroutines.core)

            // DataStore
            implementation(libs.dataStore)
            implementation(libs.dataStore.preferences)

            // Koin
            implementation(project.dependencies.platform(libs.koinBom))
            implementation(libs.koinCore)
        }
        androidMain.dependencies {
            implementation(libs.koinAndroid)
            implementation(libs.androidx.core.ktx)
        }
    }
}
