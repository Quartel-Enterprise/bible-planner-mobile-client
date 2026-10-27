plugins {
    alias(libs.plugins.bibleplanner.kotlin.multiplatform)
    alias(libs.plugins.bibleplanner.kotlin.composeMultiplatform)
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

            // Koin
            implementation(project.dependencies.platform(libs.koin.bom))
            implementation(libs.koin.core)

            // FileKit
            implementation(libs.filekit.core)

            // Compose
            implementation(libs.compose.ui)
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.kotlinx.coroutines.test)
        }

        jvmTest.dependencies {
            // Skiko native library, required to decode images on the JVM target
            implementation(compose.desktop.currentOs)
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
