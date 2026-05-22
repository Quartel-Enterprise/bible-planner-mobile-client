plugins {
    alias(libs.plugins.bibleplanner.kotlinMultiplatform)
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
            implementation(project.dependencies.platform(libs.koinBom))
            implementation(libs.koinCore)
        }
    }
}
