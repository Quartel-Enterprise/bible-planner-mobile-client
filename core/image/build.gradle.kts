plugins {
    alias(libs.plugins.bibleplanner.kotlinMultiplatform)
}

kotlin {
    android {
        namespace = "com.quare.bibleplanner.core.image"
        withHostTest {}
    }
    jvm()

    sourceSets {
        commonMain.dependencies {
            // Core
            implementation(projects.core.utils)

            // Coroutines
            implementation(libs.kotlinx.coroutines.core)
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
        }

        androidMain.dependencies {
            // KTX (androidx.core.graphics.createBitmap)
            implementation(libs.androidx.core.ktx)
        }

        getByName("androidHostTest").dependencies {
            implementation(kotlin("test"))
        }
    }
}
