plugins {
    alias(libs.plugins.bibleplanner.kotlin.multiplatform)
}

kotlin {
    android {
        namespace = "com.quare.bibleplanner.core.notification"
    }

    jvm()

    sourceSets {
        androidMain.dependencies {
            implementation(libs.androidx.core.ktx)

            // Koin
            implementation(project.dependencies.platform(libs.koin.bom))
            implementation(libs.koin.core)
        }
    }
}
