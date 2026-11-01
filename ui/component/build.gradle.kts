plugins {
    alias(libs.plugins.bibleplanner.kotlin.multiplatform)
    alias(libs.plugins.bibleplanner.kotlin.composeMultiplatform)
}

kotlin {
    android {
        namespace = "com.quare.bibleplanner.ui.component"
    }

    jvm()

    sourceSets {
        // Why: Android and the non-mobile targets draw the Material components where iOS draws native ones.
        val nonIosMain = create("nonIosMain") {
            dependsOn(commonMain.get())
        }

        androidMain {
            dependsOn(nonIosMain)
        }

        // Why: iOS and the non-mobile targets render with Skia, unlike Android.
        val skikoMain = create("skikoMain") {
            dependsOn(commonMain.get())
        }

        val nonMobileMain = create("nonMobileMain") {
            dependsOn(nonIosMain)
            dependsOn(skikoMain)
        }

        jvmMain {
            dependsOn(nonMobileMain)
        }

        wasmJsMain {
            dependsOn(nonMobileMain)
        }

        iosMain {
            dependsOn(skikoMain)
        }
        iosArm64Main {
            dependsOn(iosMain.get())
        }
        iosSimulatorArm64Main {
            dependsOn(iosMain.get())
        }

        commonMain.dependencies {
            // Core
            implementation(projects.core.provider.platform)
            implementation(projects.core.utils)
            implementation(projects.core.verseAnnotations)

            // UI
            implementation(projects.ui.theme)
            implementation(projects.ui.utils)
            implementation(projects.ui.icons)

            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.materialIconsExtended)
            implementation(libs.compose.components.resources)

            // Calf (adaptive iOS components)
            implementation(libs.calf.ui)

            // Shimmer
            implementation(libs.compose.shimmer)

            // Coil (image loading)
            implementation(libs.coil.compose)
            implementation(libs.coil.network.ktor)
        }
    }
}

compose.resources {
    publicResClass = true
}
