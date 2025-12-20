plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.serialization)
    alias(libs.plugins.androidCommonConfig)
}

kotlin {
    androidTarget()

    listOf(
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "CoreModel"
            isStatic = true
        }
    }

    jvm()
    sourceSets {
        commonMain.dependencies {
            // DateTime
            implementation(libs.kotlinx.datetime)

            // Serialization
            implementation(libs.kotlin.serialization.json)
        }
    }
}

android {
    namespace = "com.quare.bibleplanner.core.model"
}
